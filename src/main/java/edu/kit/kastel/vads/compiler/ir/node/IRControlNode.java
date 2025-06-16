package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.control.EndNode;

public sealed class IRControlNode extends IRNode
    permits IRLinearControlNode, IRBranchingControlNode, EndNode {
  protected IRControlNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }

  public boolean isEntryNode() {
    return block().entryNode() == this;
  }
}
