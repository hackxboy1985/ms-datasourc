package com.mints.demo;

//import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.util.concurrent.*;
//import com.ydtc.context.LocalRequestContextHolder;

import org.mints.masterslave.logger.MsLogger;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Guava Futures Listening Executor
 *
 * @author Jthan
 */
public class FuturesExecutorUtil {

    private static final MsLogger logger = MsLogger.getLogger(FuturesExecutorUtil.class);

    private static final int POOL_SIZE = 45;

    private static final ListeningExecutorService service =
//            MoreExecutors.listeningDecorator(TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(POOL_SIZE)));
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(POOL_SIZE));

    public static <V> void submit(Callable<V> callable, FutureCallback<V> futureCallback) {

        FutureCallback<V> futureCallbackAgent = new FutureCallback<V>() {
            @Override
            public void onSuccess(V s) {
                futureCallback.onSuccess(s);
//                LocalRequestContextHolder.destoryContext();
            }

            @Override
            public void onFailure(Throwable throwable) {
                futureCallback.onFailure(throwable);
//                LocalRequestContextHolder.destoryContext();
            }
        };

        ListenableFuture<V> listenableFuture = service.submit(callable);

        Futures.addCallback(listenableFuture, futureCallbackAgent);



    }

    public static <V> void schedule(Callable<V> callable) {
        Callable<V> callableAgent = new Callable<V>() {
            @Override
            public V call() throws Exception {
                try {
                    V v = callable.call();
//                    LocalRequestContextHolder.destoryContext();
                    return v;
                }catch (Throwable e){
                    logger.error(e.getMessage(),e);
                }
                return null;
            }
        };
        service.submit(callableAgent);
//        service.submit(callable);
    }
}
