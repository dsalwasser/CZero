package edu.kit.kastel.vads.compiler.exception;

import edu.kit.kastel.vads.compiler.util.Position;
import edu.kit.kastel.vads.compiler.util.Span;

public sealed class CompileException extends RuntimeException
    permits ParseException, SemanticException {
  private Span position;

  public CompileException(String message, Span position) {
    super(message);
    this.position = position;
  }

  public Span getPosition() {
    return position;
  }

  public void prettyPrint(String source) {
    printStackTrace();

    if (position == null) {
      System.err.println("Failed to parse the source: " + getMessage());
      return;
    }

    String[] lines = source.split("\\n|\\r");
    Position start = position.start();
    Position end = position.end();

    int line = start.line();
    int startColumn = start.column();
    int endColumn = end.column();

    if (line != end.line()
        || line >= lines.length
        || startColumn > endColumn
        || endColumn >= lines[line].length()) {
      System.err.println("Failed to parse the source: " + getMessage());
      return;
    }

    String errorLineStr = lines[line];
    int lineNumberWidth = String.valueOf(line + 1).length();

    int pointerLength = Math.max(1, Math.min(endColumn, errorLineStr.length()) - startColumn);
    String pointerLine = " ".repeat(startColumn) + "^".repeat(pointerLength);

    System.err.println("error: " + getMessage());
    System.err.printf(
        " %s--> source:%d:%d-%d%n",
        " ".repeat(lineNumberWidth - 1), line + 1, startColumn + 1, endColumn + 1);
    System.err.printf(" %s |%n", " ".repeat(lineNumberWidth));
    System.err.printf(" %" + lineNumberWidth + "d | %s%n", line + 1, errorLineStr);
    System.err.printf(" %s | %s%n", " ".repeat(lineNumberWidth), pointerLine);
  }
}
