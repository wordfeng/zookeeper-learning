package com.feng.learn.client.curator.app;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import redis.clients.jedis.Client;

/**
 * 1. 选择一个根节点 /master_select
 * 2. 多台机器同时在根节点下创建锁节点 /master_select/lock
 * 3. 创建成功的机器就是master
 */
public class MasterSelect {
    public static String MASTER_PATH = "/master_select";

    /**
     * 使用Curator封装好的MasterSelect
     */
    public void recipesMasterSelect() throws InterruptedException {
        CuratorFramework client = ClientFactory.getClient(MASTER_PATH);
        client.start();
        LeaderSelector leaderSelector = new LeaderSelector(client, MASTER_PATH, new LeaderSelectorListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                //成功获取到Master权利的时候回调函数，LeaderSelectorListenerAdapter提供了默认实现
                if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
                    throw new CancelLeadershipException("连接异常，退出竞争");
                }
            }

            /**
             * 竞争到master以后调用该方法
             * 执行完这个方法以后会立即释放(删除节点)master的权利，然后开始新的一轮选举
             */
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("成为Master");
                Thread.sleep(300);
                System.out.println("完成Master权利，释放Master");
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.start();
        Thread.sleep(3000000);


    }

}
