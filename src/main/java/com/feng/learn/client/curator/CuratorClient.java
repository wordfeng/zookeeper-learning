package com.feng.learn.client.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public class CuratorClient {

    //
    public static void main(String[] args) throws Exception {
        //RetryPolicy提供重试策略的接口
        // 实现类：
        //  1. ExponentialBackoffRetry (初始sleep时间，最大重试次数，最大sleep时间（最大间隔时间）)  Exponential：指数 Backoff：倒扣/补偿
        //       计算最大睡眠时间公式：baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount + 1)));
        //  2. BoundedExponentialBackoffRetry   Bounded:有界的
        //       2继承自1，1 2 的异同点在于1的默认最大睡眠时间是integer的最大值，2必须要传入一个睡眠时间
        //  3. RetryForever
        //  4. RetryNTimes   重试N次
        //  5. RetryOneTime  只重试一次
        //  6. RetryUntilElapsed  重试直到运行
        ExponentialBackoffRetry exponentialBackoffRetry = new ExponentialBackoffRetry(1000, 3);
        //客户端
        //connectString 服务器列表，隔开，retryPolicy 重试策略，sessionTimeoutMs会话超时时间 默认6000ms，connectionTimeoutMs 连接创建超时时间
        CuratorFramework client1 = CuratorFrameworkFactory.newClient("106.13.33.238:2181,192.168.40.128:2181/node/nodechild", exponentialBackoffRetry);
        client1.start();
        CuratorFramework client = CuratorFrameworkFactory.newClient("106.13.33.238:2181", 5000, 50000, exponentialBackoffRetry);
        client.start();

        //创建节点
        client1.create().forPath("/zNode", "data".getBytes());
        // 创建节点，如果有父节点递归创建，父节点会被创建为持久型 不管叶子节点是什么类型
        // EPHEMERAL /ɪˈfemərəl/ 临时的
        client1.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/parentNodeAwaysPersistent/ephemeralNode");

        //删除节点
        client1.delete().forPath("/zNode");
        //删除节点 并且递归删除其子节点
        client1.delete().deletingChildrenIfNeeded().forPath("/parentNodeAwaysPersistent");
        //指定版本删除  如果指定版本不一样就抛出异常 org.apache.zookeeper.KeeperException$BadVersionException: KeeperErrorCode = BadVersion for
        client1.delete().withVersion(1);
        client1.delete().withVersion(1).forPath("/");
        //强制保证删除一个节点
        //只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到节点删除成功。比如遇到一些网络异常的情况，此guaranteed的强制删除就会很有效果。
        // guaranteed /'gærən'tid/ 确保的
        client1.delete().guaranteed().forPath("/zNode");
        //一个空接口，作用未知，实现了接口的都是在声明 授权给apache
        client1.delete().quietly();

        //查询
        client1.getData().forPath("/");
        //包含状态查询
        //storing store的ing形式
        Stat stat = new Stat();
        client1.getData().storingStatIn(stat).forPath("/");
        client1.getACL();
        client1.getChildren();
        client1.getConfig();
        client1.getConnectionStateListenable();
        client1.getState();

        //异步调用 所有的方法都有
        client1.create().inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                //回调方法
            }
        }).forPath("/zNode", "data".getBytes());

        client1.getData().inBackground().forPath("/");

        // 异步版本包curator-x-async  不需要再写inBackground
        AsyncCuratorFramework wrapClient = AsyncCuratorFramework.wrap(client);
        CuratorFramework unwrap = wrapClient.unwrap();
        wrapClient.create().forPath("/");
// = ============================================
        //启动zk客户端
        client.start();
        //返回客户端状态
        //public enum CuratorFrameworkState
        //LATENT、STARTED、STOPPED
        client.getState();

        //创建节点
        client.create();
//        client.create().withACL();
        //删除节点
        client.delete();

        //更改节点数据
        client.setData();

        //检查节点是否存在
        client.checkExists();
        //获取节点数据
        client.getData().watched();
        client.getData().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {

            }
        });
        client.getData().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });
//        client.getData().usingWatcher();
        //获取权限
        client.getACL();
        //设置权限
        client.setACL();
        //获取配置
        client.getConfig();
        client.getCurrentConfig();
        //重新配置
        client.reconfig();
        //事物
        client.transaction();
        //分配可以跟transaction()一起使用的操作
        client.transactionOp();

        //https://www.jianshu.com/p/ae0c1fbbff3c
        //https://curator.apache.org/curator-framework/index.html


        client.close();
    }

}
