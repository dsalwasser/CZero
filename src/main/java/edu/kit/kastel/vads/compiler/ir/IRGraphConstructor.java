package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRNode;
import edu.kit.kastel.vads.compiler.ir.node.IRUndefinedNode;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.control.IfMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.control.JumpNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopBodyMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopExitMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopNode;
import edu.kit.kastel.vads.compiler.ir.node.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.data.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseAndNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseOrNode;
import edu.kit.kastel.vads.compiler.ir.node.data.BitwiseXORNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstBoolNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.data.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.data.EqualityNode;
import edu.kit.kastel.vads.compiler.ir.node.data.GreaterThanEqualNode;
import edu.kit.kastel.vads.compiler.ir.node.data.GreaterThanNode;
import edu.kit.kastel.vads.compiler.ir.node.data.InequalityNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LeftShiftNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LessThanEqualNode;
import edu.kit.kastel.vads.compiler.ir.node.data.LessThanNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.data.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.data.OrNode;
import edu.kit.kastel.vads.compiler.ir.node.data.RightShiftNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SideEffectNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SubNode;
import edu.kit.kastel.vads.compiler.parser.symbol.IdentName;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class IRGraphConstructor {
  private final IRGraph graph;

  private final Map<Name, Map<Block, IRDataNode>> currentDef = new HashMap<>();
  private final Map<Block, Map<Name, Phi>> incompletePhis = new HashMap<>();
  private final Set<Block> sealedBlocks = new HashSet<>();

  private final Map<Block, IRDataNode> currentSideEffect = new HashMap<>();
  private final Map<Block, Phi> incompleteSideEffectPhis = new HashMap<>();

  private final Map<Block, Map<IRDataNode, IRDataNode>> knownLiterals = new HashMap<>();

  private Block currentBlock;
  private IRControlNode currentControlNode;
  private int freshVariableCounter;

  public IRGraphConstructor(String name) {
    this.graph = new IRGraph(name);

    this.currentBlock = this.graph.startBlock();
    this.currentControlNode = this.graph.startBlock().entryNode();
    this.freshVariableCounter = 1;

    // the start block never gets any more predecessors
    sealBlock(this.currentBlock);

    writeCurrentSideEffect(newSideEffectProj(this.graph.startBlock().entryNode()));
  }

  public IRGraph graph() {
    return this.graph;
  }

  public IdentName newFreshVariableName() {
    return new IdentName("_var" + freshVariableCounter++);
  }

  public IRUndefinedNode newUndefinedNode() {
    return new IRUndefinedNode(currentBlock());
  }

  public SideEffectNode newSideEffectProj(IRNode node) {
    return new SideEffectNode(currentBlock(), node);
  }

  public ConstBoolNode newConstBool(boolean value) {
    return (ConstBoolNode)
        this.knownLiterals
            .computeIfAbsent(currentBlock(), _ -> new HashMap<>())
            .computeIfAbsent(new ConstBoolNode(currentBlock(), value), n -> n);
  }

  public ConstIntNode newConstInt(int value) {
    return (ConstIntNode)
        this.knownLiterals
            .computeIfAbsent(currentBlock(), _ -> new HashMap<>())
            .computeIfAbsent(new ConstIntNode(currentBlock(), value), n -> n);
  }

  public IRDataNode newAdd(IRDataNode left, IRDataNode right) {
    if (left instanceof ConstIntNode l && right instanceof ConstIntNode r) {
      return new ConstIntNode(currentBlock(), l.value() + r.value());
    }

    return new AddNode(currentBlock(), left, right);
  }

  public IRDataNode newSub(IRDataNode left, IRDataNode right) {
    if (left instanceof ConstIntNode l && right instanceof ConstIntNode r) {
      return new ConstIntNode(currentBlock(), l.value() - r.value());
    }
    return new SubNode(currentBlock(), left, right);
  }

  public IRDataNode newMul(IRDataNode left, IRDataNode right) {
    if (left instanceof ConstIntNode l && right instanceof ConstIntNode r) {
      return new ConstIntNode(currentBlock(), l.value() * r.value());
    }

    return new MulNode(currentBlock(), left, right);
  }

  public IRDataNode newDiv(IRDataNode left, IRDataNode right) {
    if (left instanceof ConstIntNode l
        && right instanceof ConstIntNode r
        && r.value() != 0
        && !(l.value() == Integer.MIN_VALUE && r.value() == -1)) {
      return new ConstIntNode(currentBlock(), l.value() / r.value());
    }

    return new DivNode(currentBlock(), left, right, readCurrentSideEffect());
  }

  public IRDataNode newMod(IRDataNode left, IRDataNode right) {
    if (left instanceof ConstIntNode l
        && right instanceof ConstIntNode r
        && r.value() != 0
        && !(l.value() == Integer.MIN_VALUE && r.value() == -1)) {
      return new ConstIntNode(currentBlock(), l.value() % r.value());
    }

    return new ModNode(currentBlock(), left, right, readCurrentSideEffect());
  }

  public BitwiseAndNode newBitwiseAnd(IRDataNode left, IRDataNode right) {
    return new BitwiseAndNode(currentBlock(), left, right);
  }

  public BitwiseOrNode newBitwiseOr(IRDataNode left, IRDataNode right) {
    return new BitwiseOrNode(currentBlock(), left, right);
  }

  public BitwiseXORNode newBitwiseXOR(IRDataNode left, IRDataNode right) {
    return new BitwiseXORNode(currentBlock(), left, right);
  }

  public LeftShiftNode newLeftShift(IRDataNode left, IRDataNode right) {
    return new LeftShiftNode(currentBlock(), left, right);
  }

  public RightShiftNode newRightShift(IRDataNode left, IRDataNode right) {
    return new RightShiftNode(currentBlock(), left, right);
  }

  public AddNode newAnd(IRDataNode left, IRDataNode right) {
    return new AddNode(currentBlock(), left, right);
  }

  public OrNode newOr(IRDataNode left, IRDataNode right) {
    return new OrNode(currentBlock(), left, right);
  }

  public EqualityNode newEquality(IRDataNode left, IRDataNode right) {
    return new EqualityNode(currentBlock(), left, right);
  }

  public InequalityNode newInequality(IRDataNode left, IRDataNode right) {
    return new InequalityNode(currentBlock(), left, right);
  }

  public LessThanEqualNode newLessThanEqual(IRDataNode left, IRDataNode right) {
    return new LessThanEqualNode(currentBlock(), left, right);
  }

  public LessThanNode newLessThan(IRDataNode left, IRDataNode right) {
    return new LessThanNode(currentBlock(), left, right);
  }

  public GreaterThanEqualNode newGreaterThanEqual(IRDataNode left, IRDataNode right) {
    return new GreaterThanEqualNode(currentBlock(), left, right);
  }

  public GreaterThanNode newGreaterThan(IRDataNode left, IRDataNode right) {
    return new GreaterThanNode(currentBlock(), left, right);
  }

  public IfNode newBranch(IRDataNode condition) {
    IfNode branchNode = new IfNode(currentBlock(), currentControlNode, condition);
    registerNewControlNode(branchNode);
    return branchNode;
  }

  public LoopNode newLoop() {
    LoopNode loopNode = new LoopNode(currentBlock(), currentControlNode);
    registerNewControlNode(loopNode);
    return loopNode;
  }

  public JumpNode newJump() {
    JumpNode jumpNode = new JumpNode(currentBlock(), currentControlNode);
    registerNewControlNode(jumpNode);
    return jumpNode;
  }

  public JumpNode newJump(int id) {
    JumpNode jumpNode = new JumpNode(currentBlock());
    jumpNode.addPredecessor(id, currentControlNode);
    registerNewControlNode(jumpNode);
    return jumpNode;
  }

  public IfMergeNode newIfMerge(IRControlNode... predecessors) {
    IfMergeNode mergeNode = new IfMergeNode(currentBlock(), predecessors);
    registerNewControlNode(mergeNode);
    return mergeNode;
  }

  public LoopBodyMergeNode newLoopBodyMerge(IRControlNode... predecessors) {
    LoopBodyMergeNode mergeNode = new LoopBodyMergeNode(currentBlock(), predecessors);
    registerNewControlNode(mergeNode);
    return mergeNode;
  }

  public LoopExitMergeNode newLoopEndMerge(IRControlNode... predecessors) {
    LoopExitMergeNode mergeNode = new LoopExitMergeNode(currentBlock(), predecessors);
    registerNewControlNode(mergeNode);
    return mergeNode;
  }

  public ReturnNode newReturn() {
    ReturnNode returnNode =
        new ReturnNode(currentBlock(), currentControlNode, readCurrentSideEffect());
    registerNewControlNode(returnNode);
    return returnNode;
  }

  private void registerNewControlNode(IRControlNode node) {
    if (currentBlock.entryNode() == null) {
      currentBlock.setEntryNode(node);
    }
  }

  public void setCurrentControlNode(IRControlNode controlNode) {
    this.currentControlNode = controlNode;
  }

  public IRControlNode getCurrentControlNode() {
    return currentControlNode;
  }

  public Block newBlock() {
    return new Block(graph);
  }

  public Block currentBlock() {
    return this.currentBlock;
  }

  public void setCurrentBlock(Block block) {
    this.currentBlock = block;
  }

  void writeVariable(Name variable, Block block, IRDataNode value) {
    this.currentDef.computeIfAbsent(variable, _ -> new HashMap<>()).put(block, value);
  }

  IRDataNode readVariable(Name variable, Block block) {
    IRDataNode node = this.currentDef.getOrDefault(variable, Map.of()).get(block);
    if (node != null) {
      if (node instanceof Phi phi && this.sealedBlocks.contains(block)) {
        node = tryRemoveTrivialPhi(phi);
      }

      return node;
    }

    return readVariableRecursive(variable, block);
  }

  private IRDataNode readVariableRecursive(Name variable, Block block) {
    IRDataNode val;

    if (!this.sealedBlocks.contains(block)) {
      Phi phi = new Phi(block, block.entryNode() == null ? currentControlNode : block.entryNode());
      val = phi;
      this.incompletePhis.computeIfAbsent(block, _ -> new HashMap<>()).put(variable, phi);
    } else if (block.controlPredecessors().size() == 1) {
      val = readVariable(variable, block.controlPredecessors().getFirst().block());
    } else {
      Phi phi = new Phi(block, block.entryNode());
      writeVariable(variable, block, phi);
      val = addPhiOperands(variable, phi);
    }

    writeVariable(variable, block, val);
    return val;
  }

  IRDataNode addPhiOperands(Name variable, Phi phi) {
    for (IRNode pred : phi.block().predecessors()) {
      if (pred instanceof IRControlNode) {
        phi.appendOperand(readVariable(variable, pred.block()));
      }
    }

    return tryRemoveTrivialPhi(phi);
  }

  IRDataNode tryRemoveTrivialPhi(Phi phi) {
    IRDataNode dataNode = null;

    for (IRNode predecessor : phi.predecessors()) {
      if (predecessor instanceof IRDataNode dataPredecessor) {
        // Unique value or self−reference
        if (dataPredecessor == dataNode || dataPredecessor == phi) {
          continue;
        }

        // The phi merges at least two values: not trivial
        if (dataNode != null) {
          return phi;
        }

        dataNode = dataPredecessor;
      }
    }

    // Reroute all uses of phi to same and remove phi
    for (IRNode successor : graph().successors(phi)) {
      System.out.println(successor);
      for (int i = 0; i < successor.predecessors().size(); i++) {
        if (successor.predecessor(i) == phi) {
          successor.setPredecessor(i, dataNode);

          // Try to recursively remove all phi users, which might have become trivial
          if (successor instanceof Phi successorPhi
              && sealedBlocks.contains(successorPhi.block())) {
            tryRemoveTrivialPhi(successorPhi);
          }
        }
      }
    }

    // Remove phi from data structures
    for (IRNode predecessor : phi.predecessors()) {
      graph().removeSuccessor(predecessor, phi);
    }
    phi.block().removeNode(phi);

    return dataNode;
  }

  void sealBlock(Block block) {
    for (Map.Entry<Name, Phi> entry :
        this.incompletePhis.getOrDefault(block, Map.of()).entrySet()) {
      addPhiOperands(entry.getKey(), entry.getValue());
    }

    Phi sideEffectPhi = this.incompleteSideEffectPhis.get(block);
    if (sideEffectPhi != null) {
      addPhiOperands(sideEffectPhi);
    }

    this.incompletePhis.remove(block);
    this.sealedBlocks.add(block);
  }

  public void writeCurrentSideEffect(IRDataNode node) {
    writeSideEffect(currentBlock(), node);
  }

  private void writeSideEffect(Block block, IRDataNode node) {
    this.currentSideEffect.put(block, node);
  }

  public IRDataNode readCurrentSideEffect() {
    return readSideEffect(currentBlock());
  }

  private IRDataNode readSideEffect(Block block) {
    IRDataNode node = this.currentSideEffect.get(block);
    if (node != null) {
      return node;
    }

    return readSideEffectRecursive(block);
  }

  private IRDataNode readSideEffectRecursive(Block block) {
    IRDataNode val;

    if (!this.sealedBlocks.contains(block)) {
      Phi phi = new Phi(block, block.entryNode() == null ? currentControlNode : block.entryNode());
      val = phi;

      Phi old = this.incompleteSideEffectPhis.put(block, phi);
      assert old == null : "double readSideEffectRecursive for " + block;
    } else if (block.controlPredecessors().size() == 1) {
      val = readSideEffect(block.controlPredecessors().getFirst().block());
    } else {
      Phi phi = new Phi(block, block.entryNode());
      writeSideEffect(block, phi);
      val = addPhiOperands(phi);
    }

    writeSideEffect(block, val);
    return val;
  }

  IRDataNode addPhiOperands(Phi phi) {
    for (IRNode pred : phi.block().predecessors()) {
      if (pred instanceof IRControlNode) {
        phi.appendOperand(readSideEffect(pred.block()));
      }
    }

    return tryRemoveTrivialPhi(phi);
  }
}
