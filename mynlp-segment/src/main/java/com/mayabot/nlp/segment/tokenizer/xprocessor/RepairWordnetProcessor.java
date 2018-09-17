package com.mayabot.nlp.segment.tokenizer.xprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
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
public class RepairWordnetProcessor extends BaseMynlpComponent implements WordpathProcessor {

    private CoreDictionary coreDictionary;

    /**
     * 字符串
     */
    private NatureAttribute xAttribute;

    /**
     * 字符串
     */
    private NatureAttribute numAttribute;

    int numWordId;

    @Inject
    public RepairWordnetProcessor(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
        xAttribute = coreDictionary.get(coreDictionary.X_WORD_ID);

        numWordId = coreDictionary.getWordID(CoreDictionary.TAG_NUMBER);
        numAttribute = coreDictionary.get(numWordId);
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
                    vertex.setWordInfo(numWordId, numAttribute);
                } else {
                    vertex.setWordInfo(coreDictionary.X_WORD_ID, xAttribute);
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
