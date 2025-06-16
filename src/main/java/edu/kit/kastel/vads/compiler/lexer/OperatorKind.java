package edu.kit.kastel.vads.compiler.lexer;

public enum OperatorKind {
  EQUALITY("==", Value.BOTH, Value.BOOL),
  INEQUALITY("!=", Value.BOTH, Value.BOOL),

  CONDITIONAL("?", Value.BOOL, Value.BOTH),
  NOT("!", Value.BOOL, Value.BOOL),
  AND("&&", Value.BOOL, Value.BOOL),
  OR("||", Value.BOOL, Value.BOOL),

  LESS_THAN("<", Value.INT, Value.BOOL),
  LESS_THAN_EQUAL("<=", Value.INT, Value.BOOL),
  GREATER_THAN(">", Value.INT, Value.BOOL),
  GREATER_THAN_EQUAL(">=", Value.INT, Value.BOOL),

  BITWISE_NOT("~", Value.INT, Value.INT),
  BITWISE_AND("&", Value.INT, Value.INT),
  BITWISE_XOR("^", Value.INT, Value.INT),
  BITWISE_OR("|", Value.INT, Value.INT),

  PLUS("+", Value.INT, Value.INT),
  MINUS("-", Value.INT, Value.INT),
  MUL("*", Value.INT, Value.INT),
  DIV("/", Value.INT, Value.INT),
  MOD("%", Value.INT, Value.INT),
  LEFT_SHIFT("<<", Value.INT, Value.INT),
  RIGHT_SHIFT(">>", Value.INT, Value.INT),

  ASSIGN_PLUS("+=", Value.INT, Value.INT),
  ASSIGN_MINUS("-=", Value.INT, Value.INT),
  ASSIGN_MUL("*=", Value.INT, Value.INT),
  ASSIGN_DIV("/=", Value.INT, Value.INT),
  ASSIGN_MOD("%=", Value.INT, Value.INT),
  ASSIGN_AND("&=", Value.INT, Value.INT),
  ASSIGN_XOR("^=", Value.INT, Value.INT),
  ASSIGN_OR("|=", Value.INT, Value.INT),
  ASSIGN_LEFT_SHIFT("<<=", Value.INT, Value.INT),
  ASSIGN_RIGHT_SHIFT(">>=", Value.INT, Value.INT),

  ASSIGN("=", Value.BOTH, Value.BOTH);

  private final String value;
  private final Value input;
  private final Value output;

  OperatorKind(String value, Value input, Value output) {
    this.value = value;
    this.input = input;
    this.output = output;
  }

  public Value getInput() {
    return input;
  }

  public Value getOutput() {
    return output;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public enum Value {
    BOTH,
    BOOL,
    INT
  };
}
