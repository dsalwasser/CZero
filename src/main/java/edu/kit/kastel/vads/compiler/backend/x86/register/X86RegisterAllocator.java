package edu.kit.kastel.vads.compiler.backend.x86.register;

import edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring.X86GraphColoringRegisterAllocator;
import edu.kit.kastel.vads.compiler.backend.x86.register.virtual.X86VirtualRegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;

public sealed interface X86RegisterAllocator
    permits X86GraphColoringRegisterAllocator, X86VirtualRegisterAllocator {

  public X86RegisterAllocation allocateRegisters(IrGraph graph);
}
