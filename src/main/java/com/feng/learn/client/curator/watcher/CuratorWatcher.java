package com.feng.learn.client.curator.watcher;

import com.feng.learn.client.curator.crud.CRUD;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 0. 监听方式
 * 1. NodeCache         监听自身变化。依赖包：curator-recipes
 * 2. PathChildrenCache 监听子节点变化。依赖包：curator-recipes
 * 3. TreeCache         监听自身和子节点变化
 * 4. .usingWatcher(org.apache.zookeeper.Watcher())     一次性
 * 5. .usingWatcher(CuratorWatcher())
 * 6. CuratorListener
 */
public class CuratorWatcher {

    private static String path = "/aaaa";
    private static CuratorFramework client;

    static {
        try {
            client = CRUD.create(path, "dsadsa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.start();
    }

    /**
     * 使用NodeCache类监听指定zookeeper数据节点本身的变化
     * <p>
     * 也可以监听节点是否存在, 如果原本节点不存在，在创建后就会出发NodeCacheListener
     * 如果节点被删除就无法出发NodeCacheListener
     * <p>
     * NodeCache(CuratorFramework client, String path)
     * dataISCompressed 是否进行压缩  path 数据节点的路径
     * NodeCache(CuratorFramework client, String path, boolean dataIsCompressed)
     * NodeCache定义了事件处理的回调接口 NodeCacheListener
     */
    public static void nodeCache() throws Exception {

        CountDownLatch count = new CountDownLatch(5);

        //创建前必须start client
        NodeCache nodeCache = new NodeCache(client, path, false);
        //默认为false，如果设置为true，NodeCache在第一次启动的时候就会立刻从zookeeper上对应的节点的数据内容，保存在Cache中
        nodeCache.start(true);

        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("CountDownLatch：" + count.getCount());
                System.out.println(new String(nodeCache.getCurrentData().getData()));
                count.countDown();
            }
        });

        client.setData().forPath(path);
        Thread.sleep(2000);
        client.delete().deletingChildrenIfNeeded().forPath(path);
        Thread.sleep(3000);

        count.await();

        nodeCache.close();
        client.close();
    }

    /**
     * 监听指定zookeeper数据子节点的变化情况
     * cacheData：用于配置是否把节点内容缓存起来
     * *    如果true：客户端在收到节点列表变更通知的同时，也能获取到节点的数据内容；false无法获取到节点数据内容
     * 利用threadFactory和executorService 可以通过构造一个专门的线程池，来处理事件通知
     * <p>
     * PathChildrenCache(CuratorFramework client, String path, boolean cacheData)
     * PathChildrenCache(client,path,cacheData, boolean dataIsCompressed, final CloseableExecutorService executorService)
     * PathChildrenCache(client,path,cacheData, boolean dataIsCompressed, final ExecutorService executorService)
     * PathChildrenCache(client,path,cacheData, boolean dataIsCompressed, ThreadFactory threadFactory)
     * PathChildrenCache(client,path,cacheData, ThreadFactory threadFactory)
     * //不能对二级子节点和本身监听
     */
    public static void pathChildrenCache() throws Exception {

        PathChildrenCache cache = new PathChildrenCache(client, "/aaaa", true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println("子节点增加了一个 " + event.getData());
                        break;
                    case INITIALIZED:
                        System.out.println("PathChildrenCache#start(PathChildrenCache.StartMode)" +
                                "调用了PathChildrenCache.StartMode#POST_INITIALIZED_EVENT " +
                                "表明初始缓存已经填充" + event.getData());
                        break;
                    case CHILD_REMOVED:
                        System.out.println("A child was removed from the path");
                    case CHILD_UPDATED:
                        System.out.println("A child's data was changed");
                    case CONNECTION_LOST:
                    case CONNECTION_SUSPENDED:
                    case CONNECTION_RECONNECTED:
                        System.out.println("重新连接");
                }
            }
        });
        client.create().withMode(CreateMode.PERSISTENT).forPath(path);
        Thread.sleep(3000);
        client.create().withMode(CreateMode.PERSISTENT).forPath(path + "/c1");
        Thread.sleep(3000);
        client.delete().forPath(path + "/c1");
        Thread.sleep(50000);

    }

    public void usingWatcher() throws Exception {

        client.getData().usingWatcher(new org.apache.zookeeper.Watcher() {
            @Override
            public void process(WatchedEvent event) {
//                public enum EventType {
//                    None (-1),
//                    NodeCreated (1),
//                    NodeDeleted (2),
//                    NodeDataChanged (3),
//                    NodeChildrenChanged (4), 子节点变化（添加/删除）
//                    DataWatchRemoved (5),
//                    ChildWatchRemoved (6);
                //监听只会触发一次，需要重复注册
            }
        });
        client.getData().usingWatcher(new org.apache.curator.framework.api.CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                //todo:是否是一次性探究。
            }
        });

    }


    public void curatorListener() throws Exception {

        CuratorListener curatorListener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {

                //事件类型定义在 public enum CuratorEventType

                System.out.println("监听触发事件：" + event.getType());
            }
        };
        client.getCuratorListenable().addListener(curatorListener);

    }

    public static void treeCache() throws Exception {

        TreeCache treeCache = new TreeCache(client, path);
        treeCache.start();
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                //org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type
                System.out.println(event.getType() + ": " + event.getData());
            }
        });

    }

}
