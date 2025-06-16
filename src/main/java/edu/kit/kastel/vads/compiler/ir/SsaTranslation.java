package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.IRControlNode;
import edu.kit.kastel.vads.compiler.ir.node.IRDataNode;
import edu.kit.kastel.vads.compiler.ir.node.IRUndefinedNode;
import edu.kit.kastel.vads.compiler.ir.node.control.IfMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.control.JumpNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopBodyMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopExitMergeNode;
import edu.kit.kastel.vads.compiler.ir.node.control.LoopNode;
import edu.kit.kastel.vads.compiler.ir.node.control.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.data.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.data.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.data.SideEffectNode;
import edu.kit.kastel.vads.compiler.lexer.OperatorKind;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BooleanLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.BreakTree;
import edu.kit.kastel.vads.compiler.parser.ast.ConditionalExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ContinueTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IfTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.NegateTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BinaryOperator;

/// SSA translation as described in
/// [`Simple and Efficient Construction of Static Single Assignment
// Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
///
/// This implementation also tracks side effect edges that can be used to avoid reordering of

// operations that cannot be
/// reordered.
///
/// We recommend to read the paper to better understand the mechanics implemented here.
public final class SsaTranslation {
  public static IRGraph translate(FunctionTree function) {
    SsaTranslation translation = new SsaTranslation(function);
    return translation.translate();
  }

  private static record LoopControlPlacement(IRControlNode predecessor, Block block, int id) {}

  private final FunctionTree function;
  private final IRGraphConstructor constructor;
  private final Stack<List<LoopControlPlacement>> continueStack;
  private final Stack<List<LoopControlPlacement>> breakStack;

  private SsaTranslation(FunctionTree function) {
    this.function = function;
    this.constructor = new IRGraphConstructor(function.name().name().asString());
    this.continueStack = new Stack<>();
    this.breakStack = new Stack<>();
  }

  private IRGraph translate() {
    SsaTranslationVisitor visitor = new SsaTranslationVisitor();
    this.function.accept(visitor, this);
    return this.constructor.graph();
  }

  private void writeVariable(Name variable, Block block, IRDataNode value) {
    this.constructor.writeVariable(variable, block, value);
  }

  private IRDataNode readVariable(Name variable, Block block) {
    return this.constructor.readVariable(variable, block);
  }

  private Block currentBlock() {
    return this.constructor.currentBlock();
  }

  private static record Result(boolean returns, Optional<IRDataNode> data) {

    public Result(boolean returns) {
      this(returns, Optional.empty());
    }

    public Result(IRDataNode data) {
      this(false, Optional.of(data));
    }
  }

  private static class SsaTranslationVisitor implements Visitor<SsaTranslation, Result> {

    @Override
    public Result visit(AssignmentTree assignmentTree, SsaTranslation data) {
      BinaryOperator<IRDataNode> desugar =
          switch (assignmentTree.operator().kind()) {
            case ASSIGN_MINUS -> data.constructor::newSub;
            case ASSIGN_PLUS -> data.constructor::newAdd;
            case ASSIGN_MUL -> data.constructor::newMul;
            case ASSIGN_DIV ->
                (lhs, rhs) -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs));
            case ASSIGN_MOD ->
                (lhs, rhs) -> projResultDivMod(data, data.constructor.newMod(lhs, rhs));
            case ASSIGN_AND -> data.constructor::newBitwiseAnd;
            case ASSIGN_OR -> data.constructor::newBitwiseOr;
            case ASSIGN_XOR -> data.constructor::newBitwiseXOR;
            case ASSIGN_LEFT_SHIFT -> data.constructor::newLeftShift;
            case ASSIGN_RIGHT_SHIFT -> data.constructor::newRightShift;
            case ASSIGN -> null;
            default ->
                throw new IllegalArgumentException(
                    "not an assignment operator " + assignmentTree.operator());
          };

      switch (assignmentTree.lValue()) {
        case LValueIdentTree(var name) -> {
          IRDataNode rhs = assignmentTree.expression().accept(this, data).data().orElseThrow();

          if (desugar != null) {
            rhs = desugar.apply(data.readVariable(name.name(), data.currentBlock()), rhs);
          }

          data.writeVariable(name.name(), data.currentBlock(), rhs);
        }
      }

