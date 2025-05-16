package edu.kit.kastel.vads.compiler.backend.x86.register.virtual;

import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86Register;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class X86VirtualRegisterAllocator implements X86RegisterAllocator {

  @Override
  public X86RegisterAllocation allocateRegisters(IrGraph function) {
    Map<Node, X86Register> registerAllocation = new HashMap<>();
    int curSlot = 0;

    List<Node> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function);
    for (Node node : nodes) {
      if (needsRegister(node)) {
        registerAllocation.put(node, new X86Register.Virtual(curSlot++));
      }
    }

    return new X86RegisterAllocation(registerAllocation, curSlot);
  }

  private static boolean needsRegister(Node node) {
    return node instanceof BinaryOperationNode || node instanceof Phi || node instanceof ReturnNode;
  }
}
