package com.android.loadimage.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by luotao
 * 2018/7/25
 * emil:luotaosc@foxmail.com
 * qq:751423471
 * 线程池管理 管理整个项目中所有的线程，所以不能有多个实例对象
 */
public class ThreadPoolManger {

    /**
     * 单例设计模式（饿汉式）
     */
    private static ThreadPoolManger mInstance = new ThreadPoolManger();

    public static ThreadPoolManger getInstance() {
        return mInstance;
    }

    /**
     * 核心线程池的数量，同时能够执行的线程数量
     */
    private int corePoolSize;
    /**
     * 最大线程池数量，表示当缓冲队列满的时候能继续容纳的等待任务的数量
     */
    private int maximumPoolSize;
    /**
     * 存活时间
     */
    private long keepAliveTime = 1;
    /**
     * 时间单位
     */
    private TimeUnit unit = TimeUnit.HOURS;

    private ThreadPoolExecutor executor;

    private List<Runnable> activeThread = new ArrayList<>();

    private ThreadPoolManger() {
        /**
         * 核心线程数corePoolSize
         */
        corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        //虽然maximumPoolSize
        maximumPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(
                corePoolSize, //当某个核心任务执行完毕，会依次从缓冲队列中取出等待任务
                maximumPoolSize, //5,先corePoolSize,然后new LinkedBlockingQueue<Runnable>()
                keepAliveTime, //表示的是maximumPoolSize当中等待任务的存活时间
                unit,
                new LinkedBlockingQueue<Runnable>(), //缓冲队列，用于存放等待任务，Linked的先进先出
                Executors.defaultThreadFactory(), //创建线程的工厂
                new ThreadPoolExecutor.AbortPolicy() //用来对超出maximumPoolSize的任务的处理策略
        );
    }

    /**
     * 执行任务
     */
    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        executor.execute(runnable);
    }

    /**
     * 执行任务
     */
    public void addRunnable(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        activeThread.add(runnable);
    }

    /**
     * 真正开始执行
     */
    public void run() {
        if (activeThread.size() <= 0) {
            return;
        }
        for (int i = 0; i < activeThread.size(); i++) {
            execute(activeThread.get(i));
        }
    }
}


