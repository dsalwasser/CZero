package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.lexer.token.ErrorToken;
import edu.kit.kastel.vads.compiler.lexer.token.Identifier;
import edu.kit.kastel.vads.compiler.lexer.token.Keyword;
import edu.kit.kastel.vads.compiler.lexer.token.NumberLiteral;
import edu.kit.kastel.vads.compiler.lexer.token.Operator;
import edu.kit.kastel.vads.compiler.lexer.token.Separator;
import edu.kit.kastel.vads.compiler.util.Span;

public sealed interface Token
    permits ErrorToken, Identifier, Keyword, NumberLiteral, Operator, Separator {

  Span span();

  default boolean isType() {
    return false;
  }

  default boolean isKeyword(KeywordKind keywordKind) {
    return false;
  }

  default boolean isOperator(OperatorKind operatorKind) {
    return false;
  }

  default boolean isSeparator(SeparatorKind separatorKind) {
    return false;
  }

  String asString();
}
