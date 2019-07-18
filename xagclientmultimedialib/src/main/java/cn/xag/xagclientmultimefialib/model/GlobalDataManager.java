package cn.xag.xagclientmultimefialib.model;

/**
 * Created by harlan on 2019/4/15.
 */
public class GlobalDataManager {

    private static GlobalDataManager instance;
    public static synchronized GlobalDataManager getInstance() {
        if (instance == null) {

            synchronized (GlobalDataManager.class) {

                if (instance == null)
                    instance = new GlobalDataManager();
            }
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    private boolean isRunning;
}
