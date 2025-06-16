package edu.kit.kastel.vads.compiler.ir.optimization;

import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.HashMap;
import java.util.Map;

/// This depends on [IRNode#equals(java.lang.Object)] and  [IRNode#hashCode()] methods.
/// As long as they take the block into account, it is only local, but replacement
/// is extremely simple.
/// When using classes like [HashMap] or [java.util.HashSet] without this optimization,
/// the [IRNode#equals(java.lang.Object)] and  [IRNode#hashCode()] methods must be adjusted.
public class LocalValueNumbering implements Optimizer {
  private final Map<IRNode, IRNode> knownNodes = new HashMap<>();

  @Override
  public IRNode transform(IRNode node) {
    return this.knownNodes.computeIfAbsent(node, n -> n);
  }
}
