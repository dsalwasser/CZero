package edu.kit.kastel.vads.compiler.backend.x86.register;

public sealed interface X86Register {

  public static final int NUM_REGISTERS = 16;

  public static final Physical EAX = new Physical(ID.EAX);
  public static final Physical EBX = new Physical(ID.EBX);
  public static final Physical ECX = new Physical(ID.ECX);
  public static final Physical EDX = new Physical(ID.EDX);

  public static final Physical ESI = new Physical(ID.ESI);
  public static final Physical EDI = new Physical(ID.EDI);
  public static final Physical RBP = new Physical(ID.RBP);
  public static final Physical RSP = new Physical(ID.RSP);

  public static final Physical R8D = new Physical(ID.R8D);
  public static final Physical R9D = new Physical(ID.R9D);
  public static final Physical R10D = new Physical(ID.R10D);
  public static final Physical R11D = new Physical(ID.R11D);
  public static final Physical R12D = new Physical(ID.R12D);
  public static final Physical R13D = new Physical(ID.R13D);
  public static final Physical R14D = new Physical(ID.R14D);
  public static final Physical R15D = new Physical(ID.R15D);

  public enum ID {
    EAX,
    EBX,
    ECX,
    EDX,

    ESI,
    EDI,
    RBP,
    RSP,

    R8D,
    R9D,
    R10D,
    R11D,
    R12D,
    R13D,
    R14D,
    R15D,
  }

  record Physical(ID id) implements X86Register {
    @Override
    public String toString() {
      return id.name();
    }
  }

  record Virtual(int slot) implements X86Register {
    @Override
    public String toString() {
      return String.format("DWORD PTR [%s + %d]", RSP, slot * 4);
    }
  }
}
