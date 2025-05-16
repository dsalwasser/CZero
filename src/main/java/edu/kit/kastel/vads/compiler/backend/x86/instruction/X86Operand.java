package edu.kit.kastel.vads.compiler.backend.x86.instruction;

import edu.kit.kastel.vads.compiler.backend.x86.register.X86Register;

public sealed interface X86Operand {

  public static final Register EAX = new Register(X86Register.EAX);
  public static final Register EBX = new Register(X86Register.EBX);
  public static final Register ECX = new Register(X86Register.ECX);
  public static final Register EDX = new Register(X86Register.EDX);

  public static final Register ESI = new Register(X86Register.ESI);
  public static final Register EDI = new Register(X86Register.EDI);
  public static final Register RBP = new Register(X86Register.RBP);
  public static final Register RSP = new Register(X86Register.RSP);

  public static final Register R8D = new Register(X86Register.R8D);
  public static final Register R9D = new Register(X86Register.R9D);
  public static final Register R10D = new Register(X86Register.R10D);
  public static final Register R11D = new Register(X86Register.R11D);
  public static final Register R12D = new Register(X86Register.R12D);
  public static final Register R13D = new Register(X86Register.R13D);
  public static final Register R14D = new Register(X86Register.R14D);
  public static final Register R15D = new Register(X86Register.R15D);

  record Register(X86Register register) implements X86Operand {
    @Override
    public String toString() {
      return register.toString();
    }

    public boolean isPhysical() {
      return register instanceof X86Register.Physical;
    }

    public boolean isVirtual() {
      return !isPhysical();
    }
  }

  record Immediate(int value) implements X86Operand {
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
