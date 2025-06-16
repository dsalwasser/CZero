package edu.kit.kastel.vads.compiler.parser.symbol;

public record IdentName(String identifier) implements Name {
  @Override
  public String asString() {
    return identifier();
  }
}
