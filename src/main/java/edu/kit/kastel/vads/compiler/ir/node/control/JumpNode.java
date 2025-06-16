package edu.kit.kastel.vads.compiler.ir.node.control;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRLinearControlNode;

public final class JumpNode extends IRLinearControlNode {
  public JumpNode(Block block) {
    super(block);
  }

  public JumpNode(Block block, IRControlNode predecessor) {
    super(block, predecessor);
  }
}
