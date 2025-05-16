package edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring;

import edu.kit.kastel.vads.compiler.backend.x86.registerallocation.X86RegisterAllocation;
import java.util.HashSet;
import java.util.Set;

public final class HighestColorSpillingHeuristic implements SpillingHeuristic {

  @Override
  public Set<Integer> computeColorsToSpill(InterferenceGraph graph, Coloring coloring) {
    Set<Integer> spilledColors = new HashSet<>();

    final int numColorsToSpill =
        coloring.numColors() - X86RegisterAllocation.NUM_AVAIABLE_REGISTERS;
    int highestColor = coloring.numColors() - 1;
    for (int i = 0; i < numColorsToSpill; i++) {
      spilledColors.add(highestColor);
      highestColor -= 1;
    }

    return spilledColors;
  }
}
