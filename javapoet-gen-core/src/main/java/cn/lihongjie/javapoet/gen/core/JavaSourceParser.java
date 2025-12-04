package cn.lihongjie.javapoet.gen.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Parser for Java source files using JavaParser library.
 * Converts Java source code into an AST (Abstract Syntax Tree) that can be
 * further processed to generate JavaPoet code.
 */
public class JavaSourceParser {

    private static final Logger logger = LoggerFactory.getLogger(JavaSourceParser.class);

    private final JavaParser javaParser;

    public JavaSourceParser() {
        this.javaParser = new JavaParser();
    }

    /**
     * Parse a Java source file from the given path.
     *
     * @param sourcePath the path to the Java source file
     * @return the parsed CompilationUnit
     * @throws JavaPoetGenException if parsing fails
     */
    public CompilationUnit parse(Path sourcePath) {
        logger.debug("Parsing Java source file: {}", sourcePath);

        try {
            String sourceCode = Files.readString(sourcePath);
            return parse(sourceCode, sourcePath.getFileName().toString());
        } catch (IOException e) {
            throw new JavaPoetGenException("Failed to read source file: " + sourcePath, e);
        }
    }

    /**
     * Parse Java source code from a string.
     *
     * @param sourceCode the Java source code
     * @param sourceName the name of the source (for error messages)
     * @return the parsed CompilationUnit
     * @throws JavaPoetGenException if parsing fails
     */
    public CompilationUnit parse(String sourceCode, String sourceName) {
        logger.debug("Parsing Java source: {}", sourceName);

        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);

        if (!result.isSuccessful()) {
            String errors = result.getProblems().stream()
                    .map(problem -> problem.getMessage())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("Unknown parsing error");
            throw new JavaPoetGenException("Failed to parse " + sourceName + ":\n" + errors);
        }

        Optional<CompilationUnit> cuOpt = result.getResult();
        if (cuOpt.isEmpty()) {
            throw new JavaPoetGenException("Parsing returned empty result for: " + sourceName);
        }

        logger.debug("Successfully parsed: {}", sourceName);
        return cuOpt.get();
    }

    /**
     * Parse Java source code from a string with a default source name.
     *
     * @param sourceCode the Java source code
     * @return the parsed CompilationUnit
     * @throws JavaPoetGenException if parsing fails
     */
    public CompilationUnit parse(String sourceCode) {
        return parse(sourceCode, "<inline>");
    }
}
