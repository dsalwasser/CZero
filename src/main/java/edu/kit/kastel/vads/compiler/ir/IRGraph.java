package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.control.EndNode;
import edu.kit.kastel.vads.compiler.ir.node.control.StartNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class IRGraph {
  private final String name;

  private final Block startBlock;
  private final Block endBlock;

  private final Map<IRNode, List<IRNode>> successors = new IdentityHashMap<>();

  public IRGraph(String name) {
    this.name = name;

    this.startBlock = new Block(this);
    this.endBlock = new Block(this);

    StartNode startNode = new StartNode(this.startBlock);
    this.startBlock.setEntryNode(startNode);

    EndNode endNode = new EndNode(this.endBlock);
    this.endBlock.setEntryNode(endNode);
  }

  public String name() {
    return name;
  }

  public Block startBlock() {
    return this.startBlock;
  }

  public Block endBlock() {
    return this.endBlock;
  }

  public void registerSuccessor(IRNode node, IRNode successor) {
    this.successors.computeIfAbsent(node, _ -> new ArrayList<>()).add(successor);
  }

  public void removeSuccessor(IRNode node, IRNode oldSuccessor) {
    this.successors.computeIfAbsent(node, _ -> new ArrayList<>()).remove(oldSuccessor);
  }

  public void replaceSuccessor(IRNode node, IRNode successor, int id) {
    this.successors.computeIfAbsent(node, _ -> new ArrayList<>()).set(id, successor);
  }

  /// {@return the set of nodes that have the given node as one of their inputs}
  public List<IRNode> successors(IRNode node) {
    List<IRNode> successors = this.successors.get(node);
    if (successors == null) {
      return Collections.emptyList();
    }

    return List.copyOf(successors);
  }

  public List<IRControlNode> controlSuccessors(IRNode node) {
    List<IRNode> successors = this.successors.get(node);
    if (successors == null) {
      return Collections.emptyList();
    }

    return successors.stream()
        .filter(n -> n instanceof IRControlNode)
        .map(n -> (IRControlNode) n)
        .toList();
  }
}
