package edu.kit.kastel.vads.compiler.backend.x86;

public sealed interface X86Register {

  public static final Physical EAX = new Physical(X86RegisterID.EAX);
  public static final Physical EBX = new Physical(X86RegisterID.EBX);
  public static final Physical ECX = new Physical(X86RegisterID.ECX);
  public static final Physical EDX = new Physical(X86RegisterID.EDX);

  public static final Physical ESI = new Physical(X86RegisterID.ESI);
  public static final Physical EDI = new Physical(X86RegisterID.EDI);
  public static final Physical RBP = new Physical(X86RegisterID.RBP);
  public static final Physical RSP = new Physical(X86RegisterID.RSP);

  public static final Physical R8D = new Physical(X86RegisterID.R8D);
  public static final Physical R9D = new Physical(X86RegisterID.R9D);
  public static final Physical R10D = new Physical(X86RegisterID.R10D);
  public static final Physical R11D = new Physical(X86RegisterID.R11D);
  public static final Physical R12D = new Physical(X86RegisterID.R12D);
  public static final Physical R13D = new Physical(X86RegisterID.R13D);
  public static final Physical R14D = new Physical(X86RegisterID.R14D);
  public static final Physical R15D = new Physical(X86RegisterID.R15D);

  record Physical(X86RegisterID id) implements X86Register {
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
