package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;

public final class Phi extends IRDataNode {
  public Phi(Block block, IRControlNode control) {
    super(block, control);
  }

  public IRControlNode controlNode() {
    return (IRControlNode) predecessor(0);
  }

  public IRDataNode dataNode(int id) {
    return (IRDataNode) predecessor(1 + id);
  }

  public boolean hasPhiDataNode() {
    return predecessors().size() == 2;
  }

  public void appendOperand(IRNode node) {
    addPredecessor(node);
  }
}
