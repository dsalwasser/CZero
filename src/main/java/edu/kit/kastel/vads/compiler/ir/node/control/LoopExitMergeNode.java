package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import java.util.ArrayList;
import java.util.List;

public final class LoopExitMergeNode extends IRLinearControlNode {

  private LoopNode loopNode;

  public LoopExitMergeNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }

  public LoopNode loopNode() {
    return loopNode;
  }

  public void setLoopNode(LoopNode loopNode) {
    this.loopNode = loopNode;
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
