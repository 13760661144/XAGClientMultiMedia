package cn.xag.xagclientmultimefialib.acb1usb;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

import cn.xag.xagclientmultimefialib.utils.FiFoUtlis;

public class Receiver {
    final String TAG = "Receiver";

    UsbEndpoint endpoint;
    UsbDeviceConnection connection;
    boolean endflag = false;
    EndpointReceiver receiver;
    private final long l;
    private FileOutputStream fileOutputStream;
    private final File file;

    int diex;

    private int num;
    private int oldnum;


    public void setCallBack(ReceiverCallBack callBack) {
        this.callBack = callBack;
    }

    ReceiverCallBack callBack;

    byte[] buffer = new byte[8192];


    int counter;


    public Receiver(UsbEndpoint endpoint, UsbDeviceConnection connection) throws FileNotFoundException {
        this.endpoint = endpoint;
        this.connection = connection;
        l = System.currentTimeMillis();
        file = new File(Environment.getExternalStorageDirectory(),
                "ee.txt");
        counter = 0;

    }

    public void reset() {
        counter = 0;
        endflag = false;
    }

    public int getCounter() {
        return counter;
    }

    public void startReceive() {

        receiver = new EndpointReceiver();
        receiver.executeOnExecutor(USBApplication.getExecutor());

        Log.d(TAG, endpoint.getAddress() + " start " + this.toString() + "," + receiver.hashCode());
    }

    public void endReceiver() {
        endflag = true;
    }

    class EndpointReceiver extends AsyncTask<Void, Integer, Void> {
        String TAG = "EndpointReceiver";

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int cnt;
            try {
                endflag = false;
                Random random = new Random();
                Thread.sleep(20 + random.nextInt(20));
                while (endflag != true) {
                    cnt = connection.bulkTransfer(endpoint, buffer, 0, 8192, 100);
                    if (cnt > 0) {
                        counter += cnt;
                        this.publishProgress(cnt);
                        byte[] frameBytes = new byte[cnt];
                        System.arraycopy(buffer, 0, frameBytes, 0, cnt);
                        FiFoUtlis.getInstance().FiFoWrite(frameBytes, frameBytes.length);
                        fileOutputStream.write(frameBytes);
                        fileOutputStream.flush();
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);
            if (values[0] > 0) {
                if (callBack != null) {
                    callBack.onReceiverProgress(counter);
                }
            }
        }


    }

    interface ReceiverCallBack {
        void onReceiverProgress(int cnt);
    }


}