      return new Result(false);
    }

    @Override
    public Result visit(BinaryOperationTree binaryOperationTree, SsaTranslation data) {
      IRDataNode lhs = null;
      IRDataNode rhs = null;
      if (binaryOperationTree.operatorKind() != OperatorKind.AND
          && binaryOperationTree.operatorKind() != OperatorKind.OR) {
        lhs = binaryOperationTree.lhs().accept(this, data).data().orElseThrow();
        rhs = binaryOperationTree.rhs().accept(this, data).data().orElseThrow();
      }

      IRDataNode res =
          switch (binaryOperationTree.operatorKind()) {
            case PLUS -> data.constructor.newAdd(lhs, rhs);
            case MINUS -> data.constructor.newSub(lhs, rhs);
            case MUL -> data.constructor.newMul(lhs, rhs);
            case DIV -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs));
            case MOD -> projResultDivMod(data, data.constructor.newMod(lhs, rhs));
            case EQUALITY -> data.constructor.newEquality(lhs, rhs);
            case INEQUALITY -> data.constructor.newInequality(lhs, rhs);
            case AND ->
                visit(
                        new ConditionalExpressionTree(
                            binaryOperationTree.lhs(),
                            binaryOperationTree.rhs(),
                            new BooleanLiteralTree(false, null)),
                        data)
                    .data()
                    .get();
            case OR ->
                visit(
                        new ConditionalExpressionTree(
                            binaryOperationTree.lhs(),
                            new BooleanLiteralTree(true, null),
                            binaryOperationTree.rhs()),
                        data)
                    .data()
                    .get();
            case BITWISE_AND -> data.constructor.newBitwiseAnd(lhs, rhs);
            case BITWISE_OR -> data.constructor.newBitwiseOr(lhs, rhs);
            case BITWISE_XOR -> data.constructor.newBitwiseXOR(lhs, rhs);
            case LEFT_SHIFT -> data.constructor.newLeftShift(lhs, rhs);
            case RIGHT_SHIFT -> data.constructor.newRightShift(lhs, rhs);
            case LESS_THAN -> data.constructor.newLessThan(lhs, rhs);
            case LESS_THAN_EQUAL -> data.constructor.newLessThanEqual(lhs, rhs);
            case GREATER_THAN -> data.constructor.newGreaterThan(lhs, rhs);
            case GREATER_THAN_EQUAL -> data.constructor.newGreaterThanEqual(lhs, rhs);
            default ->
                throw new IllegalArgumentException(
                    "not a binary expression operator " + binaryOperationTree.operatorKind());
          };

      return new Result(res);
    }

    @Override
    public Result visit(BlockTree blockTree, SsaTranslation data) {
      for (StatementTree statement : blockTree.statements()) {
        boolean returns = statement.accept(this, data).returns();

        // skip everything after a return in a block
        if (returns) {
          return new Result(true);
        }
      }

      return new Result(false);
    }

    @Override
    public Result visit(BooleanLiteralTree booleanLiteralTree, SsaTranslation data) {
      IRDataNode node = data.constructor.newConstBool(booleanLiteralTree.value());
      return new Result(node);
    }

    @Override
    public Result visit(BreakTree breakTree, SsaTranslation data) {
      IRControlNode controlNode = data.constructor.getCurrentControlNode();
      int id = data.constructor.graph().successors(controlNode).size();

      IRUndefinedNode undefinedNode = data.constructor.newUndefinedNode();
      undefinedNode.addPredecessor(controlNode);

      data.breakStack
          .lastElement()
          .add(new LoopControlPlacement(controlNode, data.currentBlock(), id));
      return new Result(true);
    }

    @Override
    public Result visit(ConditionalExpressionTree conditionalExpressionTree, SsaTranslation data) {
      Name variableName = data.constructor.newFreshVariableName();

      IRDataNode condition =
          conditionalExpressionTree.condition().accept(this, data).data().orElseThrow();
      IfNode branch = data.constructor.newBranch(condition);
      data.constructor.sealBlock(data.currentBlock());

      Block thenBlock = data.constructor.newBlock();
      data.constructor.setCurrentControlNode(branch);
      data.constructor.setCurrentBlock(thenBlock);

      IRDataNode lhs = conditionalExpressionTree.lhs().accept(this, data).data().orElseThrow();
      data.writeVariable(variableName, data.currentBlock(), lhs);

      JumpNode thenJump = data.constructor.newJump();

      Block thenEndBlock = data.constructor.currentBlock();
      data.constructor.sealBlock(thenEndBlock);

      Block elseBlock = data.constructor.newBlock();
      data.constructor.setCurrentControlNode(branch);
      data.constructor.setCurrentBlock(elseBlock);

      IRDataNode rhs = conditionalExpressionTree.rhs().accept(this, data).data().orElseThrow();
      data.writeVariable(variableName, data.currentBlock(), rhs);

      JumpNode elseJump = data.constructor.newJump();

      Block elseEndBlock = data.constructor.currentBlock();
      data.constructor.sealBlock(elseEndBlock);

      Block joinBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(joinBlock);

      IfMergeNode merge = data.constructor.newIfMerge(thenJump, elseJump);
      data.constructor.setCurrentControlNode(merge);

      branch.setMerge(merge);
      return new Result(data.readVariable(variableName, data.currentBlock()));
    }

    @Override
    public Result visit(ContinueTree continueTree, SsaTranslation data) {
      IRControlNode controlNode = data.constructor.getCurrentControlNode();
      int id = data.constructor.graph().successors(controlNode).size();

      IRUndefinedNode undefinedNode = data.constructor.newUndefinedNode();
      undefinedNode.addPredecessor(controlNode);

      data.continueStack
          .lastElement()
          .add(new LoopControlPlacement(controlNode, data.currentBlock(), id));
      return new Result(true);
    }

    @Override
    public Result visit(DeclarationTree declarationTree, SsaTranslation data) {
      if (declarationTree.initializer() != null) {
        IRDataNode rhs = declarationTree.initializer().accept(this, data).data().orElseThrow();
        data.writeVariable(declarationTree.name().name(), data.currentBlock(), rhs);
      }

      return new Result(false);
    }

    @Override
    public Result visit(ForTree forTree, SsaTranslation data) {
      forTree.init().ifPresent(statement -> statement.accept(this, data));

      JumpNode loopHeaderJump = data.constructor.newJump();
      data.constructor.setCurrentControlNode(loopHeaderJump);
      data.constructor.sealBlock(data.currentBlock());

      Block loopHeaderBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopHeaderBlock);

      LoopNode loop = data.constructor.newLoop();
      data.constructor.setCurrentControlNode(loop);

      IRDataNode condition = forTree.condition().accept(this, data).data().orElseThrow();
      loop.addPredecessor(condition);

      Block loopBodyBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopBodyBlock);

      data.breakStack.push(new ArrayList<>());
      data.continueStack.push(new ArrayList<>());

      boolean whileStatementReturns = forTree.statement().accept(this, data).returns();

      Block loopBodyExitBlock = data.constructor.currentBlock();
      JumpNode loopBodyExitJump = data.constructor.newJump();
      data.constructor.setCurrentControlNode(loopBodyExitJump);
      data.constructor.sealBlock(loopBodyExitBlock);

      Block loopEndBlock = data.constructor.newBlock();

      LoopBodyMergeNode bodyMergeNode;
      if (whileStatementReturns) {
        data.constructor.setCurrentBlock(loopEndBlock);
        bodyMergeNode =
            data.continueStack.getLast().isEmpty() ? null : data.constructor.newLoopBodyMerge();
      } else {
        JumpNode loopJump = data.constructor.newJump();
        data.constructor.setCurrentBlock(loopEndBlock);
        bodyMergeNode = data.constructor.newLoopBodyMerge(loopJump);
      }

      if (bodyMergeNode != null) {
        bodyMergeNode.setLoopNode(loop);
        loop.addPredecessor(bodyMergeNode);
        loop.setBodyMerge(bodyMergeNode);

        forTree.step().ifPresent(statement -> statement.accept(this, data));
      }

      Block loopExitBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopExitBlock);

      LoopExitMergeNode exitMergeNode = data.constructor.newLoopEndMerge(loop);
      exitMergeNode.setLoopNode(loop);
      loop.setExitMerge(exitMergeNode);

      for (LoopControlPlacement placement : data.breakStack.pop()) {
        data.constructor.setCurrentBlock(placement.block());
        data.constructor.setCurrentControlNode(placement.predecessor());

        JumpNode jump = data.constructor.newJump(placement.id());
        exitMergeNode.addPredecessor(jump);
      }

      for (LoopControlPlacement placement : data.continueStack.pop()) {
        data.constructor.setCurrentBlock(placement.block());
        data.constructor.setCurrentControlNode(placement.predecessor());

        JumpNode jump = data.constructor.newJump(placement.id());
        bodyMergeNode.addPredecessor(jump);
      }

      data.constructor.sealBlock(loopHeaderBlock);
      data.constructor.sealBlock(loopEndBlock);
      data.constructor.setCurrentBlock(loopExitBlock);
      data.constructor.setCurrentControlNode(exitMergeNode);

      return new Result(false);
    }

    @Override
    public Result visit(FunctionTree functionTree, SsaTranslation data) {
      functionTree.body().accept(this, data);
      data.constructor.sealBlock(data.currentBlock());
      return new Result(true);
    }

    @Override
    public Result visit(IdentExpressionTree identExpressionTree, SsaTranslation data) {
      IRDataNode value = data.readVariable(identExpressionTree.name().name(), data.currentBlock());
      return new Result(value);
    }

    @Override
    public Result visit(IfTree ifTree, SsaTranslation data) {
      // Evaluate the condition in the current block and add branch node to current
      // block
      IRDataNode condition = ifTree.expression().accept(this, data).data().orElseThrow();
      IfNode branch = data.constructor.newBranch(condition);
      data.constructor.sealBlock(data.currentBlock());

      // Create then-block, handle then-statement, and add jump node
      Block thenBlock = data.constructor.newBlock();
      data.constructor.setCurrentControlNode(branch);
      data.constructor.setCurrentBlock(thenBlock);

      boolean thenStatementReturns = ifTree.mainStatement().accept(this, data).returns();
      Block thenEndBlock = data.constructor.currentBlock();
      IRControlNode thenStatementEndNode = data.constructor.getCurrentControlNode();

      if (thenStatementReturns) {
        data.constructor.sealBlock(thenEndBlock);
      }

      // Create else-block, handle else-statement, and add jump node
      Block elseBlock = data.constructor.newBlock();
      data.constructor.setCurrentControlNode(branch);
      data.constructor.setCurrentBlock(elseBlock);

      boolean elseStatementReturns =
          ifTree
              .elseStatement()
              .map(statement -> statement.accept(this, data))
              .map(result -> result.returns())
              .orElseGet(() -> false);
      Block elseEndBlock = data.constructor.currentBlock();
      IRControlNode elseStatementEndNode = data.constructor.getCurrentControlNode();

      if (elseStatementReturns) {
        data.constructor.sealBlock(elseEndBlock);
      }

      // Create merge-block and point jumps to it
      if (thenStatementReturns && elseStatementReturns) {
        return new Result(true);
      }

      if (!thenStatementReturns && !elseStatementReturns) {
        data.constructor.setCurrentBlock(thenEndBlock);
        data.constructor.setCurrentControlNode(thenStatementEndNode);

        JumpNode thenJump = data.constructor.newJump();
        data.constructor.sealBlock(thenEndBlock);

        data.constructor.setCurrentBlock(elseEndBlock);
        data.constructor.setCurrentControlNode(elseStatementEndNode);

        JumpNode elseJump = data.constructor.newJump();
        data.constructor.sealBlock(elseEndBlock);

        Block joinBlock = data.constructor.newBlock();
        data.constructor.setCurrentBlock(joinBlock);

        IfMergeNode merge = data.constructor.newIfMerge(thenJump, elseJump);
        data.constructor.setCurrentControlNode(merge);

        branch.setMerge(merge);
      } else if (elseStatementReturns) {
        data.constructor.setCurrentBlock(thenEndBlock);
        data.constructor.setCurrentControlNode(thenStatementEndNode);
      } else {
        data.constructor.setCurrentBlock(elseEndBlock);
        data.constructor.setCurrentControlNode(elseStatementEndNode);
      }

      return new Result(false);
    }

    @Override
    public Result visit(LiteralTree literalTree, SsaTranslation data) {
      IRDataNode value = data.constructor.newConstInt((int) literalTree.parseValue().orElseThrow());
      return new Result(value);
    }

    @Override
    public Result visit(LValueIdentTree lValueIdentTree, SsaTranslation data) {
      return new Result(false);
    }

    @Override
    public Result visit(NameTree nameTree, SsaTranslation data) {
      return new Result(false);
    }

    @Override
    public Result visit(NegateTree negateTree, SsaTranslation data) {
      IRDataNode node = negateTree.expression().accept(this, data).data().orElseThrow();
      IRDataNode value = data.constructor.newSub(data.constructor.newConstInt(0), node);
      return new Result(value);
    }

    @Override
    public Result visit(ProgramTree programTree, SsaTranslation data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Result visit(ReturnTree returnTree, SsaTranslation data) {
      IRDataNode resultNode = returnTree.expression().accept(this, data).data().orElseThrow();

      ReturnNode ret = data.constructor.newReturn();
      ret.addPredecessor(resultNode);

      data.constructor.sealBlock(data.currentBlock());

      data.constructor.setCurrentControlNode(ret);
      data.constructor.graph().endBlock().entryNode().addPredecessor(ret);

      return new Result(true);
    }

    @Override
    public Result visit(TypeTree typeTree, SsaTranslation data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Result visit(WhileTree whileTree, SsaTranslation data) {
      JumpNode loopHeaderJump = data.constructor.newJump();
      data.constructor.setCurrentControlNode(loopHeaderJump);
      data.constructor.sealBlock(data.currentBlock());

      Block loopHeaderBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopHeaderBlock);

      LoopNode loop = data.constructor.newLoop();
      data.constructor.setCurrentControlNode(loop);

      IRDataNode condition = whileTree.expression().accept(this, data).data().orElseThrow();
      loop.addPredecessor(condition);

      Block loopBodyBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopBodyBlock);

      data.breakStack.push(new ArrayList<>());
      data.continueStack.push(new ArrayList<>());

      boolean whileStatementReturns = whileTree.statement().accept(this, data).returns();

      Block loopBodyExitBlock = data.constructor.currentBlock();
      JumpNode loopBodyExitJump = data.constructor.newJump();
      data.constructor.setCurrentControlNode(loopBodyExitJump);
      data.constructor.sealBlock(loopBodyExitBlock);

      Block loopEndBlock = data.constructor.newBlock();

      LoopBodyMergeNode bodyMergeNode;
      if (whileStatementReturns) {
        bodyMergeNode =
            data.continueStack.getLast().isEmpty() ? null : data.constructor.newLoopBodyMerge();
      } else {
        JumpNode loopJump = data.constructor.newJump();
        bodyMergeNode = data.constructor.newLoopBodyMerge(loopJump);
      }

      if (bodyMergeNode != null) {
        bodyMergeNode.setLoopNode(loop);
        loop.addPredecessor(bodyMergeNode);
        loop.setBodyMerge(bodyMergeNode);
      }

      Block loopExitBlock = data.constructor.newBlock();
      data.constructor.setCurrentBlock(loopExitBlock);

      LoopExitMergeNode exitMergeNode = data.constructor.newLoopEndMerge(loop);
      exitMergeNode.setLoopNode(loop);
      loop.setExitMerge(exitMergeNode);

      for (LoopControlPlacement placement : data.breakStack.pop()) {
        data.constructor.setCurrentBlock(placement.block());
        data.constructor.setCurrentControlNode(placement.predecessor());

        JumpNode jump = data.constructor.newJump(placement.id());
        exitMergeNode.addPredecessor(jump);
      }

      for (LoopControlPlacement placement : data.continueStack.pop()) {
        data.constructor.setCurrentBlock(placement.block());
        data.constructor.setCurrentControlNode(placement.predecessor());

        JumpNode jump = data.constructor.newJump(placement.id());
        bodyMergeNode.addPredecessor(jump);
      }

      data.constructor.sealBlock(loopHeaderBlock);
      data.constructor.setCurrentBlock(loopEndBlock);

      data.constructor.sealBlock(loopEndBlock);
      data.constructor.setCurrentBlock(loopExitBlock);
      data.constructor.setCurrentControlNode(exitMergeNode);

      return new Result(false);
    }

    private IRDataNode projResultDivMod(SsaTranslation data, IRDataNode node) {
      // make sure we actually have a div or a mod, as optimizations could
      // have changed it to something else already
      if (!(node instanceof DivNode || node instanceof ModNode)) {
        return node;
      }

      SideEffectNode projSideEffect = data.constructor.newSideEffectProj(node);
      data.constructor.writeCurrentSideEffect(projSideEffect);
      return projSideEffect;
    }
  }
}
