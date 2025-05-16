package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MaximumCardinalitySearch {

  public static List<Node> computeSimplicialEliminationOrdering(InterferenceGraph graph) {
    List<Node> ordering = new ArrayList<>(graph.numNodes());

    Set<Node> nodes = new HashSet<>();
    Map<Node, Integer> weights = new HashMap<>();
    for (Node u : graph.nodes()) {
      nodes.add(u);
      weights.put(u, 0);
    }

    for (int i = 0; i < graph.numNodes(); ++i) {
      Node u = nodes.stream().max(Comparator.comparingInt(weights::get)).get();
      ordering.add(u);

      for (Node v : graph.neighbors(u)) {
        if (nodes.contains(v)) {
          weights.put(v, weights.get(v) + 1);
        }
      }

      nodes.remove(u);
    }

    return ordering;
  }
}
