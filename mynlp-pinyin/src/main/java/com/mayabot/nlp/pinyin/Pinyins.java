package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.Mynlp;

/**
 * @author jimichan
 */
public class Pinyins {

    static Mynlp mynlp = Mynlp.builder().build();

    static PinyinService pinyinService = mynlp.getInstance(PinyinService.class);

    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }


}
