package com.feng.learn.client;

import java.util.Random;

public class Radnoms {
    public static void main(String[] args) {
        String[] doWhat = {"Dubbo","ES","Quartz","Kafka","Netty","Redis"};
        System.out.println(doWhat[new Random().nextInt(6)]);
    }
}
