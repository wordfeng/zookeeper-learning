package com.feng.learn.client.curator.async;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class AsyncCallZk implements BackgroundCallback {
    /**
     * 操作完成后被异步调用
     * <p>
     * CuratorEvent  定义了zookeeper服务端发送到客户端的一系列事件参数，重要的有：事件类型+响应码两个参数
     * *     所有响应码都定义在：org.apache.zookeeper.KeeperException.Code
     * *               0 ： ok
     * *              -4 ：ConnectionLoss
     * *            -110 ：NodeExists
     * *            -112 ：SessionExpired    Expired：adj 过期的
     * *    事件对应关系在：org.apache.curator.framework.api.CuratorEventType枚举类中
     * *                              CREATE | DELETE | EXISTS    | GET_DATA | SET_DATA | CHILDREN  | SYNC
     * * 对应CuratorFramework中的方法 create()| delete |checkExists| getData  | setData  |getChildren|sync(String, Object)
     * *
     * *                              GET_ACL| SET_ACL
     * </p>
     *
     * @param client 当前客户端实例
     * @param event  服务端事件
     */
    @Override
    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
        WatchedEvent watchedEvent = event.getWatchedEvent();
        List<ACL> aclList = event.getACLList();
        List<String> children = event.getChildren();
        Object context = event.getContext();
        byte[] data = event.getData();
        String name = event.getName();
        List<CuratorTransactionResult> opResults = event.getOpResults();
        String path = event.getPath();
        int resultCode = event.getResultCode();
        Stat stat = event.getStat();
        CuratorEventType type = event.getType();
    }
}
