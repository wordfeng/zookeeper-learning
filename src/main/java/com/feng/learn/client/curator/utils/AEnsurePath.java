package com.feng.learn.client.curator.utils;


import com.feng.learn.client.curator.app.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;

/**
 * 提供了一种能确保数据节点存在的机制。
 * 静默操作
 */
@Deprecated
public class AEnsurePath {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = ClientFactory.getClient();
        client.start();
        String path = "/zk-order/o1";

        client.usingNamespace("zk-order");

        EnsurePath ensurePath = new EnsurePath(path);
        ensurePath.ensure(client.getZookeeperClient());
        ensurePath.ensure(client.getZookeeperClient());

        EnsurePath ensurePath1 = client.newNamespaceAwareEnsurePath("/c1");
        ensurePath1.ensure(client.getZookeeperClient());
    }

}
