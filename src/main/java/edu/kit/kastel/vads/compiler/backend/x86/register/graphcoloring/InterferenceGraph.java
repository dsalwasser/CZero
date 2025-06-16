package edu.kit.kastel.vads.compiler.backend.x86.register.graphcoloring;

import edu.kit.kastel.vads.compiler.backend.util.ControlFlowOrder;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.control.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstIntNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InterferenceGraph {

  public static InterferenceGraph construct(IRGraph function) {
    List<IRNode> nodes = ControlFlowOrder.getNodesByControlFlowOrder(function).reversed();
    InterferenceGraph graph = new InterferenceGraph(nodes);

    Set<IRNode> liveNodes = new HashSet<>();
    for (IRNode node : nodes) {
      switch (node) {
        case BinaryOperationNode binaryOp -> {
          for (IRNode liveNode : liveNodes) {
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
        case StartNode _, Phi _, ConstIntNode _ -> {}
        default -> throw new UnsupportedOperationException();
      }
    }

    return graph;
  }

  private Map<IRNode, Set<IRNode>> graph;

  private InterferenceGraph(Collection<IRNode> nodes) {
    this.graph = new HashMap<>();

    for (IRNode node : nodes) {
      graph.put(node, new HashSet<>());
    }
  }

  private void addEdge(IRNode u, IRNode v) {
    assert u != v;

    graph.get(u).add(v);
    graph.get(v).add(u);
  }

  public int numNodes() {
    return graph.size();
  }

  public Set<IRNode> nodes() {
    return Collections.unmodifiableSet(graph.keySet());
  }

  public Collection<IRNode> neighbors(IRNode u) {
    return Collections.unmodifiableSet(graph.get(u));
  }
}
