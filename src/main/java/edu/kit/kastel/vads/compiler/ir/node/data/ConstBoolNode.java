package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class ConstBoolNode extends IRDataNode {
  private final boolean value;

  public ConstBoolNode(Block block, boolean value) {
    super(block);
    this.value = value;
  }

  public boolean value() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ConstBoolNode c && this.block() == c.block() && this.value == c.value;
  }

  @Override
  public int hashCode() {
    return this.value ? 1 : 0;
  }

  @Override
  protected String info() {
    return "[" + (this.value ? "true" : "false") + "]";
  }
}
