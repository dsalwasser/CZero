package edu.kit.kastel.vads.compiler.backend.x86.optimization;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer.RedundantMoveTransformer;

public sealed interface X86InstructionTransformer permits RedundantMoveTransformer {

  public X86Instruction transform(X86Instruction instruction);
}
