package com.feng.learn.client.curator.utils;

import com.feng.learn.client.curator.app.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.ZooKeeper;

/**
 * Curator提供的工具类
 */
public class ZkPaths {

    static String path = "/zkpaths";

    public static void main(String[] args) throws Exception {
        CuratorFramework client = ClientFactory.getClient();
        client.start();

        ZooKeeper zooKeeper = client.getZookeeperClient().getZooKeeper();
        System.out.println(ZKPaths.fixForNamespace(path, "sub"));
        System.out.println(ZKPaths.makePath(path, "sub"));
        System.out.println(ZKPaths.getNodeFromPath(path + "/sub1"));
        ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(path + "/sub1");
        System.out.println(pathAndNode.getNode());
        System.out.println(pathAndNode.getPath());

        String dir1 = "/child1";
        String dir2 = "/child2";
        ZKPaths.mkdirs(zooKeeper, dir1);
        ZKPaths.mkdirs(zooKeeper, dir2);
        System.out.println(ZKPaths.getSortedChildren(zooKeeper, path));


    }
}
