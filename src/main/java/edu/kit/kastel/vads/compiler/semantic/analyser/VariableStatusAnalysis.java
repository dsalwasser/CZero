package edu.kit.kastel.vads.compiler.semantic.analyser;

import edu.kit.kastel.vads.compiler.exception.SemanticException;
import edu.kit.kastel.vads.compiler.lexer.OperatorKind;
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
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/// Checks that variables are
/// - declared before assignment
/// - not declared twice
/// - not initialized twice
/// - assigned before referenced
public class VariableStatusAnalysis
    implements Visitor<VariableStatusAnalysis.VariableStatus, Set<Name>> {
  public static class VariableStatus {
    private Stack<List<Name>> scopeDeclaredVariables;
    private Set<Name> declaredVariables;
    private Set<Name> definedVariables;

    public VariableStatus() {
      this.scopeDeclaredVariables = new Stack<>();
      this.declaredVariables = new HashSet<>();
      this.definedVariables = new HashSet<>();
    }

    public void enterScope() {
      scopeDeclaredVariables.add(new ArrayList<>());
    }

    public void exitScope() {
      List<Name> currentScopeDeclaredVariables = scopeDeclaredVariables.pop();

      for (Name name : currentScopeDeclaredVariables) {
        declaredVariables.remove(name);
        definedVariables.remove(name);
      }
    }

    public void addDeclared(Name variable) {
      scopeDeclaredVariables.peek().add(variable);
      declaredVariables.add(variable);
    }

    public void addDefined(Name variable) {
      definedVariables.add(variable);
    }

    public void removeDefined(Name variable) {
      definedVariables.remove(variable);
    }

    public boolean isDeclared(Name variable) {
      return declaredVariables.contains(variable);
    }

    public boolean isDefined(Name variable) {
      return definedVariables.contains(variable);
    }
  }

  @Override
  public Set<Name> visit(AssignmentTree assignmentTree, VariableStatus status) {
    assignmentTree.expression().accept(this, status);
    return switch (assignmentTree.lValue()) {
      case LValueIdentTree(var variable) -> {
        if (!status.isDeclared(variable.name())) {
          throw new SemanticException(
              String.format(
                  "Variable '%s' must be declared before it assigned", variable.name().asString()),
              variable.span());
        }

        if (status.isDefined(variable.name())) {
          yield Set.of();
        }

        if (assignmentTree.operator().kind() != OperatorKind.ASSIGN) {
          throw new SemanticException(
              String.format(
                  "Variable '%s' must be initialized before it is used",
                  variable.name().asString()),
              variable.span());
        }

        status.addDefined(variable.name());
        yield Set.of(variable.name());
      }
    };
  }

  @Override
  public Set<Name> visit(BinaryOperationTree binaryOperationTree, VariableStatus status) {
    binaryOperationTree.lhs().accept(this, status);
    binaryOperationTree.rhs().accept(this, status);
    return Set.of();
  }

  @Override
  public Set<Name> visit(BlockTree blockTree, VariableStatus status) {
    status.enterScope();

    Set<Name> definedVariables = new HashSet<>();
    for (StatementTree statementTree : blockTree.statements()) {
      Set<Name> localDefinedVariables = statementTree.accept(this, status);
      definedVariables.addAll(localDefinedVariables);
    }

    status.exitScope();
    return definedVariables;
  }

  @Override
  public Set<Name> visit(BooleanLiteralTree booleanLiteralTree, VariableStatus status) {
    return Set.of();
  }

  @Override
  public Set<Name> visit(BreakTree breakTree, VariableStatus status) {
    Set<Name> defined = new HashSet<>();
    for (Name name : status.declaredVariables) {
      if (!status.isDefined(name)) {
        defined.add(name);
        status.addDefined(name);
      }
    }

    return defined;
  }

  @Override
  public Set<Name> visit(
      ConditionalExpressionTree conditionalExpressionTree, VariableStatus status) {
    conditionalExpressionTree.condition().accept(this, status);
    conditionalExpressionTree.lhs().accept(this, status);
    conditionalExpressionTree.rhs().accept(this, status);
    return Set.of();
  }

  @Override
  public Set<Name> visit(ContinueTree continueTree, VariableStatus status) {
    Set<Name> defined = new HashSet<>();
    for (Name name : status.declaredVariables) {
      if (!status.isDefined(name)) {
        defined.add(name);
        status.addDefined(name);
      }
    }

    return defined;
  }

  @Override
  public Set<Name> visit(DeclarationTree declarationTree, VariableStatus status) {
    NameTree variable = declarationTree.name();
    if (status.isDeclared(variable.name())) {
      throw new SemanticException(
          String.format(
              "Variable '%s' is already declared in an enclosing scope",
              variable.name().asString()),
          variable.span());
    }

    status.addDeclared(variable.name());
    if (declarationTree.initializer() == null) {
      return Set.of();
    }

    declarationTree.initializer().accept(this, status);
    status.addDefined(variable.name());
    return Set.of(variable.name());
  }

  @Override
  public Set<Name> visit(ForTree forTree, VariableStatus status) {
    forTree.init().ifPresent(statement -> statement.accept(this, status));
    forTree.condition().accept(this, status);
    forTree.step().ifPresent(statement -> statement.accept(this, status));
    status.enterScope();

    Set<Name> definedVariables = forTree.statement().accept(this, status);
    definedVariables.forEach(status::removeDefined);

    status.exitScope();
    return Set.of();
  }

  @Override
  public Set<Name> visit(FunctionTree functionTree, VariableStatus status) {
    functionTree.body().accept(this, status);
    return Set.of();
  }

  @Override
  public Set<Name> visit(
      IdentExpressionTree identExpressionTree, VariableStatusAnalysis.VariableStatus status) {
    Name name = identExpressionTree.name().name();
    if (!status.isDefined(name)) {
      throw new SemanticException(
          String.format("Variable '%s' must be defined before it is used", name.asString()),
          identExpressionTree.name().span());
    }

    return Set.of();
  }

  @Override
  public Set<Name> visit(IfTree ifTree, VariableStatus status) {
    ifTree.expression().accept(this, status);

    status.enterScope();
    Set<Name> definedVariablesMain = ifTree.mainStatement().accept(this, status);
    definedVariablesMain.stream().forEach(status::removeDefined);
    status.exitScope();

    status.enterScope();
    Set<Name> definedVariablesElse =
        ifTree.elseStatement().map(statement -> statement.accept(this, status)).orElse(Set.of());
    definedVariablesElse.stream().forEach(status::removeDefined);
    status.exitScope();

    Set<Name> definedVariables = new HashSet<>(definedVariablesMain);
    definedVariables.retainAll(definedVariablesElse);
    definedVariables.stream().forEach(status::addDefined);

    return definedVariables;
  }

  @Override
  public Set<Name> visit(LiteralTree literalTree, VariableStatus status) {
    return Set.of();
  }

  @Override
  public Set<Name> visit(LValueIdentTree lValueIdentTree, VariableStatus status) {
    return Set.of();
  }

  @Override
  public Set<Name> visit(NameTree nameTree, VariableStatus status) {
    return Set.of();
  }

  @Override
  public Set<Name> visit(NegateTree negateTree, VariableStatus status) {
    negateTree.expression().accept(this, status);
    return Set.of();
  }

  @Override
  public Set<Name> visit(ProgramTree programTree, VariableStatus status) {
    for (FunctionTree functionTree : programTree.topLevelTrees()) {
      functionTree.accept(this, status);
    }

    return Set.of();
  }

  @Override
  public Set<Name> visit(ReturnTree returnTree, VariableStatus status) {
    returnTree.expression().accept(this, status);

    Set<Name> defined = new HashSet<>();
    for (Name name : status.declaredVariables) {
      if (!status.isDefined(name)) {
        defined.add(name);
        status.addDefined(name);
      }
    }

    return defined;
  }

  @Override
  public Set<Name> visit(TypeTree typeTree, VariableStatus status) {
    return Set.of();
  }

  @Override
  public Set<Name> visit(WhileTree whileTree, VariableStatus status) {
    whileTree.expression().accept(this, status);
    status.enterScope();

    Set<Name> definedVariables = whileTree.statement().accept(this, status);
    definedVariables.forEach(status::removeDefined);

    status.exitScope();
    return Set.of();
  }
}
