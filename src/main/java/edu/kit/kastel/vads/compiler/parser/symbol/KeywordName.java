package edu.kit.kastel.vads.compiler.parser.symbol;

import edu.kit.kastel.vads.compiler.lexer.KeywordKind;

record KeywordName(KeywordKind type) implements Name {
  @Override
  public String asString() {
    return type().keyword();
  }
}
