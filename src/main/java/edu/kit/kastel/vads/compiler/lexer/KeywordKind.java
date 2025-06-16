package edu.kit.kastel.vads.compiler.lexer;

public enum KeywordKind {
  STRUCT("struct"),

  IF("if"),
  ELSE("else"),
  WHILE("while"),
  FOR("for"),
  CONTINUE("continue"),
  BREAK("break"),
  RETURN("return"),
  ASSERT("assert"),

  TRUE("true"),
  FALSE("false"),
  NULL("NULL"),

  PRINT("print"),
  READ("read"),
  ALLOC("alloc"),
  ALLOC_ARRAY("alloc_array"),

  INT("int"),
  BOOL("bool"),
  VOID("void"),
  CHAR("char"),
  STRING("string");

  private final String keyword;

  KeywordKind(String keyword) {
    this.keyword = keyword;
  }

  public String keyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return keyword();
  }
}
