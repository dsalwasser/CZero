package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.aasm.AasmRegister.Virtual;
import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AasmRegisterAllocator {

  public Map<IRNode, AasmRegister> allocateRegisters(IRGraph function) {
    Map<IRNode, AasmRegister> registerAllocation = new HashMap<>();
    int curSlot = 0;

    List<IRNode> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);
    for (IRNode node : nodes) {
      if (node instanceof IRDataNode) {
        registerAllocation.put(node, new Virtual(curSlot++));
      }
    }

    return registerAllocation;
  }
}
