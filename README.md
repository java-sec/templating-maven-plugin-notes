# Release Jar内嵌版本号实践方案（基于templating-maven-plugin）

# 一、业务场景

Java的应用程序通过Maven来管理模块和依赖，应用程序在运行的时候可能需要用到自己的版本号：

- 比如如果是客户端程序，跟服务端通讯的时候肯定要涉及到的一些版本兼容性的问题，那这个时候就必须得知道自己的版本号
- 比如如果是网络程序，在协议或者发送HTTP请求的User-Agent中可能就需要带上自己的版本号

总之会有各种场景的需求要能在运行时获取到版本号，本文重点关注怎么实现。

# 二、尝试通过Maven插件解决

此处尝试通过引入Maven插件的方式来解决这个问题，用到的Maven插件是`org.codehaus.mojo:templating-maven-plugin`，其Maven中央仓库地址：

```
https://mvnrepository.com/artifact/org.codehaus.mojo/templating-maven-plugin
```

这个插件工作的流程：

- 我们创建一些模板文件，通常是Java源代码跟普通的Java代码没啥区别，只是会在模板文件中在字符串的值的地方使用`${project.version}`的方式设置一些变量占位符，比如`public static final String VERSION = "${project.version}";`
- 然后把这个路径告诉插件，插件在Maven打包的阶段就会扫描给定路径下的文件，渲染变量的值做替换

下面来看一个实际的例子，为了保证尽可能不出错，我们通常会创建一个单独的文件夹，只把必要的文件放在下面，比如我们需要做版本替换，则创建一个Version类：

```java
package com.github.java.sec;

/**
 * @author CC11001100
 */
public class Version {

    // templating-maven-plugin 插件的 filter-sources goal 会把这个字段注入进来
    public static final String VERSION = "${project.version}";

    // 禁止被实例化，只能静态引用
    private Version() {
    }

}
```

位置：

![image-20230916001111667](README.assets/image-20230916001111667.png)

pom.xml大概是这样，就是引入插件然后指定模板文件的位置： 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.github.java.sec</groupId>
    <artifactId>templating-maven-plugin-notes</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <templating-maven-plugin.version>1.0.0</templating-maven-plugin.version>
    </properties>

    <developers>
        <developer>
            <name>CC11001100</name>
            <email>cc11001100@qq.com</email>
        </developer>
    </developers>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>templating-maven-plugin</artifactId>
                <version>${templating-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <id>filtering-java-templates</id>
                        <goals>
                            <goal>filter-sources</goal>
                        </goals>
                        <configuration>
                            <!-- 此处配置的是模板文件的位置 -->
                            <sourceDirectory>src/main/java-templates</sourceDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

在使用的时候直接引用常量：

```java
package com.github.java.sec;

/**
 * @author CC11001100
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(Version.VERSION);
    }

}
```

Maven compile一下，看一下编译后的文件：

![image-20230916001254831](README.assets/image-20230916001254831.png)

可以看到变量的值被正确替换了，不只是内置的变量，尝试在pom.xml中添加一个自定义的变量：

```xml
    <properties>
        <templating-maven-plugin.version>1.0.0</templating-maven-plugin.version>
        <foo.bar>foooooooooooooooooooooooooooooooooooooooobar</foo.bar>
    </properties>
```

然后在模板文件夹下继续创建类：

```java
package com.github.java.sec;

/**
 * @author CC11001100
 */
public class FooBar {
    
    // templating-maven-plugin 插件的 filter-sources goal 会把这个字段注入进来
    public static final String VERSION = "${foo.bar}";

    // 禁止被实例化，只能静态引用
    private FooBar() {
    }

}
```

继续Maven compile编译，查看编译后的源代码：

![image-20230916001625272](README.assets/image-20230916001625272.png)

# 三、存在的问题

直接在Idea中run是不会运行Maven的build插件执行替换的，只有在Maven方式编译的时候才能生效，那么如何让Idea直接run的时候运行Maven的build插件呢？要不然它不替换啊就蛋疼了

# 四、参考资料

- https://www.mojohaus.org/templating-maven-plugin/