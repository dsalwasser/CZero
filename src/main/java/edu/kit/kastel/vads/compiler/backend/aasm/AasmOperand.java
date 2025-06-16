package edu.kit.kastel.vads.compiler.backend.aasm;

public sealed interface AasmOperand {
  record Register(AasmRegister register) implements AasmOperand {
    @Override
    public String toString() {
      return register.toString();
    }
  }

  record Immediate(String value) implements AasmOperand {
    @Override
    public String toString() {
      return value;
    }
  }
}
