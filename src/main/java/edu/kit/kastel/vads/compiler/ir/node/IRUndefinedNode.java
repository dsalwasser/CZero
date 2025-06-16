package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.Block;

public final class IRUndefinedNode extends IRNode {

  public IRUndefinedNode(Block block, IRNode... predecessors) {
    super(block, predecessors);
  }
}
