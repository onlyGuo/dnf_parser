package com.xiaoyouma.dnf.parser.pvf.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.model.Pvf;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * str
 *
 * @author CN
 */
public class StrParser implements IParser {

    @Override
    public JSONObject convert(Pvf pvf, byte[] data) {
        String content = new String(data, pvf.getCharset());

        Map<String, String> dataDict = Arrays.stream(content.split("\r\n"))
                .filter(line -> !line.startsWith("//"))
                .filter(StrUtil::isNotBlank)
                .map(line -> line.split(">", 2))
                .collect(Collectors.toMap(split -> split[0], split -> split[1]));

        return new JSONObject(dataDict);
    }

    @Override
    public String convertSource(Pvf pvf, byte[] data) {
        return new String(data, pvf.getCharset());
    }

}
