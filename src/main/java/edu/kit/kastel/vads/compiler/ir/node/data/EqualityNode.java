package edu.kit.kastel.vads.compiler.ir.node.data;

import edu.kit.kastel.vads.compiler.ir.Block;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;

public final class EqualityNode extends BinaryOperationNode {

  public EqualityNode(Block block, IRDataNode left, IRDataNode right) {
    super(block, left, right);
  }

  @SuppressWarnings("EqualsDoesntCheckParameterClass") // we do, but not here
  @Override
  public boolean equals(Object obj) {
    return commutativeEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return commutativeHashCode(this);
  }
}
