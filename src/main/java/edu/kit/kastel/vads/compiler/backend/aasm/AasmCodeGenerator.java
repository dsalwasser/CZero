package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
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
import java.util.List;
import java.util.Map;

public final class AasmCodeGenerator extends CodeGenerator {

  private static final int INDENTATION = 2;

  private StringBuilder codeBuilder;

  private AasmRegisterAllocator registerAllocator;

  public AasmCodeGenerator() {
    this.codeBuilder = new StringBuilder();
    this.registerAllocator = new AasmRegisterAllocator();
  }

  @Override
  public String generateCode(List<IrGraph> program) {
    codeBuilder.setLength(0);

    for (IrGraph function : program) {
      generateCode(function);
    }

    return codeBuilder.toString();
  }

  private void generateCode(IrGraph function) {
    Map<Node, AasmRegister> registers = registerAllocator.allocateRegisters(function);
    List<Node> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);

    codeBuilder.append("function ").append(function.name()).append(" {\n");
    for (Node node : nodes) {
      switch (node) {
        case AddNode add -> binary(registers, add, "add");
        case SubNode sub -> binary(registers, sub, "sub");
        case MulNode mul -> binary(registers, mul, "mul");
        case DivNode div -> binary(registers, div, "div");
        case ModNode mod -> binary(registers, mod, "mod");
        case ReturnNode r ->
            codeBuilder.repeat(" ", INDENTATION).append("ret ").append(registers.get(r.result()));
        case ConstIntNode c ->
            codeBuilder
                .repeat(" ", INDENTATION)
                .append(registers.get(c))
                .append(" = const ")
                .append(c.value());
        case Phi _ -> throw new UnsupportedOperationException("phi");
        case Block _, ProjNode _, StartNode _ -> {
          continue;
        }
      }

      codeBuilder.append("\n");
    }
    codeBuilder.append("}");
  }

  private void binary(Map<Node, AasmRegister> registers, BinaryOperationNode node, String opcode) {
    codeBuilder
        .repeat(" ", INDENTATION)
        .append(registers.get(node))
        .append(" = ")
        .append(opcode)
        .append(" ")
        .append(registers.get(node.left()))
        .append(" ")
        .append(registers.get(node.right()));
  }
}
