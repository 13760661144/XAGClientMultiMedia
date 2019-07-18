package cn.xag.xagclientmultimefialib.aoausb;

/**
 * Created by harlen on 2018/11/28.
 */
public class FiFoUtlis {

    private static Object lockObject = new Object();
    public static int FIFO_SIZE = 1024 * 1024;

    public byte[] getBuffer() {
        return buffer;
    }


    private byte[] buffer = new byte[FIFO_SIZE];

    public int getmFront() {
        return mFront;
    }

    private int mFront = 0;//记录从缓冲buff中拷贝到取值的buff中的位置，用于每一次在缓冲buff取数据时的当前位置

    public int getRear() {
        return mRear;
    }

    private int mRear = 0; //记录从原始流读取中在缓冲buff上位置，用于下一次拷贝时的起始位置

    private boolean isEmpty = true;
    private boolean isFull = false;

    private static FiFoUtlis instance = null;

    public static synchronized FiFoUtlis getInstance() {
        if (instance == null)
            instance = new FiFoUtlis();
        return instance;
    }

    /**
     * 写视频流
     *
     * @param data
     * @param length
     * @return
     */
    public int FiFoWrite(byte[] data, int length) {
        synchronized (lockObject) {
            int count = 0;
            int bufSize = getActualSize();

            if (length < 1 || isFull) {
                isFull = true;
                return 0;
            }

            if (FIFO_SIZE - bufSize > length) {
                count = length;
                isFull = false;
            } else {
                count = FIFO_SIZE - bufSize;
                isFull = true;
            }

            if (isEmpty)
                isEmpty = false;

            if (mRear >= mFront) {
                if (FIFO_SIZE - mRear >= count) {
                    System.arraycopy(data, 0, buffer, mRear, count);
                    mRear = mRear + count >= FIFO_SIZE ? 0 : mRear + count;
                } else {
                    System.arraycopy(data, 0, buffer, mRear, FIFO_SIZE - mRear);
                    System.arraycopy(data, FIFO_SIZE - mRear, buffer, 0, count - (FIFO_SIZE - mRear));
                    mRear = mRear + count - FIFO_SIZE;
                }

            } else {
                System.arraycopy(data, 0, buffer, mRear, count);
                mRear = mRear + count >= FIFO_SIZE ? 0 : mRear + count;
            }
            return count;
        }
    }


    /**
     * 读视频流
     *
     * @param data
     * @param length
     * @return
     */
    public int FiFoRead(byte[] data, int length) {
        synchronized (lockObject) {
            int count = 0;
            int bufSize = getActualSize();

            if (length < 1 || isEmpty) {
                //isEmpty = true;
                return 0;
            }

            if (bufSize > length) {
                count = length;
                isEmpty = false;
            } else {
                count = bufSize;

                isEmpty = true;
            }

            if (isFull)
                isFull = false;

            if (mRear > mFront) {
                System.arraycopy(buffer, mFront, data, 0, count);
                mFront = mFront + count;
            } else {
                if (count > FIFO_SIZE - mFront) {
                    System.arraycopy(buffer, mFront, data, 0, FIFO_SIZE - mFront);
                    System.arraycopy(buffer, 0, data, FIFO_SIZE - mFront, count - (FIFO_SIZE - mFront));
                } else {
                    System.arraycopy(buffer, mFront, data, 0, count);
                }
                mFront = (mFront + count) >= FIFO_SIZE ? (mFront + count - FIFO_SIZE) : (mFront + count);
            }
            return count;
        }

    }


    public int FiFoCopy(byte[] data, int length) {
        synchronized (lockObject) {
            int count = 0;
            int bufSize = getActualSize();

            if (length < 1 || isEmpty) {
                isEmpty = true;
                return 0;
            }

            if (bufSize > length) {
                count = length;
                isEmpty = false;
            } else {
                count = bufSize;
            }

            if (mRear > mFront) {
                System.arraycopy(buffer, mFront, data, 0, count);
            } else {
                if (count > FIFO_SIZE - mFront) {
                    System.arraycopy(buffer, mFront, data, 0, FIFO_SIZE - mFront);
                    System.arraycopy(buffer, 0, data, FIFO_SIZE - mFront, count - (FIFO_SIZE - mFront));
                } else {
                    System.arraycopy(buffer, mFront, data, 0, count - mFront);
                }

            }
            return count;
        }
    }

    /*
     *
     *判断缓冲buff中当前是否有数据，返回当前可在缓冲buff中取值的长度
     * @return
     */
    public int getActualSize() {
        if (isEmpty == true) {
            return 0;
        } else {
            if (mRear >= mFront)
                return (mRear - mFront);
            else
                return (FIFO_SIZE - (mFront - mRear));
        }
    }
}
