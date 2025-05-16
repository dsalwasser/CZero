package edu.kit.kastel.vads.compiler.backend.x86;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86Instruction;
import edu.kit.kastel.vads.compiler.backend.x86.instruction.X86InstructionGenerator;
import edu.kit.kastel.vads.compiler.backend.x86.optimization.X86InstructionOptimizer;
import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import java.util.List;

public final class X86CodeGenerator extends CodeGenerator {

  private static final String ASSEMBLY_HEADER =
      """
      .intel_syntax noprefix

      .global main
      .global _main
      .text

      main:
      call _main

      # move the return value into the first argument for the syscall
      mov edi, eax
      # move the exit syscall number into rax
      mov eax, 0x3C
      syscall

      _main:
      """;

  private StringBuilder codeBuilder;
  private X86InstructionGenerator instructionGenerator;
  private X86InstructionOptimizer instructionOptimizer;

  public X86CodeGenerator(X86RegisterAllocator registerAllocator) {
    this.codeBuilder = new StringBuilder();
    this.instructionGenerator = new X86InstructionGenerator(registerAllocator);
    this.instructionOptimizer = new X86InstructionOptimizer();
  }

  @Override
  public String generateCode(List<IrGraph> program) {
    codeBuilder.setLength(0);
    codeBuilder.append(ASSEMBLY_HEADER);

    for (IrGraph function : program) {
      List<X86Instruction> instructions = instructionGenerator.generateInstructions(function);
      instructionOptimizer.optimize(instructions);

      for (X86Instruction instruction : instructions) {
        if (instruction instanceof X86Instruction.Nop) {
          continue;
        }

        codeBuilder.append(instruction.toString());
        codeBuilder.append('\n');
      }
    }

    return codeBuilder.toString();
  }
}
