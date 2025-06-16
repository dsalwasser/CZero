package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class SubNode extends BinaryOperationNode {
  public SubNode(Block block, IRDataNode left, IRDataNode right) {
    super(block, left, right);
  }
}
