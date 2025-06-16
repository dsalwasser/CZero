package edu.kit.kastel.vads.compiler.backend.util;

import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ControlFlowOrder {

  public static List<IRNode> getNodesByControlFlowOrder(IRGraph function) {
    List<IRNode> nodes = new ArrayList<IRNode>();
    Set<IRNode> visited = new HashSet<>();
    scan(function.endBlock().entryNode(), visited, nodes);
    return nodes;
  }

  private static void scan(IRNode node, Set<IRNode> visited, List<IRNode> nodes) {
    for (IRNode predecessor : node.predecessors()) {
      if (visited.add(predecessor)) {
        scan(predecessor, visited, nodes);
      }
    }

    nodes.add(node);
  }
}
