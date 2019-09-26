package cn.xag.xagclientmultimefialib.aoausb;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import cn.xag.xagclientmultimefialib.ThreadManager;
import cn.xag.xagclientmultimefialib.utils.FiFoUtlis;

/**
 * Created by harlan on 2019/4/8.
 */
public class AoAData implements AoADataListener {

    private AOACreate mAoaCreate;

    private Boolean mOpened;
    private FileInputStream mInputStream;

    public AoAData(AOACreate aoaCreate) {
        this.mAoaCreate = aoaCreate;
        mAoaCreate.setOnDataInputStream(this);
    }

    @Override
    public void onDataInputStream(FileInputStream inputStream) {

        this.mInputStream = inputStream;
        mOpened = true;

        ThreadManager threadManager = new ThreadManager();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sourceDatatraversal();
            }
        };

        threadManager.AccOnRunnable(runnable);
    }

    private void sourceDatatraversal() {

        int errorCount = 0;
        int BUFFER_SIZE = 512;
        byte[] buffer = new byte[BUFFER_SIZE];
        long oldTime = System.currentTimeMillis();
        boolean isVideo = true;
        int AFTDatalength = 0;
        while (mOpened) {
            try {
                int read = mInputStream.read(buffer);
               // Log.d("isPushFrameBuffer", "frameBufferListener" + read);
                if (read < 0) {
                    return;
                }

                byte[] bytes = Arrays.copyOf(buffer, read);

                if (bytes.length != 0) {
                    if (oldTime + 30000L > System.currentTimeMillis())
                    //    mOutputStream.write(bytes);
                    if (bytes.length <= 0) {
                        return;
                    }

                    for (int i = 0; i < bytes.length; i++) {
                        //区分视频流与其他流
                        if (i < bytes.length - 8 && bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x02) {
                            int length = bytes[i + 6];
                            int length1 = bytes[i + 7];
                            AFTDatalength = length1 + 8 + length * 256;
                            if (bytes.length < 511) {
                                Log.d("AoAData","isXFData"+byte2hex(bytes));
                                break;
                            } else {
                                isVideo = false;
                                AFTDatalength = AFTDatalength - 511;
                            }

                        } else {
                            if (isVideo) {
                                Log.d("AoAData", "isVideo"+String.valueOf(bytes.length));
                                //存储数据到解码器
                                FiFoUtlis.getInstance().FiFoWrite(bytes, bytes.length);
                                break;
                            } else if (AFTDatalength > 0) {
                                Log.d("AoAData", "noVideo"+String.valueOf(bytes.length));
                                if (AFTDatalength < 511) {
                                    byte[] data = new byte[bytes.length - AFTDatalength];
                                    System.arraycopy(bytes, AFTDatalength, data, 0, bytes.length - AFTDatalength);
                                    FiFoUtlis.getInstance().FiFoWrite(data, data.length);
                                    isVideo = true;
                                } else {
                                    AFTDatalength = AFTDatalength - 511;
                                  break;
                                }
                            }
                        }
                    }
                    //存储数据
                   // FiFoUtlis.getInstance().FiFoWrite(bytes, bytes.length);
                }

            } catch (IOException e) {
                e.printStackTrace();
                errorCount++;
            }
            if (errorCount > 5) {
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static String byte2hex(byte [] buffer){
        String h = "";

        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            h = h + " "+ temp;
        }

        return h;

    }
    public void close() throws IOException {

        mOpened = false;

        if (mInputStream != null) {
            mInputStream.close();
            mInputStream = null;
        }

     /*   if (mOutputStream != null) {
            mOutputStream.close();
            mOutputStream = null;
        }

        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }*/

    }


}
