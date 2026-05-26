package com.xiaoyouma.dnf.parser.pvf.parser;

import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.model.Pvf;

/**
 * PVF 脚本解析器
 *
 * @author CN
 */
public interface IParser {

    /**
     * 转换
     *
     * @param pvf  PVF
     * @param data 字节数组
     */
    JSONObject convert(Pvf pvf, byte[] data);

    /**
     * 转换
     *
     * @param pvf  PVF
     * @param path 脚本路径
     * @param data 字节数组
     */
    default JSONObject convert(Pvf pvf, String path, byte[] data) {
        return convert(pvf, data);
    }

    /**
     * 转换为脚本源码
     *
     * @param pvf  PVF
     * @param data 字节数组
     */
    default String convertSource(Pvf pvf, byte[] data) {
        return convert(pvf, data).toString();
    }

    /**
     * 转换为脚本源码
     *
     * @param pvf  PVF
     * @param path 脚本路径
     * @param data 字节数组
     */
    default String convertSource(Pvf pvf, String path, byte[] data) {
        return convertSource(pvf, data);
    }

}
