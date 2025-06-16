package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import java.util.ArrayList;
import java.util.List;

/// The base class for all nodes.
public abstract sealed class IRNode permits IRControlNode, IRDataNode, IRUndefinedNode {
  private final IRGraph graph;
  private final Block block;
  private final List<IRNode> predecessors = new ArrayList<>();

  protected IRNode(Block block, IRNode... predecessors) {
    this.graph = block.graph();

    this.block = block;
    block.addNode(this);

    this.predecessors.addAll(List.of(predecessors));
    for (IRNode predecessor : this.predecessors) {
      graph.registerSuccessor(predecessor, this);
    }
  }

  public final IRGraph graph() {
    return this.graph;
  }

  public final Block block() {
    return this.block;
  }

  public final List<IRNode> predecessors() {
    return List.copyOf(this.predecessors);
  }

  public final IRNode predecessor(int idx) {
    return this.predecessors.get(idx);
  }

  public final void setPredecessor(int idx, IRNode node) {
    this.graph.removeSuccessor(this.predecessors.get(idx), this);
    this.predecessors.set(idx, node);
    this.graph.registerSuccessor(node, this);
  }

  public final void addPredecessor(int idx, IRNode node) {
    this.predecessors.add(node);
    this.graph.replaceSuccessor(node, this, idx);
  }

  public final void addPredecessor(IRNode node) {
    this.predecessors.add(node);
    this.graph.registerSuccessor(node, this);
  }

  @Override
  public final String toString() {
    return (this.getClass().getSimpleName().replace("Node", "") + " " + info()).stripTrailing();
  }

  protected String info() {
    return "";
  }

  protected static int predecessorHash(IRNode node, int predecessor) {
    return System.identityHashCode(node.predecessor(predecessor));
  }
}
