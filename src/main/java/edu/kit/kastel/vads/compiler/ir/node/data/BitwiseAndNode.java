package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class BitwiseAndNode extends BinaryOperationNode {

  public BitwiseAndNode(Block block, IRDataNode left, IRDataNode right) {
    super(block, left, right);
  }
}
