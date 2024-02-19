//所在包
package com.test;

//导入包

/**
 * Classname: Computer
 * Package: com.test
 * Description:
 *
 * @Author: lqy
 * @Create: 2023/2/27 - 16:47
 * @Version: v1.0
 */

//这本身是一个java文件，这个java文件中包括了很多类，但是只有一个public类
//内部类问题
class Computer {
    //属性，成员变量
    private String cpu;
    private String ram;
    private String disk;

    //方法，成员方法
    public String getCpu() {
        return cpu;
    }

    public String getRam() {
        return ram;
    }

    public String getDisk() {
        return disk;
    }

    //构造器
    public Computer(String cpu, String ram, String disk) {
        this.cpu = cpu;
        this.ram = ram;
        this.disk = disk;
    }

}

class PC extends Computer {
    public PC(String cpu, String ram, String disk, String brand) {
        super(cpu, ram, disk);
        this.brand = brand;
    }

    public String getDetails() {
        return "PC{" +
                "brand='" + brand + '\'' +
                ", cpu='" + getCpu() + '\'' +
                ", ram='" + getRam() + '\'' +
                ", disk='" + getDisk() + '\'' +
                '}';
    }

    private String brand;
}

class NotePad extends Computer {
    public NotePad(String cpu, String ram, String disk, String color) {
        //父类的构造器！！！！
        super(cpu, ram, disk);
        this.color = color;
    }
    public String getDetails() {
        return "PC{" +
                "brand='" + color + '\'' +
                ", cpu='" + getCpu() + '\'' +
                ", ram='" + getRam() + '\'' +
                ", disk='" + getDisk() + '\'' +
                '}';
    }

    private String color;

}

//只有一个外部类
public class Test01 {
    public static void main(String[] args) {
        PC pc = new PC("1", "2", "3", "4");
        //局部变量
        NotePad notePad = new NotePad("5", "6", "7", "8");

        System.out.println(pc.getDetails());
        System.out.println(notePad.getDetails());
    }
}
