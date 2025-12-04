package cn.lihongjie.javapoet.gen.core;

import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main entry point for generating JavaPoet code from Java source files.
 * This class provides a simple API for converting Java source code into
 * equivalent JavaPoet API calls.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * JavaPoetGenerator generator = new JavaPoetGenerator();
 * String javaPoetCode = generator.generateFromSource("""
 *     public class HelloWorld {
 *         public static void main(String[] args) {
 *             System.out.println("Hello, World!");
 *         }
 *     }
 *     """);
 * System.out.println(javaPoetCode);
 * }</pre>
 */
public class JavaPoetGenerator {

    private final JavaSourceParser parser;
    private final JavaPoetCodeGenerator codeGenerator;

    /**
     * Create a new JavaPoetGenerator with default configuration.
     */
    public JavaPoetGenerator() {
        this(new GeneratorConfig());
    }

    /**
     * Create a new JavaPoetGenerator with custom configuration.
     *
     * @param config the generator configuration
     */
    public JavaPoetGenerator(GeneratorConfig config) {
        this.parser = new JavaSourceParser();
        this.codeGenerator = new JavaPoetCodeGenerator(config);
    }

    /**
     * Generate JavaPoet code from a Java source file.
     *
     * @param sourcePath the path to the Java source file
     * @return the generated JavaPoet code
     * @throws JavaPoetGenException if generation fails
     */
    public String generateFromFile(Path sourcePath) {
        CompilationUnit cu = parser.parse(sourcePath);
        return codeGenerator.generate(cu);
    }

    /**
     * Generate JavaPoet code from a Java source string.
     *
     * @param sourceCode the Java source code
     * @return the generated JavaPoet code
     * @throws JavaPoetGenException if generation fails
     */
    public String generateFromSource(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new JavaPoetGenException("Source code cannot be null or blank");
        }
        CompilationUnit cu = parser.parse(sourceCode);
        return codeGenerator.generate(cu);
    }

    /**
     * Generate JavaPoet code from a Java source string with a custom source name.
     *
     * @param sourceCode the Java source code
     * @param sourceName the name of the source (for error messages)
     * @return the generated JavaPoet code
     * @throws JavaPoetGenException if generation fails
     */
    public String generateFromSource(String sourceCode, String sourceName) {
        CompilationUnit cu = parser.parse(sourceCode, sourceName);
        return codeGenerator.generate(cu);
    }

    /**
     * Generate JavaPoet code from a Java source file and write it to an output file.
     *
     * @param sourcePath the path to the Java source file
     * @param outputPath the path to write the generated code to
     * @throws JavaPoetGenException if generation fails
     * @throws IOException if writing fails
     */
    public void generateToFile(Path sourcePath, Path outputPath) throws IOException {
        String generatedCode = generateFromFile(sourcePath);
        Files.writeString(outputPath, generatedCode);
    }

    /**
     * Generate JavaPoet code from a Java source string and write it to an output file.
     *
     * @param sourceCode the Java source code
     * @param outputPath the path to write the generated code to
     * @throws JavaPoetGenException if generation fails
     * @throws IOException if writing fails
     */
    public void generateToFile(String sourceCode, Path outputPath) throws IOException {
        String generatedCode = generateFromSource(sourceCode);
        Files.writeString(outputPath, generatedCode);
    }
}
