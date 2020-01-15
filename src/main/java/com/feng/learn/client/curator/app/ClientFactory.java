package com.feng.learn.client.curator.app;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public abstract class ClientFactory {

    public static String DOMAIN = "10.25.208.66:2181";


    public static CuratorFramework getClient() {
        return CuratorFrameworkFactory.builder()
                .connectString(DOMAIN)
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))
                .build();
    }

    public static CuratorFramework getClient(String connectString) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))
                .build();
    }



}
