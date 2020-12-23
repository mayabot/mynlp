package com.mayabot.nlp.module.pinyin;

import com.mayabot.nlp.Mynlp;

/**
 * @author jimichan
 */
@Deprecated
public class Pinyins {

    private static PinyinService pinyinService = Mynlp.instance().getInstance(PinyinService.class);

    @Deprecated
    public static PinyinResult convert(String text) {
        return pinyinService.text2Pinyin(text);
    }

//    public static void reset() {
//        pinyinService = service();
//    }
//
//    public static PinyinService service() {
//        return Mynlp.instance().getInstance(PinyinService.class);
//    }

}
