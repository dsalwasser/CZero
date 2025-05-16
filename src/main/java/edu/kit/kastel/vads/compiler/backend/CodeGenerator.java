package edu.kit.kastel.vads.compiler.backend;

import edu.kit.kastel.vads.compiler.backend.aasm.AasmCodeGenerator;
import edu.kit.kastel.vads.compiler.backend.x86.X86CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring.LeastUsedColorSpillingHeuristic;
import edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring.X86GraphColoringRegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import java.util.List;

public abstract sealed class CodeGenerator permits AasmCodeGenerator, X86CodeGenerator {

  public static CodeGenerator construct(TargetMachine target) {
    return switch (target) {
      case AASM -> new AasmCodeGenerator();
      case X86 ->
          new X86CodeGenerator(
              new X86GraphColoringRegisterAllocator(new LeastUsedColorSpillingHeuristic()));
    };
  }

  public abstract String generateCode(List<IrGraph> program);
}
