package com.example.version.examples;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.tools.*;
import java.util.Arrays;
import com.example.version.VersionManager;

public class VersionDemo {
    public static void main(String[] args) {
        try {
            // 准备测试环境
            prepareTestClasses();

            VersionManager manager = new VersionManager();

            System.out.println("=== 多版本类库演示 ===");

            // 加载不同版本的类
            manager.loadVersion("v1", "plugins/v1");
            manager.loadVersion("v2", "plugins/v2");

            // 加载并使用v1版本的类
            Class<?> v1Class = manager.loadClass("v1", "com.example.TestClass");
            Object v1Instance = v1Class.getDeclaredConstructor().newInstance();
            System.out.println("V1版本: " + v1Instance.toString());

            // 加载并使用v2版本的类
            Class<?> v2Class = manager.loadClass("v2", "com.example.TestClass");
            Object v2Instance = v2Class.getDeclaredConstructor().newInstance();
            System.out.println("V2版本: " + v2Instance.toString());

            // 验证类隔离
            System.out.println("\n类隔离验证:");
            System.out.println("v1 class == v2 class: " + (v1Class == v2Class));
            System.out.println("v1 classloader == v2 classloader: " +
                    (v1Class.getClassLoader() == v2Class.getClassLoader()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void prepareTestClasses() throws Exception {
        // 清理并创建目录
        deleteDirectory("plugins");
        createDirectories("plugins/v1/com/example", "plugins/v2/com/example");

        // 编译V1版本
        compileClass(
                "plugins/v1",
                "plugins/v1/com/example/TestClass.java",
                "package com.example;\n" +
                        "public class TestClass {\n" +
                        "    public String toString() {\n" +
                        "        return \"TestClass Version 1.0\";\n" +
                        "    }\n" +
                        "}"
        );

        // 编译V2版本
        compileClass(
                "plugins/v2",
                "plugins/v2/com/example/TestClass.java",
                "package com.example;\n" +
                        "public class TestClass {\n" +
                        "    public String toString() {\n" +
                        "        return \"TestClass Version 2.0\";\n" +
                        "    }\n" +
                        "}"
        );
    }

    private static void compileClass(String outputDir, String sourcePath, String source) throws Exception {
        // 写入源文件
        writeFile(sourcePath, source);

        // 获取编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // 设置输出目录
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(outputDir)));

        // 编译源文件
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(sourcePath));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);

        if (!task.call()) {
            throw new RuntimeException("Compilation failed for " + sourcePath);
        }

        fileManager.close();
    }

    private static void createDirectories(String... paths) throws Exception {
        for (String path : paths) {
            Files.createDirectories(Paths.get(path));
        }
    }

    private static void writeFile(String path, String content) throws Exception {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }

    private static void deleteDirectory(String path) throws Exception {
        if (Files.exists(Paths.get(path))) {
            Files.walk(Paths.get(path))
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}