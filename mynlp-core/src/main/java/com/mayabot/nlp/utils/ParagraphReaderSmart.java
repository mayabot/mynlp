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

package com.mayabot.nlp.utils;

import java.io.IOException;
import java.io.Reader;

/**
 * 把原始文档分成若干段。这样下面的分词器就能分段处理。
 * <p>
 * 从StringReader中获取一个大的段落。
 * 由于内存有限。需要设置一个maxlength，防止有些文字过长没有标点段落。
 * 寻找到一个合适的大小的段落，从Reader中读取一个大小合适段落，不要使用传统的readline。
 * 万一变态一行的数量太大。或者太小。或者出现的截断(一行的最后一个字母和下一行的最后一个字母是一个词)
 *
 * @author jimichan
 */
public class ParagraphReaderSmart implements ParagraphReader {

    private FastCharReader fastCharReader;

    private int expectSize;
    private int pad; //最后加塞的大小
    private int max;
    private static final int minPad = 128;
    private static final int defaultExpect = 128 + 64;

    /**
     * reader 要求
     *
     * @param reader
     */
    public ParagraphReaderSmart(Reader reader) {
        this(reader, defaultExpect);
    }

    public ParagraphReaderSmart(Reader reader, int expect) {
        this.fastCharReader = new FastCharReader(reader);
        expectsize(expect);
    }

    public ParagraphReaderSmart(FastCharReader reader) {
        this(reader, defaultExpect);
    }

    public ParagraphReaderSmart(FastCharReader reader, int expect) {
        this.fastCharReader = reader;
        expectsize(expect);
    }

    private void expectsize(int expect) {
        this.expectSize = expect;
        this.pad = Math.max(minPad, this.expectSize / 2);
        this.max = this.expectSize + this.pad;
    }

    /**
     * 选择一个好的实现
     *
     * @param string
     * @return
     */
    public static ParagraphReader prepare(String string) {
        if (string.length() < 256) {
            return new ParagraphReaderFake(string);
        } else {
            return new ParagraphReaderSmart(new FastCharReader(string));
        }
    }


    public int offset() {
        return offset;
    }

    private int offset = -1;
    private int lastlen = -1;

    /**
     * 返回一段字符串
     *
     * @return
     * @throws IOException
     */
    public String next() throws IOException {
        StringBuilder result = new StringBuilder(max);

        int l = -1;
        int count = 0;
        while (count < max && (l = fastCharReader.read()) != -1) {
            char _ch = (char) l;
            result.append(_ch);
            count++;

            if (count > expectSize) { //已经超出.越到第一个
                if (Characters.isPunctuation(_ch)) {
                    break;
                }
            }
        }
        if (offset == -1) {
            offset = 0;
            lastlen = result.length();
        } else {
            offset = offset + lastlen;
            lastlen = result.length();
        }

        if (result.length() == 0) {
            return null;
        }
        return result.toString();
    }

}
