package cn.xag.xagclientmultimefialib.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import cn.xag.xagclientmultimefialib.TypeGlobal;
import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.jniutils.FFmpegDecodH264;
import cn.xag.xagclientmultimefialib.model.GlobalDataManager;
import cn.xag.xagclientmultimefialib.model.H264DataManager;
import cn.xag.xagclientmultimefialib.utils.H264DecoderDataThread;
import cn.xag.xagclientmultimefialib.utils.H264DecoderUtlis;

/**
 * Created by 郑梦晨 on 2019/1/17.
 */
public class XAGSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private Context mContext;
    private boolean isRunning;
    private H264DecoderDataThread mH264DecoderDataThread;

    public XAGSurfaceView(Context context) {
        super(context);
        this.mContext = context;
        init();

    }

    public XAGSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    private void init() {
        this.isRunning = true;
        GlobalDataManager.getInstance().setRunning(isRunning);
        this.getHolder().addCallback(this);
        if (mH264DecoderDataThread == null) {
            mH264DecoderDataThread = new H264DecoderDataThread();
            mH264DecoderDataThread.start();
        }

    }

    public XAGSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        ThreadManager threadManager = new ThreadManager();

        if (TypeGlobal.getInstance().getFrameType() == TypeGlobal.FrameType.IS_HARDWARE_SOLUTIONS) {

            try {
                if (H264DataManager.getInstance().getH264DecoderUtlis() != null) {

                    H264DataManager.getInstance().getH264DecoderUtlis().DecoderClose();
                    H264DataManager.getInstance().setH264DecoderUtlis(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (holder.getSurface() != null) {
                H264DecoderUtlis h264DecoderUtlis = new H264DecoderUtlis(holder.getSurface(), "video/avc", 1920, 1080, 25);
                H264DataManager.getInstance().setH264DecoderUtlis(h264DecoderUtlis);
            } else {
                return;
            }

        } else if (TypeGlobal.getInstance().getFrameType() == TypeGlobal.FrameType.IS_SOFT_SOLUTION) {

            Runnable playRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (XAGSurfaceView.class) {
                        FFmpegDecodH264 fFmpegDecodH264 = new FFmpegDecodH264();
                        fFmpegDecodH264.startVideoStream(fFmpegDecodH264, holder.getSurface());
                        //  fFmpegDecodH264.startRtpsVideo("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov",surface1);
                    }

                }
            };
            threadManager.AccOnRunnable(playRunnable);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
