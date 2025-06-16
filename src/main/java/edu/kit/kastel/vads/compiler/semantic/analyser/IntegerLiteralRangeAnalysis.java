package edu.kit.kastel.vads.compiler.semantic.analyser;

import edu.kit.kastel.vads.compiler.exception.SemanticException;
import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;

public class IntegerLiteralRangeAnalysis implements NoOpVisitor<Unit> {

  @Override
  public Unit visit(LiteralTree literalTree, Unit data) {
    literalTree
        .parseValue()
        .orElseThrow(
            () ->
                new SemanticException(
                    "invalid integer literal " + literalTree.value(), literalTree.span()));
    return NoOpVisitor.super.visit(literalTree, data);
  }
}
