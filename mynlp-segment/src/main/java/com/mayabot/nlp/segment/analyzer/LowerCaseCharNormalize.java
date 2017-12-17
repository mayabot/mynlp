package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.utils.CharacterUtils;

public class LowerCaseCharNormalize implements CharNormalize {

    @Override
    public void normal(char[] text) {
        CharacterUtils.toLowerCase(text,0,text.length);
    }

}
