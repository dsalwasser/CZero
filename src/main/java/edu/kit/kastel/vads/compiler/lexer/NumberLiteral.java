package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.util.Span;

public record NumberLiteral(String value, int base, Span span) implements Token {
  @Override
  public String asString() {
    return value();
  }
}
