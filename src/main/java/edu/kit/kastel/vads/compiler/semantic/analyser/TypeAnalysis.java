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
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.util.Span;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public final class TypeAnalysis
    implements Visitor<TypeAnalysis.BlockNamespace<BasicType>, Optional<BasicType>> {

  private static final Optional<BasicType> NOT_AN_EXPRESSION = Optional.empty();

  public static class BlockNamespace<T> {
    Stack<List<Name>> scopeDeclaredNames;
    Map<Name, T> allDeclaredNames;

    public BlockNamespace() {
      this.scopeDeclaredNames = new Stack<>();
      this.allDeclaredNames = new HashMap<>();
    }

    public void enterScope() {
      scopeDeclaredNames.add(new ArrayList<>());
    }

    public void exitScope() {
      List<Name> currentScopeNames = scopeDeclaredNames.pop();

      for (Name name : currentScopeNames) {
        allDeclaredNames.remove(name);
      }
    }

    public boolean contains(Name name) {
      return allDeclaredNames.containsKey(name);
    }

    public Optional<T> get(Name name) {
      return allDeclaredNames.containsKey(name)
          ? Optional.of(allDeclaredNames.get(name))
          : Optional.empty();
    }

    public void add(Name name, T t) {
      scopeDeclaredNames.peek().add(name);
      allDeclaredNames.put(name, t);
    }

    public void update(Name name, T t) {
      allDeclaredNames.put(name, t);
    }
  }

  @Override
  public void enterBlock(BlockNamespace<BasicType> data) {
    data.enterScope();
  }

  @Override
  public void exitBlock(BlockNamespace<BasicType> data) {
    data.exitScope();
  }

  @Override
  public Optional<BasicType> visit(AssignmentTree assignmentTree, BlockNamespace<BasicType> data) {
    NameTree name = ((LValueIdentTree) assignmentTree.lValue()).name();
    BasicType expectedType = data.get(name.name()).get();

    BasicType inputType = assignmentTree.expression().accept(this, data).get();

    OperatorKind operator = assignmentTree.operator().kind();
    if (operator == OperatorKind.ASSIGN) {
      if (inputType != expectedType) {
        throw new SemanticException(
            String.format(
                "Cannot assign value of type '%s' to variable of type '%s'",
                inputType, expectedType),
            assignmentTree.span());
      }
    } else {
      checkBinaryOperation(operator, inputType, assignmentTree.span());

      BasicType outputType =
          switch (operator.getOutput()) {
            case OperatorKind.Value.BOOL -> BasicType.BOOL;
            case OperatorKind.Value.INT -> BasicType.INT;
            case OperatorKind.Value.BOTH ->
                throw new IllegalArgumentException("Unexpected value: " + operator);
          };
      checkEqualType(expectedType, outputType, assignmentTree.span());
    }

    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(
      BinaryOperationTree binaryOperationTree, BlockNamespace<BasicType> data) {
    BasicType lhsType = binaryOperationTree.lhs().accept(this, data).get();
    BasicType rhsType = binaryOperationTree.rhs().accept(this, data).get();
    checkEqualType(lhsType, rhsType, binaryOperationTree.span());

    OperatorKind operator = binaryOperationTree.operatorKind();
    checkBinaryOperation(operator, lhsType, binaryOperationTree.span());

    BasicType outputType =
        switch (operator.getOutput()) {
          case OperatorKind.Value.BOOL -> BasicType.BOOL;
          case OperatorKind.Value.INT -> BasicType.INT;
          case OperatorKind.Value.BOTH ->
              throw new IllegalArgumentException("Unexpected value: " + operator);
        };
    return Optional.of(outputType);
  }

  @Override
  public Optional<BasicType> visit(BlockTree blockTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(
      BooleanLiteralTree booleanLiteralTree, BlockNamespace<BasicType> data) {
    return Optional.of(BasicType.BOOL);
  }

  @Override
  public Optional<BasicType> visit(BreakTree breakTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(
      ConditionalExpressionTree conditionalExpressionTree, BlockNamespace<BasicType> data) {
    BasicType conditionType = conditionalExpressionTree.condition().accept(this, data).get();
    if (conditionType != BasicType.BOOL) {
      throw new SemanticException(
          String.format(
              "Condition of ternary expression must be of type 'bool', but found '%s'",
              conditionType.asString()),
          conditionalExpressionTree.condition().span());
    }

    BasicType lhsType = conditionalExpressionTree.lhs().accept(this, data).get();
    BasicType rhsType = conditionalExpressionTree.rhs().accept(this, data).get();
    if (lhsType != rhsType) {
      throw new SemanticException(
          String.format(
              "The two branches of a ternary expression must have the same type, but found '%s' and '%s'",
              lhsType.asString(), rhsType.asString()),
          conditionalExpressionTree.lhs().span().merge(conditionalExpressionTree.rhs().span()));
    }

    BasicType outputType = lhsType;
    return Optional.of(outputType);
  }

  @Override
  public Optional<BasicType> visit(ContinueTree continueTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(
      DeclarationTree declarationTree, BlockNamespace<BasicType> data) {
    BasicType expectedType = (BasicType) declarationTree.type().type();
    data.add(declarationTree.name().name(), expectedType);

    if (declarationTree.initializer() != null) {
      BasicType actualType = declarationTree.initializer().accept(this, data).get();

      if (actualType != expectedType) {
        throw new SemanticException(
            String.format(
                "Cannot initialize variable of type '%s' with value of type '%s'",
                expectedType.asString(), actualType.asString()),
            declarationTree.span());
      }
    }

    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(ForTree forTree, BlockNamespace<BasicType> data) {
    BasicType conditionType = forTree.condition().accept(this, data).get();
    if (conditionType != BasicType.BOOL) {
      throw new SemanticException(
          String.format(
              "Condition of for-statement must be of type 'bool', but found '%s'",
              conditionType.asString()),
          forTree.condition().span());
    }

    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(FunctionTree functionTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(
      IdentExpressionTree identExpressionTree, BlockNamespace<BasicType> data) {
    return Optional.of(data.get(identExpressionTree.name().name()).get());
  }

  @Override
  public Optional<BasicType> visit(IfTree ifTree, BlockNamespace<BasicType> data) {
    BasicType conditionType = ifTree.expression().accept(this, data).get();
    if (conditionType != BasicType.BOOL) {
      throw new SemanticException(
          String.format(
              "Condition in if-statement must be of type 'bool', but found '%s'",
              conditionType.asString()),
          ifTree.expression().span());
    }

    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(LiteralTree literalTree, BlockNamespace<BasicType> data) {
    return Optional.of(BasicType.INT);
  }

  @Override
  public Optional<BasicType> visit(
      LValueIdentTree lValueIdentTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(NameTree nameTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(NegateTree negateTree, BlockNamespace<BasicType> data) {
    BasicType expectedType =
        switch (negateTree.kind()) {
          case NegateTree.Kind.LOGICAL -> BasicType.BOOL;
          case NegateTree.Kind.BITWISE -> BasicType.INT;
          case NegateTree.Kind.NUMERICAL -> BasicType.INT;
        };

    BasicType actualType = negateTree.expression().accept(this, data).get();
    if (actualType != expectedType) {
      throw new SemanticException(
          String.format(
              "Unary operator '%s' cannot be applied to type '%s'. Expected type '%s'",
              negateTree.kind(), actualType.asString(), expectedType.asString()),
          negateTree.expression().span());
    }

    return Optional.of(expectedType);
  }

  @Override
  public Optional<BasicType> visit(ProgramTree programTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(ReturnTree returnTree, BlockNamespace<BasicType> data) {
    BasicType returnType = returnTree.expression().accept(this, data).get();
    if (returnType != BasicType.INT) {
      throw new SemanticException(
          String.format(
              "Return type mismatch. Expected 'int', but found '%s'", returnType.asString()),
          returnTree.expression().span());
    }

    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(TypeTree typeTree, BlockNamespace<BasicType> data) {
    return NOT_AN_EXPRESSION;
  }

  @Override
  public Optional<BasicType> visit(WhileTree whileTree, BlockNamespace<BasicType> data) {
    BasicType conditionType = whileTree.expression().accept(this, data).get();
    if (conditionType != BasicType.BOOL) {
      throw new SemanticException(
          String.format(
              "Condition in while-statement must be of type 'bool', but found '%s'",
              conditionType.asString()),
          whileTree.expression().span());
    }

    return NOT_AN_EXPRESSION;
  }

  private void checkBinaryOperation(OperatorKind operator, BasicType inputType, Span span) {
    if (operator.getInput() == OperatorKind.Value.BOOL) {
      checkEqualType(BasicType.BOOL, inputType, span);
    } else if (operator.getInput() == OperatorKind.Value.INT) {
      checkEqualType(BasicType.INT, inputType, span);
    }
  }

  private static void checkEqualType(BasicType expected, BasicType actual, Span span) {
    if (expected != actual) {
      throw new SemanticException(
          "expected type '"
              + expected.asString()
              + "' but got type '"
              + actual.toString()
              + "' instead",
          span);
    }
  }
}
