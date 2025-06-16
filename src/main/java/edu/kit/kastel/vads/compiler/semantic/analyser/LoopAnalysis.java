package edu.kit.kastel.vads.compiler.semantic.analyser;

import edu.kit.kastel.vads.compiler.exception.SemanticException;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BreakTree;
import edu.kit.kastel.vads.compiler.parser.ast.ContinueTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IfTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;

public final class LoopAnalysis implements NoOpVisitor<LoopAnalysis.LoopStatus> {

  public static class LoopStatus {
    public int numLoops = 0;
  }
  ;

  @Override
  public Unit visit(BlockTree blockTree, LoopStatus status) {
    for (StatementTree statement : blockTree.statements()) {
      statement.accept(this, status);
    }

    return NoOpVisitor.super.visit(blockTree, status);
  }

  @Override
  public Unit visit(BreakTree breakTree, LoopStatus data) {
    if (data.numLoops == 0) {
      throw new SemanticException("break-statement must be inside a loop", breakTree.span());
    }

    return NoOpVisitor.super.visit(breakTree, data);
  }

  @Override
  public Unit visit(ContinueTree continueTree, LoopStatus data) {
    if (data.numLoops == 0) {
      throw new SemanticException("continue-statement must be inside a loop", continueTree.span());
    }

    return NoOpVisitor.super.visit(continueTree, data);
  }

  @Override
  public Unit visit(ForTree forTree, LoopStatus status) {
    if (forTree.step().isPresent() && forTree.step().get() instanceof DeclarationTree declaration) {
      throw new SemanticException(
          "Step expression in a for-loop cannot be a variable declaration", declaration.span());
    }

    status.numLoops += 1;
    forTree.statement().accept(this, status);

    status.numLoops -= 1;
    return NoOpVisitor.super.visit(forTree, status);
  }

  @Override
  public Unit visit(FunctionTree functionTree, LoopStatus status) {
    functionTree.body().accept(this, status);
    return NoOpVisitor.super.visit(functionTree, status);
  }

  @Override
  public Unit visit(IfTree ifTree, LoopStatus status) {
    ifTree.mainStatement().accept(this, status);
    ifTree.elseStatement().ifPresent(statement -> statement.accept(this, status));
    return NoOpVisitor.super.visit(ifTree, status);
  }

  @Override
  public Unit visit(ProgramTree programTree, LoopStatus status) {
    for (FunctionTree functionTree : programTree.topLevelTrees()) {
      functionTree.accept(this, status);
    }

    return NoOpVisitor.super.visit(programTree, status);
  }

  @Override
  public Unit visit(WhileTree whileTree, LoopStatus status) {
    status.numLoops += 1;
    whileTree.statement().accept(this, status);

    status.numLoops -= 1;
    return NoOpVisitor.super.visit(whileTree, status);
  }
}
