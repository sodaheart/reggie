package com.test;

/**
 * Classname: com.test.test03
 * Package: PACKAGE_NAME
 * Description:
 *
 * @Author: lqy
 * @Create: 2023/2/25 - 0:11
 * @Version: v1.0
 */
public class test03 {
    private int age = 1;
    public test03(int age) {
        age = age;
    }

    public int test(){
        return age;         //这步是遵守了访问权限
    }

    public static void main(String[] args) {
        test03 test03 = new test03(2);
        System.out.println(test03.age);
        System.out.println(test03.test());
    }
}
