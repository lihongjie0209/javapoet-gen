package cn.lihongjie.javapoet.gen.cli;

import cn.lihongjie.javapoet.gen.core.GeneratorConfig;
import cn.lihongjie.javapoet.gen.core.JavaPoetGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command-line interface for JavaPoet Generator.
 * 
 * <p>Usage examples:</p>
 * <pre>
 * # Generate JavaPoet code from a single file
 * javapoet-gen MyClass.java
 * 
 * # Generate and save to output file
 * javapoet-gen MyClass.java -o output.java
 * 
 * # Process multiple files
 * javapoet-gen src/main/java/*.java -o generated/
 * </pre>
 */
@Command(
    name = "javapoet-gen",
    mixinStandardHelpOptions = true,
    version = "javapoet-gen 1.0.0",
    description = "Generate JavaPoet code from Java source files"
)
public class JavaPoetGenCli implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "Java source file(s) to process",
        arity = "1..*"
    )
    private Path[] sourceFiles;

    @Option(
        names = {"-o", "--output"},
        description = "Output file or directory. If not specified, output is written to stdout."
    )
    private Path output;

    @Option(
        names = {"-r", "--recursive"},
        description = "Process directories recursively"
    )
    private boolean recursive;

    @Option(
        names = {"--no-comments"},
        description = "Disable comment generation in output"
    )
    private boolean noComments;

    @Option(
        names = {"--no-javadoc"},
        description = "Disable Javadoc preservation in output"
    )
    private boolean noJavadoc;

    @Option(
        names = {"-v", "--verbose"},
        description = "Enable verbose output"
    )
    private boolean verbose;

    @Override
    public Integer call() {
        try {
            GeneratorConfig config = new GeneratorConfig()
                    .setGenerateComments(!noComments)
                    .setPreserveJavadoc(!noJavadoc);

            JavaPoetGenerator generator = new JavaPoetGenerator(config);

            for (Path sourcePath : sourceFiles) {
                processPath(sourcePath, generator);
            }

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private void processPath(Path path, JavaPoetGenerator generator) throws IOException {
        if (Files.isDirectory(path)) {
            processDirectory(path, generator);
        } else if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
            processFile(path, generator);
        } else {
            if (verbose) {
                System.err.println("Skipping: " + path);
            }
        }
    }

    private void processDirectory(Path dir, JavaPoetGenerator generator) throws IOException {
        List<Path> failedFiles = new ArrayList<>();
        try (var stream = recursive ? Files.walk(dir) : Files.list(dir)) {
            stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                  .forEach(p -> {
                      try {
                          processFile(p, generator);
                      } catch (IOException e) {
                          System.err.println("Error processing " + p + ": " + e.getMessage());
                          failedFiles.add(p);
                      }
                  });
        }
        if (!failedFiles.isEmpty() && verbose) {
            System.err.println("Failed to process " + failedFiles.size() + " file(s)");
        }
    }

    private void processFile(Path file, JavaPoetGenerator generator) throws IOException {
        if (verbose) {
            System.err.println("Processing: " + file);
        }

        String generated = generator.generateFromFile(file);

        if (output == null) {
            // Output to stdout
            System.out.println("// Generated from: " + file);
            System.out.println(generated);
            System.out.println();
        } else if (Files.isDirectory(output)) {
            // Output to directory
            String outputFileName = file.getFileName().toString()
                    .replace(".java", "Generator.java");
            Path outputFile = output.resolve(outputFileName);
            Files.writeString(outputFile, generated);
            if (verbose) {
                System.err.println("Written: " + outputFile);
            }
        } else {
            // Output to single file (append if multiple sources)
            if (sourceFiles.length > 1) {
                String content = Files.exists(output) ? Files.readString(output) : "";
                content += "\n// Generated from: " + file + "\n" + generated + "\n";
                Files.writeString(output, content);
            } else {
                Files.writeString(output, generated);
            }
            if (verbose) {
                System.err.println("Written: " + output);
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JavaPoetGenCli()).execute(args);
        System.exit(exitCode);
    }
}
