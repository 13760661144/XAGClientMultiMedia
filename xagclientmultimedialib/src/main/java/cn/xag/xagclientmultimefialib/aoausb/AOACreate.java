package cn.xag.xagclientmultimefialib.aoausb;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.TypeGlobal;
import cn.xag.xagclientmultimefialib.model.H264DataManager;
import cn.xag.xagclientmultimefialib.utils.FindAFrameUtlis;

/**
 * Created by harlen on 2019/1/11.
 */
public class AOACreate  {

    private static final String ACTION_USB_PERMISSION = "com.xaircraft.USB_PERMISSION";//aoa接口协议

    private Context mContext;
    private  UsbManager mUsbManager;
    private ParcelFileDescriptor mConnection;
    private FileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    private AoADataListener mAoADataListener;

    public AOACreate(Context mContext) throws IOException {
        this.mContext = mContext;
        init();
        tryOpenAccessory(mContext);
        initData();
    }

    private void initData() {
        ThreadManager threadManager = new ThreadManager();
        Runnable artosynPlayerError = new Runnable() {
            @Override
            public void run() {
                final FindAFrameUtlis decodeUtlis = new FindAFrameUtlis();
                while (true) {
                    if (FiFoUtlis.getInstance().getActualSize() < 0)
                        return;
                    decodeUtlis.searchFrame(new FindAFrameUtlis.FrameBufferListener() {
                        @Override
                        public void isPushFrameBuffer(ByteBuffer byteBuffer) {
                            if (byteBuffer == null)
                                return;
                            BlockingQueue<byte[]> h264dataQueue = H264DataManager.getInstance().getH264dataQueue();
                            h264dataQueue.offer(byteBuffer.array());
                        }
                    });
                }
            }
        };
        threadManager.AccOnRunnable(artosynPlayerError);
    }


    public void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbReceiver, filter);
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        new AoAData(this);//初始化data类
    }





    /*
     * usb接口通信广播
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //aoa握手通信
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == action) {
                Parcelable parcelableExtra = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("TAG", "ATTACHED: " + parcelableExtra.toString());
            }
            if (UsbManager.ACTION_USB_ACCESSORY_DETACHED == action) {
                Parcelable parcelableExtra = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("TAG", "**DETACHED: " + parcelableExtra.toString());
            }

        }
    };

    //打开aoa
    public void openAoa(){
        UsbAccessory accessory = getAccessory();
        mConnection = mUsbManager.openAccessory(accessory);
        mFileDescriptor = mConnection.getFileDescriptor();
        mInputStream = new FileInputStream(mFileDescriptor);
        if (mAoADataListener==null)
            return;
        if (mInputStream == null) {
            return;
        }
        //回调到数据类AoAdata处理
        mAoADataListener.onDataInputStream(mInputStream);

    }


    private UsbAccessory getAccessory() {
        UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
        if (accessoryList != null && accessoryList.length != 0) {
            return accessoryList[0];
        } else {
            return null;
        }
    }


    public void tryOpenAccessory(Context context) throws IOException {
        UsbManager systemService = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessoryList = systemService.getAccessoryList();
        if (accessoryList != null && accessoryList.length > 0 && mContext != null) {
           openAoa();
        }
    }


    public void setOnDataInputStream( AoADataListener aoADataListener){
        this.mAoADataListener=aoADataListener;
    }


}
