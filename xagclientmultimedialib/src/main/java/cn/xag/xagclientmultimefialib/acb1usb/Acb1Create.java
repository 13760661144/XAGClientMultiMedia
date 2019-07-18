package cn.xag.xagclientmultimefialib.acb1usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.TypeGlobal;
import cn.xag.xagclientmultimefialib.aoausb.FiFoUtlis;
import cn.xag.xagclientmultimefialib.jniutils.Live555Rtsp;
import cn.xag.xagclientmultimefialib.model.H264DataManager;
import cn.xag.xagclientmultimefialib.utils.FindAFrameUtlis;

/**
 * Created by harlan on 2019/4/16.
 */
public class Acb1Create {

    UsbDevice comDevice;
    UsbDeviceCommunication communication;
    private UsbManager manager;

    private List<UsbDevice> usbdeviceList = new ArrayList<>();
    HashMap<UsbEndpoint, String> endPointMap = new HashMap<UsbEndpoint, String>();
    private Context mContext;

    private String TAG = "Acb1Create";

    public Acb1Create(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return;
        } else {
            Log.i(TAG, "usb设备：" + String.valueOf(manager.toString()));
        }
        HashMap deviceList = manager.getDeviceList();

        Log.i(TAG, "usb设备：" + String.valueOf(deviceList.size()));
        Iterator deviceIterator = deviceList.values().iterator();
        usbdeviceList.clear();
        while (deviceIterator.hasNext()) {
            UsbDevice device = (UsbDevice) deviceIterator.next();
            usbdeviceList.add(device);

            Log.d(TAG, "found vid:" + device.getVendorId() + ",pid:" + device.getProductId());
        }

        if (usbdeviceList != null && usbdeviceList.size() > 0) {
            comDevice = usbdeviceList.get(0);

        }
//                GrantUsbPermission.test(getApplication(), comDevice);
//                GrantUsbPermission.grantAutomaticPermission(getApplicationContext(), comDevice);

        if (comDevice != null) {
            communication = new UsbDeviceCommunication(comDevice);
            communication.openDevice(manager);
        }


        String text = "EpIn: \n";
        if (communication!=null){
            for (final UsbEndpoint u : communication.epBulkIn) {
                text += u.toString() + "\n";

//                    if(u.getAddress() == 131)
                {
                    Log.d("startReceive", u.getAddress() + "start");

                    Receiver r = null;
                    try {
                        r = new Receiver(u, communication.getDeviceConnection());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    r.setCallBack(new Receiver.ReceiverCallBack() {
                        @Override
                        public void onReceiverProgress(int cnt) {
                            endPointMap.put(u, "Receive:" + cnt);
                        }
                    });
                    r.startReceive();
                }
            }
        }

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


}
