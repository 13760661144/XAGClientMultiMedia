package cn.xag.xagclientmultimefialib.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.TypeGlobal;
import cn.xag.xagclientmultimefialib.drawer.CameraDrawer;
import cn.xag.xagclientmultimefialib.jniutils.FFmpegDecodH264;
import cn.xag.xagclientmultimefialib.model.GlobalDataManager;
import cn.xag.xagclientmultimefialib.model.H264DataManager;
import cn.xag.xagclientmultimefialib.utils.H264DecoderDataThread;
import cn.xag.xagclientmultimefialib.helper.H264Decoder;

/**
 * Created by harlen on 2019/1/11.
 */
public class XAGGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private CameraDrawer mCameraDrawer;
    private int mScreenWidth;
    private int mScreenHeight;
    private Context mContext;
    private boolean isRunning;
    private H264DecoderDataThread mH264DecoderDataThread;

    public XAGGLSurfaceView(Context context) {
        super(context);
        mContext = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        /**初始化Camera的绘制类*/
        mCameraDrawer = new CameraDrawer(getResources(), context);
        init();
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


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Surface surface = null;
        mCameraDrawer.onSurfaceCreated(gl, config);
        mCameraDrawer.setPreviewSize(mScreenWidth, mScreenHeight);
        SurfaceTexture texture = mCameraDrawer.getTexture();
        texture.setOnFrameAvailableListener(this);
        surface = new Surface(texture);
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

            if (surface != null) {
                H264Decoder h264DecoderUtlis = new H264Decoder(surface, "video/avc", 1920, 1080, 25);
                H264DataManager.getInstance().setH264DecoderUtlis(h264DecoderUtlis);
            } else {
                return;
            }

        } else if (TypeGlobal.getInstance().getFrameType() == TypeGlobal.FrameType.IS_SOFT_SOLUTION) {

            final Surface finalSurface = surface;

            Runnable playRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (XAGSurfaceView.class) {
                        FFmpegDecodH264 fFmpegDecodH264 = new FFmpegDecodH264();
                        fFmpegDecodH264.startVideoStream(fFmpegDecodH264, finalSurface);
                        //  fFmpegDecodH264.startRtpsVideo("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov",surface1);
                    }

                }
            };
            threadManager.AccOnRunnable(playRunnable);
        }
        final Surface finalSurface = surface;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //          mMainActivity.startVideoStream1(mMainActivity, finalSurface);
            }
        }).start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d("onDrawFrame", "onDrawFrame");
        mCameraDrawer.onDrawFrame(gl);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

}
