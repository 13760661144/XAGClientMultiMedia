package cn.xag.xagclientmultimedia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;



import java.io.FileOutputStream;

import cn.xag.xagclientmultimefialib.ExternalRequest;
import cn.xag.xagclientmultimefialib.TypeGlobal;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FileOutputStream out = null;
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        findViewById(R.id.surface);

        out = null;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ExternalRequest.getInstance().setRequestType(this, TypeGlobal.CommTheWay.IS_AOA, TypeGlobal.FrameType.IS_HARDWARE_SOLUTIONS,null);

        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQ_CODE);
        }
    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        // sfv_video.onPause();
        MainActivity.this.finish();
        onDestroy();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onStop() {
        super.onStop();
     //   mSwitchingView.onStop();
        Log.e("ActivityYuvOrRgbViewer", "onStop");
    }

    @Override
    public void onClick(View v) {

    }

}
