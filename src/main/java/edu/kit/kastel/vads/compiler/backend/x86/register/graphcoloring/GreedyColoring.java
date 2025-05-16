package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GreedyColoring {

  public static Coloring applyGreedyColoring(InterferenceGraph graph, List<Node> ordering) {
    Map<Node, Integer> assignment = new HashMap<>();

    Integer maxColor = -1;
    Set<Integer> usedColors = new HashSet<>();
    for (int i = 0; i < graph.numNodes(); ++i) {
      Node u = ordering.get(i);

      for (Node v : graph.neighbors(u)) {
        if (assignment.containsKey(v)) {
          usedColors.add(assignment.get(v));
        }
      }

      int color = 0;
      while (usedColors.contains(color)) {
        color += 1;
      }
      usedColors.clear();

      maxColor = Math.max(maxColor, color);
      assignment.put(u, color);
    }

    final int numColors = maxColor + 1;
    return new Coloring(assignment, numColors);
  }
}
