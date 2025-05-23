package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.aasm.AasmRegister.AasmVirtualRegister;
import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AasmRegisterAllocator {

  public Map<Node, AasmRegister> allocateRegisters(IrGraph function) {
    Map<Node, AasmRegister> registers = new HashMap<>();
    int slot = 0;

    List<Node> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);
    for (Node node : nodes) {
      if (needsRegister(node)) {
        registers.put(node, new AasmVirtualRegister(slot++));
      }
    }

    return registers;
  }

  private static boolean needsRegister(Node node) {
    return node instanceof ConstIntNode
        || node instanceof BinaryOperationNode
        || node instanceof Phi
        || node instanceof ReturnNode;
  }
}
