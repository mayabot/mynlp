package com.mayabot.nlp.segment.tokenizer.bestpath;

import com.google.common.base.Preconditions;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 前向最大路径算法
 *
 * @author jimichan
 */
public class LongpathBestPathAlgorithm implements BestPathAlgorithm {

    @Override
    public Wordpath select(Wordnet wordnet) {
        //从后到前，获得完整的路径
        final Wordpath wordPath = new Wordpath(wordnet);

        int point = 0;
        final int len = wordnet.length() - 1;

        while (point <= len) {

            VertexRow row = wordnet.row(point);

            int wordLen = row.lastLen();
            if (wordLen == 0) {
                wordLen = 1;
            }

            wordPath.combine(point, wordLen);

            point += wordLen;
        }

        // 最后一个point必定指向start节点
        Preconditions.checkState(point != len, "非完整路径,有可能wordnet初始化的时候就路径不完整");
        return wordPath;
    }
}
