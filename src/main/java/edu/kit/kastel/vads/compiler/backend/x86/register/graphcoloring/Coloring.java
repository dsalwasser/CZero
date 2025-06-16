package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.Map;

public final record Coloring(Map<IRNode, Integer> assignment, int numColors) {

  public int get(IRNode node) {
    return assignment.get(node);
  }
}
