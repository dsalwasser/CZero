package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRBranchingControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class IfNode extends IRBranchingControlNode {
  private IfMergeNode merge;

  public IfNode(Block block, IRControlNode predecessor, IRDataNode condition) {
    super(block, predecessor, condition);
  }

  public IRControlNode predecessor() {
    return (IRControlNode) predecessor(0);
  }

  public IRDataNode condition() {
    return (IRDataNode) predecessor(1);
  }

  public IRControlNode thenSuccessor() {
    return (IRControlNode) graph().successors(this).get(0);
  }

  public IRControlNode elseSuccessor() {
    return (IRControlNode) graph().successors(this).get(1);
  }

  public IfMergeNode merge() {
    return merge;
  }

  public void setMerge(IfMergeNode joinNode) {
    this.merge = joinNode;
  }

  public boolean hasMerge() {
    return this.merge != null;
  }
}
