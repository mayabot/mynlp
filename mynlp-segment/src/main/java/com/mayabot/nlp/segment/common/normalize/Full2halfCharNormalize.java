package com.mayabot.nlp.segment.common.normalize;

import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.utils.Characters;

/**
 * 全角字符转半角字符
 *
 * @author jimichan
 */
public class Full2halfCharNormalize implements CharNormalize {
    @Override
    public void normal(char[] text) {
        Characters.fullWidth2halfWidth(text);
    }

    public static final Full2halfCharNormalize instace = new Full2halfCharNormalize();
}
