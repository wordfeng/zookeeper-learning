package com.feng.learn.client.curator.async;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * todo:探究，流式api是否可以复用
 */
@SuppressWarnings("all")
public class AsyncCRUD {

    private static final RetryPolicy retryPolicy;
    private static final CuratorFramework client;

    static {
        retryPolicy = new RetryNTimes(5, 1000);

        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(2000)
                .namespace("test")
                .retryPolicy(retryPolicy)
                .build();
    }


    /**
     * 创建初始节点
     *
     * @return Curator
     */
    public static CuratorFramework create() throws Exception {
        CountDownLatch initTaskCountDownLatch = new CountDownLatch(2);
        ExecutorService initTaskExecutor = Executors.newFixedThreadPool(2);

        //初始化根节点
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                //public T inBackground()
                //public T inBackground(Object context);
                //public T inBackground(BackgroundCallback callback);
                //public T inBackground(BackgroundCallback callback, Object context);
                //public T inBackground(BackgroundCallback callback, Executor executor);
                //public T inBackground(BackgroundCallback callback, Object context, Executor executor);
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        int resultCode = event.getResultCode();
                        CuratorEventType type = event.getType();
                        initTaskCountDownLatch.countDown();
                    }
                }, initTaskExecutor)//传入线程池，异步事件逻辑会交给线程池去做
                .forPath("/app", "init data".getBytes());

        //初始化配置节点
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        CuratorEventType type = event.getType();
                        int resultCode = event.getResultCode();
                        initTaskCountDownLatch.countDown();
                    }
                })//没有传入Executor，使用Zookeeper默认的EventThread处理
                .forPath("/app/config", "config data".getBytes());


        initTaskCountDownLatch.await();
        initTaskExecutor.shutdown();

        return client;
    }

    /**
     * 删除节点
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static CuratorFramework delete(String path) throws Exception {

        // withVersion: dataVersion
        // guaranteed()：强制保证删除，只要客户端会话有效，就会在后台一直执行删除，直到成功
        // 由于网络问题会导致zk删除失败，所以guaranteed还是s非常实用的
//        Stat oldStat = new Stat();
        client.delete()
                .guaranteed()
                .deletingChildrenIfNeeded()
//                .inBackground()
//                .withVersion(oldStat.getVersion()) //使用CAS（Compare and swap）机制
                .forPath(path);

        return client;
    }

    public static Stat getStat(String path) throws Exception {
        Stat stat = new Stat();//zookeeper的类
        client.getData().storingStatIn(stat).forPath(path);
        //上下两个冲突只能分开用
        client.getData().inBackground().forPath(path);

        stat.getCtime(); // create time's timestamp
        stat.getMtime();// 最后修改时间

        stat.getCzxid();// 节点创建时的事物id
        stat.getMzxid();// 最后一次修改该节点的事物id
        stat.getPzxid();// 最后修改子节点（添加/删除）的事物id

        stat.getAversion();// AclVersion
        stat.getCversion(); // childNodeVersion delete/create operator will make it +1
        stat.getVersion(); // dataVersion

        stat.getDataLength();// 节点的数据长度
        stat.getNumChildren(); //子节点个数
        stat.getEphemeralOwner();// 临时节点的拥有者的会话id，如果不是临时节点值为0

        // eq 值的全比较
        boolean isEq = stat.equals(new Stat(5, 5, 45, 5, 5, 5, 5, 5, 5, 5, 5));
        // 值的挨个比较，只要有一个大或者小就直接返回-1，1，如果全相等则返回0
        stat.compareTo(new Stat());

        //序列化/反序列化
        //传入一个实现InputArchive/OutputArchive接口对象，实现类有Xml、Csv、Binary
        String serializePath = "/serialize.stat";
        stat.serialize(
                new BinaryOutputArchive(
                        new DataOutputStream(
                                new FileOutputStream(serializePath))), "tag");
        stat.deserialize(
                new BinaryInputArchive(
                        new DataInputStream(
                                new FileInputStream(serializePath))), "tag");


        String filePath = "/data.stat";
        stat.write(new DataOutputStream(new FileOutputStream(filePath)));
        stat.readFields(new DataInputStream(new FileInputStream(filePath)));

        String s = stat.toString();

        return stat;
    }

    /**
     * 修改
     */
    public static void update(String path, String data) throws Exception {
        client.setData()
                .withVersion(5)
//                .inBackground()
                .forPath(path, data.getBytes());

    }


    /**
     * 关闭
     */
    public static void close() {
        client.close();
    }


    public static void main(String[] args) throws Exception {


        List<ACL> acls = client.getACL().forPath("/xiao/yu");
        System.out.println("acl: " + acls);//[31,s{'world,'anyone} 31 代表cdrwa 11111
        client.getData().watched();
//        System.out.println("data: " + data);
        CuratorFrameworkState state = client.getState();
        System.out.println("state" + state);


    }
}
