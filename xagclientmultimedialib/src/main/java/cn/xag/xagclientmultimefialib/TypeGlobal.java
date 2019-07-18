package cn.xag.xagclientmultimefialib;

/**
 * Created by harlan on 2019/2/20.
 *主功能类型分类全局变量
 * CommTheWay ：请求模式 aoa，abc1，wifi
 * FrameType ：解码方式 这里只区分硬解软解
 */
public class TypeGlobal {

    private static TypeGlobal instance = null;

    public static synchronized TypeGlobal getInstance() {
        if (instance == null) {

            synchronized (TypeGlobal.class) {

                if (instance == null)
                    instance = new TypeGlobal();
            }
        }
        return instance;
    }

    //全局变量，解码类型
    public enum FrameType {
        IS_HARDWARE_SOLUTIONS, IS_SOFT_SOLUTION
    }

    //请求方式
    public enum CommTheWay{
        IS_AOA,IS_RTSP,IS_ACB1
    }

    public CommTheWay getCommTheWay() {
        return mCommTheWay;
    }

    private CommTheWay mCommTheWay;

    public void setCommTheWay(CommTheWay commTheWay){
        if (commTheWay!=null){
            this.mCommTheWay=commTheWay;
        }
    }

    public FrameType getFrameType() {
        if (frameType == null) {
            frameType = FrameType.IS_SOFT_SOLUTION;
        }
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    private FrameType frameType;
}
