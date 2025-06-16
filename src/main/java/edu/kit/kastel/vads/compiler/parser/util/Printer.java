package edu.kit.kastel.vads.compiler.parser.util;

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
import edu.kit.kastel.vads.compiler.parser.ast.Tree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import java.util.List;

/// This is a utility class to help with debugging the parser.
public class Printer {

  private final Tree ast;
  private final StringBuilder builder = new StringBuilder();
  private boolean requiresIndent;
  private int indentDepth;

  public Printer(Tree ast) {
    this.ast = ast;
  }

  public static String print(Tree ast) {
    Printer printer = new Printer(ast);
    printer.printRoot();
    return printer.builder.toString();
  }

  private void printRoot() {
    printTree(this.ast);
  }

  private void printTree(Tree tree) {
    switch (tree) {
      case BlockTree(List<StatementTree> statements, _) -> {
        print("{");
        lineBreak();
        this.indentDepth++;
        for (StatementTree statement : statements) {
          printTree(statement);
        }
        this.indentDepth--;
        print("}");
      }
      case BreakTree(var _) -> {
        print("break;");
      }
      case BooleanLiteralTree(var value, _) -> {
        print(value ? "true" : "false");
      }
      case ConditionalExpressionTree(var condition, var lhs, var rhs) -> {
        print(condition);
        print(" ? ");
        print(lhs);
        print(" : ");
        print(rhs);
      }
      case ContinueTree(var _) -> {
        print("continue;");
      }
      case FunctionTree(var returnType, var name, var body) -> {
        printTree(returnType);
        space();
        printTree(name);
        print("()");
        space();
        printTree(body);
      }
      case ForTree(var init, var condition, var step, var statement, var _) -> {
        print("for (");
        init.ifPresent(initStatement -> print(initStatement));

        print("; ");
        print(condition);

        print("; ");
        step.ifPresent(stepStatement -> print(stepStatement));

        print(")");
        lineBreak();

        print(statement);
      }
      case IfTree(var expression, var main, var elseStatement, var _) -> {
        print("if (");
        print(expression);

        print(") ");
        print(main);

        elseStatement.ifPresent(
            statement -> {
              print(" else ");
              print(statement);
            });
      }
      case WhileTree(var expression, var statement, var _) -> {
        print("while (");
        print(expression);

        print(")");
        print(statement);
      }
      case NameTree(var name, _) -> print(name.asString());
      case ProgramTree(var topLevelTrees) -> {
        for (FunctionTree function : topLevelTrees) {
          printTree(function);
          lineBreak();
        }
      }
      case TypeTree(var type, _) -> print(type.asString());
      case BinaryOperationTree(var lhs, var rhs, var op) -> {
        print("(");
        printTree(lhs);
        print(")");
        space();
        this.builder.append(op);
        space();
        print("(");
        printTree(rhs);
        print(")");
      }
      case LiteralTree(var value, _, _) -> this.builder.append(value);
      case NegateTree(var kind, var expression, _) -> {
        switch (kind) {
          case NegateTree.Kind.LOGICAL -> print("!");
          case NegateTree.Kind.BITWISE -> print("~");
          case NegateTree.Kind.NUMERICAL -> print("-");
        }
        print("(");
        printTree(expression);
        print(")");
      }
      case AssignmentTree(var lValue, var op, var expression) -> {
        printTree(lValue);
        space();
        this.builder.append(op);
        space();
        printTree(expression);
        semicolon();
      }
      case DeclarationTree(var type, var name, var initializer) -> {
        printTree(type);
        space();
        printTree(name);
        if (initializer != null) {
          print(" = ");
          printTree(initializer);
        }
        semicolon();
      }
      case ReturnTree(var expr, _) -> {
        print("return ");
        printTree(expr);
        semicolon();
      }
      case LValueIdentTree(var name) -> printTree(name);
      case IdentExpressionTree(var name) -> printTree(name);
    }
  }

  private void print(String str) {
    if (this.requiresIndent) {
      this.requiresIndent = false;
      this.builder.append(" ".repeat(4 * this.indentDepth));
    }
    this.builder.append(str);
  }

  private void lineBreak() {
    this.builder.append("\n");
    this.requiresIndent = true;
  }

  private void semicolon() {
    this.builder.append(";");
    lineBreak();
  }

  private void space() {
    this.builder.append(" ");
  }
}
