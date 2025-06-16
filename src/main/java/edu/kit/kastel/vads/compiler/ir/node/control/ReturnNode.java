package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SideEffectNode;
import java.util.ArrayList;
import java.util.List;

public final class ReturnNode extends IRLinearControlNode {
  public static final int CONTROL = 0;
  public static final int SIDE_EFFECT = 1;
  public static final int RESULT = 2;

  public ReturnNode(Block block, IRControlNode predecessor, IRDataNode sideEffect) {
    super(block, predecessor, sideEffect);
  }

  public IRControlNode control() {
    return (IRControlNode) predecessor(CONTROL);
  }

  public IRDataNode result() {
    return (IRDataNode) predecessor(RESULT);
  }

  public List<SideEffectNode> sideEffectNodes() {
    List<SideEffectNode> sideEffectNodes = new ArrayList<>();

    for (IRNode predecessor : predecessors()) {
      if (predecessor != result() && predecessor instanceof SideEffectNode sideEffectNode) {
        sideEffectNodes.add(sideEffectNode);
      }
    }

    return sideEffectNodes;
  }
}
