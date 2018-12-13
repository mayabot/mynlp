package com.mayabot.nlp.segment.hmmner;

import com.google.inject.Singleton;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
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
@Singleton
public class RepairWordnetProcessor extends BaseSegmentComponent implements WordpathProcessor {

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
                    vertex.setAbsWordNatureAndFreq(Nature.m);
                } else {
                    vertex.setAbsWordNatureAndFreq(Nature.x);
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
