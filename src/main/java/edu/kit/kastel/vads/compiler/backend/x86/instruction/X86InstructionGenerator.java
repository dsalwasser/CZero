package edu.kit.kastel.vads.compiler.backend.x86.instruction;

import edu.kit.kastel.vads.compiler.backend.util.DivisionOperation;
import edu.kit.kastel.vads.compiler.backend.util.SimpleArithmeticOperation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86Register;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.control.IfMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.control.JumpNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopBodyMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopExitMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopNode;
import edu.kit.kastel.vads.compiler.ir.node.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.control.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.data.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.data.AndNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseAndNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseOrNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseXORNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstBoolNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.data.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.data.EqualityNode;
import edu.kit.kastel.vads.compiler.ir.node.data.GreaterThanEqualNode;
import edu.kit.kastel.vads.compiler.ir.node.data.GreaterThanNode;
import edu.kit.kastel.vads.compiler.ir.node.data.InequalityNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LeftShiftNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LessThanEqualNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LessThanNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.data.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.data.OrNode;
import edu.kit.kastel.vads.compiler.ir.node.data.RightShiftNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SideEffectNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SubNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class X86InstructionGenerator {

  private static enum BranchStatus {
    NONE,
    THEN,
    ELSE
  }

  private static record CodeGenerationStatus(BranchStatus branchStatus) {
    public CodeGenerationStatus() {
      this(BranchStatus.NONE);
    }

    public CodeGenerationStatus takeThenBranch() {
      return new CodeGenerationStatus(BranchStatus.THEN);
    }

    public CodeGenerationStatus takeElseBranch() {
      return new CodeGenerationStatus(BranchStatus.ELSE);
    }
  }

  private static final X86Operand.Register SPILL_OPERAND =
      new X86Operand.Register(X86RegisterAllocation.SPILL_REGISTER);

  private final X86RegisterAllocator registerAllocator;
  private X86RegisterAllocation registerAllocation;

  private Map<IRControlNode, Integer> labels;
  private int curLabel;

  private List<X86Instruction> instructions;

  public X86InstructionGenerator(X86RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }

  public List<X86Instruction> generateInstructions(IRGraph function) {
    registerAllocation = registerAllocator.allocateRegisters(function);

    labels = new HashMap<>();
    curLabel = 1;

    instructions = new ArrayList<>();

    // Add function prologue to create a stack frame by saving the old base pointer,
    // creating a new stack frame, and allocating enough space on the stack to hold
    // the virtual registers.
    final int stackSize = registerAllocation.numVirtualRegisters() * 4;
    addInstruction(new X86Instruction.Push(X86Operand.RBP));
    addInstruction(new X86Instruction.Mov(X86Operand.RBP, X86Operand.RSP));
    addInstruction(new X86Instruction.Sub(X86Operand.RSP, new X86Operand.Immediate(stackSize)));

    IRControlNode startNode = function.startBlock().entryNode();
    generateInstructions(startNode, new CodeGenerationStatus());

    return instructions;
  }

  private void generateInstructions(IRControlNode controlNode, CodeGenerationStatus status) {
    switch (controlNode) {
      case StartNode start -> {
        generateInstructions(start.successor(), status);
      }
      case IfNode branch -> {
        final int thenLabel = curLabel++;
        final int elseLabel = curLabel++;
        if (branch.hasMerge()) {
          final int joinLabel = curLabel++;
          labels.put(branch.merge(), joinLabel);
        }

        if (branch.condition() instanceof BinaryOperationNode op) {
          generateInstructions(op.left());
          generateInstructions(op.right());
        }

        switch (branch.condition()) {
          case ConstBoolNode c -> {
            if (!c.value()) {
              addInstruction(new X86Instruction.Jmp(elseLabel));
            }
          }
          case AndNode and -> {
            handleTestOperator(getResultOperand(and.left()));
            addInstruction(new X86Instruction.Jz(elseLabel));

            handleTestOperator(getResultOperand(and.right()));
            addInstruction(new X86Instruction.Jz(elseLabel));
          }
          case OrNode or -> {
            handleTestOperator(getResultOperand(or.left()));
            addInstruction(new X86Instruction.Jnz(thenLabel));

            handleTestOperator(getResultOperand(or.right()));
            addInstruction(new X86Instruction.Jz(elseLabel));
          }
          case EqualityNode eq -> {
            handleComparisonOperator(getResultOperand(eq.left()), getResultOperand(eq.right()));
            addInstruction(new X86Instruction.Jne(elseLabel));
          }
          case InequalityNode neq -> {
            handleComparisonOperator(getResultOperand(neq.left()), getResultOperand(neq.right()));
            addInstruction(new X86Instruction.Je(elseLabel));
          }
          case LessThanNode lt -> {
            handleComparisonOperator(getResultOperand(lt.left()), getResultOperand(lt.right()));
            addInstruction(new X86Instruction.Jge(elseLabel));
          }
          case LessThanEqualNode lte -> {
            handleComparisonOperator(getResultOperand(lte.left()), getResultOperand(lte.right()));
            addInstruction(new X86Instruction.Jg(elseLabel));
          }
          case GreaterThanNode gt -> {
            handleComparisonOperator(getResultOperand(gt.left()), getResultOperand(gt.right()));
            addInstruction(new X86Instruction.Jle(elseLabel));
          }
          case GreaterThanEqualNode gte -> {
            handleComparisonOperator(getResultOperand(gte.left()), getResultOperand(gte.right()));
            addInstruction(new X86Instruction.Jl(elseLabel));
          }
          case Phi phi -> {
            handleTestOperator(getResultOperand(phi));
            addInstruction(new X86Instruction.Jz(elseLabel));
          }
          default ->
              throw new UnsupportedOperationException(branch.condition().getClass().getName());
        }

        addLabel(thenLabel);
        generateInstructions(branch.thenSuccessor(), status.takeThenBranch());

        addLabel(elseLabel);
        generateInstructions(branch.elseSuccessor(), status.takeElseBranch());

        if (branch.hasMerge()) {
          addLabel(labels.get(branch.merge()));
          generateInstructions(branch.merge(), status);
        }
      }
      case LoopNode loop -> {
        final int loopEntryLabel = curLabel++;
        final int loopBodyEntryLabel = curLabel++;
        final int loopBodyExitLabel = curLabel++;
        final int loopExitLabel = curLabel++;

        labels.put(loop, loopEntryLabel);
        labels.put(loop.bodyMerge(), loopBodyExitLabel);
        labels.put(loop.exitMerge(), loopExitLabel);

        for (Phi phi : loop.phis()) {
          IRDataNode dataNode = phi.dataNode(0);
          generateInstructions(dataNode);
          insertPhiMove(phi, dataNode);
        }

        addLabel(loopEntryLabel);
        generateInstructions(loop.condition());

        if (loop.condition() instanceof BinaryOperationNode op) {
          generateInstructions(op.left());
          generateInstructions(op.right());
        }

        switch (loop.condition()) {
          case ConstBoolNode c -> {
            if (!c.value()) {
              addInstruction(new X86Instruction.Jmp(loopExitLabel));
            }
          }
          case AndNode and -> {
            handleTestOperator(getResultOperand(and.left()));
            addInstruction(new X86Instruction.Jz(loopExitLabel));

            handleTestOperator(getResultOperand(and.right()));
            addInstruction(new X86Instruction.Jz(loopExitLabel));
          }
          case OrNode or -> {
            handleTestOperator(getResultOperand(or.left()));
            addInstruction(new X86Instruction.Jnz(loopExitLabel));

            handleTestOperator(getResultOperand(or.right()));
            addInstruction(new X86Instruction.Jz(loopExitLabel));
          }
          case EqualityNode eq -> {
            handleComparisonOperator(getResultOperand(eq.left()), getResultOperand(eq.right()));
            addInstruction(new X86Instruction.Jne(loopExitLabel));
          }
          case InequalityNode neq -> {
            handleComparisonOperator(getResultOperand(neq.left()), getResultOperand(neq.right()));
            addInstruction(new X86Instruction.Je(loopExitLabel));
          }
          case LessThanNode lt -> {
            handleComparisonOperator(getResultOperand(lt.left()), getResultOperand(lt.right()));
            addInstruction(new X86Instruction.Jge(loopExitLabel));
          }
          case LessThanEqualNode lte -> {
            handleComparisonOperator(getResultOperand(lte.left()), getResultOperand(lte.right()));
            addInstruction(new X86Instruction.Jg(loopExitLabel));
          }
          case GreaterThanNode gt -> {
            handleComparisonOperator(getResultOperand(gt.left()), getResultOperand(gt.right()));
            addInstruction(new X86Instruction.Jle(loopExitLabel));
          }
          case GreaterThanEqualNode gte -> {
            handleComparisonOperator(getResultOperand(gte.left()), getResultOperand(gte.right()));
            addInstruction(new X86Instruction.Jl(loopExitLabel));
          }
          case Phi phi -> {
            handleTestOperator(getResultOperand(phi));
            addInstruction(new X86Instruction.Jz(loopExitLabel));
          }
          default -> throw new UnsupportedOperationException(loop.condition().getClass().getName());
        }

        addLabel(loopBodyEntryLabel);
        generateInstructions(loop.body(), status);

        addLabel(loopBodyExitLabel);
        generateInstructions(loop.bodyMerge(), status);

        addLabel(loopExitLabel);
        generateInstructions(loop.exit(), status);
      }
      case JumpNode jump -> {
        final IRControlNode targetNode = jump.successor();

        switch (targetNode) {
          case IfMergeNode merge -> {
            for (Phi phi : merge.phis()) {
              IRDataNode dataNode =
                  phi.dataNode(status.branchStatus() == BranchStatus.THEN ? 0 : 1);
              generateInstructions(dataNode);
              insertPhiMove(phi, dataNode);
            }

            final int targetLabel = labels.get(targetNode);
            addInstruction(new X86Instruction.Jmp(targetLabel));
          }
          case LoopBodyMergeNode _ -> {
            final int targetLabel = labels.get(targetNode);
            addInstruction(new X86Instruction.Jmp(targetLabel));
          }
          case LoopExitMergeNode merge -> {
            for (Phi phi : merge.phis()) {
              IRDataNode dataNode = phi.dataNode(merge.predecessors().indexOf(jump));
              generateInstructions(dataNode);
              insertPhiMove(phi, dataNode);
            }

            final int targetLabel = labels.get(targetNode);
            addInstruction(new X86Instruction.Jmp(targetLabel));
          }
          default -> {
            generateInstructions(targetNode, status);
          }
        }
      }
      case IfMergeNode join -> {
        generateInstructions(join.successor(), status);
      }
      case LoopBodyMergeNode merge -> {
        LoopNode loop = merge.getLoopNode();

        for (IRNode node : loop.block().nodes().reversed()) {
          if (node instanceof Phi phi) {
            IRDataNode dataNode = phi.dataNode(1);
            generateInstructions(dataNode);
            insertPhiMove(phi, dataNode);
          }
        }

        final int targetLabel = labels.get(loop);
        addInstruction(new X86Instruction.Jmp(targetLabel));
      }
      case LoopExitMergeNode merge -> {
        for (Phi phi : merge.phis()) {
          IRDataNode dataNode = phi.dataNode(merge.predecessors().indexOf(merge.loopNode()));
          generateInstructions(dataNode);
          insertPhiMove(phi, dataNode);
        }

        generateInstructions(merge.successor(), status);
      }
      case ReturnNode ret -> {
        for (SideEffectNode sideEffectNode : ret.sideEffectNodes()) {
          sideEffectNode.input().ifPresent(input -> generateInstructions(input));
        }

        generateInstructions(ret.result());

        // Move the result into the return register, add a function epilogue to tear
        // down the stack frame, and return from the function
        addInstruction(new X86Instruction.Mov(X86Operand.EAX, getResultOperand(ret.result())));
        addInstruction(new X86Instruction.Leave());
        addInstruction(new X86Instruction.Return());
      }
      default -> {}
    }
  }

  private void generateInstructions(IRDataNode dataNode) {
    switch (dataNode) {
      case AddNode add -> handleBinaryOperator(add, SimpleArithmeticOperation.ADD);
      case SubNode sub -> handleBinaryOperator(sub, SimpleArithmeticOperation.SUB);
      case MulNode mul -> handleBinaryOperator(mul, SimpleArithmeticOperation.MUL);
      case DivNode div -> handleDivisionOperator(div, DivisionOperation.DIV);
      case ModNode mod -> handleDivisionOperator(mod, DivisionOperation.MOD);
      case EqualityNode eq -> handleBinaryOperator(eq, SimpleArithmeticOperation.EQ);
      case InequalityNode neq -> handleBinaryOperator(neq, SimpleArithmeticOperation.NEQ);
      case AndNode and -> handleBinaryOperator(and, SimpleArithmeticOperation.AND);
      case OrNode or -> handleBinaryOperator(or, SimpleArithmeticOperation.OR);
      case BitwiseAndNode and -> handleBinaryOperator(and, SimpleArithmeticOperation.BITWISE_AND);
      case BitwiseOrNode or -> handleBinaryOperator(or, SimpleArithmeticOperation.BITWISE_OR);
      case BitwiseXORNode xor -> handleBinaryOperator(xor, SimpleArithmeticOperation.BITWISE_XOR);
      case LeftShiftNode lsh -> handleBinaryOperator(lsh, SimpleArithmeticOperation.LEFT_SHIFT);
      case RightShiftNode rsh -> handleBinaryOperator(rsh, SimpleArithmeticOperation.RIGHT_SHIFT);
      case LessThanNode lt -> handleBinaryOperator(lt, SimpleArithmeticOperation.LT);
      case LessThanEqualNode lte -> handleBinaryOperator(lte, SimpleArithmeticOperation.LTE);
      case GreaterThanNode gt -> handleBinaryOperator(gt, SimpleArithmeticOperation.GT);
      case GreaterThanEqualNode gte -> handleBinaryOperator(gte, SimpleArithmeticOperation.GTE);
      case SideEffectNode sideEffect ->
          sideEffect.input().ifPresent(input -> generateInstructions(input));
      default -> {}
    }
  }

  private void handleBinaryOperator(BinaryOperationNode node, SimpleArithmeticOperation operation) {
    generateInstructions(node.left());
    generateInstructions(node.right());

    X86Operand dest = getResultOperand(node);
    X86Operand left = getResultOperand(node.left());
    X86Operand right = getResultOperand(node.right());

    if (dest.equals(left)) {
      handleBinaryOperator(operation, left, right);
    } else if (dest.equals(right) && SimpleArithmeticOperation.isCommutative(operation)) {
      handleBinaryOperator(operation, right, left);
    } else {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, left));
      handleBinaryOperator(operation, SPILL_OPERAND, right);
      addInstruction(new X86Instruction.Mov(dest, SPILL_OPERAND));
    }
  }

  private void handleBinaryOperator(
      SimpleArithmeticOperation operation, X86Operand left, X86Operand right) {
    final boolean spillLeftRegister = !(left instanceof X86Operand.Register reg) || reg.isVirtual();
    if (spillLeftRegister) {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, left));
    }

    X86Operand target = spillLeftRegister ? SPILL_OPERAND : left;
    switch (operation) {
      case ADD -> addInstruction(new X86Instruction.Add(target, right));
      case SUB -> addInstruction(new X86Instruction.Sub(target, right));
      case MUL -> addInstruction(new X86Instruction.IMul(target, right));
      case EQ -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Sete(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
      case NEQ -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Setne(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
      case AND, BITWISE_AND -> {
        addInstruction(new X86Instruction.And(target, right));
      }
      case OR, BITWISE_OR -> {
        addInstruction(new X86Instruction.Or(target, right));
      }
      case BITWISE_XOR -> {
        addInstruction(new X86Instruction.Xor(target, right));
      }
      case LEFT_SHIFT -> {
        addInstruction(new X86Instruction.Sal(target, right));
      }
      case RIGHT_SHIFT -> {
        addInstruction(new X86Instruction.Sar(target, right));
      }
      case LT -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Setl(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
      case LTE -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Setle(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
      case GT -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Setg(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
      case GTE -> {
        X86Register byteRegister =
            ((X86Register.Physical) ((X86Operand.Register) target).register()).to8Bit();
        X86Operand.Register byteOperand = new X86Operand.Register(byteRegister);
        addInstruction(new X86Instruction.Cmp(target, right));
        addInstruction(new X86Instruction.Setge(byteOperand));
        addInstruction(new X86Instruction.Movzx(target, byteOperand));
      }
    }

    if (spillLeftRegister) {
      addInstruction(new X86Instruction.Mov(left, SPILL_OPERAND));
    }
  }

  private void handleDivisionOperator(BinaryOperationNode node, DivisionOperation operation) {
    generateInstructions(node.left());
    generateInstructions(node.right());

    X86Operand dest = getResultOperand(node);
    X86Operand left = getResultOperand(node.left());
    X86Operand right = getResultOperand(node.right());

    addInstruction(new X86Instruction.Mov(X86Operand.EAX, left));
    addInstruction(new X86Instruction.Cdq());

    if (right instanceof X86Operand.Register register && register.isPhysical()) {
      addInstruction(new X86Instruction.IDiv(right));
    } else {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, right));
      addInstruction(new X86Instruction.IDiv(SPILL_OPERAND));
    }

    X86Operand result =
        switch (operation) {
          case DIV -> X86Operand.EAX;
          case MOD -> X86Operand.EDX;
        };

    addInstruction(new X86Instruction.Mov(dest, result));
  }

  private void handleComparisonOperator(X86Operand left, X86Operand right) {
    if ((left instanceof X86Operand.Register reg1 && reg1.isPhysical())
        || (right instanceof X86Operand.Register reg2 && reg2.isPhysical())) {
      addInstruction(new X86Instruction.Cmp(left, right));
    } else {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, left));
      addInstruction(new X86Instruction.Cmp(SPILL_OPERAND, right));
    }
  }

  private void handleTestOperator(X86Operand operand) {
    if (operand instanceof X86Operand.Register reg && reg.isPhysical()) {
      addInstruction(new X86Instruction.Test(operand, operand));
    } else {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, operand));
      addInstruction(new X86Instruction.Test(SPILL_OPERAND, SPILL_OPERAND));
    }
  }

  private void insertPhiMove(Phi phi, IRDataNode dataNode) {
    if (dataNode instanceof SideEffectNode sideEffectNode && sideEffectNode.input().isEmpty()) {
      return;
    }

    X86Operand srcOperand = getResultOperand(dataNode);
    final boolean srcIsVirtual = srcOperand instanceof X86Operand.Register reg && reg.isVirtual();

    X86Operand dstOperand = getResultOperand(phi);
    final boolean dstIsVirtual = dstOperand instanceof X86Operand.Register reg && reg.isVirtual();

    if (srcIsVirtual && dstIsVirtual) {
      addInstruction(new X86Instruction.Mov(SPILL_OPERAND, srcOperand));
      addInstruction(new X86Instruction.Mov(dstOperand, SPILL_OPERAND));
    } else {
      addInstruction(new X86Instruction.Mov(dstOperand, srcOperand));
    }
  }

  private void addLabel(int id) {
    instructions.add(new X86Instruction.Label(id));
  }

  private void addInstruction(X86Instruction instruction) {
    instructions.add(instruction);
  }

  private X86Operand getResultOperand(IRNode node) {
    return switch (node) {
      case ConstBoolNode boolNode -> new X86Operand.Immediate(boolNode.value() ? 1 : 0);
      case ConstIntNode intNode -> new X86Operand.Immediate(intNode.value());
      case SideEffectNode sideEffect -> getResultOperand(sideEffect.input().get());
      default -> new X86Operand.Register(registerAllocation.get(node));
    };
  }
}
