package edu.kit.kastel.vads.compiler.backend.x86.instruction;

import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.backend.util.DivisionOperation;
import edu.kit.kastel.vads.compiler.backend.util.SimpleArithmeticOperation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import java.util.ArrayList;
import java.util.List;

public final class X86InstructionGenerator {

  private static final X86Operand SPILL_OPERAND =
      new X86Operand.Register(X86RegisterAllocation.SPILL_REGISTER);

  private X86RegisterAllocator registerAllocator;
  private X86RegisterAllocation registerAllocation;

  private List<X86Instruction> instructions;

  public X86InstructionGenerator(X86RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }

  public List<X86Instruction> generateInstructions(IrGraph function) {
    instructions = new ArrayList<>();
    registerAllocation = registerAllocator.allocateRegisters(function);

    // Add function prologue to create a stack frame by saving the old base pointer,
    // creating a new stack frame, and allocating enough space on the stack to hold
    // the virtual registers.
    final int stackSize = registerAllocation.numVirtualRegisters() * 4;
    addInstruction(new X86Instruction.Push(X86Operand.RBP));
    addInstruction(new X86Instruction.Mov(X86Operand.RBP, X86Operand.RSP));
    addInstruction(new X86Instruction.Sub(X86Operand.RSP, new X86Operand.Immediate(stackSize)));

    List<Node> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);
    for (Node node : nodes) {
      switch (node) {
        case AddNode add -> handleBinaryOperator(add, SimpleArithmeticOperation.ADD);
        case SubNode sub -> handleBinaryOperator(sub, SimpleArithmeticOperation.SUB);
        case MulNode mul -> handleBinaryOperator(mul, SimpleArithmeticOperation.MUL);
        case DivNode div -> handleDivisionOperator(div, DivisionOperation.DIV);
        case ModNode mod -> handleDivisionOperator(mod, DivisionOperation.MOD);
        case ReturnNode ret -> handleReturn(ret);
        case ConstIntNode _ -> {}
        case Phi _ -> throw new UnsupportedOperationException("phi");
        case Block _, ProjNode _, StartNode _ -> {}
      }
    }

    return instructions;
  }

  private void handleBinaryOperator(BinaryOperationNode node, SimpleArithmeticOperation operation) {
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
    }

    if (spillLeftRegister) {
      addInstruction(new X86Instruction.Mov(left, SPILL_OPERAND));
    }
  }

  private void handleDivisionOperator(BinaryOperationNode node, DivisionOperation operation) {
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

  private void handleReturn(ReturnNode node) {
    // Move the result into the return register, add a function epilogue to tear
    // down the stack frame, and return from the function
    addInstruction(new X86Instruction.Mov(X86Operand.EAX, getResultOperand(node.result())));
    addInstruction(new X86Instruction.Leave());
    addInstruction(new X86Instruction.Return());
  }

  private void addInstruction(X86Instruction instruction) {
    instructions.add(instruction);
  }

  private X86Operand getResultOperand(Node node) {
    if (node instanceof ConstIntNode intNode) {
      return new X86Operand.Immediate(intNode.value());
    }

    return new X86Operand.Register(registerAllocation.get(node));
  }
}
