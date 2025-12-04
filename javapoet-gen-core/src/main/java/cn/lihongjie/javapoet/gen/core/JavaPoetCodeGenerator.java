package cn.lihongjie.javapoet.gen.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates JavaPoet API code from a parsed Java AST.
 * This class converts JavaParser AST nodes into equivalent JavaPoet builder code.
 */
public class JavaPoetCodeGenerator {

    private final GeneratorConfig config;
    private final Set<String> usedImports = new LinkedHashSet<>();

    public JavaPoetCodeGenerator() {
        this(new GeneratorConfig());
    }

    public JavaPoetCodeGenerator(GeneratorConfig config) {
        this.config = config;
    }

    /**
     * Generate JavaPoet code from a CompilationUnit.
     *
     * @param cu the parsed CompilationUnit
     * @return the generated JavaPoet code as a string
     */
    public String generate(CompilationUnit cu) {
        usedImports.clear();
        StringBuilder sb = new StringBuilder();

        // Add standard JavaPoet imports
        addJavaPoetImports();

        // Process each type declaration
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type instanceof ClassOrInterfaceDeclaration classDecl) {
                sb.append(generateTypeSpec(classDecl, cu));
            } else if (type instanceof EnumDeclaration enumDecl) {
                sb.append(generateEnumSpec(enumDecl, cu));
            }
        }

        // Build final output with imports
        StringBuilder result = new StringBuilder();
        result.append(generateImports());
        result.append("\n");
        result.append(sb);

        return result.toString();
    }

    private void addJavaPoetImports() {
        usedImports.add("com.squareup.javapoet.JavaFile");
        usedImports.add("com.squareup.javapoet.TypeSpec");
        usedImports.add("com.squareup.javapoet.MethodSpec");
        usedImports.add("com.squareup.javapoet.FieldSpec");
        usedImports.add("com.squareup.javapoet.ParameterSpec");
        usedImports.add("com.squareup.javapoet.AnnotationSpec");
        usedImports.add("com.squareup.javapoet.ClassName");
        usedImports.add("com.squareup.javapoet.TypeName");
        usedImports.add("com.squareup.javapoet.ParameterizedTypeName");
        usedImports.add("com.squareup.javapoet.CodeBlock");
        usedImports.add("javax.lang.model.element.Modifier");
    }

    private String generateImports() {
        StringBuilder sb = new StringBuilder();
        for (String imp : usedImports) {
            sb.append("import ").append(imp).append(";\n");
        }
        return sb.toString();
    }

    /**
     * Generate TypeSpec code for a class or interface.
     */
    private String generateTypeSpec(ClassOrInterfaceDeclaration classDecl, CompilationUnit cu) {
        StringBuilder sb = new StringBuilder();
        String className = classDecl.getNameAsString();
        String varName = toLowerCamelCase(className) + "Spec";

        sb.append("// Generated JavaPoet code for: ").append(className).append("\n\n");

        // Generate field specs first
        List<String> fieldVarNames = new ArrayList<>();
        for (FieldDeclaration field : classDecl.getFields()) {
            for (VariableDeclarator var : field.getVariables()) {
                String fieldVarName = var.getNameAsString() + "Field";
                fieldVarNames.add(fieldVarName);
                sb.append(generateFieldSpec(field, var, fieldVarName));
                sb.append("\n");
            }
        }

        // Generate method specs
        List<String> methodVarNames = new ArrayList<>();
        for (MethodDeclaration method : classDecl.getMethods()) {
            String methodVarName = method.getNameAsString() + "Method";
            // Handle overloaded methods
            int count = (int) methodVarNames.stream().filter(n -> n.startsWith(method.getNameAsString())).count();
            if (count > 0) {
                methodVarName = method.getNameAsString() + count + "Method";
            }
            methodVarNames.add(methodVarName);
            sb.append(generateMethodSpec(method, methodVarName));
            sb.append("\n");
        }

        // Generate constructor specs
        List<String> constructorVarNames = new ArrayList<>();
        int constructorIndex = 0;
        for (ConstructorDeclaration constructor : classDecl.getConstructors()) {
            String constructorVarName = "constructor" + (constructorIndex > 0 ? constructorIndex : "");
            constructorVarNames.add(constructorVarName);
            sb.append(generateConstructorSpec(constructor, constructorVarName));
            sb.append("\n");
            constructorIndex++;
        }

        // Generate TypeSpec
        sb.append("TypeSpec ").append(varName).append(" = TypeSpec");

        if (classDecl.isInterface()) {
            sb.append(".interfaceBuilder(\"").append(className).append("\")\n");
        } else {
            sb.append(".classBuilder(\"").append(className).append("\")\n");
        }

        // Add modifiers
        String modifiers = generateModifiers(classDecl.getModifiers());
        if (!modifiers.isEmpty()) {
            sb.append("    .addModifiers(").append(modifiers).append(")\n");
        }

        // Add superclass
        classDecl.getExtendedTypes().forEach(extendedType -> {
            sb.append("    .superclass(").append(generateTypeName(extendedType)).append(")\n");
        });

        // Add interfaces
        classDecl.getImplementedTypes().forEach(implementedType -> {
            sb.append("    .addSuperinterface(").append(generateTypeName(implementedType)).append(")\n");
        });

        // Add annotations
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            sb.append("    .addAnnotation(").append(generateAnnotationSpec(annotation)).append(")\n");
        }

        // Add fields
        for (String fieldVarName : fieldVarNames) {
            sb.append("    .addField(").append(fieldVarName).append(")\n");
        }

        // Add constructors
        for (String constructorVarName : constructorVarNames) {
            sb.append("    .addMethod(").append(constructorVarName).append(")\n");
        }

        // Add methods
        for (String methodVarName : methodVarNames) {
            sb.append("    .addMethod(").append(methodVarName).append(")\n");
        }

        // Add Javadoc if present
        classDecl.getJavadocComment().ifPresent(javadoc -> {
            sb.append("    .addJavadoc(").append(escapeString(javadoc.getContent())).append(")\n");
        });

        sb.append("    .build();\n\n");

        // Generate JavaFile
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        sb.append("JavaFile javaFile = JavaFile.builder(\"").append(packageName).append("\", ").append(varName).append(")\n");
        sb.append("    .build();\n");

        return sb.toString();
    }

    /**
     * Generate FieldSpec code.
     */
    private String generateFieldSpec(FieldDeclaration field, VariableDeclarator var, String varName) {
        StringBuilder sb = new StringBuilder();

        sb.append("FieldSpec ").append(varName).append(" = FieldSpec.builder(");
        sb.append(generateTypeName(var.getType())).append(", ");
        sb.append("\"").append(var.getNameAsString()).append("\"");

        // Add modifiers inline if simple
        String modifiers = generateModifiers(field.getModifiers());
        if (!modifiers.isEmpty()) {
            sb.append(", ").append(modifiers);
        }
        sb.append(")\n");

        // Add initializer if present
        var.getInitializer().ifPresent(init -> {
            sb.append("    .initializer(").append(generateCodeBlockForExpression(init)).append(")\n");
        });

        // Add annotations
        for (AnnotationExpr annotation : field.getAnnotations()) {
            sb.append("    .addAnnotation(").append(generateAnnotationSpec(annotation)).append(")\n");
        }

        // Add Javadoc if present
        field.getJavadocComment().ifPresent(javadoc -> {
            sb.append("    .addJavadoc(").append(escapeString(javadoc.getContent())).append(")\n");
        });

        sb.append("    .build();\n");

        return sb.toString();
    }

    /**
     * Generate MethodSpec code.
     */
    private String generateMethodSpec(MethodDeclaration method, String varName) {
        StringBuilder sb = new StringBuilder();

        sb.append("MethodSpec ").append(varName).append(" = MethodSpec.methodBuilder(\"");
        sb.append(method.getNameAsString()).append("\")\n");

        // Add modifiers
        String modifiers = generateModifiers(method.getModifiers());
        if (!modifiers.isEmpty()) {
            sb.append("    .addModifiers(").append(modifiers).append(")\n");
        }

        // Add return type
        sb.append("    .returns(").append(generateTypeName(method.getType())).append(")\n");

        // Add parameters
        for (Parameter param : method.getParameters()) {
            sb.append("    .addParameter(").append(generateParameterSpec(param)).append(")\n");
        }

        // Add type parameters (generics)
        method.getTypeParameters().forEach(tp -> {
            sb.append("    .addTypeVariable(com.squareup.javapoet.TypeVariableName.get(\"")
              .append(tp.getNameAsString()).append("\"))\n");
            usedImports.add("com.squareup.javapoet.TypeVariableName");
        });

        // Add thrown exceptions
        for (var thrownType : method.getThrownExceptions()) {
            sb.append("    .addException(").append(generateTypeName(thrownType)).append(")\n");
        }

        // Add annotations
        for (AnnotationExpr annotation : method.getAnnotations()) {
            sb.append("    .addAnnotation(").append(generateAnnotationSpec(annotation)).append(")\n");
        }

        // Add method body
        method.getBody().ifPresent(body -> {
            for (Statement stmt : body.getStatements()) {
                sb.append(generateStatement(stmt));
            }
        });

        // Add Javadoc if present
        method.getJavadocComment().ifPresent(javadoc -> {
            sb.append("    .addJavadoc(").append(escapeString(javadoc.getContent())).append(")\n");
        });

        sb.append("    .build();\n");

        return sb.toString();
    }

    /**
     * Generate MethodSpec code for a constructor.
     */
    private String generateConstructorSpec(ConstructorDeclaration constructor, String varName) {
        StringBuilder sb = new StringBuilder();

        sb.append("MethodSpec ").append(varName).append(" = MethodSpec.constructorBuilder()\n");

        // Add modifiers
        String modifiers = generateModifiers(constructor.getModifiers());
        if (!modifiers.isEmpty()) {
            sb.append("    .addModifiers(").append(modifiers).append(")\n");
        }

        // Add parameters
        for (Parameter param : constructor.getParameters()) {
            sb.append("    .addParameter(").append(generateParameterSpec(param)).append(")\n");
        }

        // Add thrown exceptions
        for (var thrownType : constructor.getThrownExceptions()) {
            sb.append("    .addException(").append(generateTypeName(thrownType)).append(")\n");
        }

        // Add annotations
        for (AnnotationExpr annotation : constructor.getAnnotations()) {
            sb.append("    .addAnnotation(").append(generateAnnotationSpec(annotation)).append(")\n");
        }

        // Add constructor body
        constructor.getBody().getStatements().forEach(stmt -> {
            sb.append(generateStatement(stmt));
        });

        // Add Javadoc if present
        constructor.getJavadocComment().ifPresent(javadoc -> {
            sb.append("    .addJavadoc(").append(escapeString(javadoc.getContent())).append(")\n");
        });

        sb.append("    .build();\n");

        return sb.toString();
    }

    /**
     * Generate code for enum types.
     */
    private String generateEnumSpec(EnumDeclaration enumDecl, CompilationUnit cu) {
        StringBuilder sb = new StringBuilder();
        String enumName = enumDecl.getNameAsString();
        String varName = toLowerCamelCase(enumName) + "Spec";

        sb.append("// Generated JavaPoet code for enum: ").append(enumName).append("\n\n");

        sb.append("TypeSpec ").append(varName).append(" = TypeSpec.enumBuilder(\"")
          .append(enumName).append("\")\n");

        // Add modifiers
        String modifiers = generateModifiers(enumDecl.getModifiers());
        if (!modifiers.isEmpty()) {
            sb.append("    .addModifiers(").append(modifiers).append(")\n");
        }

        // Add enum constants
        for (EnumConstantDeclaration constant : enumDecl.getEntries()) {
            if (constant.getArguments().isEmpty()) {
                sb.append("    .addEnumConstant(\"").append(constant.getNameAsString()).append("\")\n");
            } else {
                sb.append("    .addEnumConstant(\"").append(constant.getNameAsString())
                  .append("\", TypeSpec.anonymousClassBuilder(");
                String args = constant.getArguments().stream()
                        .map(this::expressionToString)
                        .collect(Collectors.joining(", "));
                sb.append(escapeString(args)).append(").build())\n");
            }
        }

        // Add interfaces
        enumDecl.getImplementedTypes().forEach(implementedType -> {
            sb.append("    .addSuperinterface(").append(generateTypeName(implementedType)).append(")\n");
        });

        sb.append("    .build();\n\n");

        // Generate JavaFile
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        sb.append("JavaFile javaFile = JavaFile.builder(\"").append(packageName).append("\", ")
          .append(varName).append(")\n");
        sb.append("    .build();\n");

        return sb.toString();
    }

    /**
     * Generate parameter spec code.
     */
    private String generateParameterSpec(Parameter param) {
        StringBuilder sb = new StringBuilder();

        boolean hasAnnotations = !param.getAnnotations().isEmpty();
        boolean isFinal = param.isFinal();

        if (hasAnnotations || isFinal) {
            sb.append("ParameterSpec.builder(").append(generateTypeName(param.getType()))
              .append(", \"").append(param.getNameAsString()).append("\")");

            if (isFinal) {
                sb.append(".addModifiers(Modifier.FINAL)");
            }

            for (AnnotationExpr annotation : param.getAnnotations()) {
                sb.append(".addAnnotation(").append(generateAnnotationSpec(annotation)).append(")");
            }

            sb.append(".build()");
        } else {
            sb.append(generateTypeName(param.getType())).append(", \"")
              .append(param.getNameAsString()).append("\"");
        }

        return sb.toString();
    }

    /**
     * Generate annotation spec code.
     */
    private String generateAnnotationSpec(AnnotationExpr annotation) {
        StringBuilder sb = new StringBuilder();

        String annotationName = annotation.getNameAsString();

        // Check if it's a well-known annotation
        String annotationType = resolveAnnotationType(annotationName);

        if (annotation instanceof MarkerAnnotationExpr) {
            sb.append("AnnotationSpec.builder(").append(annotationType).append(").build()");
        } else if (annotation instanceof SingleMemberAnnotationExpr single) {
            sb.append("AnnotationSpec.builder(").append(annotationType).append(")")
              .append(".addMember(\"value\", ")
              .append(generateAnnotationValue(single.getMemberValue()))
              .append(").build()");
        } else if (annotation instanceof NormalAnnotationExpr normal) {
            sb.append("AnnotationSpec.builder(").append(annotationType).append(")");
            for (MemberValuePair pair : normal.getPairs()) {
                sb.append(".addMember(\"").append(pair.getNameAsString()).append("\", ")
                  .append(generateAnnotationValue(pair.getValue())).append(")");
            }
            sb.append(".build()");
        }

        return sb.toString();
    }

    private String resolveAnnotationType(String annotationName) {
        // Common annotations mapping
        Map<String, String> commonAnnotations = Map.of(
                "Override", "Override.class",
                "Deprecated", "Deprecated.class",
                "SuppressWarnings", "SuppressWarnings.class",
                "FunctionalInterface", "FunctionalInterface.class",
                "SafeVarargs", "SafeVarargs.class"
        );

        return commonAnnotations.getOrDefault(annotationName,
                "ClassName.get(\"\", \"" + annotationName + "\")");
    }

    private String generateAnnotationValue(Expression expr) {
        if (expr instanceof StringLiteralExpr strLit) {
            return "\"$S\", " + escapeString(strLit.getValue());
        } else if (expr instanceof ClassExpr classExpr) {
            return "\"$T.class\", " + generateTypeName(classExpr.getType());
        } else if (expr instanceof ArrayInitializerExpr arrayInit) {
            return escapeString("{" + arrayInit.getValues().stream()
                    .map(this::expressionToString)
                    .collect(Collectors.joining(", ")) + "}");
        } else {
            return escapeString(expressionToString(expr));
        }
    }

    /**
     * Generate TypeName code for a given type.
     */
    private String generateTypeName(Type type) {
        if (type instanceof VoidType) {
            return "TypeName.VOID";
        } else if (type instanceof PrimitiveType primitiveType) {
            return switch (primitiveType.getType()) {
                case BOOLEAN -> "TypeName.BOOLEAN";
                case BYTE -> "TypeName.BYTE";
                case CHAR -> "TypeName.CHAR";
                case DOUBLE -> "TypeName.DOUBLE";
                case FLOAT -> "TypeName.FLOAT";
                case INT -> "TypeName.INT";
                case LONG -> "TypeName.LONG";
                case SHORT -> "TypeName.SHORT";
            };
        } else if (type instanceof ClassOrInterfaceType classType) {
            String typeName = classType.getNameAsString();

            // Handle type arguments (generics)
            if (classType.getTypeArguments().isPresent()) {
                NodeList<Type> typeArgs = classType.getTypeArguments().get();
                String baseType = resolveClassName(typeName);
                String args = typeArgs.stream()
                        .map(this::generateTypeName)
                        .collect(Collectors.joining(", "));
                return "ParameterizedTypeName.get(" + baseType + ", " + args + ")";
            }

            return resolveClassName(typeName);
        } else if (type.isArrayType()) {
            return "com.squareup.javapoet.ArrayTypeName.of(" +
                   generateTypeName(type.asArrayType().getComponentType()) + ")";
        }

        return "ClassName.get(\"\", \"" + type.asString() + "\")";
    }

    private String resolveClassName(String typeName) {
        // Map common types to their class literals
        Map<String, String> commonTypes = Map.ofEntries(
                Map.entry("String", "String.class"),
                Map.entry("Object", "Object.class"),
                Map.entry("Integer", "Integer.class"),
                Map.entry("Long", "Long.class"),
                Map.entry("Double", "Double.class"),
                Map.entry("Float", "Float.class"),
                Map.entry("Boolean", "Boolean.class"),
                Map.entry("Byte", "Byte.class"),
                Map.entry("Short", "Short.class"),
                Map.entry("Character", "Character.class"),
                Map.entry("List", "java.util.List.class"),
                Map.entry("ArrayList", "java.util.ArrayList.class"),
                Map.entry("Map", "java.util.Map.class"),
                Map.entry("HashMap", "java.util.HashMap.class"),
                Map.entry("Set", "java.util.Set.class"),
                Map.entry("HashSet", "java.util.HashSet.class"),
                Map.entry("Collection", "java.util.Collection.class"),
                Map.entry("Optional", "java.util.Optional.class"),
                Map.entry("Stream", "java.util.stream.Stream.class")
        );

        return commonTypes.getOrDefault(typeName,
                "ClassName.get(\"\", \"" + typeName + "\")");
    }

    /**
     * Generate code for modifiers.
     */
    private String generateModifiers(NodeList<Modifier> modifiers) {
        if (modifiers.isEmpty()) {
            return "";
        }

        return modifiers.stream()
                .map(mod -> "Modifier." + mod.getKeyword().name())
                .collect(Collectors.joining(", "));
    }

    /**
     * Generate statement code.
     */
    private String generateStatement(Statement stmt) {
        StringBuilder sb = new StringBuilder();

        if (stmt instanceof ReturnStmt returnStmt) {
            if (returnStmt.getExpression().isPresent()) {
                sb.append("    .addStatement(\"return ")
                  .append(escapeForStatement(expressionToString(returnStmt.getExpression().get())))
                  .append("\")\n");
            } else {
                sb.append("    .addStatement(\"return\")\n");
            }
        } else if (stmt instanceof ExpressionStmt exprStmt) {
            sb.append("    .addStatement(")
              .append(escapeString(expressionToString(exprStmt.getExpression())))
              .append(")\n");
        } else if (stmt instanceof IfStmt ifStmt) {
            sb.append("    .beginControlFlow(\"if (")
              .append(escapeForStatement(expressionToString(ifStmt.getCondition())))
              .append(")\")\n");
            if (ifStmt.getThenStmt() instanceof BlockStmt block) {
                for (Statement s : block.getStatements()) {
                    sb.append(generateStatement(s));
                }
            } else {
                sb.append(generateStatement(ifStmt.getThenStmt()));
            }
            if (ifStmt.getElseStmt().isPresent()) {
                Statement elseStmt = ifStmt.getElseStmt().get();
                if (elseStmt instanceof IfStmt) {
                    sb.append("    .nextControlFlow(\"else if (...)\")\n");
                } else {
                    sb.append("    .nextControlFlow(\"else\")\n");
                    if (elseStmt instanceof BlockStmt block) {
                        for (Statement s : block.getStatements()) {
                            sb.append(generateStatement(s));
                        }
                    } else {
                        sb.append(generateStatement(elseStmt));
                    }
                }
            }
            sb.append("    .endControlFlow()\n");
        } else if (stmt instanceof ForStmt forStmt) {
            String init = forStmt.getInitialization().stream()
                    .map(this::expressionToString)
                    .collect(Collectors.joining(", "));
            String compare = forStmt.getCompare()
                    .map(this::expressionToString)
                    .orElse("");
            String update = forStmt.getUpdate().stream()
                    .map(this::expressionToString)
                    .collect(Collectors.joining(", "));
            sb.append("    .beginControlFlow(\"for (")
              .append(escapeForStatement(init)).append("; ")
              .append(escapeForStatement(compare)).append("; ")
              .append(escapeForStatement(update)).append(")\")\n");
            if (forStmt.getBody() instanceof BlockStmt block) {
                for (Statement s : block.getStatements()) {
                    sb.append(generateStatement(s));
                }
            } else {
                sb.append(generateStatement(forStmt.getBody()));
            }
            sb.append("    .endControlFlow()\n");
        } else if (stmt instanceof ForEachStmt forEachStmt) {
            sb.append("    .beginControlFlow(\"for ($T ")
              .append(forEachStmt.getVariable().getVariables().get(0).getNameAsString())
              .append(" : ")
              .append(escapeForStatement(expressionToString(forEachStmt.getIterable())))
              .append(")\", ")
              .append(generateTypeName(forEachStmt.getVariable().getCommonType()))
              .append(")\n");
            if (forEachStmt.getBody() instanceof BlockStmt block) {
                for (Statement s : block.getStatements()) {
                    sb.append(generateStatement(s));
                }
            } else {
                sb.append(generateStatement(forEachStmt.getBody()));
            }
            sb.append("    .endControlFlow()\n");
        } else if (stmt instanceof WhileStmt whileStmt) {
            sb.append("    .beginControlFlow(\"while (")
              .append(escapeForStatement(expressionToString(whileStmt.getCondition())))
              .append(")\")\n");
            if (whileStmt.getBody() instanceof BlockStmt block) {
                for (Statement s : block.getStatements()) {
                    sb.append(generateStatement(s));
                }
            } else {
                sb.append(generateStatement(whileStmt.getBody()));
            }
            sb.append("    .endControlFlow()\n");
        } else if (stmt instanceof TryStmt tryStmt) {
            sb.append("    .beginControlFlow(\"try\")\n");
            for (Statement s : tryStmt.getTryBlock().getStatements()) {
                sb.append(generateStatement(s));
            }
            for (var catchClause : tryStmt.getCatchClauses()) {
                sb.append("    .nextControlFlow(\"catch ($T ")
                  .append(catchClause.getParameter().getNameAsString())
                  .append(")\", ")
                  .append(generateTypeName(catchClause.getParameter().getType()))
                  .append(")\n");
                for (Statement s : catchClause.getBody().getStatements()) {
                    sb.append(generateStatement(s));
                }
            }
            tryStmt.getFinallyBlock().ifPresent(finallyBlock -> {
                sb.append("    .nextControlFlow(\"finally\")\n");
                for (Statement s : finallyBlock.getStatements()) {
                    sb.append(generateStatement(s));
                }
            });
            sb.append("    .endControlFlow()\n");
        } else if (stmt instanceof ThrowStmt throwStmt) {
            sb.append("    .addStatement(\"throw ")
              .append(escapeForStatement(expressionToString(throwStmt.getExpression())))
              .append("\")\n");
        } else if (stmt instanceof BlockStmt blockStmt) {
            for (Statement s : blockStmt.getStatements()) {
                sb.append(generateStatement(s));
            }
        } else {
            // Fallback for other statement types
            sb.append("    .addStatement(").append(escapeString(stmt.toString().trim())).append(")\n");
        }

        return sb.toString();
    }

    /**
     * Generate CodeBlock code for an expression.
     */
    private String generateCodeBlockForExpression(Expression expr) {
        if (expr instanceof StringLiteralExpr strLit) {
            return "\"$S\", " + escapeString(strLit.getValue());
        } else if (expr instanceof IntegerLiteralExpr || expr instanceof LongLiteralExpr ||
                   expr instanceof DoubleLiteralExpr || expr instanceof BooleanLiteralExpr) {
            return "\"$L\", " + expr.toString();
        } else if (expr instanceof ClassExpr classExpr) {
            return "\"$T.class\", " + generateTypeName(classExpr.getType());
        } else if (expr instanceof ObjectCreationExpr objCreate) {
            return escapeString("new " + objCreate.getType().asString() +
                    "(" + objCreate.getArguments().stream()
                    .map(this::expressionToString)
                    .collect(Collectors.joining(", ")) + ")");
        } else {
            return escapeString(expressionToString(expr));
        }
    }

    /**
     * Convert expression to string representation.
     */
    private String expressionToString(Expression expr) {
        return expr.toString();
    }

    /**
     * Escape a string for use in generated code.
     */
    private String escapeString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t") + "\"";
    }

    /**
     * Escape a string for use in addStatement calls.
     */
    private String escapeForStatement(String s) {
        return s.replace("$", "$$");
    }

    /**
     * Convert a name to lower camel case.
     */
    private String toLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
