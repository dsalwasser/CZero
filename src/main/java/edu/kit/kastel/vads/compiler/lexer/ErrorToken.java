package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.util.Span;

public record ErrorToken(String value, Span span) implements Token {
  @Override
  public String asString() {
    return value();
  }
}
