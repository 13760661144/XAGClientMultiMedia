package cn.xag.xagclientmultimefialib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


/**
 * Created by harlan on 2019/1/22.
 * <p>
 * 地图与视频播放器的切换控件
 */
public class SwitchingView extends FrameLayout  {

    private Context mContext;
    private XAGSurfaceView mXAGSurfaceView;
    private XAGGLSurfaceView mXAGGLSurfaceView;
  //  private ARMapView mArMapView;
    boolean isSurfaceViewBig = true;
    private RelativeLayout mRll;
    private SurfaceView mSurfaceView;

    public SwitchingView(Context context) {
        super(context);
        mContext = context;
        initUI();
    }

    public SwitchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initUI();
    }

    public SwitchingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initUI();
    }

    private void initUI() {
        LayoutParams mLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout frameLayout = new FrameLayout(mContext);
        this.addView(frameLayout, mLp);

        mRll = new RelativeLayout(mContext);
        frameLayout.addView(mRll, mLp);
        mXAGSurfaceView = new XAGSurfaceView(mContext);
        mRll.addView(mXAGSurfaceView,mLp);

    }

   public SurfaceView getmSurfaceView(){
        if (mSurfaceView==null)
            return null;
        return mSurfaceView;
   }


    public void onStop() {
       // mArMapView.onStop();
    }

    public void onStart() {

    }
}
