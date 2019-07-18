package cn.xag.xagclientmultimefialib.acb1usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieanping on 16/3/30.
 */
public class UsbDeviceCommunication {

    final String TAG = UsbDeviceCommunication.class.getSimpleName();

    private UsbDevice mUsbDevice;

    public UsbDeviceConnection getDeviceConnection() {
        return mDeviceConnection;

    }

    private UsbDeviceConnection mDeviceConnection;
    private List<UsbInterface> mInterfaces;
    private UsbInterface comInterface;



    public UsbDeviceCommunication(UsbDevice device) {
        mUsbDevice = device;
        findInterface();
        if (mInterfaces != null) {
            for (UsbInterface usbInterface : mInterfaces) {
                assignEndpoint(usbInterface);
            }
        }
    }

    public boolean openDevice(UsbManager manager) {
        if (comInterface != null) {
            UsbDeviceConnection connection = null;
            // 判断是否有权限
            if (manager.hasPermission(mUsbDevice)) {
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                connection = manager.openDevice(mUsbDevice);
                if (connection == null) {
                    return false;
                }
                if (connection.claimInterface(comInterface, true)) {
                    Log.i(TAG, "找到接口");
                    mDeviceConnection = connection;
                    return true;
                } else {
                    connection.close();
                }
            } else {
                Log.i(TAG, "没有权限");
            }
        } else {
            Log.i(TAG, "没有找到接口");
        }
        return false;
    }

    public void close() {
        if (mDeviceConnection != null)
            mDeviceConnection.close();
    }

    public int send(byte[] buffer, int offset, int length) {
        if (mDeviceConnection != null && epBulkOut != null) {
            return mDeviceConnection.bulkTransfer(epBulkOut.get(0), buffer, offset, length, 0);
        }
        return 0;
    }

    public int receive(byte[] buffer, int offset, int length, int timeout) {
        if (mDeviceConnection != null && epBulkIn != null) {
            return mDeviceConnection.bulkTransfer(epBulkIn.get(0), buffer, offset, length, timeout);
        }
        return 0;
    }


    private void findInterface() {
        if (mUsbDevice == null) {
            Log.i(TAG, "没有找到设备");
            return;
        }
        mInterfaces = new ArrayList<>();

        for (int i = 0; i < mUsbDevice.getInterfaceCount();i++) {
            // 获取设备接口，一般都是一个接口，你可以打印getInterfaceCount()方法查看接
            // 口的个数，在这个接口上有两个端点，OUT 和 IN
            UsbInterface intf = mUsbDevice.getInterface(i);
            Log.d(TAG, i + " " + intf);
//            mInterface = intf;
            mInterfaces.add(intf);
//            break;
        }

    }

//    private UsbEndpoint epOut;
//    private UsbEndpoint epIn;
//
//    // 用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
//    private void getEndpoint(UsbInterface intf) {
//
//        if (intf.getEndpoint(1) != null) {
//
//            epOut = intf.getEndpoint(1);
//        }
//        if (intf.getEndpoint(0) != null) {
//            epIn = intf.getEndpoint(0);
//        }
//
//    }

    public ArrayList<UsbEndpoint> epBulkOut = new ArrayList<UsbEndpoint>();
    public ArrayList<UsbEndpoint> epBulkIn = new ArrayList<UsbEndpoint>();
    public ArrayList<UsbEndpoint> epControl = new ArrayList<UsbEndpoint>();
    public ArrayList<UsbEndpoint> epIntEndpointOut = new ArrayList<UsbEndpoint>();
    public ArrayList<UsbEndpoint> epIntEndpointIn = new ArrayList<UsbEndpoint>();


    // 分配端点，IN | OUT，即输入输出；可以通过判断
    private UsbEndpoint assignEndpoint(UsbInterface mInterface) {

        for (int i = 0; i < mInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = mInterface.getEndpoint(i);
            // look for bulk endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epBulkOut.add(ep);
                    System.out.println("Find the BulkEndpointOut," + "index:"
                            + i + "," + "使用端点号："
                            + ep.describeContents());
                } else {
                    epBulkIn.add(ep);
                    System.out
                            .println("Find the BulkEndpointIn:" + "index:" + i
                                    + "," + "使用端点号："
                                    + ep.describeContents());
                }

                comInterface = mInterface;
                Log.d(TAG, "communication use:" + comInterface);
            }
            // look for contorl endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL) {
                epControl.add(ep);
                System.out.println("find the ControlEndPoint:" + "index:" + i
                        + "," + ep.describeContents());
            }
            // look for interrupte endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epIntEndpointOut.add(ep);
                    System.out.println("find the InterruptEndpointOut:"
                            + "index:" + i + ","
                            + ep.describeContents());
                }
                if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                    epIntEndpointIn.add(ep);
                    System.out.println("find the InterruptEndpointIn:"
                            + "index:" + i + ","
                            + ep.describeContents());
                }
            }
        }
//        if (epBulkOut == null && epBulkIn == null && epControl == null
//                && epIntEndpointOut == null && epIntEndpointIn == null) {
//            throw new IllegalArgumentException("not endpoint is founded!");
//        }
        if (epBulkOut.size() == 0 && epBulkIn .size() == 0 && epControl.size() == 0
                && epIntEndpointOut.size() == 0 && epIntEndpointIn.size() == 0) {
            throw new IllegalArgumentException("not endpoint is founded!");
        }
        return epIntEndpointIn.get(0);
    }

}
