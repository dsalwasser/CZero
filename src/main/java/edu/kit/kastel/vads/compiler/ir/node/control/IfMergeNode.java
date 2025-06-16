package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import java.util.ArrayList;
import java.util.List;

public final class IfMergeNode extends IRLinearControlNode {

  public IfMergeNode(Block block, IRControlNode... predecessors) {
    super(block, predecessors);
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
