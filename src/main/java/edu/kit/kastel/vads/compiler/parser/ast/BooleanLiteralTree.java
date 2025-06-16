package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Span;

public record BooleanLiteralTree(boolean value, Span position) implements ExpressionTree {

  @Override
  public Span span() {
    return position;
  }

  @Override
  public <T, R> R accept(Visitor<T, R> visitor, T data) {
    return visitor.visit(this, data);
  }
}
