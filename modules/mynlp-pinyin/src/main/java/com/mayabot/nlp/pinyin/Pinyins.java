package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.Mynlps;

/**
 * @author jimichan
 */
public class Pinyins {

    static PinyinDictionary pinyinService = Mynlps.instanceOf(PinyinDictionary.class);

    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }

    public static void reset() {
        pinyinService = Mynlps.instanceOf(PinyinDictionary.class);
    }


}
