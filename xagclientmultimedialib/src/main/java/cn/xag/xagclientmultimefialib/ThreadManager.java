package cn.xag.xagclientmultimefialib;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by harlan on 2019/2/13.
 *
 * 线程池 视频库所有线程应放入此处
 */
public class ThreadManager {

    private final ExecutorService mExecutorService;

    public ThreadManager() {

        mExecutorService = Executors.newFixedThreadPool(10);//开启的线程的总数量
    }

    public void AccOnRunnable(Runnable runnable) {
        if (runnable!=null){
            mExecutorService.execute(runnable);
        }else {
            Log.d("DataThreadUtils","Runnable == NULL");
        }
    }
}
