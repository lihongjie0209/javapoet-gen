package cn.lihongjie.javapoet.gen.core;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavaSourceParser.
 */
class JavaSourceParserTest {

    private JavaSourceParser parser;

    @BeforeEach
    void setUp() {
        parser = new JavaSourceParser();
    }

    @Test
    void testParseSimpleClass() {
        String source = """
                package com.example;
                
                public class HelloWorld {
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        assertTrue(cu.getPackageDeclaration().isPresent());
        assertEquals("com.example", cu.getPackageDeclaration().get().getNameAsString());
        assertEquals(1, cu.getTypes().size());
        assertEquals("HelloWorld", cu.getTypes().get(0).getNameAsString());
    }

    @Test
    void testParseClassWithMethods() {
        String source = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                    
                    public int subtract(int a, int b) {
                        return a - b;
                    }
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var classDecl = cu.getClassByName("Calculator");
        assertTrue(classDecl.isPresent());
        assertEquals(2, classDecl.get().getMethods().size());
    }

    @Test
    void testParseClassWithFields() {
        String source = """
                public class Person {
                    private String name;
                    private int age;
                    public static final String DEFAULT_NAME = "Unknown";
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var classDecl = cu.getClassByName("Person");
        assertTrue(classDecl.isPresent());
        assertEquals(3, classDecl.get().getFields().size());
    }

    @Test
    void testParseInterface() {
        String source = """
                public interface Runnable {
                    void run();
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var interfaceDecl = cu.getInterfaceByName("Runnable");
        assertTrue(interfaceDecl.isPresent());
        assertTrue(interfaceDecl.get().isInterface());
    }

    @Test
    void testParseEnum() {
        String source = """
                public enum Color {
                    RED, GREEN, BLUE
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var enumDecl = cu.getEnumByName("Color");
        assertTrue(enumDecl.isPresent());
        assertEquals(3, enumDecl.get().getEntries().size());
    }

    @Test
    void testParseFromFile(@TempDir Path tempDir) throws IOException {
        String source = """
                package test;
                
                public class FileTest {
                    public void hello() {
                        System.out.println("Hello");
                    }
                }
                """;

        Path testFile = tempDir.resolve("FileTest.java");
        Files.writeString(testFile, source);

        CompilationUnit cu = parser.parse(testFile);

        assertNotNull(cu);
        assertTrue(cu.getClassByName("FileTest").isPresent());
    }

    @Test
    void testParseInvalidCodeThrowsException() {
        String invalidSource = """
                public class Broken {
                    public void method( {
                    }
                }
                """;

        assertThrows(JavaPoetGenException.class, () -> parser.parse(invalidSource));
    }

    @Test
    void testParseWithCustomSourceName() {
        String source = "public class Test {}";

        CompilationUnit cu = parser.parse(source, "CustomSource.java");

        assertNotNull(cu);
    }

    @Test
    void testParseClassWithAnnotations() {
        String source = """
                @Deprecated
                public class OldClass {
                    @Override
                    public String toString() {
                        return "old";
                    }
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var classDecl = cu.getClassByName("OldClass");
        assertTrue(classDecl.isPresent());
        assertFalse(classDecl.get().getAnnotations().isEmpty());
    }

    @Test
    void testParseClassWithGenerics() {
        String source = """
                import java.util.List;
                
                public class Container<T> {
                    private List<T> items;
                    
                    public void add(T item) {
                        items.add(item);
                    }
                }
                """;

        CompilationUnit cu = parser.parse(source);

        assertNotNull(cu);
        var classDecl = cu.getClassByName("Container");
        assertTrue(classDecl.isPresent());
        assertFalse(classDecl.get().getTypeParameters().isEmpty());
    }
}
