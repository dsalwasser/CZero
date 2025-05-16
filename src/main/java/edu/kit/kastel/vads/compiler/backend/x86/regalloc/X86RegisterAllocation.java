package edu.kit.kastel.vads.compiler.backend.x86.regalloc;

import edu.kit.kastel.vads.compiler.backend.x86.X86Register;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.Map;

public final record X86RegisterAllocation(
    Map<Node, X86Register> allocation, int numVirtualRegisters) {

  public X86Register get(Node node) {
    return allocation.get(node);
  }
}
