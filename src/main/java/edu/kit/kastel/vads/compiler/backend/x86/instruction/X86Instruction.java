package edu.kit.kastel.vads.compiler.backend.x86.instruction;

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

  record Shl(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("shl %s, %s", left, right);
    }
  }

  record And(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("and %s, %s", left, right);
    }
  }

  record Or(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("or %s, %s", left, right);
    }
  }

  record Xor(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("xor %s, %s", left, right);
    }
  }

  record Sal(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("sal %s, %s", left, right);
    }
  }

  record Sar(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("sar %s, %s", left, right);
    }
  }

  record Sete(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("sete %s", reg);
    }
  }

  record Setne(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("setne %s", reg);
    }
  }

  record Setl(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("setl %s", reg);
    }
  }

  record Setle(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("setle %s", reg);
    }
  }

  record Setg(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("setg %s", reg);
    }
  }

  record Setge(X86Operand.Register reg) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("setge %s", reg);
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

  record Movzx(X86Operand dest, X86Operand src) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("movzx %s, %s", dest, src);
    }
  }

  record Cmp(X86Operand left, X86Operand right) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("cmp %s, %s", left, right);
    }
  }

  record Je(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("je .LB%d", id);
    }
  }

  record Jne(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jne .LB%d", id);
    }
  }

  record Jl(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jl .LB%d", id);
    }
  }

  record Jz(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jz .LB%d", id);
    }
  }

  record Jnz(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jnz .LB%d", id);
    }
  }

  record Jle(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jle .LB%d", id);
    }
  }

  record Jg(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jg .LB%d", id);
    }
  }

  record Jge(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jge .LB%d", id);
    }
  }

  record Test(X86Operand lhs, X86Operand rhs) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("test %s, %s", lhs, rhs);
    }
  }

  record Jmp(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format("jmp .LB%d", id);
    }
  }

  record Label(int id) implements X86Instruction {
    @Override
    public String toString() {
      return String.format(".LB%d:", id);
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

  record Nop() implements X86Instruction {
    @Override
    public String toString() {
      return "nop";
    }
  }
}
