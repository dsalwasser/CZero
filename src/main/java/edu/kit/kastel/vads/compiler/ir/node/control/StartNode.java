package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;

public final class StartNode extends IRLinearControlNode {
  public StartNode(Block block) {
    super(block);
  }
}
