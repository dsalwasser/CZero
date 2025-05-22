package edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Operand;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.X86InstructionRewriter;
import edu.kit.kastel.vads.compiler.util.MathUtil;

public final class StrengthReductionRewriter implements X86InstructionRewriter {

  @Override
  public X86Instruction transform(X86Instruction instruction) {
    return switch (instruction) {
      // x * 2^n == x << n
      case X86Instruction.IMul(X86Operand left, X86Operand.Immediate imm)
          when MathUtil.isPowerOfTwo(imm.value()) ->
          new X86Instruction.Shl(
              left, new X86Operand.Immediate(MathUtil.getPowerOfTwoExponent(imm.value())));

      // 2^n * x == x << n
      case X86Instruction.IMul(X86Operand.Immediate imm, X86Operand right)
          when MathUtil.isPowerOfTwo(imm.value()) ->
          new X86Instruction.Shl(
              right, new X86Operand.Immediate(MathUtil.getPowerOfTwoExponent(imm.value())));

      default -> instruction;
    };
  }
}
