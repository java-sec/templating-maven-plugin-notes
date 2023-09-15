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
