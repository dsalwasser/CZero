package edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.Map;

public final record Coloring(Map<Node, Integer> assignment, int numColors) {

  public int get(Node node) {
    return assignment.get(node);
  }
}
