package edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Operand;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.X86InstructionRewriter;

public final class RedundantMoveRewriter implements X86InstructionRewriter {

  @Override
  public X86Instruction transform(X86Instruction instruction) {
    return switch (instruction) {
      // x <- x == nop
      case X86Instruction.Mov(X86Operand dest, X86Operand.Immediate src) when src.equals(dest) ->
          new X86Instruction.Nop();

      default -> instruction;
    };
  }
}
