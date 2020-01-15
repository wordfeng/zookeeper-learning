package com.feng.learn.client.curator.app;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;


public class DistributedLock {
    public static void main(String[] args) throws Exception {
        lock();
//        orderWithoutLock();
    }

    static String MASTER_PATH = "/distributed_lock";


    public static void lock() throws Exception {

        CuratorFramework client = ClientFactory.getClient();
        client.start();

        InterProcessMutex lock = new InterProcessMutex(client, MASTER_PATH);
        CountDownLatch count = new CountDownLatch(1);


        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println("await");
                    count.await();
                    System.out.println("acquire");
                    lock.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String orderNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss|SSS"));
                System.out.println("订单号：" + orderNumber);
                try {
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }

        System.out.println("countDown");
        count.countDown();

        Thread.currentThread().join();
        client.close();
    }


    public static void orderWithoutLock(){
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {

                String orderNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss|SSS"));
                System.out.println("订单号：" + orderNumber);

            }).start();
        }

    }

}
