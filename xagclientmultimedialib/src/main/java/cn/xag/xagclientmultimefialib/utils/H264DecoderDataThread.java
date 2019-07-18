package cn.xag.xagclientmultimefialib.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import cn.xag.xagclientmultimefialib.model.GlobalDataManager;
import cn.xag.xagclientmultimefialib.model.H264DataManager;

/**
 * Created by harlan on 2019/4/15.
 * h264获取码流数据的线程0
 */
public class H264DecoderDataThread extends Thread {
    @Override
    public void run() {
        super.run();
        while (GlobalDataManager.getInstance().isRunning()) {
            try {
                byte[] poll = H264DataManager.getInstance().getH264dataQueue().poll();
                Log.d("H264DecoderDataThread", "run: "+poll.length);
                if (poll == null) {
                    Thread.sleep(50);
                    continue;
                }
                if (H264DataManager.getInstance().getH264DecoderUtlis() != null) {
                    if (!H264DataManager.getInstance().getH264DecoderUtlis().onFrame(poll, 0, poll.length)) {
                        Thread.sleep(1);
                    }
                }
            } catch (Exception e) {
                //  e.printStackTrace();
            }
        }

        H264DataManager.getInstance().getH264dataQueue().clear();
    }
}
