package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Span;

public record NameTree(Name name, Span span) implements Tree {
  @Override
  public <T, R> R accept(Visitor<T, R> visitor, T data) {
    return visitor.visit(this, data);
  }
}
