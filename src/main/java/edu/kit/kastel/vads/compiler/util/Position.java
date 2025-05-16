package edu.kit.kastel.vads.compiler.util;

public sealed interface Position {
  int line();

  int column();

  record SimplePosition(int line, int column) implements Position {
    @Override
    public String toString() {
      return line() + ":" + column();
    }
  }
}
