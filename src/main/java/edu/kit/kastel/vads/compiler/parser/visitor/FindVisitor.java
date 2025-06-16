package edu.kit.kastel.vads.compiler.parser.visitor;

import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BooleanLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.BreakTree;
import edu.kit.kastel.vads.compiler.parser.ast.ConditionalExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ContinueTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IfTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.NegateTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import java.util.Optional;

public interface FindVisitor<R> extends Visitor<Void, Optional<R>> {

  @Override
  default Optional<R> visit(AssignmentTree assignmentTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(BinaryOperationTree binaryOperationTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(BlockTree blockTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(BooleanLiteralTree booleanLiteralTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(BreakTree breakTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(ConditionalExpressionTree conditionalExpressionTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(ContinueTree continueTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(DeclarationTree declarationTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(ForTree forTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(FunctionTree functionTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(IdentExpressionTree identExpressionTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(IfTree ifTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(LiteralTree literalTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(LValueIdentTree lValueIdentTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(NameTree nameTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(NegateTree negateTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(ProgramTree programTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(ReturnTree returnTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(TypeTree typeTree, Void data) {
    return Optional.empty();
  }

  @Override
  default Optional<R> visit(WhileTree whileTree, Void data) {
    return Optional.empty();
  }
}
