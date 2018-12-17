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

package com.mayabot.nlp.segment.tokenizer.xprocessor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.mayabot.nlp.fst.FST;
import com.mayabot.nlp.fst.FstMatcher;
import com.mayabot.nlp.fst.FstNode;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.SegmentComponentOrder;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 各种场景的合并场景
 * 1. 数量词  数字和量词。例如 72kg
 * 2. 连接符号\单词和数字
 * 3. email
 * @author jimichan
 */
public class CommonRuleWordpathProcessor extends BaseSegmentComponent implements WordpathProcessor {
//
//    private final int x_cluster_wordid;
//    private final NatureAttribute x_cluster_nature = NatureAttribute.create(Nature.x, 100000);

    private FST<Vertex> mqFst;

    private boolean enableMqMerge = false;

    private HashSet<String> QuantityUnit = Sets.newHashSet(
            "公里 米 kg mm 平方 斤 公斤 斗 升 尺 寸 丈".split(" ")
    );

    /**
     * 单词和数字和连接符号连接在一起
     */
    private boolean enableConnectionSymbol = true;
    //
    private Pattern connectionSymbolPattern = Pattern.compile("[_\\-\\w\\d][_\\-\\w\\d]+");

    private boolean enableEmail = false;

    private Pattern emailPattern = Pattern.compile("\\w+(?:\\.\\w+)*@\\w+(?:(?:\\.\\w+)+)");


    /**
     * 构造函数
     *
     * @param coreDictionary
     */
    @Inject
    public CommonRuleWordpathProcessor(CoreDictionary coreDictionary) {
        this.setOrder(SegmentComponentOrder.DEFAULT);
        mqFst = make(fst -> {
            FstNode<Vertex> shuzi = fst.start().to("shuzi",
                    (index, vertex) -> vertex != null && vertex.isNature(Nature.m));

            FstNode<Vertex> danwei = shuzi.to("danwei",
                    (index, vertex) -> vertex != null && (vertex.isNature(Nature.q) ||
                            QuantityUnit.contains(vertex.realWord()))
            );

            danwei.to("$", (index, vertex) -> true);
        });

    }

    private FST<Vertex> make(Consumer<FST<Vertex>> x) {
        FST<Vertex> fst = new FST<>();
        x.accept(fst);
        return fst;
    }


    @Override
    public Wordpath process(Wordpath wordPath) {
        final Wordnet wordnet = wordPath.getWordnet();

        ArrayList<Vertex> inputList = Lists.newArrayList(wordPath.getBestPathWithBE());


        if (enableMqMerge) {
            run(mqFst, inputList, wordPath);
        }

        if (enableConnectionSymbol) {

            run(connectionSymbolPattern, wordnet, wordPath);
        }

        if (enableEmail) {
            run(emailPattern, wordnet, wordPath);
        }
        return wordPath;
    }

    public CommonRuleWordpathProcessor setEnableMqMerge(boolean enableMqMerge) {
        this.enableMqMerge = enableMqMerge;
        return this;
    }

    private void run(Pattern pattern, Wordnet wordnet, Wordpath wordPath) {
        Matcher matcher = pattern.matcher(wordnet);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            boolean cut = wordPath.willCutOtherWords(start, end - start);
            if (cut) {
                continue;
            }

            Vertex vertex = wordPath.combine(start, end - start);
            vertex.setAbsWordNatureAndFreq(Nature.x);
        }
    }

    private void run(FST<Vertex> fst, ArrayList<Vertex> inputList, Wordpath wordPath) {

        FstMatcher<Vertex, Vertex> m = fst.newMatcher(inputList);
        Wordnet wordnet = wordPath.getWordnet();

        while (m.find()) {

            int from = m.getStartObj().getRowNum();

            int len = m.getEndObj().getRowNum() + m.getEndObj().length() - from;

            if (wordPath.willCutOtherWords(from, len)) {
                continue;
            }

            Vertex vertex = wordPath.combine(from, len);

            vertex.setAbsWordNatureAndFreq(Nature.x);
        }
    }

    public boolean isEnableMqMerge() {
        return enableMqMerge;
    }

    public boolean isEnableConnectionSymbol() {
        return enableConnectionSymbol;
    }

    public CommonRuleWordpathProcessor setEnableConnectionSymbol(boolean enableConnectionSymbol) {
        this.enableConnectionSymbol = enableConnectionSymbol;
        return this;
    }

    public boolean isEnableEmail() {
        return enableEmail;
    }

    public CommonRuleWordpathProcessor setEnableEmail(boolean enableEmail) {
        this.enableEmail = enableEmail;
        return this;
    }

}
