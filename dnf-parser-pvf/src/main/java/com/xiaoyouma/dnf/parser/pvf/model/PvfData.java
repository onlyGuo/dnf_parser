package com.xiaoyouma.dnf.parser.pvf.model;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * PVF 数据
 *
 * @author CN
 */
@Data
public class PvfData {

    private List<Integer> unitTypes = new ArrayList<>();

    private List<Object> values = new ArrayList<>();

    /**
     * 获取json
     */
    public JSONObject getDict() {
        return loadDict(this.unitTypes, this.values);
    }

    /**
     * 获取源码
     */
    public String getSource() {
        List<String> segmentKeysWithEndMark = getSegmentKeysWithEndMark();
        List<String> lineValues = new ArrayList<>();
        StringBuilder source = new StringBuilder();
        int indent = 0;

        for (int i = 0; i < values.size(); i++) {
            int unitType = unitTypes.get(i);
            Object value = values.get(i);

            if (unitType == 5 && value instanceof String strValue && strValue.startsWith("[") && strValue.endsWith("]")) {
                flushSourceLine(source, lineValues, indent);

                if (strValue.startsWith("[/")) {
                    indent = Math.max(0, indent - 1);
                }

                source.append("    ".repeat(indent)).append(strValue).append(System.lineSeparator());

                if (!strValue.startsWith("[/") && segmentKeysWithEndMark.contains(strValue)) {
                    indent += 1;
                }
                continue;
            }

            lineValues.add(renderValue(value));
        }

        flushSourceLine(source, lineValues, indent);
        return source.toString().stripTrailing();
    }

    /**
     * 加载json
     */
    private JSONObject loadDict(List<Integer> unitTypes, List<Object> values) {
        // 存放带结束符的段落
        List<String> segmentKeysWithEndMark = getSegmentKeysWithEndMark();

        JSONObject res = new JSONObject(true);
        List<Object> segment = new ArrayList<>();
        List<Integer> segTypes = new ArrayList<>();
        String segmentKey = null;

        for (int i = 0; i < values.size(); i++) {
            int unitType = unitTypes.get(i);
            Object value = values.get(i);

            if (unitType == 5) {

                String strValue = (String) value;

                // 判断是否为新的段
                if (segmentKey == null) {
                    segmentKey = strValue.contains("/") ? null : strValue;
                    continue;
                } else {
                    if (!segmentKeysWithEndMark.contains(segmentKey) || strValue.replace("/", "").equals(segmentKey)) {
                        addSegment(res, segmentKeysWithEndMark, segTypes, segmentKey, segment);
                        segmentKey = strValue.contains("/") ? null : strValue;
                        segTypes.clear();
                        segment.clear();
                        continue;
                    }
                }
            }

            segment.add(value);
            segTypes.add(unitType);
        }

        if (segmentKey != null) {
            addSegment(res, segmentKeysWithEndMark, segTypes, segmentKey, segment);
        }

        return res;
    }

    /**
     * 添加片段
     */
    private void addSegment(JSONObject res, List<String> segmentKeysWithEndMark, List<Integer> segTypes, String segmentKey, List<Object> segment) {
        String oldSegmentKey = segmentKey;
        if (res.containsKey(segmentKey)) {
            int suffix = 1;
            while (res.containsKey(segmentKey + "-" + suffix)) {
                suffix += 1;
            }
            segmentKey = segmentKey + "-" + suffix;
        }
        if ((segmentKeysWithEndMark.contains(segmentKey) || segmentKeysWithEndMark.contains(oldSegmentKey)) && segTypes.contains(5)) {
            res.set(segmentKey, loadDict(segTypes, segment));
        } else {
            res.set(segmentKey, segment);
        }
    }

    /**
     * 刷新源码行
     */
    private void flushSourceLine(StringBuilder source, List<String> lineValues, int indent) {
        if (lineValues.isEmpty()) {
            return;
        }
        source.append("    ".repeat(indent))
                .append(String.join("\t", lineValues))
                .append(System.lineSeparator());
        lineValues.clear();
    }

    /**
     * 渲染值
     */
    private String renderValue(Object value) {
        if (value == null) {
            return "null";
        }

        Class<?> clz = value.getClass();
        if (clz.isArray()) {
            List<String> values = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                values.add(String.valueOf(Array.get(value, i)));
            }
            return String.join("\t", values);
        }

        return String.valueOf(value);
    }

    /**
     * 存放带结束符的段落
     */
    private List<String> getSegmentKeysWithEndMark() {
        List<String> segmentKeysWithEndMark = new ArrayList<>();
        for (Object value : this.values) {
            if (value instanceof String strValue) {
                if (strValue.startsWith("[/") && strValue.endsWith("]")) {
                    segmentKeysWithEndMark.add(strValue.replace("/", ""));
                }
            }
        }
        return segmentKeysWithEndMark;
    }

}
