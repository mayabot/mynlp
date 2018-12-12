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

package com.mayabot.nlp.segment.hmmner.place;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mayabot.nlp.algorithm.Viterbi;
import com.mayabot.nlp.common.EnumFreqPair;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.common.VertexTagCharSequenceTempChar;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.hmmner.OptimizeProcessor;
import com.mayabot.nlp.segment.hmmner.place.ns.NSDictionary;
import com.mayabot.nlp.segment.hmmner.place.ns.PlaceDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * HMM-Viterbi 地名名识别处理器
 *
 * @author jimichan
 */

public class PlaceRecognition extends BaseSegmentComponent implements OptimizeProcessor {

    private PlaceDictionary personDictionary;

    final long a_total_freq;
    final EnumFreqPair<NSTag> defaultEnumFreqPair;

//    final int place_word_id;
//    final String place_word_tag;
//    final int place_natureAttribute;


    public static PlaceRecognition build(Injector injector) {
        return injector.getInstance(PlaceRecognition.class);
    }


    @Inject
    public PlaceRecognition(PlaceDictionary placeDictionary, CoreDictionary coreDictionary) {
        this.personDictionary = placeDictionary;

        a_total_freq = placeDictionary.getTransformMatrixDictionary().getTotalFrequency(NSTag.Z);
        defaultEnumFreqPair = new EnumFreqPair(NSTag.Z, a_total_freq);
//
//        place_word_id = coreDictionary.getWordID(CoreDictionary.NS_TAG);
//        place_word_tag = CoreDictionary.NS_TAG;
//        place_natureAttribute =
//                coreDictionary.get(place_word_id);
    }

    @Override
    public boolean process(Vertex[] pathWithBE, Wordnet wordnet) {

//        地名角色观察：[  S 1163565 ][南翔 G 1 H 1 ][向 A 1076 B 115 X 70 C 49 D 5 ][宁夏 G 1 H 1 ][固原市 G 1 ][彭 C 85 ][阳 D 1255 C 81 B 1 ][县 H 6878 B 25 A 23 D 19 X 3 ][红 C 1000 B 46 A 3 ][河镇 G 1 H 1 ][黑 C 960 B 25 ][牛 D 24 C 8 B 7 ][沟 H 107 D 90 E 36 C 27 B 14 A 3 ][村 H 4467 D 68 B 28 A 8 C 3 ][捐赠 B 10 A 1 ][了 A 4115 B 97 ][挖掘机 B 1 ][  B 1322 ]
//        地名角色标注：[ /S ,南翔/G ,向/X ,宁夏/G ,固原市/G ,彭/C ,阳/D ,县/H ,红/C ,河镇/H ,黑/C ,牛/D ,沟/E ,村/H ,捐赠/B ,了/A ,挖掘机/B , /S]
//        识别出地名：彭阳县 CDH
//        识别出地名：红河镇 CH
//        识别出地名：黑牛沟村 CDEH

        char[] text = wordnet.getCharArray();

        // 绑定标签
        roleObserve(text, pathWithBE);

//		List<EnumFreqPair<NSTag>> xlist = Lists.newArrayList();
//		for (Vertex v : pathWithBE) {
//			System.out.print("【 "+v.realWord()+" "+v.getTempObj()+" ] ");
//			xlist.add(v.getTempObj());
//		}

        //选择标签

        Viterbi.computeEnumSimply2(pathWithBE,
                (v) -> v.getTempObj(),
                personDictionary.getTransformMatrixDictionary(),
                (v, tag) -> {
                    v.setTempChar(tag.name().charAt(0));
                });

//		for (Vertex v : pathWithBE) {
//			System.out.print(v.getTempChar()+" ");
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

                    //System.out.println(NAME);

                    if (isBadCase(name)) {
                        return;
                    }

                    ai.incrementAndGet();
                    //FIXME 如果已结之前已结存在，那么只需要添加词性和对应的词频，而不是覆盖
                    wordnet.put(pathWithBE[begin].offset(), name.length()).
                            setAbsWordNatureAndFreq(Nature.ns);
//                            setWordInfo(place_word_id, place_word_tag,Nature.ns
//                                    , place_natureAttribute);
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

//        //Begin
//        pathWithBE[0].setTempObj(defaultEnumFreqPair);
//
//        //END
//        pathWithBE[pathWithBE.length - 1].setTempObj(defaultEnumFreqPair);

        NSDictionary nrDictionary = personDictionary.getDictionary();

        for (int i = 0; i < pathWithBE.length; i++) {
            Vertex vertex = pathWithBE[i];

            if (Nature.ns.equals(vertex.nature) && vertex.freq <= 1000) {
                if (vertex.length < 3) {
                    // 二字地名，认为其可以再接一个后缀或前缀
                    vertex.setTempObj(new EnumFreqPair<>(NSTag.H, NSTag.G));
                } else {
                    // 否则只可以再加后缀
                    vertex.setTempObj(new EnumFreqPair<>(NSTag.G));
                }
                continue;
            }

            EnumFreqPair<NSTag> nrEnumFreqPair = null;

            if (vertex.isAbsWord()) {
                nrEnumFreqPair = nrDictionary.get(vertex.absWordLabel());
            } else {
                nrEnumFreqPair = nrDictionary.get(text, vertex.offset(), vertex.length);
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
        return nrEnumFreqPair.containsLabel(NSTag.Z);
    }

}
