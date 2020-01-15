package com.feng.learn.client.official;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Zk {

    public static void main(String[] args) throws InterruptedException, KeeperException, IOException {
        CountDownLatch CONN_SIGNAL = new CountDownLatch(1);
        Semaphore CONN_SIGNAL_2 = new Semaphore(1);
        CONN_SIGNAL_2.acquire();

        ZooKeeper zk = new ZooKeeper("192.168.40.128:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    CONN_SIGNAL.countDown();
                    CONN_SIGNAL_2.release();
                }
            }
        });
        CONN_SIGNAL.await();
        CONN_SIGNAL_2.acquire();
        zk.create("/zkNode", "what the fuck??".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        Stat exists = zk.exists("/zkNode", true);
        System.out.println(Stat.signature());
        zk.close();
    }
}
