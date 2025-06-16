package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;

public final class LoopBodyMergeNode extends IRLinearControlNode {

  private LoopNode loopNode;

  public LoopBodyMergeNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }

  public void setLoopNode(LoopNode loopNode) {
    this.loopNode = loopNode;
  }

  public LoopNode getLoopNode() {
    return loopNode;
  }
}
