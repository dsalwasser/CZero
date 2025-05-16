package edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.X86InstructionTransformer;

public final class RedundantMoveTransformer implements X86InstructionTransformer {

  @Override
  public X86Instruction transform(X86Instruction instruction) {
    if (instruction instanceof X86Instruction.Mov mov && mov.src().equals(mov.dest())) {
      return new X86Instruction.Nop();
    }

    return instruction;
  }
}
