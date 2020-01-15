package com.feng.learn.client.curator.app;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;

/**
 * 分布式计数器
 */
public class DisAtomicCounter {

    static String COUNTER = "/counter";

    public static void main(String[] args) throws Exception {
//        counter();
        counterNoLock();
    }

    public static void counter() throws Exception {
        CuratorFramework client = ClientFactory.getClient();
        client.start();
        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(
                client, COUNTER, new RetryNTimes(3, 1000));
        AtomicValue<Integer> value = atomicInteger.add(1);
        System.out.println(value.succeeded());
        System.out.println(value.preValue());
        System.out.println(value.postValue());

    }

    /**
     * 无锁
     */
    public static void counterNoLock() throws Exception {
        CuratorFramework client = ClientFactory.getClient();
        client.start();
        CountDownLatch count = new CountDownLatch(1);
        String noLockPath = "/counterNOLock";
        client.create().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                count.countDown();
            }
        }).forPath(noLockPath, "0".getBytes());

        count.await();
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    byte[] bytes = client.getData().forPath(noLockPath);
                    Integer old = new Integer(new String(bytes));
                    System.out.println(old);
                    client.setData().forPath(noLockPath, String.valueOf(old + 1).getBytes());
                    sout(client,noLockPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void sout(CuratorFramework client,String noLockPath) throws Exception {
        System.out.println(new String(client.getData().forPath(noLockPath)));
    }

}
