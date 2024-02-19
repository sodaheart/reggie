package com.test;

/**
 * Classname: Hello
 * Package: com.test
 * Description:
 *
 * @Author: lqy
 * @Create: 2023/3/2 - 23:00
 * @Version: v1.0
 */
interface fruit {
    public abstract void eat();
}

class Apple implements fruit {
    public void eat() {
        System.out.println("Apple");
    }
}

class Orange implements fruit {
    public void eat() {
        System.out.println("Orange");
    }
}

class Factory {
    public static fruit getInstance(String ClassName) {
        fruit f = null;
        try {

            f = (fruit) Class.forName(ClassName).newInstance();     //工厂中使用反射！核心代码
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }
}

public class Hello {
    public static void main(String[] a) {
        fruit f = Factory.getInstance("Apple");
        if (f != null) {
            f.eat();
        }
    }
}

