package com.zt;

import java.io.*;

public class Test {
    public static void main(String[] args) {
        try{
            File file = new File("d://text.txt");
            if(file.createNewFile()){
                System.out.println("Create file successed");
            }
            MyApp.method1("d://text.txt", "123123123\n");
            MyApp.method1("d://text.txt", "汉字测试");
        }catch(Exception e){
            System.out.println(e);
        }
    }

}
