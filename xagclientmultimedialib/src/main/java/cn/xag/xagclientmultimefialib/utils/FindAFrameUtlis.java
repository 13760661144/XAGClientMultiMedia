package cn.xag.xagclientmultimefialib.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import cn.xag.xagclientmultimefialib.aoausb.FiFoUtlis;
import cn.xag.xagclientmultimefialib.model.H264DataManager;

/**
 * Created by harlen on 2018/11/28.
 */
public class FindAFrameUtlis {

    public int flagCurrent = 0;
    private int copyFrameSize = 0;//拷贝每一帧数据的大小
    private int countFlag = 0;

    private Boolean isIframe = false; //判断是否为i帧
    private final FiFoUtlis mStreamFiFo;
    private static FindAFrameUtlis instance = null;

    public static synchronized FindAFrameUtlis getInstance() {
        if (instance == null)
            instance = new FindAFrameUtlis();
        return instance;
    }

    public FindAFrameUtlis() {
        mStreamFiFo = FiFoUtlis.getInstance();
    }

    public void searchFrame(FrameBufferListener frameBufferListener) {

        int i = 0;

        int llen = mStreamFiFo.getActualSize();

        for (i = 0; i < llen; i++) {
            if (countFlag == 1) {
                copyFrameSize++;
            }
            //h264二进制数规范为前00 00 00 01或 00 00 01
            boolean specificationHead = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i) % mStreamFiFo.FIFO_SIZE] == (byte) 0x00;
            boolean specificationTwo = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 1) % mStreamFiFo.FIFO_SIZE] == (byte) 0x00;
            boolean specificationThree = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 2) % mStreamFiFo.FIFO_SIZE] == (byte) 0x00 || mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 2) % mStreamFiFo.FIFO_SIZE] == 0x01;
            boolean specificationFour = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 3) % mStreamFiFo.FIFO_SIZE] == (byte) 0x01;

            //判断第一和第二位是否是00
            if (!(specificationHead && specificationTwo)) {
                continue;
            }

            //判断第三和第四位是否是00 ，01
            if (!(specificationThree && specificationFour
            )) {
                continue;
            }

            //是AUD封装过的数据
            boolean isAUD = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 4) % mStreamFiFo.FIFO_SIZE] == (byte) 0x9;
            //是P帧
            boolean isPFrame = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 4) % mStreamFiFo.FIFO_SIZE] == (byte) 0x41;
            //是sps
            boolean isSPS = mStreamFiFo.getBuffer()[(mStreamFiFo.getmFront() + i + 4) % mStreamFiFo.FIFO_SIZE] == (byte) 0x67;
            //第五位有可能是pps 或是 sps帧 ，09为AUD封装过的数据
            if (!(isAUD || isPFrame || isSPS)) {
                continue;
            }

            countFlag++;

            if (countFlag == 1) {
                //判断第一帧是否为sps
                if (isSPS)
                    isIframe = true;

                flagCurrent = i;
                i += 64;
                copyFrameSize += 64;

            }

            if (countFlag == 2) {
                Log.e("FindAFrameUtlis", "ByteBuffer");
                ByteBuffer byteBuffer = startCopying();
                frameBufferListener.isPushFrameBuffer(byteBuffer);
            }
        }
        flagCurrent = 0;
        countFlag = 0;
        copyFrameSize = 0;
        isIframe = false;

    }


    public interface FrameBufferListener {
        void isPushFrameBuffer(ByteBuffer byteBuffer);
    }

    //开始拷贝数据
    private ByteBuffer startCopying() {
        byte[] dataa = null;
        ByteBuffer byteBuffer = null;

        //不是i帧不需要过滤，直接拷贝
        if (isIframe == false) {
            dataa = new byte[copyFrameSize + flagCurrent];
            mStreamFiFo.FiFoRead(dataa, copyFrameSize + flagCurrent);
            byteBuffer = ByteBuffer.wrap(dataa, flagCurrent, copyFrameSize);
            flagCurrent = 0;
            countFlag = 0;
            copyFrameSize = 0;
            isIframe = false;
            return byteBuffer;
        }

        //是i帧需要判断之后过滤才能拷贝
        dataa = new byte[copyFrameSize + flagCurrent];
        mStreamFiFo.FiFoRead(dataa, copyFrameSize + flagCurrent);

        //二进制数出现如下情况需要替换sps帧数据
        if (dataa[flagCurrent + 9] == (byte) 0x03 && dataa[flagCurrent + 10] == (byte) 0xC0 && dataa[flagCurrent + 11] == (byte) 0x11) {
            byte[] sps = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x67, (byte) 0x4D, (byte) 0x00, (byte) 0x28, (byte) 0xF4,
                    (byte) 0x03, (byte) 0xC0, (byte) 0x11, (byte) 0x3F, (byte) 0x2E, (byte) 0x02, (byte) 0x20, (byte) 0x00, (byte) 0x00,
                    (byte) 0x03, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x81, (byte) 0xE3, (byte) 0x06,
                    (byte) 0x54};

            byte[] bdata = new byte[copyFrameSize + 14];
            System.arraycopy(sps, 0, bdata, 0, sps.length);
            System.arraycopy(dataa, flagCurrent + 14, bdata, sps.length, dataa.length - flagCurrent - 14);

            byteBuffer = ByteBuffer.wrap(bdata, 0, copyFrameSize + 14);
            return byteBuffer;

        }

        //二进制数出现如下情况需要替换sps帧数据
        if (dataa[flagCurrent + 9] == (byte) 0x02 && dataa[flagCurrent + 10] == (byte) 0x80 && dataa[flagCurrent + 11] == (byte) 0x2D) {
            byte[] sps = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x67, (byte) 0x4D, (byte) 0x00, (byte) 0x28, (byte) 0xF4,
                    (byte) 0x02, (byte) 0x80, (byte) 0x2D, (byte) 0xD8, (byte) 0x08, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                    (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x1E, (byte) 0x07, (byte) 0x8C, (byte) 0x19, (byte) 0x50};

            byte[] bdata = new byte[copyFrameSize + 14];
            System.arraycopy(sps, 0, bdata, 0, sps.length);
            System.arraycopy(dataa, flagCurrent + 13, bdata, sps.length, dataa.length - flagCurrent - 13);

            byteBuffer = ByteBuffer.wrap(bdata, 0, copyFrameSize + 14);
            return byteBuffer;
        }

        //没有出现规定帧需要修改的情况直接拷贝
        byteBuffer = ByteBuffer.wrap(dataa, flagCurrent, copyFrameSize);
        return byteBuffer;
    }

    private static byte[] mPpsSps = new byte[0];

    public void makeSpsPps(byte[] outData) {
        // 记录pps和sps
        if ((outData[0] == 0 && outData[1] == 0 && outData[2] == 1 && (outData[3] & 0x1f) == 7) || (outData[0] == 0
                && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && (outData[4] & 0x1f) == 7)) {
            mPpsSps = outData;
        } else if ((outData[0] == 0 && outData[1] == 0 && outData[2] == 1 && (outData[3] & 0x1f) == 5)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1
                && (outData[4] & 0x1f) == 5)) {
            // 在关键帧前面加上pps和sps数据
            byte[] data = new byte[mPpsSps.length + outData.length];
            System.arraycopy(mPpsSps, 0, data, 0, mPpsSps.length);
            System.arraycopy(outData, 0, data, mPpsSps.length, outData.length);
            outData = data;
        }

        try {
            BlockingQueue<byte[]> h264dataQueue = H264DataManager.getInstance().getH264dataQueue();
            h264dataQueue.offer(outData);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

}
