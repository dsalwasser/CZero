package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class GreaterThanEqualNode extends BinaryOperationNode {

  public GreaterThanEqualNode(Block block, IRDataNode left, IRDataNode right) {
    super(block, left, right);
  }
}
