package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.util.NodeSupport;

public final class ReturnNode extends Node {
  public static final int SIDE_EFFECT = 0;
  public static final int RESULT = 1;

  public ReturnNode(Block block, Node sideEffect, Node result) {
    super(block, sideEffect, result);
  }

  public Node result() {
    return NodeSupport.predecessorSkipProj(this, ReturnNode.RESULT);
  }
}
