package edu.kit.kastel.vads.compiler.lexer;

public enum SeparatorKind {
  PAREN_OPEN("("),
  PAREN_CLOSE(")"),
  BRACE_OPEN("{"),
  BRACE_CLOSE("}"),
  COLON(":"),
  SEMICOLON(";");

  private final String value;

  SeparatorKind(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
