package com.mayabot.nlp.module.trans;

import com.mayabot.nlp.Mynlp;

/**
 * 繁简体转换
 *
 * @author jimichan
 */
@Deprecated
public class TransformService {

    private static Mynlp mynlp = Mynlp.instance();

    /**
     * 简体转繁体
     *
     * @param text 简体文字
     * @return 繁体文字
     */
    @Deprecated
    public static String s2t(String text) {
        return mynlp.s2t(text);
    }

    /**
     * 繁体转简体
     *
     * @param text 繁体内容
     * @return 简体字符串
     */
    @Deprecated
    public static String t2s(String text) {
        return mynlp.t2s(text);
    }
}
