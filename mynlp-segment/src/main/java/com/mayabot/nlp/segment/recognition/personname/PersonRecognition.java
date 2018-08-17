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

package com.mayabot.nlp.segment.recognition.personname;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.algorithm.Viterbi;
import com.mayabot.nlp.segment.common.EnumFreqPair;
import com.mayabot.nlp.segment.common.VertexTagCharSequenceTempChar;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.recognition.personname.nr.NRDictionary;
import com.mayabot.nlp.segment.recognition.personname.nr.PersonDictionary;
import com.mayabot.nlp.segment.support.DefaultNameComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mayabot.nlp.segment.recognition.personname.NRTag.A;

/**
 * HMM-Viterbi 中文人名识别处理器
 *
 * @author jimichan
 */

public class PersonRecognition extends DefaultNameComponent implements OptimizeProcessor {

    public static PersonRecognition build(Injector injector) {
        return injector.getInstance(PersonRecognition.class);
    }

    private PersonDictionary personDictionary;


//	ObjectPredicate<? super Enum> predicate = (x)->x.getClass() == PersionNameKey.class;

    final long a_total_freq;
    final EnumFreqPair<NRTag> defaultEnumFreqPair;

    final int perope_word_id;
    final String people_word_tag;
    final NatureAttribute People_natureAttribute;


    @Inject
    public PersonRecognition(PersonDictionary personDictionary, CoreDictionary coreDictionary) {
        this.personDictionary = personDictionary;

        a_total_freq = personDictionary.getTransformMatrixDictionary().getTotalFrequency(NRTag.A);
        defaultEnumFreqPair = new EnumFreqPair(NRTag.A, a_total_freq);

        perope_word_id = coreDictionary.getWordID(CoreDictionary.TAG_PEOPLE);
        people_word_tag = CoreDictionary.TAG_PEOPLE;
        People_natureAttribute =
                coreDictionary.get(perope_word_id);

    }

