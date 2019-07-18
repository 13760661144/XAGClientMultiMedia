package cn.xag.xagclientmultimefialib.acb1usb;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class USBApplication extends Application {

    static USBApplication application;
    static Executor e = Executors.newScheduledThreadPool(5);
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    static USBApplication getInstance(){
        return application;
    }

    static Executor getExecutor(){
        return e;
    }
}
