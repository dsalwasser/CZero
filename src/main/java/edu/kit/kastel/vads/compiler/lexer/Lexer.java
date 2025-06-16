package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.lexer.token.ErrorToken;
import edu.kit.kastel.vads.compiler.lexer.token.Identifier;
import edu.kit.kastel.vads.compiler.lexer.token.Keyword;
import edu.kit.kastel.vads.compiler.lexer.token.NumberLiteral;
import edu.kit.kastel.vads.compiler.lexer.token.Operator;
import edu.kit.kastel.vads.compiler.lexer.token.Separator;
import edu.kit.kastel.vads.compiler.util.Position;
import edu.kit.kastel.vads.compiler.util.Span;
import java.util.Optional;

public final class Lexer {
  private final String source;

  private int pos;
  private int lineStart;
  private int line;

  public Lexer(String source) {
    this.source = source;
  }

  public Optional<Token> nextToken() {
    Optional<ErrorToken> error = skipWhitespace();
    if (error.isPresent()) {
      return Optional.of(error.get());
    }

    if (this.pos >= this.source.length()) {
      return Optional.empty();
    }

    Token t =
        switch (peek()) {
          case '(' -> separator(SeparatorKind.PAREN_OPEN);
          case ')' -> separator(SeparatorKind.PAREN_CLOSE);
          case '{' -> separator(SeparatorKind.BRACE_OPEN);
          case '}' -> separator(SeparatorKind.BRACE_CLOSE);
          case ':' -> separator(SeparatorKind.COLON);
          case ';' -> separator(SeparatorKind.SEMICOLON);

          case '?' -> singleOperator(OperatorKind.CONDITIONAL);
          case '~' -> singleOperator(OperatorKind.BITWISE_NOT);

          case '+' -> matchSingleOrAssignOperator(OperatorKind.PLUS, OperatorKind.ASSIGN_PLUS);
          case '-' -> matchSingleOrAssignOperator(OperatorKind.MINUS, OperatorKind.ASSIGN_MINUS);
          case '*' -> matchSingleOrAssignOperator(OperatorKind.MUL, OperatorKind.ASSIGN_MUL);
          case '/' -> matchSingleOrAssignOperator(OperatorKind.DIV, OperatorKind.ASSIGN_DIV);
          case '%' -> matchSingleOrAssignOperator(OperatorKind.MOD, OperatorKind.ASSIGN_MOD);
          case '^' ->
              matchSingleOrAssignOperator(OperatorKind.BITWISE_XOR, OperatorKind.ASSIGN_XOR);

          // '=' might be an assign '=' or an equality '=='
          case '=' ->
              matchSingleOrCompoundOperator(OperatorKind.ASSIGN, '=', OperatorKind.EQUALITY);
          // '!' might be a not '!' or an inequality '!='
          case '!' -> matchSingleOrCompoundOperator(OperatorKind.NOT, '=', OperatorKind.INEQUALITY);

          // '&' might be a bitwise-and '&', a logical-and '&&', or an and-assign '&='
          case '&' ->
              matchSingleOrCompoundOperator(
                  OperatorKind.BITWISE_AND, '&', OperatorKind.AND, '=', OperatorKind.ASSIGN_AND);

          // '|' might be a bitwise-or '|', a logical-or '||', or an or-assign '|='
          case '|' ->
              matchSingleOrCompoundOperator(
                  OperatorKind.BITWISE_OR, '|', OperatorKind.OR, '=', OperatorKind.ASSIGN_OR);

          // '<' might be a less-than '<', a less-then-equal '<=', a left-shift '<<', or a
          // assign-left-shift '<<='
          case '<' ->
              matchComparisonOrShiftOperator(
                  OperatorKind.LESS_THAN,
                  OperatorKind.LESS_THAN_EQUAL,
                  OperatorKind.LEFT_SHIFT,
                  OperatorKind.ASSIGN_LEFT_SHIFT);

          // '>' might be a greater-than '>', a greater-then-equal '>=', a right-shift
          // '>>', or a assign->>-shift '>>='
          case '>' ->
              matchComparisonOrShiftOperator(
                  OperatorKind.GREATER_THAN,
                  OperatorKind.GREATER_THAN_EQUAL,
                  OperatorKind.RIGHT_SHIFT,
                  OperatorKind.ASSIGN_RIGHT_SHIFT);

          default -> {
            if (isIdentifierChar(peek())) {
              if (isNumeric(peek())) {
                yield lexNumber();
              }
              yield lexIdentifierOrKeyword();
            }
            yield new ErrorToken(String.valueOf(peek()), buildSpan(1));
          }
        };

    return Optional.of(t);
  }

