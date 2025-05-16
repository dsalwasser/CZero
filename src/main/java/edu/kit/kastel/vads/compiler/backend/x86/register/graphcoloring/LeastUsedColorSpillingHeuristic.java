package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.backend.x86.register.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LeastUsedColorSpillingHeuristic implements SpillingHeuristic {

  @Override
  public Set<Integer> computeColorsToSpill(InterferenceGraph graph, Coloring coloring) {
    Set<Integer> spilledColors = new HashSet<>();

    Map<Integer, Integer> colorOccurrences = new HashMap<>();
    for (Node node : graph.nodes()) {
      int nodeColor = coloring.get(node);
      colorOccurrences.put(nodeColor, colorOccurrences.getOrDefault(nodeColor, 0) + 1);
    }

    List<Integer> colors = new ArrayList<>();
    for (int color = 0; color < coloring.numColors(); ++color) {
      colors.add(color);
    }
    colors.sort(Comparator.comparingInt(colorOccurrences::get));

    final int numColorsToSpill =
        coloring.numColors() - X86RegisterAllocation.NUM_AVAIABLE_REGISTERS;
    for (int i = 0; i < numColorsToSpill; i++) {
      spilledColors.add(colors.get(i));
    }

    return spilledColors;
  }
}
