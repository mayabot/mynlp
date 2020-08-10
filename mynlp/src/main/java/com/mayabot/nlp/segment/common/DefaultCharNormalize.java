package com.mayabot.nlp.segment.common;

import com.mayabot.nlp.common.utils.CharNormUtils;
import com.mayabot.nlp.segment.CharNormalize;

/**
 * 大小转小写。
 * 全角转半角，其他字符归一化。
 *
 * @author jimichan
 */
public class DefaultCharNormalize implements CharNormalize {
    @Override
    public void normal(char[] text) {
        CharNormUtils.convert(text);
    }

    public static final DefaultCharNormalize instance = new DefaultCharNormalize();
}
