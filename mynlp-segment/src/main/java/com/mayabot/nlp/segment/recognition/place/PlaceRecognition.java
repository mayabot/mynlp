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

package com.mayabot.nlp.segment.recognition.place;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.algorithm.Viterbi;
import com.mayabot.nlp.segment.common.EnumFreqPair;
import com.mayabot.nlp.segment.common.VertexTagCharSequenceTempChar;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.recognition.place.ns.NSDictionary;
import com.mayabot.nlp.segment.recognition.place.ns.PlaceDictionary;
import com.mayabot.nlp.segment.support.DefaultNameComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mayabot.nlp.segment.recognition.place.NSTag.*;

/**
 * HMM-Viterbi 地名名识别处理器
 *
 * @author jimichan
 */

public class PlaceRecognition  extends DefaultNameComponent implements OptimizeProcessor {

    private PlaceDictionary personDictionary;

    final long a_total_freq;
    final EnumFreqPair<NSTag> defaultEnumFreqPair;

    final int place_word_id;
    final String place_word_tag;
    final NatureAttribute place_natureAttribute;


    public static PlaceRecognition build(Injector injector) {
        return injector.getInstance(PlaceRecognition.class);
    }


    @Inject
    public PlaceRecognition(PlaceDictionary placeDictionary, CoreDictionary coreDictionary) {
        this.personDictionary = placeDictionary;

        a_total_freq = placeDictionary.getTransformMatrixDictionary().getTotalFrequency(Z);
        defaultEnumFreqPair = new EnumFreqPair(Z, a_total_freq);

        place_word_id = coreDictionary.getWordID(CoreDictionary.TAG_PLACE);
        place_word_tag = CoreDictionary.TAG_PLACE;
        place_natureAttribute =
                coreDictionary.get(place_word_id);
    }

    @Override
    public boolean process(Vertex[] pathWithBE, Wordnet wordnet) {

        char[] text = wordnet.getCharArray();

        // 绑定标签
        roleObserve(text, pathWithBE);

//		List<EnumFreqPair<NSTag>> xlist = Lists.newArrayList();
//		for (Vertex v : pathWithBE) {
//			System.out.print("【 "+v.realWord()+" "+v.getObj(NameKey.freqpair)+" ] ");
//			xlist.add(v.getObj(NameKey.freqpair));
//		}

        //选择标签

        Viterbi.computeEnumSimply2(pathWithBE,
                (v) -> v.getTempObj(),
                personDictionary.getTransformMatrixDictionary(),
                (v, tag) -> {
                    v.setTempChar(tag.name().charAt(0));
                });

//		for (Vertex v : pathWithBE) {
//			System.out.print(v.getChar(NameKey.tag)+" ");
//		}
//		System.out.println();

        AtomicInteger ai = new AtomicInteger();
        CharSequence tagString = new VertexTagCharSequenceTempChar(pathWithBE);
        // 解码
        personDictionary.getTrie().parseText(
                tagString, (begin, end, value) -> {
                    StringBuilder sbName = new StringBuilder();
                    for (int i = begin; i < end; ++i) {
                        sbName.append(pathWithBE[i].realWord());
                    }
                    String name = sbName.toString();

                    //System.out.println(name);

                    if (isBadCase(name)) {
                        return;
                    }

                    ai.incrementAndGet();
                    //FIXME 如果已结之前已结存在，那么只需要添加词性和对应的词频，而不是覆盖
                    wordnet.put(pathWithBE[begin].realWordOffset(), name.length()).
                            setWordInfo(place_word_id, place_word_tag, place_natureAttribute);
                }
        );

        //清理
        for (Vertex v : pathWithBE) {
            v.clearTemp();
        }

        return ai.intValue() > 0;
    }


    /**
     * 角色观察(从模型中加载所有词语对应的所有角色,允许进行一些规则补充)
     * <p>
     * 粗分结果
     *
     * @return
     */
    public void roleObserve(char[] text, Vertex[] pathWithBE) {

        //Begin
        pathWithBE[0].setTempObj(defaultEnumFreqPair);

        //END
        pathWithBE[pathWithBE.length - 1].setTempObj(defaultEnumFreqPair);

        NSDictionary nrDictionary = personDictionary.getDictionary();

        for (int i = 1; i < pathWithBE.length - 1; i++) {
            Vertex vertex = pathWithBE[i];

            if (Nature.ns.equals(vertex.guessNature()) && vertex.natureAttribute.getTotalFrequency() <= 1000) {
                if (vertex.length <= 3) {
                    vertex.setTempObj(new EnumFreqPair<>(H, G)); // 二字地名，认为其可以再接一个后缀或前缀
                } else {
                    vertex.setTempObj(new EnumFreqPair<>(G));// 否则只可以再加后缀
                }
            }

            EnumFreqPair<NSTag> nrEnumFreqPair = null;

            if (vertex.abstractWord != null) {
                nrEnumFreqPair = nrDictionary.get(vertex.abstractWord);
            } else {
                nrEnumFreqPair = nrDictionary.get(text, vertex.realWordOffset(), vertex.length);
            }


            if (nrEnumFreqPair == null) {
                nrEnumFreqPair = defaultEnumFreqPair;
            }

            vertex.setTempObj(nrEnumFreqPair);
        }

    }

    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，这些bad case会以“盖公章 A 1”的形式加入词典中<BR>
     * 这个方法返回人名是否是bad case
     *
     * @param name
     * @return
     */
    private boolean isBadCase(String name) {
        EnumFreqPair<NSTag> nrEnumFreqPair = personDictionary.getDictionary().get(name);
        if (nrEnumFreqPair == null) {
            return false;
        }
        return nrEnumFreqPair.containsLabel(Z);
    }

}
