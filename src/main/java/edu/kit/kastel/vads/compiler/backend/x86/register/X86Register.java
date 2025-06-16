package edu.kit.kastel.vads.compiler.backend.x86.register;

public sealed interface X86Register {

  public static final int NUM_REGISTERS = 16;

  // 32-bit registers
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

  // 8-bit counterparts
  public static final Physical AL = new Physical(ID.AL);
  public static final Physical BL = new Physical(ID.BL);
  public static final Physical CL = new Physical(ID.CL);
  public static final Physical DL = new Physical(ID.DL);

  public static final Physical SIL = new Physical(ID.SIL);
  public static final Physical DIL = new Physical(ID.DIL);
  public static final Physical BPL = new Physical(ID.BPL);
  public static final Physical SPL = new Physical(ID.SPL);

  public static final Physical R8B = new Physical(ID.R8B);
  public static final Physical R9B = new Physical(ID.R9B);
  public static final Physical R10B = new Physical(ID.R10B);
  public static final Physical R11B = new Physical(ID.R11B);
  public static final Physical R12B = new Physical(ID.R12B);
  public static final Physical R13B = new Physical(ID.R13B);
  public static final Physical R14B = new Physical(ID.R14B);
  public static final Physical R15B = new Physical(ID.R15B);

  public enum ID {
    EAX,
    AL,

    EBX,
    BL,

    ECX,
    CL,

    EDX,
    DL,

    ESI,
    SIL,

    EDI,
    DIL,

    RBP,
    BPL,

    RSP,
    SPL,

    R8D,
    R8B,

    R9D,
    R9B,

    R10D,
    R10B,

    R11D,
    R11B,

    R12D,
    R12B,

    R13D,
    R13B,

    R14D,
    R14B,

    R15D,
    R15B
  }

  record Physical(ID id) implements X86Register {
    @Override
    public String toString() {
      return id.name();
    }

    public X86Register to8Bit() {
      return switch (id) {
        case EAX, AL -> AL;
        case EBX, BL -> BL;
        case ECX, CL -> CL;
        case EDX, DL -> DL;
        case ESI, SIL -> SIL;
        case EDI, DIL -> DIL;
        case RBP, BPL -> BPL;
        case RSP, SPL -> SPL;
        case R8D, R8B -> R8B;
        case R9D, R9B -> R9B;
        case R10D, R10B -> R10B;
        case R11D, R11B -> R11B;
        case R12D, R12B -> R12B;
        case R13D, R13B -> R13B;
        case R14D, R14B -> R14B;
        case R15D, R15B -> R15B;
      };
    }
  }

  record Virtual(int slot) implements X86Register {
    @Override
    public String toString() {
      return String.format("DWORD PTR [%s + %d]", RSP, slot * 4);
    }
  }
}