  private Optional<ErrorToken> skipWhitespace() {
    enum CommentType {
      SINGLE_LINE,
      MULTI_LINE
    }

    CommentType currentCommentType = null;
    int multiLineCommentDepth = 0;
    int commentStart = -1;
    while (hasMore()) {
      switch (peek()) {
        case ' ', '\t' -> this.pos++;
        case '\n', '\r' -> {
          this.pos++;
          this.lineStart = this.pos;
          this.line++;
          if (currentCommentType == CommentType.SINGLE_LINE) {
            currentCommentType = null;
          }
        }
        case '/' -> {
          if (currentCommentType == CommentType.SINGLE_LINE) {
            this.pos++;
            continue;
          }

          if (hasMore(1)) {
            if (peek(1) == '/' && currentCommentType == null) {
              currentCommentType = CommentType.SINGLE_LINE;
            } else if (peek(1) == '*') {
              currentCommentType = CommentType.MULTI_LINE;
              multiLineCommentDepth++;
            } else if (currentCommentType == CommentType.MULTI_LINE) {
              this.pos++;
              continue;
            } else {
              return Optional.empty();
            }

            commentStart = this.pos;
            this.pos += 2;
            continue;
          }

          // are we in a multi line comment of any depth?
          if (multiLineCommentDepth > 0) {
            this.pos++;
            continue;
          }

          return Optional.empty();
        }
        default -> {
          if (currentCommentType == CommentType.MULTI_LINE) {
            if (peek() == '*' && hasMore(1) && peek(1) == '/') {
              this.pos += 2;
              multiLineCommentDepth--;
              currentCommentType = multiLineCommentDepth == 0 ? null : CommentType.MULTI_LINE;
            } else {
              this.pos++;
            }

            continue;
          } else if (currentCommentType == CommentType.SINGLE_LINE) {
            this.pos++;
            continue;
          }

          return Optional.empty();
        }
      }
    }

    if (!hasMore(0) && currentCommentType == CommentType.MULTI_LINE) {
      ErrorToken errorToken = new ErrorToken(this.source.substring(commentStart), buildSpan(0));
      return Optional.of(errorToken);
    }

    return Optional.empty();
  }

  private Separator separator(SeparatorKind parenOpen) {
    return new Separator(parenOpen, buildSpan(1));
  }

  private Token lexIdentifierOrKeyword() {
    int off = 1;
    while (hasMore(off) && isIdentifierChar(peek(off))) {
      off++;
    }

    String id = this.source.substring(this.pos, this.pos + off);
    // This is a naive solution. Using a better data structure (hashmap, trie)
    // likely performs better.
    for (KeywordKind value : KeywordKind.values()) {
      if (value.keyword().equals(id)) {
        return new Keyword(value, buildSpan(off));
      }
    }

    return new Identifier(id, buildSpan(off));
  }

  private Token lexNumber() {
    if (isHexPrefix()) {
      int off = 2;
      while (hasMore(off) && isHex(peek(off))) {
        off++;
      }

      if (off == 2) {
        // 0x without any further hex digits
        return new ErrorToken(this.source.substring(this.pos, this.pos + off), buildSpan(2));
      }

      return new NumberLiteral(this.source.substring(this.pos, this.pos + off), 16, buildSpan(off));
    }

    int off = 1;
    while (hasMore(off) && isNumeric(peek(off))) {
      off++;
    }

    if (peek() == '0' && off > 1) {
      // leading zero is not allowed
      return new ErrorToken(this.source.substring(this.pos, this.pos + off), buildSpan(off));
    }

    return new NumberLiteral(this.source.substring(this.pos, this.pos + off), 10, buildSpan(off));
  }

  private boolean isHexPrefix() {
    return peek() == '0' && hasMore(1) && (peek(1) == 'x' || peek(1) == 'X');
  }

  private boolean isIdentifierChar(char c) {
    return c == '_' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
  }

  private boolean isNumeric(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isHex(char c) {
    return isNumeric(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private Token matchSingleOrAssignOperator(OperatorKind single, OperatorKind assign) {
    return matchSingleOrCompoundOperator(single, '=', assign);
  }

  private Token matchSingleOrCompoundOperator(
      OperatorKind simple, char expectedCharForCompund, OperatorKind compound) {
    if (hasMore(1) && peek(1) == expectedCharForCompund) {
      return new Operator(compound, buildSpan(2));
    }

    return new Operator(simple, buildSpan(1));
  }

  private Token matchSingleOrCompoundOperator(
      OperatorKind simple,
      char expectedCharForFirstCompund,
      OperatorKind firstCompound,
      char expectedCharForSecondCompund,
      OperatorKind secondCompound) {
    if (hasMore(1)) {
      char nextChar = peek(1);

      if (nextChar == expectedCharForFirstCompund) {
        return new Operator(firstCompound, buildSpan(2));

      } else if (nextChar == expectedCharForSecondCompund) {
        return new Operator(secondCompound, buildSpan(2));
      }
    }

    return new Operator(simple, buildSpan(1));
  }

  private Token matchComparisonOrShiftOperator(
      OperatorKind strictComparison,
      OperatorKind comparison,
      OperatorKind shift,
      OperatorKind shiftAssign) {
    final char comparisonChar = peek();

    if (hasMore(1)) {
      final char nextChar = peek(1);

      if (nextChar == '=') {
        return new Operator(comparison, buildSpan(2));
      } else if (nextChar == comparisonChar) {
        if (hasMore(1) && peek(2) == '=') {
          return new Operator(shiftAssign, buildSpan(3));
        } else {
          return new Operator(shift, buildSpan(2));
        }
      }
    }

    return new Operator(strictComparison, buildSpan(1));
  }

  private Token singleOperator(OperatorKind single) {
    return new Operator(single, buildSpan(1));
  }

  private Span buildSpan(int proceed) {
    int start = this.pos;
    this.pos += proceed;
    Position.SimplePosition s = new Position.SimplePosition(this.line, start - this.lineStart);
    Position.SimplePosition e =
        new Position.SimplePosition(this.line, start - this.lineStart + proceed);
    return new Span.SimpleSpan(s, e);
  }

  private char peek() {
    return this.source.charAt(this.pos);
  }

  private boolean hasMore() {
    return hasMore(0);
  }

  private boolean hasMore(int offset) {
    return this.pos + offset < this.source.length();
  }

  private char peek(int offset) {
    return this.source.charAt(this.pos + offset);
  }
}
