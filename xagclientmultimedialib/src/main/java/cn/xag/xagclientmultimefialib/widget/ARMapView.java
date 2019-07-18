/*
package cn.xag.xagclientmultimefialib.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.ZoomButtonsController;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import cn.xag.xagclientmultimefialib.R;
import cn.xag.xagclientmultimefialib.osdmroid.ArrowPoint;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

*/
/**
 * Created by 郑梦晨 on 2019/1/22.
 *//*

public class ARMapView extends RelativeLayout {

    private Context mContext;
    private IMapController mMapController;
    private MapView mMapView;
    private ArrowPoint point;

    public ARMapView(Context context) {
        super(context);
        mContext = context;
        initUi();
    }

    public ARMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ARMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initUi() {

        this.setBackgroundColor(Color.BLACK);
        mMapView = new MapView(mContext);
      //  mMapView.setTileSource(TileSourceFactory.HIKEBIKEMAP);
        this.addView(mMapView);
        mMapView.setClickable(true);
        //显示地图下方的缩放按钮
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapController = mMapView.getController();
        //设置初始化缩放级别
        mMapController.setZoom(14);
        //添加比例尺
        ScaleBarOverlay scaleBar = new ScaleBarOverlay(mMapView);
        mMapView.getOverlays().add(scaleBar);
        //初始化放大缩小控制器
        initZoomController();
        //设置中心点
        GeoPoint geoPoint = new GeoPoint(39.901873, 116.326655);
        mMapController.setCenter(geoPoint);

        geoPoint = new GeoPoint(39.9086536380, 116.3977031282);
        mMapController.setCenter(geoPoint);
        final ArrowPoint arrowPoint = new ArrowPoint();
        arrowPoint.geoPoint = geoPoint;
        arrowPoint.imgResource = R.mipmap.icon_geo;//R.mipmap.star;
        Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                addImg(arrowPoint);
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<Object>() {
            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
              mMapView.invalidate();
            }
        });
        addMarker(mMapView, geoPoint, "Start Point");
        addingWaypoints(mMapView,geoPoint);
    }
    private void initZoomController() {

        Field f = null;
        try {
            f = mMapView.getClass().getDeclaredField("mZoomController");//通过反射找到缩放的控制器
            f.setAccessible(true);
            System.out.println("反射类中所有的方法");
            Method[] fm = Class.forName("android.widget.ZoomButtonsController").getMethods();
            for (int i = 0; i < fm.length; i++) {
                if (fm[i].getName().equals("setOnZoomListener")) {
                    fm[i].setAccessible(true);
                    fm[i].invoke((ZoomButtonsController) f.get(mMapView), new ZoomButtonsController.OnZoomListener() {
                        @Override
                        public void onVisibilityChanged(boolean b) {

                        }

                        @Override
                        public void onZoom(boolean b) {
                            if (b) {
                                mMapView.getController().zoomIn();
                            } else {
                                mMapView.getController().zoomOut();
                            }

                        }
                    });
                }
                System.out.println("fm:" + fm[i].getName() + "____"
                        + fm[i].getReturnType().getName());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void addMarker( MapView map, GeoPoint point, String title) {
        Marker startMarker =new Marker(map);
        //Lat ‎23.746466 Lng 90.376015
        startMarker.setPosition(point);
        startMarker.setTitle(title);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        map.invalidate();
    }
    private void addingWaypoints( MapView map, GeoPoint startPoint) {
        ArrayList waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(new GeoPoint(39.901873, 116.326655));
        GeoPoint endPoint = new GeoPoint(39.301873, 117.326655);
        waypoints.add(endPoint);

        addMarker(map, endPoint, "End Point");
        addPolyLine(map, waypoints);

    }

    private void addPolyLine(MapView map,ArrayList<GeoPoint> waypoints) {
        //add your points here
        Polyline line = new Polyline();//see note below!
        line.setPoints(waypoints);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
               // Toast.makeText(this, "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
               return true;
            }
        });
        map.getOverlays().add(line);
        map.invalidate();
    }


    */
/**
     * 添加图片
     *
     * @param point
     * @return
     *//*

    private void addImg(ArrowPoint point) {
        this.point = point;
        OSRMRoadManager osrmRoadManager = new OSRMRoadManager(mContext);
//        RotateAnimation rotateAnimation = new RotateAnimation(0f,(float) degree);
//        rotateAnimation.
        ArrayList<GeoPoint> arrayList=new ArrayList<>();
        arrayList.add(new GeoPoint(30.816237, 100.366725));
        arrayList.add(new GeoPoint(30.776498, 100.373592));
        Road road = osrmRoadManager.getRoad(arrayList);
        Polyline polyline = RoadManager.buildRoadOverlay(road);
        mMapView.getOverlays().add(polyline);

        Drawable drawable = mMapView.getContext().getResources().getDrawable(R.mipmap.icon_geo);

        for (int i=0 ;i<road.mNodes.size();i++){
            RoadNode roadNode = road.mNodes.get(i);
            Marker marker = new Marker(mMapView);
            marker.setPosition(roadNode.mLocation);
            marker.setIcon(drawable);
            marker.setTitle("你好");
            mMapView.getOverlays().add(marker);
            marker.setSnippet(roadNode.mInstructions);
            marker.setSubDescription( road.getLengthDurationText(mContext,roadNode.mLength,roadNode.mDuration));
        }
    }


    public void onStop(){

    }

    */
/**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     *//*



}
*/
