package cn.xag.xagclientmultimefialib.jniutils;

import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import cn.xag.xagclientmultimefialib.model.H264DataManager;
import cn.xag.xagclientmultimefialib.utils.FiFoUtlis;

/**
 * Created by harlan on 2019/2/28.
 */
public class  FFmpegDecodH264 {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avfilter");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
    }

    public int syncStream(byte[] data, int len) {
        int count = FiFoUtlis.getInstance().FiFoRead(data, len);
        return count;
    }

    public void pushFrameBuffer(ByteBuffer byteBuffer){
        BlockingQueue<byte[]> h264dataQueue = H264DataManager.getInstance().getH264dataQueue();
        h264dataQueue.offer(byteBuffer.array());
    }


    //软解
    public  native void startVideoStream(Object id, Surface surface);



    //rtsp实时解码
    public native int startRtpsVideo(String url,Surface surface);



}
