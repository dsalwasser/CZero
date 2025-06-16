package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Position;
import edu.kit.kastel.vads.compiler.util.Span;
import java.util.Optional;

public final record IfTree(
    ExpressionTree expression,
    StatementTree mainStatement,
    Optional<StatementTree> elseStatement,
    Position start)
    implements StatementTree {
  @Override
  public Span span() {
    return new Span.SimpleSpan(start(), elseStatement.orElse(mainStatement).span().end());
  }

  @Override
  public <T, R> R accept(Visitor<T, R> visitor, T data) {
    return visitor.visit(this, data);
  }
}
