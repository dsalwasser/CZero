package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;

public final class EndNode extends IRControlNode {
  public EndNode(Block block) {
    super(block);
  }
}
