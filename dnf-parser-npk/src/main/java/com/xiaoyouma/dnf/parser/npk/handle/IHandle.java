package com.xiaoyouma.dnf.parser.npk.handle;

import com.xiaoyouma.dnf.parser.npk.model.NpkImg;
import com.xiaoyouma.dnf.parser.npk.model.NpkTexture;

import java.io.InputStream;

/**
 * img版本处理器
 *
 * @author CN
 */
public interface IHandle {

    /**
     * 读取流
     *
     * @param stream 数据流
     * @param img    npk img
     */
    void readStream(InputStream stream, NpkImg img);

    /**
     * 转换数据
     *
     * @param texture 贴图
     */
    byte[] convertData(NpkTexture texture);

}
