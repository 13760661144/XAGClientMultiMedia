package cn.xag.xagclientmultimefialib.live555rtsp;

import android.util.Log;

import cn.xag.xagclientmultimefialib.TypeGlobal;
import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.jniutils.Live555Rtsp;
import cn.xag.xagclientmultimefialib.helper.FindAFrameHelper;

/**
 * Created by harlan on 2019/4/11.
 */
public class Live555Create {

    private String mUrl;
    private String TAG = "Live555Create";

    public Live555Create(String url) {
        if (url != null) {
            this.mUrl = url;
        } else {
            Log.e(TAG,"mUrl == null 使用rtsp必须传入url");
            return;
        }
        ThreadManager threadManager = new ThreadManager();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Live555Rtsp.Play(mUrl);
            }
        };
        threadManager.AccOnRunnable(runnable);

        Live555Rtsp.setRTSPVideoListener(new Live555Rtsp.RTSPVideoListener() {
            @Override
            public void videoCallBack(byte[] data, int len) {
               if (TypeGlobal.getInstance().getCommTheWay()==TypeGlobal.CommTheWay.IS_RTSP){
                    FindAFrameHelper.getInstance().makeSpsPps(data);
                }
            }
        });
    }
}
