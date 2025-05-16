package edu.kit.kastel.vads.compiler.backend.x86.registerallocation.graphcoloring;

import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InterferenceGraph {

  public static InterferenceGraph construct(IrGraph function) {
    List<Node> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function).reversed();
    InterferenceGraph graph = new InterferenceGraph(nodes);

    Set<Node> liveNodes = new HashSet<>();
    for (Node node : nodes) {
      switch (node) {
        case BinaryOperationNode binaryOp -> {
          for (Node liveNode : liveNodes) {
            if (node == liveNode) {
              continue;
            }

            graph.addEdge(node, liveNode);
          }

          liveNodes.remove(binaryOp);
          liveNodes.add(binaryOp.left());
          liveNodes.add(binaryOp.right());
        }
        case ReturnNode ret -> liveNodes.add(ret.result());
        case StartNode _, ProjNode _, Phi _, ConstIntNode _, Block _ -> {}
      }
    }

    return graph;
  }

  private Map<Node, Set<Node>> graph;

  private InterferenceGraph(Collection<Node> nodes) {
    this.graph = new HashMap<>();

    for (Node node : nodes) {
      graph.put(node, new HashSet<>());
    }
  }

  private void addEdge(Node u, Node v) {
    assert u != v;

    graph.get(u).add(v);
    graph.get(v).add(u);
  }

  public int numNodes() {
    return graph.size();
  }

  public Set<Node> nodes() {
    return Collections.unmodifiableSet(graph.keySet());
  }

  public Collection<Node> neighbors(Node u) {
    return Collections.unmodifiableSet(graph.get(u));
  }
}
