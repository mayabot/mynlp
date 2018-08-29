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

package com.mayabot.nlp.segment.common;

import com.mayabot.nlp.segment.wordnet.Vertex;

/**
 * 把 Vertex[] 里面的 temp char 伪装成 CharSequence
 *
 * @author jimichan
 */
public class VertexTagCharSequenceTempChar implements CharSequence {

    private Vertex[] text;
    private int offset;
    private int length;


    public VertexTagCharSequenceTempChar(Vertex[] text) {
        this(text, 0, text.length);
    }

    public VertexTagCharSequenceTempChar(Vertex[] text, int offset, int length) {
        this.text = text;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= length)) {
            throw new StringIndexOutOfBoundsException(index);
        }

        return text[offset + index].getTempChar();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new VertexTagCharSequenceTempChar(text, start + offset, end - start);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < length + offset; i++) {
            sb.append(text[i].getTempChar());
        }
        return sb.toString();
    }
}
