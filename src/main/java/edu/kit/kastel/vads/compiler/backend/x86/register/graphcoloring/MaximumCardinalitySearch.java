package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MaximumCardinalitySearch {

  public static List<IRNode> computeSimplicialEliminationOrdering(InterferenceGraph graph) {
    List<IRNode> ordering = new ArrayList<>(graph.numNodes());

    Set<IRNode> nodes = new HashSet<>();
    Map<IRNode, Integer> weights = new HashMap<>();
    for (IRNode u : graph.nodes()) {
      nodes.add(u);
      weights.put(u, 0);
    }

    for (int i = 0; i < graph.numNodes(); ++i) {
      IRNode u = nodes.stream().max(Comparator.comparingInt(weights::get)).get();
      ordering.add(u);

      for (IRNode v : graph.neighbors(u)) {
        if (nodes.contains(v)) {
          weights.put(v, weights.get(v) + 1);
        }
      }

      nodes.remove(u);
    }

    return ordering;
  }
}
