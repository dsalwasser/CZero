package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.control.IfMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.JumpNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopBodyMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopExitMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.control.StartNode;
import java.util.List;

public sealed class IRLinearControlNode extends IRControlNode
    permits StartNode, JumpNode, IfMergeNode, LoopBodyMergeNode, LoopExitMergeNode, ReturnNode {
  public IRLinearControlNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }

  public IRControlNode successor() {
    List<IRNode> successors = graph().successors(this);
    return (IRControlNode)
        successors.stream().filter(node -> node instanceof IRControlNode).findFirst().get();
  }
}
