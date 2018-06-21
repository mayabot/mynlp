package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.MynlpInjector;

/**
 * @author jimichan
 */
public class Pinyins {

    static Text2PinyinService text2PinyinService = MynlpInjector.getInstance(Text2PinyinService.class);


    public static PinyinResult convert(String text) {
        return text2PinyinService.text2Pinyin(text);
    }


}
