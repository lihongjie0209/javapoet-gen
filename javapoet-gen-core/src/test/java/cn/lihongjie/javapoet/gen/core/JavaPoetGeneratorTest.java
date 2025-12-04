package cn.lihongjie.javapoet.gen.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavaPoetGenerator.
 */
class JavaPoetGeneratorTest {

    private JavaPoetGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JavaPoetGenerator();
    }

    @Test
    void testGenerateSimpleClass() {
        String source = """
                package com.example;
                
                public class HelloWorld {
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("TypeSpec.classBuilder(\"HelloWorld\")"));
        assertTrue(result.contains("Modifier.PUBLIC"));
        assertTrue(result.contains("JavaFile.builder(\"com.example\""));
    }

    @Test
    void testGenerateClassWithMethod() {
        String source = """
                package com.example;
                
                public class HelloWorld {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("MethodSpec.methodBuilder(\"main\")"));
        assertTrue(result.contains("Modifier.PUBLIC"));
        assertTrue(result.contains("Modifier.STATIC"));
        assertTrue(result.contains("returns(TypeName.VOID)"));
    }

    @Test
    void testGenerateClassWithFields() {
        String source = """
                package com.example;
                
                public class Person {
                    private String name;
                    private int age;
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("FieldSpec.builder(String.class, \"name\""));
        assertTrue(result.contains("FieldSpec.builder(TypeName.INT, \"age\""));
        assertTrue(result.contains("Modifier.PRIVATE"));
    }

    @Test
    void testGenerateClassWithConstructor() {
        String source = """
                package com.example;
                
                public class Person {
                    private String name;
                    
                    public Person(String name) {
                        this.name = name;
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("MethodSpec.constructorBuilder()"));
        assertTrue(result.contains("Modifier.PUBLIC"));
    }

    @Test
    void testGenerateInterface() {
        String source = """
                package com.example;
                
                public interface Greeting {
                    void sayHello();
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("TypeSpec.interfaceBuilder(\"Greeting\")"));
    }

    @Test
    void testGenerateClassWithAnnotation() {
        String source = """
                package com.example;
                
                public class MyService {
                    @Override
                    public String toString() {
                        return "MyService";
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("addAnnotation"));
        assertTrue(result.contains("Override.class"));
    }

    @Test
    void testGenerateEnum() {
        String source = """
                package com.example;
                
                public enum Color {
                    RED, GREEN, BLUE
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("TypeSpec.enumBuilder(\"Color\")"));
        assertTrue(result.contains("addEnumConstant(\"RED\")"));
        assertTrue(result.contains("addEnumConstant(\"GREEN\")"));
        assertTrue(result.contains("addEnumConstant(\"BLUE\")"));
    }

    @Test
    void testGenerateClassWithGenerics() {
        String source = """
                package com.example;
                
                import java.util.List;
                
                public class Container {
                    private List<String> items;
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("ParameterizedTypeName.get"));
    }

    @Test
    void testGenerateClassWithSuperclass() {
        String source = """
                package com.example;
                
                public class Dog extends Animal {
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("superclass"));
    }

    @Test
    void testGenerateClassImplementingInterface() {
        String source = """
                package com.example;
                
                public class MyRunnable implements Runnable {
                    @Override
                    public void run() {
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("addSuperinterface"));
    }

    @Test
    void testGenerateMethodWithControlFlow() {
        String source = """
                package com.example;
                
                public class Calculator {
                    public int abs(int value) {
                        if (value < 0) {
                            return -value;
                        }
                        return value;
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("beginControlFlow"));
        assertTrue(result.contains("endControlFlow"));
    }

    @Test
    void testGenerateMethodWithLoop() {
        String source = """
                package com.example;
                
                public class Counter {
                    public int sum(int n) {
                        int result = 0;
                        for (int i = 0; i < n; i++) {
                            result += i;
                        }
                        return result;
                    }
                }
                """;

        String result = generator.generateFromSource(source);

        assertNotNull(result);
        assertTrue(result.contains("beginControlFlow") && result.contains("for"));
    }

    @Test
    void testImportsIncluded() {
        String source = """
                public class Test {
                }
                """;

        String result = generator.generateFromSource(source);

        assertTrue(result.contains("import com.squareup.javapoet.TypeSpec;"));
        assertTrue(result.contains("import com.squareup.javapoet.JavaFile;"));
        assertTrue(result.contains("import javax.lang.model.element.Modifier;"));
    }
}
