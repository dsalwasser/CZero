package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.lexer.KeywordKind;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.lexer.OperatorKind;
import edu.kit.kastel.vads.compiler.lexer.SeparatorKind;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.lexer.token.Identifier;
import edu.kit.kastel.vads.compiler.lexer.token.Keyword;
import edu.kit.kastel.vads.compiler.lexer.token.Operator;
import edu.kit.kastel.vads.compiler.lexer.token.Separator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TokenSource {
  private final List<Token> tokens;

  private int index;

  public TokenSource(Lexer lexer) {
    this.tokens =
        Stream.generate(lexer::nextToken)
            .takeWhile(Optional::isPresent)
            .map(Optional::orElseThrow)
            .toList();
  }

  TokenSource(List<Token> tokens) {
    this.tokens = List.copyOf(tokens);
  }

  public Token peek() {
    if (!hasMore()) {
      throw new ParseException("reached end of file");
    }

    return this.tokens.get(this.index);
  }

  public Keyword expectKeyword(KeywordKind type) {
    Token token = peek();
    if (!(token instanceof Keyword kw) || kw.type() != type) {
      throw new ParseException("expected keyword '" + type + "' but got " + token);
    }

    this.index++;
    return kw;
  }

  public Separator expectSeparator(SeparatorKind kind) {
    Token token = peek();
    if (!(token instanceof Separator sep) || sep.kind() != kind) {
      throw new ParseException("expected separator '" + kind + "' but got " + token);
    }

    this.index++;
    return sep;
  }

  public Operator expectOperator(OperatorKind kind) {
    Token token = peek();
    if (!(token instanceof Operator op) || op.kind() != kind) {
      throw new ParseException("expected operator '" + kind + "' but got " + token);
    }

    this.index++;
    return op;
  }

  public Identifier expectIdentifier() {
    Token token = peek();
    if (!(token instanceof Identifier ident)) {
      throw new ParseException("expected identifier but got " + token);
    }

    this.index++;
    return ident;
  }

  public Token consume() {
    Token token = peek();
    this.index++;
    return token;
  }

  public boolean hasMore() {
    return this.index < this.tokens.size();
  }
}
