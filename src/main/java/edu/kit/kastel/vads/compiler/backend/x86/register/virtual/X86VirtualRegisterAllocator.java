package edu.kit.kastel.vads.compiler.backend.x86.register.virtual;

import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86Register;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class X86VirtualRegisterAllocator implements X86RegisterAllocator {

  @Override
  public X86RegisterAllocation allocateRegisters(IRGraph function) {
    Map<IRNode, X86Register> registerAllocation = new HashMap<>();
    int curSlot = 0;

    List<IRNode> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);
    for (IRNode node : nodes) {
      if (node instanceof IRDataNode) {
        registerAllocation.put(node, new X86Register.Virtual(curSlot++));
      }
    }

    return new X86RegisterAllocation(registerAllocation, curSlot);
  }
}
