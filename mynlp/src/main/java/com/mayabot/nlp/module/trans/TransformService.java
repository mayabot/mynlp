package com.mayabot.nlp.module.trans;

import com.mayabot.nlp.Mynlps;

/**
 * 繁简体转换
 *
 * @author jimichan
 */
public class TransformService {

    private static Simplified2Traditional simplified2Traditional;

    private static Traditional2Simplified traditional2Simplified;

    /**
     * 简体转换为繁体
     *
     * @return Simplified2Traditional
     */
    public static Simplified2Traditional simplified2Traditional() {
        if (simplified2Traditional == null) {
            simplified2Traditional = Mynlps.instanceOf(Simplified2Traditional.class);
        }
        return simplified2Traditional;
    }

    /**
     * 简体转繁体
     *
     * @param text 简体文字
     * @return 繁体文字
     */
    public static String s2t(String text) {
        return simplified2Traditional().transform(text);
    }


    /**
     * 简体转换为繁体
     *
     * @return Traditional2Simplified
     */
    public static Traditional2Simplified traditional2Simplified() {
        if (traditional2Simplified == null) {
            traditional2Simplified = Mynlps.instanceOf(Traditional2Simplified.class);
        }
        return traditional2Simplified;
    }

    /**
     * 繁体转简体
     *
     * @param text 繁体内容
     * @return 简体字符串
     */
    public static String t2s(String text) {
        return traditional2Simplified().transform(text);
    }
}
