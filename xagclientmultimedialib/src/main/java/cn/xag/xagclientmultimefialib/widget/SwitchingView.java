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
public class SwitchingView extends FrameLayout implements View.OnClickListener {

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
       // mSurfaceView = new SurfaceView(mContext);
      //  mXAGSurfaceView = new XAGSurfaceView(mContext);
      //  mRll.addView(mXAGSurfaceView,mLp);
        mXAGSurfaceView = new XAGSurfaceView(mContext);
        mRll.addView(mXAGSurfaceView,mLp);

       // VideoUtils.LocalShow.addView(mTestView);

      //  mTestView.setOnClickListener(this);
      //  mRll.addView(mSurfaceView,mLp);

    /*    mLp = new LayoutParams(500, 300);
        mArMapView = new ARMapView(this.mContext);
        frameLayout.addView(mArMapView, mLp);
        mArMapView.setOnClickListener(this);
        zoomOpera(mRll, mTestView, mArMapView, null);*/

    }

   public SurfaceView getmSurfaceView(){
        if (mSurfaceView==null)
            return null;
        return mSurfaceView;
   }

    /**
     * 大小视图切换 （小视图在前面、大视图在后面）
     *
     * @param sourcView  之前相对布局大小
     * @param beforeview 之前surfaceview
     * @param detView    之后相对布局大小
     * @param afterview  之后的surfaceview
     */
    private void zoomOpera(View sourcView, TextureView beforeview,
                           View detView, TextureView afterview) {
        if (beforeview != null) {
            if (beforeview.getVisibility() == GONE) {
                beforeview.setVisibility(VISIBLE);
            }
        }
        if (afterview != null) {
            if (afterview.getVisibility() == GONE) {
                afterview.setVisibility(VISIBLE);
            }
        }
        FrameLayout paretview = (FrameLayout) sourcView.getParent();
        paretview.removeView(detView);
        paretview.removeView(sourcView);
        int defaultLocalwidth = 500;
        int defaultLocalHeight = 300;
        int defaultLocalMargin = 20;
        //设置远程大视图RelativeLayout 的属性
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        sourcView.setLayoutParams(params1);

        //设置本地小视图RelativeLayout 的属性
        params1 = new RelativeLayout.LayoutParams(defaultLocalwidth, defaultLocalHeight);
        params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params1.setMargins(0, defaultLocalMargin, defaultLocalMargin, 0);

        detView.setLayoutParams(params1);

        paretview.addView(sourcView);
        paretview.addView(detView);
     //   mArMapView.initUi();
    }

    @Override
    public void onClick(View v) {
/*
        mArMapView.setVisibility(GONE);
        mTestView.setVisibility(GONE);
        if (v == mArMapView) {
            if (isSurfaceViewBig) {
                zoomOpera(mArMapView, null, mRll, mTestView);
                isSurfaceViewBig = false;
            } else {
                zoomOpera(mRll, mTestView, mArMapView, null);
                isSurfaceViewBig = true;
            }

        } else if (v == mTestView) {

            if (isSurfaceViewBig) {
                zoomOpera(mRll, mTestView, mArMapView, null);
                isSurfaceViewBig = false;
            } else {
                zoomOpera(mArMapView, null, mRll, mTestView);
                isSurfaceViewBig = true;
            }
        }*/
    }

    public void onStop() {
       // mArMapView.onStop();
    }

    public void onStart() {

    }
}
