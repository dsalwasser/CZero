package edu.kit.kastel.vads.compiler.backend.util;

public enum SimpleArithmeticOperation {
  ADD,
  SUB,
  MUL;

  public static boolean isCommutative(SimpleArithmeticOperation operation) {
    return switch (operation) {
      case ADD -> true;
      case SUB -> false;
      case MUL -> true;
    };
  }
}
