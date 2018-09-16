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

package com.mayabot.nlp.segment.tokenizer.recognition.org;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.common.EnumFreqPair;
import com.mayabot.nlp.segment.common.SecondOrderViterbi;
import com.mayabot.nlp.segment.common.VertexTagCharSequenceTempChar;
import com.mayabot.nlp.segment.dictionary.EnumTransformMatrix;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.tokenizer.recognition.org.nt.NTDictionary;
import com.mayabot.nlp.segment.tokenizer.recognition.org.nt.OrganizationDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import static com.mayabot.nlp.segment.tokenizer.recognition.org.NTTag.Z;

/**
 * HMM-Viterbi 组织机构名识别处理器
 *
 * @author jimichan
 */
public class OrganizationRecognition extends BaseMynlpComponent implements OptimizeProcessor {


    public static OrganizationRecognition build(Injector injector) {
        return injector.getInstance(OrganizationRecognition.class);
    }

    private OrganizationDictionary dictionary;

//    private enum NameKey {
//        freqpair,
//        tag
//    }

    //ObjectPredicate<? super Enum> predicate = (x) -> x.getClass() == NameKey.class;

    final long a_total_freq;
    final EnumFreqPair<NTTag> defaultEnumFreqPair;

    final int word_id;
    final String word_tag;
    final NatureAttribute natureAttribute;

    private SecondOrderViterbi<NTTag, Vertex> viterbi;


    @Inject
    public OrganizationRecognition(OrganizationDictionary placeDictionary, CoreDictionary coreDictionary) {
        this.dictionary = placeDictionary;

        a_total_freq = placeDictionary.getTransformMatrixDictionary().getTotalFrequency(NTTag.Z);
        defaultEnumFreqPair = new EnumFreqPair(Z, a_total_freq);

        word_id = coreDictionary.getWordID(CoreDictionary.TAG_GROUP);
        word_tag = CoreDictionary.TAG_GROUP;
        natureAttribute = coreDictionary.get(word_id);

        EnumTransformMatrix<NTTag> trans = placeDictionary.getTransformMatrixDictionary();
        viterbi = new SecondOrderViterbi<>(
                (pre, cur) -> trans.getTP(pre.getKey(), cur.getKey()) - Math.log(cur.getValue() + 1e-8) / trans.getTotalFrequency(cur.getKey()),
                (vertex) -> ((EnumFreqPair) vertex.getTempObj()).getMap(),
                (v, n) -> {
                    v.setTempChar(n.name().charAt(0));
//                    v.setChar(NameKey.tag, n.NAME().charAt(0));
                });
//        viterbi = new SecondOrderViterbi<>(
//                (pre, cur) -> trans.getTP(pre.getKey(), cur.getKey()) - Math.log(cur.getValue() + 1e-8) / trans.getTotalFrequency(cur.getKey()),
//                (vertex) -> ((EnumFreqPair) vertex.getObj(NameKey.freqpair)).getMap(),
//                (v, n) -> {
//                    v.setChar(NameKey.tag, n.NAME().charAt(0));
//                });
    }

    @Override
    public boolean process(Vertex[] pathWithBE, Wordnet wordnet) {

        char[] text = wordnet.getCharArray();

//		机构名角色观察：[  S 1169907 ][我 A 11 B 2 ][在 A 5758 B 1802 X 72 ][上海 G 92134 B 1200 A 470 X 4 ][林原 F 6781 B 769 A 266 X 6 ][科技 C 1149 B 26 D 14 A 6 X 1 ][有限公司 K 1000 D 1000 ][兼职 A 10 B 2 ][工作 C 305 B 289 A 1 ][， A 26883 B 4588 X 129 ][  B 8423 ]
//		机构名角色标注：[ /S ,我/A ,在/A ,上海/G ,林原/F ,科技/C ,有限公司/D ,兼职/B ,工作/B ,，/A , /S]

        IntWarp ai2 = new IntWarp();

        // 绑定标签
        roleObserve(text, pathWithBE);
        viterbi.viterbi(pathWithBE);

        CharSequence tagString = new VertexTagCharSequenceTempChar(pathWithBE);
        // 解码
        dictionary.getTrie().parseText(
                tagString, (begin, end, value) -> {
                    StringBuilder sbName = new StringBuilder();
                    for (int i = begin; i < end; ++i) {
                        sbName.append(pathWithBE[i].realWord());
                    }
                    String name = sbName.toString();

                    //System.out.println("--- "+NAME);

                    if (isBadCase2(name)) {
                        return;
                    }

                    ai2.count++;
                    //FIXME 如果已结之前已结存在，那么只需要添加词性和对应的词频，而不是覆盖
                    Vertex word = wordnet.put(pathWithBE[begin].realWordOffset(), name.length());
//					if(word.realWord().equals("信息阿里巴巴股份有限公司")){
//						System.out.println("");
//					}
                    word.setWordInfo(word_id, word_tag, natureAttribute);
                }
        );

        //清理
        for (Vertex v : pathWithBE) {
            //v.clear(predicate);
            v.clearTemp();
        }

        return ai2.count > 0;
    }

    static class IntWarp {
        int count = 0;
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
        pathWithBE[0].setTempObj(new EnumFreqPair<>(NTTag.S, 1169907));

        //END
        pathWithBE[pathWithBE.length - 1].setTempObj(new EnumFreqPair<>(NTTag.Z));

        NTDictionary ntDictionary = dictionary.getDictionary();

        for (int i = 1; i < pathWithBE.length - 1; i++) {
            Vertex vertex = pathWithBE[i];

            // 构成更长的
            Nature nature = vertex.guessNature();

            if (Nature.nrf.equals(nature) && vertex.natureAttribute.getTotalFrequency() <= 1000) {
                vertex.setTempObj(new EnumFreqPair<>(NTTag.F, 1000));
            } else if (Nature.ni.equals(nature) ||
                    Nature.nic.equals(nature) ||
                    Nature.nis.equals(nature) ||
                    Nature.nit.equals(nature)
                    ) {
                vertex.setTempObj(new EnumFreqPair<>(NTTag.K, 1000, NTTag.D, 1000));
            } else if (Nature.m.equals(nature)) {
                vertex.setTempObj(new EnumFreqPair<>(NTTag.M, 1000));
            } else {
                EnumFreqPair<NTTag> nrEnumFreqPair = null;

                if (vertex.abstractWord != null) {
                    nrEnumFreqPair = ntDictionary.get(vertex.abstractWord);
                } else {
                    nrEnumFreqPair = ntDictionary.get(text, vertex.realWordOffset(), vertex.length);
                }

                if (nrEnumFreqPair == null) {
                    nrEnumFreqPair = defaultEnumFreqPair;
                }

                vertex.setTempObj(nrEnumFreqPair);
            }
        }

    }


    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，这些bad case会以“盖公章 A 1”的形式加入词典中<BR>
     * 这个方法返回人名是否是bad case
     *
     * @param name
     * @return
     */
    private boolean isBadCase2(String name) {
        EnumFreqPair<NTTag> nrEnumFreqPair = dictionary.getDictionary().get(name);
        if (nrEnumFreqPair == null) {
            return false;
        }
        return nrEnumFreqPair.containsLabel(Z);
    }

}
