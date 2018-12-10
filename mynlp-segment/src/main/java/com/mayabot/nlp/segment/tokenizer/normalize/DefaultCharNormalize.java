package com.mayabot.nlp.segment.tokenizer.normalize;

import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.utils.CharNormUtils;

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

    public static final DefaultCharNormalize instace = new DefaultCharNormalize();
}
