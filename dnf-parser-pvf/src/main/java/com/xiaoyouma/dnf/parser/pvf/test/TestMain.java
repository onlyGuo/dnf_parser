package com.xiaoyouma.dnf.parser.pvf.test;

import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.coder.PvfCoder;

import java.nio.charset.Charset;

public class TestMain {
    public static void main(String[] args) {
        PvfCoder.initialize("/Users/xiatian/Desktop/Script.pvf", Charset.forName("Big5"));
        JSONObject entries = PvfCoder.loadScript("stackable/book_skill2.stk");
        System.out.println(entries);
        System.out.println(PvfCoder.loadScriptSource("stackable/book_skill2.stk"));
    }
}
