package com.mayabot.nlp.module.pinyin;

import static com.mayabot.nlp.Mynlp.getInstance;

/**
 * @author jimichan
 */
public class Pinyins {

    private static PinyinService pinyinService = service();

    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }

    public static void reset() {
        pinyinService = service();
    }

    public static PinyinService service() {
        return getInstance(PinyinService.class);
    }

}
