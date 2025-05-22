package edu.kit.kastel.vads.compiler.lexer.token;

import edu.kit.kastel.vads.compiler.lexer.KeywordKind;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.util.Span;

public record Keyword(KeywordKind type, Span span) implements Token {
  @Override
  public boolean isKeyword(KeywordKind keywordType) {
    return type() == keywordType;
  }

  @Override
  public String asString() {
    return type().keyword();
  }
}
