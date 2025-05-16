package edu.kit.kastel.vads.compiler.ir.util;

import edu.kit.kastel.vads.compiler.util.Span;

/// Provides information to ease debugging
public sealed interface DebugInfo {
  enum NoInfo implements DebugInfo {
    INSTANCE
  }

  record SourceInfo(Span span) implements DebugInfo {}
}
