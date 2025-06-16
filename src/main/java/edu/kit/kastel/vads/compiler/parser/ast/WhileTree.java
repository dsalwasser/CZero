package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Position;
import edu.kit.kastel.vads.compiler.util.Span;

public record WhileTree(ExpressionTree expression, StatementTree statement, Position start)
    implements StatementTree {
  @Override
  public Span span() {
    return new Span.SimpleSpan(start(), statement.span().end());
  }

  @Override
  public <T, R> R accept(Visitor<T, R> visitor, T data) {
    return visitor.visit(this, data);
  }
}
