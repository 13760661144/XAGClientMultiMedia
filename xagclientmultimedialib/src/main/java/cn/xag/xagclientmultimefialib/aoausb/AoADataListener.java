package cn.xag.xagclientmultimefialib.aoausb;

import java.io.FileInputStream;

/**
 * Created by harlan on 2019/4/8.
 *AOA模式通讯成功后返回流的接口
 */
public interface AoADataListener {
    void onDataInputStream(FileInputStream inputStream);
}
