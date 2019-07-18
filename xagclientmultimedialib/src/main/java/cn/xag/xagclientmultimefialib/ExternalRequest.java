package cn.xag.xagclientmultimefialib;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cn.xag.xagclientmultimefialib.acb1usb.Acb1Create;
import cn.xag.xagclientmultimefialib.aoausb.AOACreate;
import cn.xag.xagclientmultimefialib.live555rtsp.Live555Create;

/**
 * Created by harlan on 2019/4/15.
 * 整个库核心功能的外部回调
 */
public class ExternalRequest {

    private static ExternalRequest instance;

    private TypeGlobal.CommTheWay mCommTheWay;

    private TypeGlobal.FrameType mFrameType;

    private String TAG = "ExternalRequest";

    private Context mContext;

    private String mUrl;

    public static synchronized ExternalRequest getInstance() {
        if (instance == null) {

            synchronized (ExternalRequest.class) {

                if (instance == null)
                    instance = new ExternalRequest();
            }
        }

        return instance;
    }

    public ExternalRequest() {

    }

    /**
     * 设置请求模式与解码类型
     *
     * @param commTheWay 请求模式
     * @param frameType  解码类型
     * @param URL        rtsp的请求url
     */
    public void setRequestType(Context context, TypeGlobal.CommTheWay commTheWay, TypeGlobal.FrameType frameType, String URL) {

        if (commTheWay != null && frameType != null && context != null) {
            this.mCommTheWay = commTheWay;
            this.mFrameType = frameType;
            this.mContext = context;
            this.mUrl = URL;
        } else {
            Log.e(TAG, "ExternalRequest or setRequestType == null !!!缺少必要参数");
            return;
        }

        TypeGlobal.getInstance().setFrameType(mFrameType);
        TypeGlobal.getInstance().setCommTheWay(mCommTheWay);

        initXAGVideoPlayer();

    }

    private void initXAGVideoPlayer() {

        if (TypeGlobal.getInstance().getCommTheWay() == TypeGlobal.CommTheWay.IS_AOA) { //aoa
            try {
                AOACreate aoaCreate = new AOACreate(mContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (TypeGlobal.getInstance().getCommTheWay() == TypeGlobal.CommTheWay.IS_RTSP) { //wifi

            Live555Create live555Create = new Live555Create(mUrl);

        } else if (TypeGlobal.getInstance().getCommTheWay() == TypeGlobal.CommTheWay.IS_ACB1) {
            Acb1Create acb1Create = new Acb1Create(mContext);
        }
    }

}
