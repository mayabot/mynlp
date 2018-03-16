package com.mayabot.nlp.segment.xprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.CharSet;

/**
 * 由于对WordPath的处理，有可能造成wordnet里面不匹配。
 * 比如 宝马5102017年度销售 . 在正则表达式处理后，进行截断，造成510在word内部缺失
 *
 * @author jimichan jimichan@gmail.com
 */
public class RepairWordnetProcessor implements WordpathProcessor {

    private CoreDictionary coreDictionary;

    private NatureAttribute X_attribute;//字符串
    private NatureAttribute Num_attribute;//字符串

    int numWordId;

    @Inject
    public RepairWordnetProcessor(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
        X_attribute = coreDictionary.get(coreDictionary.X_WORD_ID);

        numWordId = coreDictionary.getWordID(CoreDictionary.TAG_NUMBER);
        Num_attribute = coreDictionary.get(numWordId);
    }

    @Override
    public Wordpath process(Wordpath wordPath) {
        Wordnet wordnet = wordPath.getWordnet();
        Wordpath.WordPointer wordPointer = wordPath.wordPointer();
        char[] charArray = wordnet.getCharArray();
        while (wordPointer.next()) {
            int from = wordPointer.getFrom();
            int len = wordPointer.getLen();
            Vertex vertex = wordnet.getVertex(from, len);
            if (vertex == null) {
                vertex = wordnet.put(from, len);

                if (isNum(charArray, from, len)) {
                    vertex.setWordInfo(numWordId, Num_attribute);
                } else {
                    vertex.setWordInfo(coreDictionary.X_WORD_ID, X_attribute);
                }

            }
        }
        return null;
    }

    static CharSet nums = CharSet.ASCII_NUMERIC;

    private boolean isNum(char[] string, int from, int len) {
        for (int i = from; i < from + len; i++) {
            if (!nums.contains(string[i])) {
                return false;
            }
        }
        return true;
    }
}
