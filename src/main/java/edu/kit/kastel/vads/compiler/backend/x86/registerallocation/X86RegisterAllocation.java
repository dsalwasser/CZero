package edu.kit.kastel.vads.compiler.backend.x86.registerallocation;

import edu.kit.kastel.vads.compiler.backend.x86.X86Register;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final record X86RegisterAllocation(
    Map<Node, X86Register> allocation, int numVirtualRegisters) {

  public static final X86Register SPILL_REGISTER = X86Register.R15D;
  public static final Set<X86Register> RESERVED_REGISTERS =
      Set.of(X86Register.EAX, X86Register.EDX, X86Register.RBP, X86Register.RSP, SPILL_REGISTER);

  public static final int NUM_AVAIABLE_REGISTERS =
      X86Register.NUM_REGISTERS - RESERVED_REGISTERS.size();
  public static final List<X86Register> AVAILABLE_REGISTERS =
      List.of(
          X86Register.EBX,
          X86Register.ECX,
          X86Register.ESI,
          X86Register.EDI,
          X86Register.R8D,
          X86Register.R9D,
          X86Register.R10D,
          X86Register.R11D,
          X86Register.R12D,
          X86Register.R13D,
          X86Register.R14D);

  public X86Register get(Node node) {
    return allocation.get(node);
  }
}
