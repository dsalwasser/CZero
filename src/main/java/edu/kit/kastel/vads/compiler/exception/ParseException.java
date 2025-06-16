package edu.kit.kastel.vads.compiler.exception;

import edu.kit.kastel.vads.compiler.util.Span;

public final class ParseException extends CompileException {

  public ParseException(String message, Span position) {
    super(message, position);
  }
}
