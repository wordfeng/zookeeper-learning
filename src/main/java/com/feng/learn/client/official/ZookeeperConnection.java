package com.feng.learn.client.official;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperConnection {

    private ZooKeeper zk;

    private final CountDownLatch connectSignal = new CountDownLatch(1);


    public ZooKeeper connect(String host, int sessionTimeout) throws IOException, InterruptedException {
        zk = new ZooKeeper(host, sessionTimeout, watchedEvent -> {
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectSignal.countDown();
            }
            System.out.println("watcher "+watchedEvent);
        });
        connectSignal.await();
        return zk;
    }

    public void close() throws InterruptedException {
        zk.close();
    }

}
