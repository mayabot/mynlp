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

package com.mayabot.nlp.segment.wordnet;

import java.util.BitSet;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * 格式化 输出 wordnet
 *
 * @author jimichan
 */
class WordNetToStringBuilder {

    private Wordnet wordnet;
    private boolean showAttr = true;

    WordNetToStringBuilder(Wordnet wordnet, boolean showAttr) {
        this.wordnet = wordnet;
        this.showAttr = showAttr;
    }

    public String toString() {

        BitSet noover = this.wordnet.findNoOverWords();

        StringBuilder sb = new StringBuilder();

        char[] text = wordnet.getCharArray();

        for (int i = -1; i <= wordnet.getCharSizeLength(); i++) {
            StringBuilder line = new StringBuilder();

            VertexRow row = wordnet.row(i);

            char _char = 0;
            if (i >= 0 && i < text.length) {
                _char = text[i];
            } else {
                if (i == -1) {
                    _char = '¤';
                } else {
                    _char = '¶';
                }
            }
            //line.append(ansi().eraseScreen().render("@|red Hello|@ @|green World|@"));

            line.append(String.format("@|cyan %d|@\t@|green %c |@", i, _char));


            line.append("\t@|yellow ||@\t");

            if (row.isEmpty()) {
                if (noover.get(i)) {
                    line.append("\t@|green NULL|@");
                } else {
                    line.append("\t@|red NULL|@");
                }
            } else {
                if (i == -1) {
                    line.append("\t@|green BEGIN|@");
                } else if (i >= text.length) {
                    line.append("\t@|green END|@");
                } else {
                    int count = 0;
                    for (Vertex v : row) {
                        line.append("\t");

                        if (count > 0) {
                            line.append("\t");
                        }

                        line.append(text, i, v.length);// 原始词

                        if (v.abstractWord != null) {
                            line.append("[").append(v.abstractWord).append("]"); // 等效词
                        }

                        if (showAttr && v.natureAttribute != null) {
                            line.append(" ").append(v.natureAttribute);
                        }

                        if (v.weight != 0) {
                            line.append("(").append(v.weight).append(")");
                        }

                        //line.append(" From[" + v.from + "]");
                        count++;
                    }
                }
            }

            sb.append(line).append("\n");
        }

        return ansi().eraseScreen().render(sb.toString()).toString();
    }
}
