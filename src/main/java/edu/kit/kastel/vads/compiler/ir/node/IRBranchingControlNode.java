package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.control.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopNode;

public sealed class IRBranchingControlNode extends IRControlNode permits IfNode, LoopNode {
  protected IRBranchingControlNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }
}
