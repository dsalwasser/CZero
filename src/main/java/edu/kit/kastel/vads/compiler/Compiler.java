package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.TargetMachine;
import edu.kit.kastel.vads.compiler.exception.ParseException;
import edu.kit.kastel.vads.compiler.exception.SemanticException;
import edu.kit.kastel.vads.compiler.ir.IRGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import java.util.ArrayList;
import java.util.List;

public final class Compiler {
  public static Compiler construct(TargetMachine target) {
    return new Compiler(CodeGenerator.construct(target));
  }

  public static Compiler construct(CodeGenerator codeGenerator) {
    return new Compiler(codeGenerator);
  }

  private CodeGenerator codeGenerator;

  private Compiler(CodeGenerator codeGenerator) {
    this.codeGenerator = codeGenerator;
  }

  public String compile(String source) throws ParseException, SemanticException {
    Lexer lexer = new Lexer(source);
    TokenSource tokenSource = new TokenSource(lexer);

    Parser parser = new Parser(tokenSource);
    ProgramTree program = parser.parseProgram();

    SemanticAnalysis semanticAnalyser = new SemanticAnalysis(program);
    semanticAnalyser.analyze();

    List<IRGraph> ir = new ArrayList<>();
    for (FunctionTree function : program.topLevelTrees()) {
      IRGraph irGraph = SsaTranslation.translate(function);
      ir.add(irGraph);
    }

    return codeGenerator.generateCode(ir);
  }
}
