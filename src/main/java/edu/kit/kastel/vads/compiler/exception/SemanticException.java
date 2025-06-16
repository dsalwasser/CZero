package edu.kit.kastel.vads.compiler.exception;

import edu.kit.kastel.vads.compiler.util.Span;

public final class SemanticException extends CompileException {

  public SemanticException(String message, Span position) {
    super(message, position);
  }
}
