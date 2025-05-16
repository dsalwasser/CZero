package edu.kit.kastel.vads.compiler.backend.aasm;

public sealed interface AasmRegister {

  public record AasmVirtualRegister(int id) implements AasmRegister {
    @Override
    public String toString() {
      return "%" + id();
    }
  }
}
