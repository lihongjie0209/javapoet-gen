package cn.lihongjie.javapoet.gen.core;

/**
 * Exception thrown when JavaPoet code generation fails.
 */
public class JavaPoetGenException extends RuntimeException {

    public JavaPoetGenException(String message) {
        super(message);
    }

    public JavaPoetGenException(String message, Throwable cause) {
        super(message, cause);
    }
}
