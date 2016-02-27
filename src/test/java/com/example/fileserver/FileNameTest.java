package com.example.fileserver;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FileNameTest {

    public static void main(String[] args){
        String name = "123.456.789";
        String[] split = name.split("\\.");
        System.out.println(split[split.length-1]);
    }
}
