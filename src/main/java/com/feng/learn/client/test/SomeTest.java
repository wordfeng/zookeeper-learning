package com.feng.learn.client.test;

import com.feng.learn.client.curator.app.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.TestingZooKeeperServer;

import java.io.File;

/**
 * TestingServer：简易启动ZooKeeper服务的方法，可以方便的启动一个标准的zk服务器，用来做单元测试
 * public TestingServer(int port)
 * public TestingServer(boolean start)
 * public TestingServer(int port, boolean start)
 * public TestingServer(int port, File tempDirectory)
 * public TestingServer(int port, File tempDirectory, boolean start)
 * 如果没有指定dataDir，默认在java.io.tmpdir中创建一个临时目录作为数据存储
 * <p>
 * TestingCluster：启动zookeeper集群的工具类
 */
public class SomeTest {
    static String path = "/zookeeper";

    public static void main(String[] args) throws Exception {
//        test();
        cluster();
    }

    /**
     * 启动简易zk服务器
     */
    public static void test() throws Exception {
        TestingServer server = new TestingServer(2182, new File("/testing_server/data"));

        CuratorFramework client = ClientFactory.getClient("10.25.208.66:2182");
        client.start();
        Thread.sleep(5000);

        System.out.println(client.getChildren().forPath(path));

        System.in.read();
        server.close();

    }

    /**
     * 启动zk集群
     */
    public static void cluster() throws Exception {
        //3台
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        Thread.sleep(2000);
        TestingZooKeeperServer leader = null;
        for (TestingZooKeeperServer server : cluster.getServers()) {
            System.out.print("id：" + server.getInstanceSpec().getServerId());
            System.out.print("状态：" + server.getQuorumPeer().getServerState() + "\t");
            System.out.println("数据路径：" + server.getInstanceSpec().getDataDirectory().getAbsolutePath());

            if (server.getQuorumPeer().getServerState().equals("leading")) {
                leader = server;
            }
        }
        leader.kill();
        System.out.println("=======================================");
        System.out.println("after leader kill: ");
        for (TestingZooKeeperServer server : cluster.getServers()) {
            System.out.print("id：" + server.getInstanceSpec().getServerId());
            System.out.print("状态：" + server.getQuorumPeer().getServerState() + "\t");
            System.out.println("数据路径：" + server.getInstanceSpec().getDataDirectory().getAbsolutePath());
        }
        System.out.println("============sleep 3000 ================");
        Thread.sleep(3000);
        for (TestingZooKeeperServer server : cluster.getServers()) {
            System.out.print("id：" + server.getInstanceSpec().getServerId());
            System.out.print("状态：" + server.getQuorumPeer().getServerState() + "\t");
            System.out.println("数据路径：" + server.getInstanceSpec().getDataDirectory().getAbsolutePath());
        }
        cluster.stop();

    }


}
