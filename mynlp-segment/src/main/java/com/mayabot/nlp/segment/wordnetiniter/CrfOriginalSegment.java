/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.wordnetiniter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.corpus.tag.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.model.crf.CRFModelComponent;
import com.mayabot.nlp.segment.model.crf.CRFSegmentModel;
import com.mayabot.nlp.segment.model.crf.Table;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.utils.CharacterHelper;

/**
 * CRF基础的分词器
 */
@Singleton
public class CrfOriginalSegment implements WordnetInitializer {


    private final CRFSegmentModel crfModel;

    public static CrfOriginalSegment build(Injector injector) {
        return injector.getInstance(CrfOriginalSegment.class);
    }

    @Inject
    public CrfOriginalSegment(CRFModelComponent crfModelComponent) {
        this.crfModel = crfModelComponent.getCrfSegmentModel();
    }

    @Override
    public void initialize(Wordnet wordnet) {

        Table table = new Table();
        table.v = atomSegmentToTable(wordnet.getCharArray());
        crfModel.tag(table);

        System.out.println(table);

        int offset = 0;
        OUTER:
        for (int i = 0; i < table.v.length; offset += table.v[i][1].length(), ++i) {
            String[] line = table.v[i];
            switch (line[2].charAt(0)) {
                case 'B': {
                    int begin = offset;
                    while (table.v[i][2].charAt(0) != 'E') {
                        offset += table.v[i][1].length();
                        ++i;
                        if (i == table.v.length) {
                            break;
                        }
                    }
                    if (i == table.v.length) {
                        Vertex v = wordnet.put(begin, offset - begin);
                        v.setWordInfo(-1, null, NatureAttribute.create(Nature.x, 1000));
                        //termList.add(new Term(new String(sentence, begin, offset - begin), null));

                        break OUTER;
                    } else {
                        Vertex v = wordnet.put(begin, offset - begin + table.v[i][1].length());
                        v.setWordInfo(-1, null, NatureAttribute.create(Nature.x, 1000));
                        //termList.add(new Term(new String(sentence, begin, offset - begin + table.v[i][1].length()), null));
                    }
                }
                break;
                default: {
                    Vertex v = wordnet.put(offset, table.v[i][1].length());
                    v.setWordInfo(-1, null, NatureAttribute.create(Nature.x, 1000));
                    //termList.add(new Term(new String(sentence, offset, table.v[i][1].length()), null));

                }
                break;
            }
        }

        System.out.println(wordnet);

    }


    public String[][] atomSegmentToTable(char[] sentence) {
        String[][] table = new String[sentence.length][3];
        int size = 0;
        final int maxLen = sentence.length - 1;
        final StringBuilder sbAtom = new StringBuilder();
        out:
        for (int i = 0; i < sentence.length; i++) {
            if (sentence[i] >= '0' && sentence[i] <= '9') {
                sbAtom.append(sentence[i]);
                if (i == maxLen) {
                    table[size][0] = "M";
                    table[size][1] = sbAtom.toString();
                    ++size;
                    sbAtom.setLength(0);
                    break;
                }
                char c = sentence[++i];
                while (c == '.' || c == '%' || (c >= '0' && c <= '9')) {
                    sbAtom.append(sentence[i]);
                    if (i == maxLen) {
                        table[size][0] = "M";
                        table[size][1] = sbAtom.toString();
                        ++size;
                        sbAtom.setLength(0);
                        break out;
                    }
                    c = sentence[++i];
                }
                table[size][0] = "M";
                table[size][1] = sbAtom.toString();
                ++size;
                sbAtom.setLength(0);
                --i;
            } else if (CharacterHelper.isEnglishLetter(sentence[i]) || sentence[i] == ' ') {
                sbAtom.append(sentence[i]);
                if (i == maxLen) {
                    table[size][0] = "W";
                    table[size][1] = sbAtom.toString();
                    ++size;
                    sbAtom.setLength(0);
                    break;
                }
                char c = sentence[++i];
                while (CharacterHelper.isEnglishLetter(c) || c == ' ') {
                    sbAtom.append(sentence[i]);
                    if (i == maxLen) {
                        table[size][0] = "W";
                        table[size][1] = sbAtom.toString();
                        ++size;
                        sbAtom.setLength(0);
                        break out;
                    }
                    c = sentence[++i];
                }
                table[size][0] = "W";
                table[size][1] = sbAtom.toString();
                ++size;
                sbAtom.setLength(0);
                --i;
            } else {
                table[size][0] = table[size][1] = String.valueOf(sentence[i]);
                ++size;
            }
        }

        return resizeArray(table, size);
    }

    /**
     * 数组减肥，原子分词可能会导致表格比原来的短
     *
     * @param array
     * @param size
     * @return
     */
    private static String[][] resizeArray(String[][] array, int size) {
        String[][] nArray = new String[size][];
        System.arraycopy(array, 0, nArray, 0, size);
        return nArray;
    }

}
