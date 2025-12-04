# JavaPoet Generator

一个用于从 Java 源代码生成 [JavaPoet](https://github.com/square/javapoet) API 调用代码的库。

## 项目结构

这是一个 Maven 多模块项目：

```
javapoet-gen/
├── pom.xml                      # 父 POM
├── javapoet-gen-core/           # 核心库模块 (Maven)
│   ├── pom.xml
│   └── src/main/java/
│       └── cn/lihongjie/javapoet/gen/core/
│           ├── JavaPoetGenerator.java       # 主入口类
│           ├── JavaSourceParser.java        # Java 源码解析器
│           ├── JavaPoetCodeGenerator.java   # JavaPoet 代码生成器
│           ├── GeneratorConfig.java         # 配置类
│           └── JavaPoetGenException.java    # 异常类
├── javapoet-gen-cli/            # 命令行工具模块 (Maven)
│   ├── pom.xml
│   └── src/main/java/
│       └── cn/lihongjie/javapoet/gen/cli/
│           └── JavaPoetGenCli.java          # CLI 主程序
└── javapoet-gen-idea-plugin/    # IntelliJ IDEA 插件 (Gradle)
    ├── build.gradle
    └── src/main/
        ├── java/.../idea/
        │   ├── action/                      # 右键菜单动作
        │   ├── dialog/                      # 结果对话框
        │   └── toolwindow/                  # 工具窗口
        └── resources/
            └── META-INF/plugin.xml          # 插件配置
```

## 快速开始

### 作为库使用

```java
import cn.lihongjie.javapoet.gen.core.JavaPoetGenerator;

// 创建生成器
JavaPoetGenerator generator = new JavaPoetGenerator();

// 从 Java 源代码生成 JavaPoet 代码
String javaPoetCode = generator.generateFromSource("""
    package com.example;
    
    public class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello, World!");
        }
    }
    """);

System.out.println(javaPoetCode);
```

### 命令行使用

```bash
# 构建项目
mvn clean package

# 生成 JavaPoet 代码
java -jar javapoet-gen-cli/target/javapoet-gen-cli-1.0.0-SNAPSHOT-all.jar MyClass.java

# 输出到文件
java -jar javapoet-gen-cli/target/javapoet-gen-cli-1.0.0-SNAPSHOT-all.jar MyClass.java -o output.java

# 处理目录
java -jar javapoet-gen-cli/target/javapoet-gen-cli-1.0.0-SNAPSHOT-all.jar src/main/java -r -o generated/
```

## 示例

### 输入 Java 代码

```java
package com.example;

public class Person {
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
}
```

### 生成的 JavaPoet 代码

```java
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Modifier;

// Generated JavaPoet code for: Person

FieldSpec nameField = FieldSpec.builder(String.class, "name", Modifier.PRIVATE)
    .build();

FieldSpec ageField = FieldSpec.builder(TypeName.INT, "age", Modifier.PRIVATE)
    .build();

MethodSpec constructor = MethodSpec.constructorBuilder()
    .addModifiers(Modifier.PUBLIC)
    .addParameter(String.class, "name")
    .addParameter(TypeName.INT, "age")
    .addStatement("this.name = name")
    .addStatement("this.age = age")
    .build();

MethodSpec getNameMethod = MethodSpec.methodBuilder("getName")
    .addModifiers(Modifier.PUBLIC)
    .returns(String.class)
    .addStatement("return name")
    .build();

MethodSpec getAgeMethod = MethodSpec.methodBuilder("getAge")
    .addModifiers(Modifier.PUBLIC)
    .returns(TypeName.INT)
    .addStatement("return age")
    .build();

TypeSpec personSpec = TypeSpec.classBuilder("Person")
    .addModifiers(Modifier.PUBLIC)
    .addField(nameField)
    .addField(ageField)
    .addMethod(constructor)
    .addMethod(getNameMethod)
    .addMethod(getAgeMethod)
    .build();

JavaFile javaFile = JavaFile.builder("com.example", personSpec)
    .build();
```

## 支持的特性

- ✅ 类和接口
- ✅ 枚举类型
- ✅ 字段（包括初始化器）
- ✅ 方法（包括静态方法、默认方法）
- ✅ 构造函数
- ✅ 注解
- ✅ 泛型
- ✅ 继承和接口实现
- ✅ 控制流（if/else、for、while、try/catch）
- ✅ Javadoc 注释

## 构建

```bash
# 编译 Maven 模块
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package

# 安装到本地仓库（IDEA插件构建需要）
mvn install
```

## IntelliJ IDEA 插件

### 构建插件

```bash
# 首先安装核心模块
mvn install -pl javapoet-gen-core

# 构建插件
cd javapoet-gen-idea-plugin
./gradlew buildPlugin

# 插件文件位置: build/distributions/javapoet-gen-idea-plugin-*.zip
```

### 安装插件

1. 在 IntelliJ IDEA 中，打开 `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
2. 选择生成的 zip 文件
3. 重启 IDE

### 使用方法

1. 在项目视图或编辑器中右键点击任意 Java 文件
2. 选择 "Generate JavaPoet Code"
3. 选择操作：
   - 复制到剪贴板
   - 在新编辑器标签页中打开
   - 显示在工具窗口中

### 开发/调试插件

```bash
cd javapoet-gen-idea-plugin
./gradlew runIde  # 启动带有插件的 IDEA 实例
```

## 模块说明

### javapoet-gen-core

核心库，提供 Java 源码解析和 JavaPoet 代码生成功能。

依赖：
- JavaParser - 用于解析 Java 源代码
- JavaPoet - 目标生成库

### javapoet-gen-cli

命令行工具，提供便捷的 CLI 接口。

依赖：
- javapoet-gen-core
- picocli - 命令行参数解析

## 配置选项

```java
GeneratorConfig config = new GeneratorConfig()
    .setGenerateComments(true)      // 是否生成注释
    .setPreserveJavadoc(true)       // 是否保留 Javadoc
    .setInlineSimpleSpecs(true);    // 是否内联简单的 Spec

JavaPoetGenerator generator = new JavaPoetGenerator(config);
```

## 许可证

MIT License
