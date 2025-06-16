package edu.kit.kastel.vads.compiler.backend.util;

public enum SimpleArithmeticOperation {
  ADD,
  SUB,
  MUL,

  EQ,
  NEQ,

  AND,
  OR,

  BITWISE_AND,
  BITWISE_OR,
  BITWISE_XOR,

  LEFT_SHIFT,
  RIGHT_SHIFT,

  LT,
  LTE,
  GT,
  GTE;

  public static boolean isCommutative(SimpleArithmeticOperation operation) {
    return switch (operation) {
      case ADD, MUL, EQ, NEQ, AND, OR, BITWISE_AND, BITWISE_OR, BITWISE_XOR -> true;
      case SUB, LEFT_SHIFT, RIGHT_SHIFT, LT, LTE, GT, GTE -> false;
    };
  }
}
