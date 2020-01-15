package com.feng.learn.client.official;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZookeeperCreate {

    private static ZooKeeper zk;

    private static ZookeeperConnection zkConn;


    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        zkConn = new ZookeeperConnection();
        zk = zkConn.connect("localhost:2181", 5000);

        create("/node2", "what the fuck this???".getBytes());
        zkConn.close();
    }

    public static void create(String path, byte[] data) throws KeeperException, InterruptedException {
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

}
