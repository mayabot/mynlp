package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.Mynlps;

/**
 * @author jimichan
 */
public class Pinyins {

    static PinyinService pinyinService = Mynlps.instanceOf(PinyinService.class);

    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }

    public static void reset() {
        pinyinService = Mynlps.instanceOf(PinyinService.class);
    }

    public static PinyinService service() {
        return Mynlps.instanceOf(PinyinService.class);
    }

}
