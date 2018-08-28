package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.Mynlp;

/**
 * @author jimichan
 */
public class Pinyins {

    static PinyinService pinyinService = Mynlp.getInstance(PinyinService.class);

    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }

    public static void reset() {
        pinyinService = Mynlp.getInstance(PinyinService.class);
    }


}
