package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.backend.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.TargetMachine;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.ParseException;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.semantic.SemanticException;
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
    Lexer lexer = Lexer.forString(source);
    TokenSource tokenSource = new TokenSource(lexer);

    Parser parser = new Parser(tokenSource);
    ProgramTree program = parser.parseProgram();

    SemanticAnalysis semanticAnalyser = new SemanticAnalysis(program);
    semanticAnalyser.analyze();

    List<IrGraph> ir = new ArrayList<>();
    for (FunctionTree function : program.topLevelTrees()) {
      SsaTranslation translation = new SsaTranslation(function, new LocalValueNumbering());
      ir.add(translation.translate());
    }

    return codeGenerator.generateCode(ir);
  }
}
