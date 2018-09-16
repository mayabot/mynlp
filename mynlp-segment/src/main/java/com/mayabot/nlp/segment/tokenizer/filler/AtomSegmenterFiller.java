/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.segment.tokenizer.filler;

import com.google.inject.Inject;
import com.mayabot.nlp.fst.FST;
import com.mayabot.nlp.fst.FstCondition;
import com.mayabot.nlp.fst.FstMatcher;
import com.mayabot.nlp.fst.FstNode;
import com.mayabot.nlp.segment.WordnetFiller;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.utils.CharSet;
import com.mayabot.nlp.utils.Characters;

/**
 * 寻找wordnet中的连续空白行, 识别出字符串、数字（浮点数）、等固定模式。
 * 通过FST实现，可以自由扩展模式。
 * 主要作用把粗分之间的空格搞定
 * 做原子分词用填充他们
 *
 * @author jimichan
 */
public class AtomSegmenterFiller implements WordnetFiller {

    //原子分词: 连续空的，相同的type作为整体

    private CoreDictionary coreDictionary;

    public final FST<VertexRow> fst;

    final int numWordId;

    @Inject
    public AtomSegmenterFiller(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;

        fst = new FST<>();

        FstNode<VertexRow> startNode = fst.start();

        //数字
        {
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return row.isEmpty() && CharSet.ASCII_NUMERIC.contains(row.theChar());
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("NullAndNumber", condition);
            node.linkIfReadEndFlag("$number");

            FstNode<VertexRow> dian = node.to("found_dian", (i, obj) -> obj.theChar() == '.');

            node.to("$number", condition.not());
            node.loop(condition);

            dian.linkIfReadEndFlag("$number");
            dian.to("$number", condition.not());
            dian.loop(condition);
        }

        //英文
        {
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return row.isEmpty() && CharSet.ASCII_ALPHA.contains(row.theChar());
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("NullAndALPHA", condition);
            node.linkIfReadEndFlag("$alpha");
            node.to("$alpha", condition.not());
            node.loop(condition);
        }

        //中文数字
        {
            final CharSet zhonwenCharSet = CharSet.getInstance("零○〇一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟");
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return zhonwenCharSet.contains(row.theChar());
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("ChinaNum", condition);
            node.linkIfReadEndFlag("$chinaNum");
            node.to("$chinaNum", condition.not());
            node.loop(condition);
        }

        // 标点符号
        {
            //
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return row.isEmpty() && Characters.isPunctuation(row.theChar());
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("isPunctuation", condition);
            node.to("$punctuation", FstCondition.TRUE());//无论读取到什么都结束
        }

        // 中文字符
        {
            CharSet charSet = CharSet.getInstance("\u3007\u4E00-\u9FBF\u9FA6-\u9FCB\u3400-\u4DB5\u2F00-\u2FD5\u31C0-\u31E3\u2FF0-\u2FFB");
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return row.isEmpty() && charSet.contains(row.theChar());
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("NullAndChina", condition);
            node.to("$china", FstCondition.TRUE());
        }

        //其他
        {
            final FstCondition<VertexRow> condition = (index, row) -> {
                if (index < Integer.MAX_VALUE) {
                    return row.isEmpty();
                }
                return false;
            };

            FstNode<VertexRow> node = startNode.to("other", condition);
            node.to("$other", FstCondition.TRUE());//无论读取到什么都结束
        }

        numWordId = coreDictionary.getWordID(CoreDictionary.TAG_NUMBER);
    }

    @Override
    public void fill(Wordnet wordnet) {

        FstMatcher<VertexRow, VertexRow> matcher = fst.newMatcher(wordnet.getSlotList());

        while (matcher.find()) {
            int from = matcher.getStart();
            int len = matcher.getLength();
            String nodeId = matcher.getEndNodeId();

            //FIXME 可以优化wordID查找
            switch (nodeId) {
                case "$number":
                case "$chinaNum": {
                    //六万一千公里   [万一] 被词典选中了
                    //如果都是null，那么就连接起来。如果中间有断点，那么林外单子填充
                    boolean foundNotEmpty = false;
                    for (int i = from; i < from + len; i++) {
                        if (wordnet.getRow(i).isNotEmpty()) {
                            foundNotEmpty = true;
                            break;
                        }
                    }
                    wordnet.put(from, len).
                            setWordInfo(numWordId, CoreDictionary.TAG_NUMBER, NatureAttribute.create(Nature.m, 100000));
                    if (foundNotEmpty) {
                        for (int i = from; i < from + len; i++) {
                            if (wordnet.getRow(i).isEmpty()) {
                                wordnet.put(i, 1).
                                        setWordInfo(numWordId, CoreDictionary.TAG_NUMBER, NatureAttribute.create(Nature.m, 100000));
                            }
                        }
                    }
                }
                break;
                case "$alpha": {
                    //单词变成字符串x
                    int wordID = coreDictionary.getWordID(CoreDictionary.TAG_CLUSTER);
                    wordnet.put(from, len).
                            setWordInfo(wordID, CoreDictionary.TAG_CLUSTER, coreDictionary.get(wordID));
                }
                break;
                case "$punctuation": {
                    int wordID = -1;
                    NatureAttribute na = NatureAttribute.create(Nature.w, 10000);
                    wordnet.put(from, len).
                            setWordInfo(wordID, null, na);
                }
                break;
                case "$china": {
                    int wordID = -1;
                    NatureAttribute na = NatureAttribute.create(Nature.n, 10000);
                    wordnet.put(from, len).
                            setWordInfo(wordID, null, na);
                }
                break;
                case "$other": {
                    int wordID = -1;
                    NatureAttribute na = NatureAttribute.create(Nature.n, 10000);
                    wordnet.put(from, len).
                            setWordInfo(wordID, null, na);
                }
                break;
            }

        }
    }

}
