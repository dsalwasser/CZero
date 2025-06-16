package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRBranchingControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import java.util.ArrayList;
import java.util.List;

public final class LoopNode extends IRBranchingControlNode {
  private LoopBodyMergeNode bodyMerge;
  private LoopExitMergeNode exitMerge;

  public LoopNode(Block block, IRControlNode predecessor) {
    super(block, predecessor);
  }

  public IRControlNode predecessor() {
    return (IRControlNode) predecessor(0);
  }

  public IRDataNode condition() {
    return (IRDataNode) predecessor(1);
  }

  public IRControlNode body() {
    return graph().controlSuccessors(this).get(0);
  }

  public IRControlNode exit() {
    return graph().controlSuccessors(this).get(1);
  }

  public LoopBodyMergeNode bodyMerge() {
    return bodyMerge;
  }

  public LoopExitMergeNode exitMerge() {
    return exitMerge;
  }

  public void setBodyMerge(LoopBodyMergeNode merge) {
    this.bodyMerge = merge;
  }

  public void setExitMerge(LoopExitMergeNode exitMerge) {
    this.exitMerge = exitMerge;
  }

  public List<Phi> phis() {
    List<Phi> phis = new ArrayList<>();

    for (IRNode node : graph().successors(this)) {
      if (node instanceof Phi phi) {
        phis.add(phi);
      }
    }

    return phis.reversed();
  }
}
