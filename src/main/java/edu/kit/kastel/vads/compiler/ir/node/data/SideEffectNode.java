package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import java.util.Optional;

public final class SideEffectNode extends IRDataNode {
  public static final int INPUT = 0;

  public SideEffectNode(Block block, IRNode predecessor) {
    super(block, predecessor);
  }

  public Optional<IRDataNode> input() {
    if (predecessor(INPUT) instanceof IRDataNode dataNode) {
      return Optional.of(dataNode);
    }

    return Optional.empty();
  }
}
