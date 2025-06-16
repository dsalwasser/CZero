package edu.kit.kastel.vads.compiler.lexer.token;

import edu.kit.kastel.vads.compiler.lexer.OperatorKind;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.util.Span;

public record Operator(OperatorKind kind, Span span) implements Token {
  @Override
  public boolean isOperator(OperatorKind operatorKind) {
    return kind() == operatorKind;
  }

  @Override
  public String asString() {
    return kind().toString();
  }
}
