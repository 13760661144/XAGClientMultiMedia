package cn.xag.xagclientmultimefialib.aoausb;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import cn.xag.xagclientmultimefialib.ThreadManager;

/**
 * Created by harlan on 2019/4/8.
 */
public class AoAData implements AoADataListener {

    private AOACreate mAoaCreate;

    private Boolean mOpened;
    private FileInputStream mInputStream;

    public AoAData(AOACreate aoaCreate) {
        this.mAoaCreate=aoaCreate;
        mAoaCreate.setOnDataInputStream(this);
    }

    @Override
    public void onDataInputStream(FileInputStream inputStream) {

        this.mInputStream=inputStream;
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

        while (mOpened) {
            try {
                int read = mInputStream.read(buffer);
                Log.d("isPushFrameBuffer", "frameBufferListener" + read);
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
                    //存储数据
                    FiFoUtlis.getInstance().FiFoWrite(bytes, bytes.length);
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
