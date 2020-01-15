package com.feng.learn.client.curator.retry.policy;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;

import java.util.concurrent.TimeUnit;

public class MyRetryPolicy extends MySleepingRetry {

    public MyRetryPolicy(int n) {
        this(n, 5);
    }

    protected MyRetryPolicy(int n, int m) {
        super(n);
    }

    @Override
    protected long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
        return 0;
    }
}


abstract class MySleepingRetry implements RetryPolicy {

    private final int n;

    protected MySleepingRetry(int n) {
        this.n = n;
    }

    // made public for testing
    public int getN() {
        return n;
    }

    /**
     * 当这个操作由于某种原因失效的时候，返回true重试
     *
     * @param retryCount    已经重试次数，第一次重试为0
     * @param elapsedTimeMs 从第一次重试开始已经花费的时间
     * @param sleeper       用于sleep指定时间。Curator建议不使用Thread.sleep来进行sleep操作
     * @return true：已失败重试，false：
     */
    @Override
    public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
        if (retryCount < n) {
            try {
                sleeper.sleepFor(getSleepTimeMs(retryCount, elapsedTimeMs), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            return true;
        }
        return false;
    }

    protected abstract long getSleepTimeMs(int retryCount, long elapsedTimeMs);
}