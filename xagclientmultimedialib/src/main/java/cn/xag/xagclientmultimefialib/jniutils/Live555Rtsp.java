package cn.xag.xagclientmultimefialib.jniutils;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import cn.xag.xagclientmultimefialib.aoausb.FiFoUtlis;


/**
 * Created by harlan on 2019/4/4.
 */
public class Live555Rtsp {
    static {
        System.loadLibrary("native-lib");
    }
    private  static RTSPVideoListener mVideoListener;
    public void onNativeCallBack(byte[] data, int len) throws IOException {
        // 获取的数据回调，加入自己的逻辑
        Log.i("Live555Rtsp","len: "+len+" byte[]: "+data.toString());
        if(null != mVideoListener) mVideoListener.videoCallBack(data,len);
    }


    public  void onNativeInfo(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            return;
        }
        Log.d("RTSPClient", errorMsg);
    }


    private static native int Live555Rtsp(String path);

    public static void Play(String path){
        Live555Rtsp(path);
    }


    public static void setRTSPVideoListener(RTSPVideoListener listener) {
        mVideoListener = listener;
    }



    public interface RTSPVideoListener {
        void videoCallBack(byte[] data, int len);
    }


}
