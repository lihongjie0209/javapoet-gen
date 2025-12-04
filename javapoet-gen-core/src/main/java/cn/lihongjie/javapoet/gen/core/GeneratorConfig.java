package cn.lihongjie.javapoet.gen.core;

/**
 * Configuration options for the JavaPoet code generator.
 */
public class GeneratorConfig {

    /**
     * Whether to generate comments in the output.
     */
    private boolean generateComments = true;

    /**
     * Whether to use static imports for common JavaPoet types.
     */
    private boolean useStaticImports = false;

    /**
     * The indentation string to use.
     */
    private String indent = "    ";

    /**
     * Whether to inline simple field and parameter specs.
     */
    private boolean inlineSimpleSpecs = true;

    /**
     * Whether to preserve original Javadoc comments.
     */
    private boolean preserveJavadoc = true;

    public GeneratorConfig() {
    }

    public boolean isGenerateComments() {
        return generateComments;
    }

    public GeneratorConfig setGenerateComments(boolean generateComments) {
        this.generateComments = generateComments;
        return this;
    }

    public boolean isUseStaticImports() {
        return useStaticImports;
    }

    public GeneratorConfig setUseStaticImports(boolean useStaticImports) {
        this.useStaticImports = useStaticImports;
        return this;
    }

    public String getIndent() {
        return indent;
    }

    public GeneratorConfig setIndent(String indent) {
        this.indent = indent;
        return this;
    }

    public boolean isInlineSimpleSpecs() {
        return inlineSimpleSpecs;
    }

    public GeneratorConfig setInlineSimpleSpecs(boolean inlineSimpleSpecs) {
        this.inlineSimpleSpecs = inlineSimpleSpecs;
        return this;
    }

    public boolean isPreserveJavadoc() {
        return preserveJavadoc;
    }

    public GeneratorConfig setPreserveJavadoc(boolean preserveJavadoc) {
        this.preserveJavadoc = preserveJavadoc;
        return this;
    }
}
