package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.backend.TargetMachine;
import edu.kit.kastel.vads.compiler.exception.ParseException;
import edu.kit.kastel.vads.compiler.exception.SemanticException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "CZero",
    version = "1.0",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    exitCodeOnInvalidInput = 3)
public class CZeroCLI implements Callable<Integer> {

  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int IO_EXCEPTION_EXIT_CODE = 4;
  private static final int PARSE_EXCEPTION_EXIT_CODE = 42;
  private static final int SEMANTIC_EXCEPTION_EXIT_CODE = 7;
  private static final int ASSEMBLE_EXCEPTION_EXIT_CODE = 17;

  @Option(
      names = {"-S", "--source"},
      required = true,
      description = "Path to the source file to compile")
  private Path sourcePath;

  @Option(
      names = {"-O", "--output"},
      required = true,
      description =
          "Path to the output file where the compilation result of the source file will be stored")
  private Path outputPath;

  @Option(
      names = {"-T", "--target"},
      defaultValue = "X86",
      description = "Valid targets: ${COMPLETION-CANDIDATES} | Default target: ${DEFAULT-VALUE}")
  private TargetMachine target;

  private String source;

  @Override
  public Integer call() {

    boolean requiresAssembly = target == TargetMachine.X86;
    Path compileOutputPath =
        requiresAssembly ? outputPath.resolveSibling(outputPath.getFileName() + ".s") : outputPath;

    try {
      source = Files.readString(sourcePath);
    } catch (IOException e) {
      System.err.println("Failed to read the source file");
      return IO_EXCEPTION_EXIT_CODE;
    }

    try {
      compile(compileOutputPath);
    } catch (IOException e) {
      System.err.println("Failed to write " + (requiresAssembly ? "assembly" : "output") + " file");
      return IO_EXCEPTION_EXIT_CODE;
    } catch (ParseException e) {
      e.prettyPrint(source);
      return PARSE_EXCEPTION_EXIT_CODE;
    } catch (SemanticException e) {
      e.prettyPrint(source);
      return SEMANTIC_EXCEPTION_EXIT_CODE;
    }

    if (requiresAssembly) {
      try {
        assemble(compileOutputPath, outputPath);
      } catch (IOException | InterruptedException | RuntimeException e) {
        System.err.println("Failed to generate the executable from assembly");
        System.exit(ASSEMBLE_EXCEPTION_EXIT_CODE);
      }
    }

    return SUCCESS_EXIT_CODE;
  }

  private void compile(Path output) throws IOException {
    Compiler compiler = Compiler.construct(target);
    String code = compiler.compile(source);
    Files.writeString(output, code);
  }

  private void assemble(Path input, Path output) throws IOException, InterruptedException {
    List<String> command =
        List.of(
            "gcc", input.toString(),
            "-o", output.toString());

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.inheritIO();

    Process process = processBuilder.start();
    int exitCode = process.waitFor();

    if (exitCode != 0) {
      throw new RuntimeException("GCC failed with exit code: " + exitCode);
    }
  }

  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new CZeroCLI());
    System.exit(commandLine.execute(args));
  }
}
