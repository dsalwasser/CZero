package edu.kit.kastel.vads.compiler.backend.x86.optimization;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer.RedundantMoveRewriter;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer.StrengthReductionRewriter;

public sealed interface X86InstructionRewriter
    permits RedundantMoveRewriter, StrengthReductionRewriter {

  public X86Instruction transform(X86Instruction instruction);
}
