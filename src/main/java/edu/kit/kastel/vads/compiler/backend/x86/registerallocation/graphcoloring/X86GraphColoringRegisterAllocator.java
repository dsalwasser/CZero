package edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring;

import edu.kit.kastel.vads.compiler.backend.x86.X86Register;
import edu.kit.kastel.vads.compiler.backend.x86.registerallocation.X86RegisterAllocation;
import edu.kit.kastel.vads.compiler.backend.x86.registerallocation.X86RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class X86GraphColoringRegisterAllocator implements X86RegisterAllocator {

  private final SpillingHeuristic spillingHeuristic;

  public X86GraphColoringRegisterAllocator(SpillingHeuristic spillingHeuristic) {
    this.spillingHeuristic = spillingHeuristic;
  }

  @Override
  public X86RegisterAllocation allocateRegisters(IrGraph function) {
    InterferenceGraph graph = InterferenceGraph.construct(function);
    List<Node> ordering = MaximumCardinalitySearch.computeSimplicialEliminationOrdering(graph);

    Coloring coloring = GreedyColoring.applyGreedyColoring(graph, ordering);
    Set<Integer> spilledColors = spillingHeuristic.computeColorsToSpill(graph, coloring);

    Map<Integer, X86Register> colorMapping = computeColorMapping(coloring, spilledColors);
    Map<Node, X86Register> registerAllocation = computeRegisterAllocation(coloring, colorMapping);

    final int numVirutalRegisters = spilledColors.size();
    return new X86RegisterAllocation(registerAllocation, numVirutalRegisters);
  }

  private static Map<Integer, X86Register> computeColorMapping(
      Coloring coloring, Set<Integer> spilledColors) {
    Map<Integer, X86Register> colorMapping = new HashMap<>();

    int curVirtualRegister = 0;
    int curPhysicalRegister = 0;
    for (int color = 0; color < coloring.numColors(); ++color) {
      if (spilledColors.contains(color)) {
        colorMapping.put(color, new X86Register.Virtual(curVirtualRegister++));
      } else {
        colorMapping.put(
            color, X86RegisterAllocation.AVAILABLE_REGISTERS.get(curPhysicalRegister++));
      }
    }

    return colorMapping;
  }

  private static Map<Node, X86Register> computeRegisterAllocation(
      Coloring coloring, Map<Integer, X86Register> colorMapping) {
    Map<Node, X86Register> registerAllocation = new HashMap<>();

    coloring
        .assignment()
        .forEach(
            (node, color) -> {
              registerAllocation.put(node, colorMapping.get(color));
            });

    return registerAllocation;
  }
}
