package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.visitor.RecursivePostorderVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.semantic.analyser.IntegerLiteralRangeAnalysis;
import edu.kit.kastel.vads.compiler.semantic.analyser.LoopAnalysis;
import edu.kit.kastel.vads.compiler.semantic.analyser.ReturnAnalysis;
import edu.kit.kastel.vads.compiler.semantic.analyser.TypeAnalysis;
import edu.kit.kastel.vads.compiler.semantic.analyser.VariableStatusAnalysis;

public class SemanticAnalysis {

  private final ProgramTree program;

  public SemanticAnalysis(ProgramTree program) {
    this.program = program;
  }

  public void analyze() {
    runAnalyzer(new ReturnAnalysis(), null);
    runAnalyzer(new LoopAnalysis(), new LoopAnalysis.LoopStatus());
    runAnalyzer(new VariableStatusAnalysis(), new VariableStatusAnalysis.VariableStatus());
    runAnalyzer(
        new RecursivePostorderVisitor<>(new TypeAnalysis()), new TypeAnalysis.BlockNamespace<>());
    runAnalyzer(new RecursivePostorderVisitor<>(new IntegerLiteralRangeAnalysis()), Unit.INSTANCE);
  }

  private <T, R> void runAnalyzer(Visitor<T, R> visitor, T t) {
    this.program.accept(visitor, t);
  }
}
