package edu.kit.kastel.vads.compiler.lexer.token;

import edu.kit.kastel.vads.compiler.lexer.SeparatorKind;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.util.Span;

public record Separator(SeparatorKind kind, Span span) implements Token {
  @Override
  public boolean isSeparator(SeparatorKind separatorKind) {
    return kind() == separatorKind;
  }

  @Override
  public String asString() {
    return kind().toString();
  }
}
