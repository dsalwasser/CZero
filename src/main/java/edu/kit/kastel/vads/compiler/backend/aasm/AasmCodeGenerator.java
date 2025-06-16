package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AasmCodeGenerator extends CodeGenerator {

  private static class CodeBuilder {
    private static final int INDENTATION = 2;

    private final StringBuilder builder = new StringBuilder();

    public void addLine(String line) {
      builder.append(line).append("\n");
    }

    public void addInstruction(String instruction) {
      builder.repeat(" ", INDENTATION).append(instruction).append("\n");
    }

    public void addLabel(int label) {
      builder.append("\n").append(String.format("label#%d:", label)).append("\n");
    }

    public void reset() {
      builder.setLength(0);
    }

    @Override
    public String toString() {
      return builder.toString();
    }
  }

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

  private final CodeBuilder builder = new CodeBuilder();

  private final AasmRegisterAllocator registerAllocator = new AasmRegisterAllocator();
  private Map<IRNode, AasmRegister> registerAllocation;

  private Map<IRControlNode, Integer> labels;
  private int curLabel;

  @Override
  public String generateCode(List<IRGraph> program) {
    builder.reset();

    for (IRGraph ir : program) {
      generateCode(ir);
    }

    return builder.toString();
  }

  private void generateCode(IRGraph ir) {
    this.registerAllocation = registerAllocator.allocateRegisters(ir);
    this.labels = new HashMap<>();
    this.curLabel = 1;

    builder.addLine(String.format("function %s {", ir.name()));

    IRControlNode startNode = ir.startBlock().entryNode();
    generateCode(startNode, new CodeGenerationStatus());

    builder.addLine("}");
  }

  private void generateCode(IRControlNode controlNode, CodeGenerationStatus status) {
    switch (controlNode) {
      case StartNode start -> {
        generateCode(start.successor(), status);
      }
      case IfNode branch -> {
        final int thenLabel = curLabel++;
        final int elseLabel = curLabel++;
        if (branch.hasMerge()) {
          final int mergeLabel = curLabel++;
          labels.put(branch.merge(), mergeLabel);
        }

        generateCode(branch.condition());
        builder.addInstruction(
            String.format(
                "if %s then goto label#%d else goto label#%d",
                getResultOperand(branch.condition()), thenLabel, elseLabel));

        builder.addLabel(thenLabel);
        generateCode(branch.thenSuccessor(), status.takeThenBranch());

        builder.addLabel(elseLabel);
        generateCode(branch.elseSuccessor(), status.takeElseBranch());

        if (branch.hasMerge()) {
          builder.addLabel(labels.get(branch.merge()));
          generateCode(branch.merge(), status);
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
          generateCode(dataNode);

          if (isMoveable(dataNode)) {
            builder.addInstruction(
                String.format("%s = %s", registerAllocation.get(phi), getResultOperand(dataNode)));
          }
        }

        builder.addLabel(loopEntryLabel);
        generateCode(loop.condition());

        builder.addInstruction(
            String.format(
                "if %s then goto label#%d else goto label#%d",
                getResultOperand(loop.condition()), loopBodyEntryLabel, loopExitLabel));

        System.out.println("entry:" + loopBodyEntryLabel);
        builder.addLabel(loopBodyEntryLabel);
        generateCode(loop.body(), status);

        builder.addLabel(loopBodyExitLabel);
        generateCode(loop.bodyMerge(), status);

        builder.addLabel(loopExitLabel);
        generateCode(loop.exitMerge(), status);
      }
      case JumpNode jump -> {
        final IRControlNode targetNode = jump.successor();

        switch (targetNode) {
          case IfMergeNode merge -> {
            for (Phi phi : merge.phis()) {
              IRDataNode dataNode =
                  phi.dataNode(status.branchStatus() == BranchStatus.THEN ? 0 : 1);
              generateCode(dataNode);

              if (isMoveable(dataNode)) {
                builder.addInstruction(
                    String.format(
                        "%s = %s", registerAllocation.get(phi), getResultOperand(dataNode)));
              }
            }

            final int targetLabel = labels.get(targetNode);
            builder.addInstruction(String.format("jump #%d", targetLabel));
          }
          case LoopBodyMergeNode _ -> {
            final int targetLabel = labels.get(targetNode);
            builder.addInstruction(String.format("jump #%d", targetLabel));
          }
          case LoopExitMergeNode merge -> {
            for (Phi phi : merge.phis()) {
              IRDataNode dataNode = phi.dataNode(merge.predecessors().indexOf(jump));
              generateCode(dataNode);

              if (isMoveable(dataNode)) {
                builder.addInstruction(
                    String.format(
                        "%s = %s", registerAllocation.get(phi), getResultOperand(dataNode)));
              }
            }

            final int targetLabel = labels.get(targetNode);
            builder.addInstruction(String.format("jump #%d", targetLabel));
          }
          default -> {
            generateCode(targetNode, status);
          }
        }
      }
      case IfMergeNode merge -> {
        generateCode(merge.successor(), status);
      }
      case LoopBodyMergeNode merge -> {
        LoopNode loop = merge.getLoopNode();

        for (IRNode node : loop.block().nodes().reversed()) {
          if (node instanceof Phi phi) {
            IRDataNode pred = phi.dataNode(1);
            generateCode(pred);

            if (isMoveable(pred)) {
              builder.addInstruction(
                  String.format("%s = %s", registerAllocation.get(phi), getResultOperand(pred)));
            }
          }
        }

        builder.addInstruction(String.format("jump #%d", labels.get(loop)));
      }
      case LoopExitMergeNode merge -> {
        for (Phi phi : merge.phis()) {
          IRDataNode dataNode = phi.dataNode(merge.predecessors().indexOf(merge.loopNode()));
          generateCode(dataNode);

          if (isMoveable(dataNode)) {
            builder.addInstruction(
                String.format("%s = %s", registerAllocation.get(phi), getResultOperand(dataNode)));
          }
        }

        generateCode(merge.successor(), status);
      }
      case ReturnNode ret -> {
        for (SideEffectNode sideEffectNode : ret.sideEffectNodes()) {
          sideEffectNode.input().ifPresent(dataNode -> generateCode(dataNode));
        }

        generateCode(ret.result());
        builder.addInstruction(String.format("return %s", getResultOperand(ret.result())));
      }
      default -> {}
    }
  }

  private boolean isMoveable(IRDataNode dataNode) {
    return !(dataNode instanceof SideEffectNode sideEffectNode)
        || sideEffectNode.input().isPresent();
  }

  private void generateCode(IRDataNode dataNode) {
    if (dataNode instanceof SideEffectNode sideEffectNode) {
      if (sideEffectNode.input().isPresent()) {
        generateCode(sideEffectNode.input().get());
        return;
      }
    }

    switch (dataNode) {
      case BinaryOperationNode node -> {
        generateCode(node.left());
        generateCode(node.right());

        String operation =
            switch (node) {
              case AddNode _ -> "add";
              case DivNode _ -> "div";
              case ModNode _ -> "mod";
              case MulNode _ -> "mul";
              case SubNode _ -> "sub";
              case EqualityNode _ -> "eq";
              case InequalityNode _ -> "neq";
              case AndNode _ -> "and";
              case OrNode _ -> "or";
              case BitwiseAndNode _ -> "band";
              case BitwiseOrNode _ -> "bor";
              case BitwiseXORNode _ -> "bxor";
              case LeftShiftNode _ -> "lsh";
              case RightShiftNode _ -> "rsh";
              case LessThanNode _ -> "lt";
              case LessThanEqualNode _ -> "lte";
              case GreaterThanNode _ -> "gt";
              case GreaterThanEqualNode _ -> "gte";
            };

        builder.addInstruction(
            String.format(
                "%s = %s %s %s",
                registerAllocation.get(node),
                operation,
                getResultOperand(node.left()),
                getResultOperand(node.right())));
      }
      default -> {}
    }
  }

  private AasmOperand getResultOperand(IRDataNode node) {
    return switch (node) {
      case ConstBoolNode boolNode -> new AasmOperand.Immediate(boolNode.value() ? "true" : "false");
      case ConstIntNode intNode -> new AasmOperand.Immediate(String.valueOf(intNode.value()));
      case SideEffectNode sideEffect -> getResultOperand(sideEffect.input().get());
      default -> new AasmOperand.Register(registerAllocation.get(node));
    };
  }
}