    @Override
    public boolean process(Vertex[] pathWithBE, Wordnet wordnet) {

        char[] text = wordnet.getCharArray();

        // 绑定标签
        roleObserve(text, pathWithBE);

//		List<EnumFreqPair<NRTag>> xlist = Lists.newArrayList();
//
//		for (Vertex v : pathWithBE) {
//			System.out.print("【 "+v.realWord()+" "+v.getObj(PersionNameKey.freqpair)+" ] ");
//			xlist.add(v.getObj(PersionNameKey.freqpair));
//		}

        //选择标签
        Viterbi.computeEnumSimply2(pathWithBE,
                (v) -> v.getTempObj(),
                personDictionary.getTransformMatrixDictionary(),
                (v, tag) -> {
                    v.setTempChar(tag.name().charAt(0));
                });


//		将角色为U的片断pf分裂为pKfB（若f为姓）、pKfC（若f为双名首字）或pKfE（若f为单名）。
//		(3) 将角色为V的片断tn分裂为tDnL（若t为双名末字）或tEnL（若t为单名）
        int fenlieAddNodeCount = 0;
        for (Vertex v : pathWithBE) {
            char tag = v.getTempChar();
            if ('U' == tag) {
                // 人名的上文和姓成词  这里 有关 天 培 的 壮烈
                // 分裂 KB
                fenlieAddNodeCount += 1;
            } else if ('V' == tag) {
                //人名的末字和下文成词 龚 学 平等 领导, 邓 颖 超生 前
                fenlieAddNodeCount += 1;
            }
        }

        CharSequence tagString = null;
        int[] tagMapOffset = null;
        int[] tagMapLength = null;

        // 注意Begin和end节点

        if (fenlieAddNodeCount > 0) {
            char[] tagchar = new char[fenlieAddNodeCount + pathWithBE.length];
            tagMapOffset = new int[tagchar.length];
            tagMapLength = new int[tagchar.length];
            int point = -1;


            for (int i = 0; i < pathWithBE.length; i++) {
                Vertex v = pathWithBE[i];

                char tag = v.getTempChar();
                switch (tag) {
                    case 'U':
                        //first abstractWord
                        tagMapOffset[++point] = v.realWordOffset();
                        tagMapLength[point] = v.length - 1;
                        tagchar[point] = 'K';

                        // second abstractWord
                        tagMapOffset[++point] = v.realWordOffset() + v.length - 1;
                        tagMapLength[point] = 1;
                        tagchar[point] = 'B';

                        break;
                    case 'V':
                        //人名的末字和下文成词 龚 学 平等 领导, 邓 颖 超生 前
                        //first abstractWord
                        tagMapOffset[++point] = v.realWordOffset();
                        tagMapLength[point] = 1;

                        if (pathWithBE[i - 1].getTempChar() == 'B') {
                            tagchar[point] = 'E';
                        } else {
                            tagchar[point] = 'D';
                        }

                        // second abstractWord
                        tagMapOffset[++point] = v.realWordOffset() + 1;
                        tagMapLength[point] = v.length - 1;
                        tagchar[point] = 'L';

                        break;
                    default:
                        tagMapOffset[++point] = v.realWordOffset();
                        tagMapLength[point] = v.length;
                        tagchar[point] = v.getTempChar();
                        break;
                }
            }

            tagString = new String(tagchar);
        } else {
            tagString = new VertexTagCharSequenceTempChar(pathWithBE);
        }
//
//		System.out.println(tagString);
//		System.out.println(new VertexTagCharSequence(pathWithBE, PersionNameKey.tag));
//

        AtomicInteger ai = new AtomicInteger();

        if (fenlieAddNodeCount > 0) {
            final int[] _tagMapOffset = tagMapOffset;
            final int[] _tagMapLength = tagMapLength;

            personDictionary.getTrie().parseText(
                    tagString, (begin, end, value) -> {
                        StringBuilder sbName = new StringBuilder();
                        for (int i = begin; i < end; ++i) {
                            for (int j = _tagMapOffset[i]; j < _tagMapOffset[i] + _tagMapLength[i]; j++) {
                                sbName.append(wordnet.charAt(j));
                            }
                        }
                        String name = sbName.toString();

                        //System.out.println(NAME);

                        switch (value) {
                            case BCD:
                                if (name.charAt(0) == name.charAt(2)) {
                                    return; // 姓和最后一个名不可能相等的
                                }
                        }
                        if (isBadCase(name)) {
                            return;
                        }

                        ai.incrementAndGet();
                        //FIXME 如果已结之前已结存在，那么只需要添加词性和对应的词频，而不是覆盖
                        wordnet.put(_tagMapOffset[begin], name.length()).
                                setWordInfo(perope_word_id, people_word_tag, People_natureAttribute);
                    }
            );
        } else {
            personDictionary.getTrie().parseText(
                    tagString, (begin, end, value) -> {
                        StringBuilder sbName = new StringBuilder();
                        for (int i = begin; i < end; ++i) {
                            sbName.append(pathWithBE[i].realWord());
                        }
                        String name = sbName.toString();

                        //System.out.println(NAME);

                        switch (value) {
                            case BCD:
                                if (name.charAt(0) == name.charAt(2)) {
                                    return; // 姓和最后一个名不可能相等的
                                }
                        }
                        if (isBadCase(name)) {
                            return;
                        }

                        ai.incrementAndGet();
                        //FIXME 如果已结之前已结存在，那么只需要添加词性和对应的词频，而不是覆盖
                        wordnet.put(pathWithBE[begin].realWordOffset(), name.length()).
                                setWordInfo(perope_word_id, people_word_tag, People_natureAttribute);
                    }
            );
        }


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
        pathWithBE[0].setTempObj(new EnumFreqPair(NRTag.A, NRTag.K));

        //END
        pathWithBE[pathWithBE.length - 1].setTempObj(defaultEnumFreqPair);

        NRDictionary nrDictionary = personDictionary.getDictionary();

        for (int i = 1; i < pathWithBE.length - 1; i++) {
            Vertex vertex = pathWithBE[i];
            EnumFreqPair<NRTag> nrEnumFreqPair = nrDictionary.get(text, vertex.realWordOffset(), vertex.length);

            if (nrEnumFreqPair == null) {
                Nature nature = vertex.guessNature();
                if (Nature.nr.equals(nature)) {
                    // 有些双名实际上可以构成更长的三名
                    if (vertex.natureAttribute.getTotalFrequency() <= 1000 && vertex.length == 2) {
                        nrEnumFreqPair = EnumFreqPair.create(NRTag.X, NRTag.G);
                    } else {
                        nrEnumFreqPair = new EnumFreqPair<>(NRTag.A, a_total_freq);
                    }
                } else if (Nature.nnt.equals(nature)) {
                    nrEnumFreqPair = EnumFreqPair.create(NRTag.G, NRTag.K);
                } else {
                    nrEnumFreqPair = new EnumFreqPair<>(NRTag.A, a_total_freq);
                }
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
        EnumFreqPair<NRTag> nrEnumFreqPair = personDictionary.getDictionary().get(name);
        if (nrEnumFreqPair == null) {
            return false;
        }
        return nrEnumFreqPair.containsLabel(A);
    }


}
