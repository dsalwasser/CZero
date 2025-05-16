package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import java.util.Set;

public sealed interface SpillingHeuristic
    permits LeastUsedColorSpillingHeuristic, HighestColorSpillingHeuristic {

  public Set<Integer> computeColorsToSpill(InterferenceGraph graph, Coloring coloring);
}
