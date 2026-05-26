package com.xiaoyouma.dnf.parser.npk.model;

import com.xiaoyouma.dnf.parser.npk.enums.ColorBit;
import com.xiaoyouma.dnf.parser.npk.enums.CompressMode;
import com.xiaoyouma.dnf.parser.npk.enums.ImgVersion;
import com.xiaoyouma.dnf.parser.npk.handle.HandleFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * npk 贴图
 *
 * @author CN
 */
@Slf4j
@Data
public class NpkTexture {

    // img
    private NpkImg img;

    // 贴图在img中的下标
    private int index;

    // 是否指向帧
    private boolean isLink;

    // 当贴图为链接贴图时所指向的贴图
    public NpkTexture linkTarget;

    // 颜色系统
    private ColorBit colorBit;

    // 压缩状态
    private CompressMode compressMode;

    // 图像宽
    private int width;

    public int getWidth() {
        return isLink ? linkTarget.getWidth() : width;
    }

    // 图像高
    private int height;

    public int getHeight() {
        return isLink ? linkTarget.getHeight() : height;
    }

    // 图像大小
    private int length;

    // 图像X坐标
    private int x;

    public int getX() {
        return isLink ? linkTarget.getX() : x;
    }

    // 图像Y坐标
    private int y;

    public int getY() {
        return isLink ? linkTarget.getY() : y;
    }

    // 帧域宽
    private int frameWidth;

    // 帧域高
    private int frameHeight;

    // 数据偏移量
    private int dataOffset;

    // 数据
    private byte[] data;

    public NpkTexture(NpkImg img) {
        this.img = img;
    }

    /**
     * 获取bgra 数据
     */
    public byte[] getBgraData() {
        if (isLink) {
            return linkTarget.getBgraData();
        } else {
            return HandleFactory.of(ImgVersion.of(img.getVersion())).convertData(this);
        }
    }

    /**
     * 转 png 字节数组
     */
    public byte[] toPngBytes() {
        int width = getWidth();
        int height = getHeight();
        byte[] bgraData = getBgraData();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int offset = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int b = Byte.toUnsignedInt(bgraData[offset]);
                int g = Byte.toUnsignedInt(bgraData[offset + 1]);
                int r = Byte.toUnsignedInt(bgraData[offset + 2]);
                int a = Byte.toUnsignedInt(bgraData[offset + 3]);
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                offset += 4;
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("texture to png bytes fail", e);
        }
    }

}
