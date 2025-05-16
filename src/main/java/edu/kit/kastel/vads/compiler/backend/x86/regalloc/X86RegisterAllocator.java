package edu.kit.kastel.vads.compiler.backend.x86.regalloc;

import edu.kit.kastel.vads.compiler.ir.IrGraph;

public sealed interface X86RegisterAllocator permits X86VirtualRegisterAllocator {

  public X86RegisterAllocation allocateRegisters(IrGraph graph);
}
