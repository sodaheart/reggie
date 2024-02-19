package com.test;

/**
 * Classname: Person
 * Package: com.test
 * Description:
 *
 * @Author: lqy
 * @Create: 2024-01-23 - 16:56
 * @Version: v1.0
 */
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return this.name;
    }
    public int getAge() {
        return this.age;
    }
}
