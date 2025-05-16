package edu.kit.kastel.vads.compiler.backend.util;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ControlFlowOrder {

  public static List<Node> getNodesByControlFlowOrder(IrGraph function) {
    List<Node> nodes = new ArrayList<Node>();
    Set<Node> visited = new HashSet<>();
    scan(function.endBlock(), visited, nodes);
    return nodes;
  }

  private static void scan(Node node, Set<Node> visited, List<Node> nodes) {
    for (Node predecessor : node.predecessors()) {
      if (visited.add(predecessor)) {
        scan(predecessor, visited, nodes);
      }
    }

    nodes.add(node);
  }
}
