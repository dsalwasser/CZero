package edu.kit.kastel.vads.compiler.semantic.analyser;

import edu.kit.kastel.vads.compiler.exception.SemanticException;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IfTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.visitor.FindVisitor;
import java.util.Optional;

public class ReturnAnalysis implements FindVisitor<Boolean> {

  @Override
  public Optional<Boolean> visit(BlockTree blockTree, Void data) {
    boolean returns = false;

    for (StatementTree statement : blockTree.statements()) {
      returns |= statement.accept(this, data).orElse(false);
    }

    return Optional.of(returns);
  }

  @Override
  public Optional<Boolean> visit(FunctionTree functionTree, Void data) {
    return functionTree.body().accept(this, data);
  }

  @Override
  public Optional<Boolean> visit(IfTree ifTree, Void data) {
    boolean mainReturns = ifTree.mainStatement().accept(this, data).orElse(false);
    boolean elseReturns =
        ifTree
            .elseStatement()
            .map(elseStatement -> elseStatement.accept(this, data).orElse(false))
            .orElse(true);

    boolean returns = mainReturns && elseReturns;
    return Optional.of(returns);
  }

  @Override
  public Optional<Boolean> visit(ProgramTree programTree, Void data) {
    for (FunctionTree functionTree : programTree.topLevelTrees()) {
      boolean returns = functionTree.accept(this, data).orElse(false);

      if (!returns) {
        throw new SemanticException(
            String.format(
                "Function '%s' must return a value on all code paths",
                functionTree.name().name().asString()),
            functionTree.name().span());
      }
    }

    return FindVisitor.super.visit(programTree, data);
  }

  @Override
  public Optional<Boolean> visit(ReturnTree returnTree, Void data) {
    return Optional.of(true);
  }
}
