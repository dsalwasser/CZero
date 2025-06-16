package edu.kit.kastel.vads.compiler.lexer.token;

import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.util.Span;

public record Identifier(String value, Span span) implements Token {
  @Override
  public String asString() {
    return value();
  }
}
