package com.feng.learn.client.curator.app;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 控制多线程之间同步的经典方法
 * JDK中有CyclicBarrier实现
 */
public class MyDistributedBarrier {

    public static CyclicBarrier BARRIER;
    static String DIS_BARRIER = "/barrier";
    static DistributedBarrier barrier;


    public static void main(String[] args) throws Exception {
//        jdkBarrier();
//        disBarrier();
        autoReleaseBarrier();
    }

    /**
     * 主动释放版
     * public DistributedBarrier(CuratorFramework client, String barrierPath)
     */
    public static void disBarrier() throws Exception {

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                CuratorFramework client = ClientFactory.getClient();
                client.start();
                barrier = new DistributedBarrier(client, DIS_BARRIER);
                System.out.println(Thread.currentThread().getName() + "号Barrier设置");
                try {
                    barrier.setBarrier();//设置Barrier
                    barrier.waitOnBarrier();//等待barrier的释放
                    System.err.println("start ...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        Thread.sleep(1000);

        //释放barrier
        barrier.removeBarrier();

    }


    public static void autoReleaseBarrier() throws Exception {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                CuratorFramework client = ClientFactory.getClient();
                client.start();
                DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, DIS_BARRIER, 5);
                try {
                    Thread.sleep(Math.round(Math.random() * 3000));
                    System.out.println(Thread.currentThread().getName() + "进入Barrier");
                    //准备状态，准备人数达到5个后，所有成员会被同时触发进入
                    barrier.enter();
                    System.err.println("start ...");
                    Thread.sleep(Math.round(Math.random() * 3000));
                    //这里开始等待，处于退出状态，准备退出的成员达到5个所有成员会被同时触发退出
                    barrier.leave();
                    System.out.println("over ...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    /**
     * jdk CyclicBarrier
     */
    public static void jdkBarrier() {
        BARRIER = new CyclicBarrier(3);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new Runner("1 "));
        executor.submit(new Runner("2 "));
        executor.submit(new Runner("3 "));

        executor.shutdown();
    }
}

@Data
class Runner implements Runnable {
    private String name;

    public Runner(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(name + " ready!");
        try {
            MyDistributedBarrier.BARRIER.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println("Run！");

    }
}
