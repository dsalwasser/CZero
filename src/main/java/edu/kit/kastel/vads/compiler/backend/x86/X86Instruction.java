package edu.kit.kastel.vads.compiler.backend.x86;

public sealed interface X86Instruction {

  record Add(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("add %s, %s", left, right);
    }
  }

  record Sub(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("sub %s, %s", left, right);
    }
  }

  record IMul(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("imul %s, %s", left, right);
    }
  }

  record IDiv(X86Operand divisor) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("idiv %s", divisor);
    }
  }

  record Cdq() implements X86Instruction {
    @Override
    public String toString() {
      return "cdq";
    }
  }

  record Push(X86Operand op) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("push %s", op);
    }
  }

  record Mov(X86Operand dest, X86Operand src) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("mov %s, %s", dest, src);
    }
  }

  record Leave() implements X86Instruction {
    @Override
    public String toString() {
      return "leave";
    }
  }

  record Return() implements X86Instruction {
    @Override
    public String toString() {
      return "ret";
    }
  }
}
