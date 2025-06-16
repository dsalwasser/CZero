package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Span;

public record ConditionalExpressionTree(
    ExpressionTree condition, ExpressionTree lhs, ExpressionTree rhs) implements ExpressionTree {
  @Override
  public Span span() {
    return condition().span().merge(rhs().span());
  }

  @Override
  public <T, R> R accept(Visitor<T, R> visitor, T data) {
    return visitor.visit(this, data);
  }
}
