package cn.xag.xagclientmultimefialib.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import cn.xag.xagclientmultimefialib.helper.H264Decoder;

/**
 * Created by harlan on 2019/4/15.
 */
public class H264DataManager {

    private static H264DataManager instance;
    public static synchronized H264DataManager getInstance() {
        if (instance == null) {

            synchronized (GlobalDataManager.class) {

                if (instance == null)
                    instance = new H264DataManager();
            }
        }
        return instance;
    }

    public BlockingQueue<byte[]> getH264dataQueue() {
        return h264dataQueue;
    }

    private BlockingQueue<byte[]> h264dataQueue = new ArrayBlockingQueue<byte[]>(1000);

    public H264Decoder getH264DecoderUtlis() {
        return h264DecoderUtlis;
    }

    public void setH264DecoderUtlis(H264Decoder h264DecoderUtlis) {
        this.h264DecoderUtlis = h264DecoderUtlis;
    }

    private H264Decoder h264DecoderUtlis;
}
