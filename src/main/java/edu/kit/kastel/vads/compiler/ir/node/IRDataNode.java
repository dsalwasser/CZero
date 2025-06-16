package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.data.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstBoolNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SideEffectNode;

public sealed class IRDataNode extends IRNode
    permits BinaryOperationNode, ConstBoolNode, ConstIntNode, Phi, SideEffectNode {

  protected IRDataNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }
}
