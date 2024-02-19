package com.test;

/**
 * Classname: Student
 * Package: com.test
 * Description:
 *
 * @Author: lqy
 * @Create: 2024-01-23 - 16:56
 * @Version: v1.0
 */
public class Student extends Person{
    private String id;
    private int score;

    public Student(String name, int age, String id, int score) {
        super(name, age);
        this.id = id;
        this.score = score;
    }

    public void say() {
        System.out.println("大家好，我是" + super.getName() + ", 今年" + super.getAge() + "岁," + "学号是" + id
                + ", 这次考试考了" + score + "分");
    }

    public static void main(String[] args) {
        Student student = new Student("罗锲禹", 22, "201900810289", 80);
        student.say();
    }
}