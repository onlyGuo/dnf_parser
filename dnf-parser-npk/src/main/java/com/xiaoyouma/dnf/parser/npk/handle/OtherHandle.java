package com.xiaoyouma.dnf.parser.npk.handle;

import com.xiaoyouma.dnf.parser.npk.model.NpkImg;
import com.xiaoyouma.dnf.parser.npk.model.NpkTexture;

import java.io.InputStream;

/**
 * 其它
 *
 * @author CN
 */
public class OtherHandle implements IHandle {

    @Override
    public void readStream(InputStream stream, NpkImg img) {
        throw new RuntimeException("未知的版本号");
    }

    @Override
    public byte[] convertData(NpkTexture texture) {
        return new byte[0];
    }

}
