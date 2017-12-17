package com.mayabot.nlp.segment.segment;

import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.utils.CharacterUtils;

public class UpperCaseCharNormalize implements CharNormalize {

    @Override
    public void normal(char[] text) {
        CharacterUtils.toUpperCase(text,0,text.length);
    }
    
}
