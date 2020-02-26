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

package com.mayabot.nlp.segment.wordnet;

import org.fusesource.jansi.Ansi;

import java.util.BitSet;

/**
 * 格式化 输出 wordnet
 *
 * @author jimichan
 */
class WordNetToStringBuilder {

    private Wordnet wordnet;
    private boolean showAttr;

    WordNetToStringBuilder(Wordnet wordnet, boolean showAttr) {
        this.wordnet = wordnet;
        this.showAttr = showAttr;
    }

    @Override
    public String toString() {

        BitSet noover = this.wordnet.findNoOverWords();

        StringBuilder sb = new StringBuilder();

        char[] text = wordnet.getCharArray();

        for (int i = -1; i <= wordnet.getCharSizeLength(); i++) {
            StringBuilder line = new StringBuilder();

            VertexRow row = wordnet.row(i);

            char char_ = 0;
            if (i >= 0 && i < text.length) {
                char_ = text[i];
            } else {
                if (i == -1) {
                    char_ = '¤';
                } else {
                    char_ = '¶';
                }
            }

            line.append(String.format("@|cyan %d|@\t@|green %c |@", i, char_));


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

                        // 原始词
                        line.append(text, i, v.length);

                        if (v.isAbsWord()) {
                            // 等效词
                            line.append("[").append(v.absWordLabel()).append("]");
                        }

                        if (showAttr && v.nature != null) {
                            line.append(" ").append(v.nature);
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

        try {
            Ansi ansi = Ansi.ansi();
            return ansi.eraseScreen().render(sb.toString()).toString();
        } catch (Throwable e) {
            //@|green BEGIN|@
            //@|cyan 12|@	@|green ¶ |@	@|yellow ||@		@|green END|@
            return sb.toString().replaceAll("@\\|\\w+? (.+?)\\|@", "$1");
        }

    }
}
