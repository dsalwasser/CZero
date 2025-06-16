package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

public final class Block {

  private IRGraph graph;
  private @Nullable IRControlNode entryNode;
  private List<IRNode> nodes;

  public Block(IRGraph graph) {
    this.graph = graph;
    this.nodes = new ArrayList<>();
  }

  public void setEntryNode(IRControlNode entryNode) {
    this.entryNode = entryNode;
  }

  public void addNode(IRNode node) {
    this.nodes.add(node);
  }

  public void removeNode(IRNode node) {
    this.nodes.remove(node);
  }

  public List<IRNode> nodes() {
    return Collections.unmodifiableList(nodes);
  }

  public IRControlNode entryNode() {
    return entryNode;
  }

  public IRGraph graph() {
    return graph;
  }

  public List<? extends IRNode> predecessors() {
    return this.entryNode.predecessors();
  }

  public List<? extends IRControlNode> controlPredecessors() {
    return this.entryNode.predecessors().stream()
        .filter(node -> node instanceof IRControlNode)
        .map(node -> (IRControlNode) node)
        .toList();
  }
}
