package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class DivNode extends BinaryOperationNode {
  public static final int SIDE_EFFECT = 2;

  public DivNode(Block block, IRDataNode left, IRDataNode right, IRDataNode sideEffect) {
    super(block, left, right, sideEffect);
  }

  @Override
  public boolean equals(Object obj) {
    // side effect, must be very careful with value numbering.
    // this is the most conservative approach
    return obj == this;
  }
}
