package edu.kit.kastel.vads.compiler.lexer;

public enum OperatorKind {
  EQUALITY("=="),
  INEQUALITY("!="),

  CONDITIONAL("?"),
  NOT("!"),
  AND("&&"),
  OR("||"),

  LESS_THAN("<"),
  LESS_THAN_EQUAL("<="),
  GREATER_THAN(">"),
  GREATER_THAN_EQUAL(">="),

  BITWISE_NOT("~"),
  BITWISE_AND("&"),
  BITWISE_XOR("^"),
  BITWISE_OR("|"),

  PLUS("+"),
  MINUS("-"),
  MUL("*"),
  DIV("/"),
  MOD("%"),
  LEFT_SHIFT("<<"),
  RIGHT_SHIFT(">>"),

  ASSIGN_PLUS("+="),
  ASSIGN_MINUS("-="),
  ASSIGN_MUL("*="),
  ASSIGN_DIV("/="),
  ASSIGN_MOD("%="),
  ASSIGN_AND("&="),
  ASSIGN_XOR("^="),
  ASSIGN_OR("|="),
  ASSIGN_LEFT_SHIFT("<<="),
  ASSIGN_RIGHT_SHIFT(">>="),

  ASSIGN("=");

  private final String value;

  OperatorKind(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
