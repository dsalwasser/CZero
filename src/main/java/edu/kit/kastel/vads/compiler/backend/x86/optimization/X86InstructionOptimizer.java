package edu.kit.kastel.vads.compiler.backend.x86.optimization;

import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.transformer.RedundantMoveTransformer;
import java.util.List;

public final class X86InstructionOptimizer {

  private List<X86InstructionTransformer> instructionTransformers =
      List.of(new RedundantMoveTransformer());

  public void optimize(List<X86Instruction> instructions) {
    for (int i = 0; i < instructions.size(); i++) {
      final X86Instruction instruction = instructions.get(i);

      for (X86InstructionTransformer transformer : instructionTransformers) {
        final X86Instruction transformedInstruction = transformer.transform(instruction);

        if (instruction != transformedInstruction) {
          instructions.set(i, transformedInstruction);
          break;
        }
      }
    }
  }
}
