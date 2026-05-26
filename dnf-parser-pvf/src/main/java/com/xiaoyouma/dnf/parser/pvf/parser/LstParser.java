package com.xiaoyouma.dnf.parser.pvf.parser;

import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.model.Pvf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * lst
 *
 * @author CN
 */
public class LstParser implements IParser {

    @Override
    public JSONObject convert(Pvf pvf, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.position(2);

        JSONObject dataDict = new JSONObject();

        int index = -1;

        while (buffer.hasRemaining()) {
            if (buffer.remaining() - 5 >= 0) {

                byte type = buffer.get();

                switch (type) {
                    case 0x02:
                        index = buffer.getInt();
                        break;
                    case 0x07:
                        String strValue = pvf.getStringTable(buffer.getInt()).toLowerCase();
                        dataDict.set(String.valueOf(index), strValue);
                        break;
                }
            } else {
                break;
            }
        }
        return dataDict;
    }
}
