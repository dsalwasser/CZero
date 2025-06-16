package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.exception.ParseException;
import edu.kit.kastel.vads.compiler.lexer.KeywordKind;
import edu.kit.kastel.vads.compiler.lexer.OperatorKind;
import edu.kit.kastel.vads.compiler.lexer.SeparatorKind;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.lexer.token.Identifier;
import edu.kit.kastel.vads.compiler.lexer.token.Keyword;
import edu.kit.kastel.vads.compiler.lexer.token.NumberLiteral;
import edu.kit.kastel.vads.compiler.lexer.token.Operator;
import edu.kit.kastel.vads.compiler.lexer.token.Separator;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BooleanLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.BreakTree;
import edu.kit.kastel.vads.compiler.parser.ast.ConditionalExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ContinueTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IfTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueTree;
import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.NegateTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.util.Span;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
  private final TokenSource tokenSource;

  public Parser(TokenSource tokenSource) {
    this.tokenSource = tokenSource;
  }

  public ProgramTree parseProgram() {
    FunctionTree functionTree = parseFunction();
    ProgramTree programTree = new ProgramTree(List.of(functionTree));

    if (this.tokenSource.hasMore()) {
      Token nexToken = this.tokenSource.peek();
      throw new ParseException("expected end of input but got " + nexToken, nexToken.span());
    }

    return programTree;
  }

  private FunctionTree parseFunction() {
    Keyword returnType = this.tokenSource.expectKeyword(KeywordKind.INT);

    Identifier identifier = this.tokenSource.expectIdentifier();
    if (!identifier.value().equals("main")) {
      throw new ParseException("expected main function but got " + identifier, identifier.span());
    }

    this.tokenSource.expectSeparator(SeparatorKind.PAREN_OPEN);
    this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);

    BlockTree body = parseBlock();
    return new FunctionTree(new TypeTree(BasicType.INT, returnType.span()), name(identifier), body);
  }

  private BlockTree parseBlock() {
    Separator bodyOpen = this.tokenSource.expectSeparator(SeparatorKind.BRACE_OPEN);

    List<StatementTree> statements = new ArrayList<>();
    while (!(this.tokenSource.peek() instanceof Separator sep
        && sep.kind() == SeparatorKind.BRACE_CLOSE)) {
      statements.add(parseStatement());
    }

    Separator bodyClose = this.tokenSource.expectSeparator(SeparatorKind.BRACE_CLOSE);
    return new BlockTree(statements, bodyOpen.span().merge(bodyClose.span()));
  }

  private StatementTree parseStatement() {
    final Token nextToken = this.tokenSource.peek();
    if (nextToken.isSeparator(SeparatorKind.BRACE_OPEN)) {
      return parseBlock();
    }

    StatementTree statement;
    if (nextToken.isKeyword(KeywordKind.IF)) {
      statement = parseIfStatement();
    } else if (nextToken.isKeyword(KeywordKind.WHILE)) {
      statement = parseWhileStatement();
    } else if (nextToken.isKeyword(KeywordKind.FOR)) {
      statement = parseForStatement();
    } else if (nextToken.isKeyword(KeywordKind.CONTINUE)) {
      statement = parseContinueStatement();
    } else if (nextToken.isKeyword(KeywordKind.BREAK)) {
      statement = parseBreakStatement();
    } else if (nextToken.isKeyword(KeywordKind.RETURN)) {
      statement = parseReturn();
    } else {
      statement = parseSimpleStatement();
      this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);
    }

    return statement;
  }

  private StatementTree parseSimpleStatement() {
    if (this.tokenSource.peek().isType()) {
      return parseDeclaration();
    } else {
      return parseSimple();
    }
  }

  private StatementTree parseDeclaration() {
    TypeTree type = parseType();
    Identifier ident = this.tokenSource.expectIdentifier();

    ExpressionTree expr = null;
    if (this.tokenSource.peek().isOperator(OperatorKind.ASSIGN)) {
      this.tokenSource.expectOperator(OperatorKind.ASSIGN);
      expr = parseExpression();
    }

    return new DeclarationTree(type, name(ident), expr);
  }

  private StatementTree parseIfStatement() {
    Keyword ifKeyword = this.tokenSource.expectKeyword(KeywordKind.IF);

    this.tokenSource.expectSeparator(SeparatorKind.PAREN_OPEN);
    ExpressionTree expression = parseExpression();
    this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);

    StatementTree statement = parseStatement();
    Optional<StatementTree> elseStatement = Optional.empty();
    if (this.tokenSource.peek().isKeyword(KeywordKind.ELSE)) {
      this.tokenSource.expectKeyword(KeywordKind.ELSE);
      elseStatement = Optional.of(parseStatement());
    }

    return new IfTree(expression, statement, elseStatement, ifKeyword.span().start());
  }

  private StatementTree parseWhileStatement() {
    Keyword whileKeyword = this.tokenSource.expectKeyword(KeywordKind.WHILE);

    this.tokenSource.expectSeparator(SeparatorKind.PAREN_OPEN);
    ExpressionTree expression = parseExpression();
    this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);

    StatementTree statement = parseStatement();
    return new WhileTree(expression, statement, whileKeyword.span().start());
  }

  private StatementTree parseForStatement() {
    Keyword forKeyword = this.tokenSource.expectKeyword(KeywordKind.FOR);
    this.tokenSource.expectSeparator(SeparatorKind.PAREN_OPEN);

    Optional<StatementTree> initStatement = Optional.empty();
    if (!this.tokenSource.peek().isSeparator(SeparatorKind.SEMICOLON)) {
      initStatement = Optional.of(parseSimpleStatement());
    }
    this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);

    ExpressionTree conditionExpression = parseExpression();
    this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);

    Optional<StatementTree> stepStatement = Optional.empty();
    if (!this.tokenSource.peek().isSeparator(SeparatorKind.PAREN_CLOSE)) {
      stepStatement = Optional.of(parseSimpleStatement());
    }
    this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);

    StatementTree statement = parseStatement();
    return new ForTree(
        initStatement, conditionExpression, stepStatement, statement, forKeyword.span().start());
  }

  private StatementTree parseContinueStatement() {
    Keyword continueKeyword = this.tokenSource.expectKeyword(KeywordKind.CONTINUE);
    this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);
    return new ContinueTree(continueKeyword.span());
  }

  private StatementTree parseBreakStatement() {
    Keyword breakKeyword = this.tokenSource.expectKeyword(KeywordKind.BREAK);
    this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);
    return new BreakTree(breakKeyword.span());
  }

  private TypeTree parseType() {
    Token token = this.tokenSource.consume();
    if (token.isKeyword(KeywordKind.BOOL)) {
      return new TypeTree(BasicType.BOOL, token.span());
    } else if (token.isKeyword(KeywordKind.INT)) {
      return new TypeTree(BasicType.INT, token.span());
    }

    throw new ParseException("expected a type but got " + token, token.span());
  }

  private StatementTree parseSimple() {
    LValueTree lValue = parseLValue();
    Operator assignmentOperator = parseAssignmentOperator();
    ExpressionTree expression = parseExpression();
    return new AssignmentTree(lValue, assignmentOperator, expression);
  }

  private Operator parseAssignmentOperator() {
    if (this.tokenSource.peek() instanceof Operator op) {
      return switch (op.kind()) {
        case ASSIGN,
            ASSIGN_PLUS,
            ASSIGN_MINUS,
            ASSIGN_MUL,
            ASSIGN_DIV,
            ASSIGN_MOD,
            ASSIGN_AND,
            ASSIGN_XOR,
            ASSIGN_OR,
            ASSIGN_LEFT_SHIFT,
            ASSIGN_RIGHT_SHIFT -> {
          this.tokenSource.consume();
          yield op;
        }
        default -> throw new ParseException("expected assignment but got " + op.kind(), op.span());
      };
    }

    Token nextToken = this.tokenSource.peek();
    throw new ParseException("expected assignment but got " + nextToken, nextToken.span());
  }

  private LValueTree parseLValue() {
    if (this.tokenSource.peek().isSeparator(SeparatorKind.PAREN_OPEN)) {
      this.tokenSource.expectSeparator(SeparatorKind.PAREN_OPEN);
      LValueTree inner = parseLValue();
      this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);
      return inner;
    }

    Identifier identifier = this.tokenSource.expectIdentifier();
    return new LValueIdentTree(name(identifier));
  }

  private StatementTree parseReturn() {
    Keyword ret = this.tokenSource.expectKeyword(KeywordKind.RETURN);
    ExpressionTree expression = parseExpression();

    this.tokenSource.expectSeparator(SeparatorKind.SEMICOLON);
    return new ReturnTree(expression, ret.span().start());
  }

  private ExpressionTree parseExpression() {
    ExpressionTree lhs = parseLogical();

    while (true) {
      Token token = this.tokenSource.peek();

      if (token instanceof Operator(var type, _) && type == OperatorKind.CONDITIONAL) {
        this.tokenSource.consume();

        ExpressionTree main = parseExpression();
        this.tokenSource.expectSeparator(SeparatorKind.COLON);

        ExpressionTree other = parseExpression();
        return new ConditionalExpressionTree(lhs, main, other);
      }

      return lhs;
    }
  }

  private ExpressionTree parseLogical() {
    ExpressionTree lhs = parseBitwiseTerm();

    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.AND || type == OperatorKind.OR)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseBitwiseTerm(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseBitwiseTerm() {
    ExpressionTree lhs = parseEquality();

    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.BITWISE_AND
              || type == OperatorKind.BITWISE_OR
              || type == OperatorKind.BITWISE_XOR)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseEquality(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseEquality() {
    ExpressionTree lhs = parseRelational();

    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.EQUALITY || type == OperatorKind.INEQUALITY)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseRelational(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseRelational() {
    ExpressionTree lhs = parseShift();

    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.LESS_THAN
              || type == OperatorKind.LESS_THAN_EQUAL
              || type == OperatorKind.GREATER_THAN
              || type == OperatorKind.GREATER_THAN_EQUAL)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseShift(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseShift() {
    ExpressionTree lhs = parseAdditive();

    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.LEFT_SHIFT || type == OperatorKind.RIGHT_SHIFT)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseAdditive(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseAdditive() {
    ExpressionTree lhs = parseMultiplicative();
    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.PLUS || type == OperatorKind.MINUS)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseMultiplicative(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseMultiplicative() {
    ExpressionTree lhs = parseFactor();
    while (true) {
      if (this.tokenSource.peek() instanceof Operator(var type, _)
          && (type == OperatorKind.MUL || type == OperatorKind.DIV || type == OperatorKind.MOD)) {
        this.tokenSource.consume();
        lhs = new BinaryOperationTree(lhs, parseFactor(), type);
      } else {
        return lhs;
      }
    }
  }

  private ExpressionTree parseFactor() {
    return switch (this.tokenSource.peek()) {
      case Separator(var type, _) when type == SeparatorKind.PAREN_OPEN -> {
        this.tokenSource.consume();
        ExpressionTree expression = parseExpression();
        this.tokenSource.expectSeparator(SeparatorKind.PAREN_CLOSE);
        yield expression;
      }
      case Operator(var type, _) when type == OperatorKind.NOT
          | type == OperatorKind.BITWISE_NOT
          | type == OperatorKind.MINUS -> {
        NegateTree.Kind kind =
            switch (type) {
              case OperatorKind.NOT -> NegateTree.Kind.LOGICAL;
              case OperatorKind.BITWISE_NOT -> NegateTree.Kind.BITWISE;
              case OperatorKind.MINUS -> NegateTree.Kind.NUMERICAL;
              default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };

        Span span = this.tokenSource.consume().span();
        yield new NegateTree(kind, parseFactor(), span);
      }
      case Identifier ident -> {
        this.tokenSource.consume();
        yield new IdentExpressionTree(name(ident));
      }
      case NumberLiteral(String value, int base, Span span) -> {
        this.tokenSource.consume();
        yield new LiteralTree(value, base, span);
      }
      case Keyword(KeywordKind kind, Span span) when kind == KeywordKind.TRUE
          || kind == KeywordKind.FALSE -> {
        this.tokenSource.consume();
        boolean value = kind == KeywordKind.TRUE ? true : false;
        yield new BooleanLiteralTree(value, span);
      }
      case Token t -> throw new ParseException("invalid factor " + t, t.span());
    };
  }

  private static NameTree name(Identifier ident) {
    return new NameTree(Name.forIdentifier(ident), ident.span());
  }
}
