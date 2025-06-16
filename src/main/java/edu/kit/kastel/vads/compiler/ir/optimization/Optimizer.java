package edu.kit.kastel.vads.compiler.ir.optimization;

import edu.kit.kastel.vads.compiler.ir.node.IRNode;

/// An interface that allows replacing a node with a more optimal one.
public interface Optimizer {

  IRNode transform(IRNode node);
}
