package edu.kit.kastel.vads.compiler.ir.util;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IrGraphVisualizer {

  public static String toDot(IRGraph graph) {
    StringBuilder dot = new StringBuilder();
    dot.append("digraph IR {\n");
    dot.append("  node [shape=box];\n");
    dot.append("\n");

    IdentityHashMap<IRNode, Unit> visited = new IdentityHashMap<>();
    Map<Block, List<IRNode>> blockMap = new HashMap<>();
    Set<String> edges = new HashSet<>();

    Deque<IRNode> stack = new ArrayDeque<>();
    stack.push(graph.endBlock().entryNode());

    while (!stack.isEmpty()) {
      IRNode node = stack.pop();
      if (visited.containsKey(node)) continue;
      visited.put(node, Unit.INSTANCE);

      blockMap.computeIfAbsent(node.block(), _ -> new ArrayList<>()).add(node);

      List<IRNode> preds = node.predecessors();
      for (int i = 0; i < preds.size(); i++) {
        IRNode pred = preds.get(i);
        stack.push(pred);

        int id = i;
        boolean reverse = false;
        StringBuilder style = new StringBuilder();
        if (pred instanceof IRControlNode) {
          id = graph.successors(pred).indexOf(node);

          if (node instanceof Phi) {
            reverse = true;
            style.append("color=blue");
          } else {
            style.append("color=red");
          }
        } else if (pred instanceof IRDataNode) {
          style.append("style=dashed");
        }

        // Add label for the edge
        if (style.length() > 0) {
          style.append(", ");
        }
        style.append(String.format("label=\"%d\"", id));

        String edge;
        if (reverse) {
          edge =
              String.format(
                  "  \"%s\" -> \"%s\" [%s];\n",
                  System.identityHashCode(node), System.identityHashCode(pred), style.toString());
        } else {
          edge =
              String.format(
                  "  \"%s\" -> \"%s\" [%s];\n",
                  System.identityHashCode(pred), System.identityHashCode(node), style.toString());
        }

        if (edges.add(edge)) {
          dot.append(edge);
        }
      }
    }
    dot.append("\n");

    int clusterId = 0;
    for (Map.Entry<Block, List<IRNode>> entry : blockMap.entrySet()) {
      dot.append(String.format("  subgraph cluster_%d {\n", clusterId++));
      dot.append("    style=dashed;\n");
      dot.append(String.format("    label=\"Block\";\n"));
      for (IRNode node : entry.getValue()) {
        dot.append(
            String.format("    \"%s\" [label=\"%s\"];\n", System.identityHashCode(node), node));
      }
      dot.append("  }\n");
    }

    dot.append("}\n");
    return dot.toString();
  }
}
