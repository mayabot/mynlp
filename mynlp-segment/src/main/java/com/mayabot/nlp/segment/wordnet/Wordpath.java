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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * WordPath表示对文本划分词的描述。
 * <p>
 * bitset 下标[i]
 * true 表示在1 后面插入分割,0表示不分割
 * <p>
 * 使用切分符号来表达单词路径的切分，而不是采用List。
 * 这个数据结构具有非常好的性质,随时合并、分裂、拆分，而且性能和空间优越
 *
 * @author jimichan jimichan@gmail.com
 */
public class Wordpath {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(Wordpath.class);

    private BitSet bitSet;

    private int length;

    private Wordnet wordnet;


    public Wordpath(Wordnet wordnet) {
        this.wordnet = wordnet;
        this.length = wordnet.length();
        this.bitSet = new BitSet(length);

        //每个字后面都有切分符号,默认切成单子
        bitSet.set(0, length);
    }

    public Wordnet getWordnet() {
        return wordnet;
    }

    /**
     * @return 一个包含头和尾的选择路径
     */
    public Iterable<Vertex> getBestPathWithBE() {
        //FIXME 此处可以优化
        Iterable<Vertex> b = ImmutableList.of(wordnet.getBeginRow().getFirst());
        Iterable<Vertex> m = this::iteratorVertex;
        Iterable<Vertex> e = ImmutableList.of(wordnet.getEndRow().getFirst());

        return Iterables.concat(b, m, e);
    }

    /**
     * 返回一个完整最优路径的迭代器。如果最后一个next不完整，那么抛出异常. 这样选择路径的时候，就不需要构建一个PATH list的数据结构了
     *
     * @return 不包含开始和结束的点路径
     */
    public Iterator<Vertex> iteratorVertex() {
        return new AbstractIterator<Vertex>() {

            private WordPointer pointer = wordPointer();


            @Override
            protected Vertex computeNext() {

                boolean hasNext = pointer.next();

                if (!hasNext) {
                    return endOfData();
                }

                final int from = pointer.getFrom();
                final int len = pointer.getLen();

                Vertex theVertex = wordnet.getVertex(from, len);

                if (theVertex == null) {
                    //System.out.println(wordnet.toMoreString());
                    //@ RepairWordnetProcessor 这里去修复了这个错误，到时要在之前去调用
                    logger.error("row: " + from + " len " + len + " select is null");
                    throw new IllegalStateException("row: " + from + " len " + len + " select is null");

                }

                return theVertex;
            }
        };
    }


    /**
     * 直接连接划定的长度的词汇。至于是否打破，可以自行弥补
     *
     * @param from
     * @param length
     * @return 合并后对应的Vertex
     */
    public Vertex combine(int from, int length) {
        this.connect(from, length);
        if (wordnet.isNotContains(from, length)) {
            return wordnet.put(from, length);
        } else {
            return wordnet.getVertex(from, length);
        }
    }

    /**
     * 最优路径算法一般会调用这个方法,划定一个词语
     *
     * @param vertex
     */
    public void combine(Vertex vertex) {
        this.connect(vertex.getRowNum(), vertex.length());
    }


    /**
     * 寻找path里面的词语，但是词图中却不存在.自动创建Vertex。consumer可以去设置Vertex的属性内容
     */
    public void findunloadVertext(Consumer<Vertex> consumer) {
        final int lastIndex = length - 1;
        // 0 1 2 3 4 5 6
        // 0 1 1 0 1 1 1
        //bitSet.nextSetBit()
//        printlnBitSet(bitSet);
        for (int i = 0; i < length; ) {
            int nextSplitIndex = bitSet.nextSetBit(i);

            if (i == lastIndex) {
                if (wordnet.isNotContains(i, 1)) {
                    // 一个点
                    consumer.accept(wordnet.put(i, 1));
                }
                i++;
            } else {
                int len = nextSplitIndex - i + 1;
                if (wordnet.isNotContains(i, len)) {
                    // 一个点
                    consumer.accept(wordnet.put(i, len));

                }
                i += len;
            }
        }
    }

    /**
     * 计算切分出，多个个词片断出来
     *
     * @return word count
     */
    public int wordCount() {
        return bitSet.cardinality();
    }

    public int wordCountInPath() {
        return this.wordCount();
    }

    public class WordPointer {
        private int from;
        private int len;

        private final int lastIndex = length - 1;
        private int i = 0;

        public WordPointer() {
        }

        public int getFrom() {
            return from;
        }

        public int getLen() {
            return len;
        }

        public boolean next() {
            int nextSplitIndex = Wordpath.this.bitSet.nextSetBit(i);

            if (i >= length) {
                return false;
            }

            if (i == lastIndex) {
                this.from = i;
                this.len = 1;
                i++;

            } else {
                int le = nextSplitIndex - i + 1;
                this.from = i;
                this.len = le;
                i += le;
            }

            return true;
        }
    }

    public WordPointer wordPointer() {
        return new WordPointer();
    }


    /**
     * 划定一个词语,并保持状态
     * 从from位置开始，将长度为length的连城一片
     *
     * @param from
     * @param length
     */
    private void connect(int from, int length) {
        if (length <= 0 || from < 0) {
            return;
        }
        //设置前面的插板
        if (from > 0) {
            bitSet.set(from - 1);
        }
        //设置最后的插板
        bitSet.set(from + length - 1);

        //消除之间所有吃插板
        bitSet.set(from, from + length - 1, false);
    }

    /**
     * 是否可以联合多个单词片断，但是有没有打断别的分词
     * 比如 AA B CC D E , 此时可以联合BCC
     * 但是不可以联合ABCC 因为打断了AA
     *
     * @param from
     * @param len
     * @return false表示没有破坏前后词, true是破坏了
     */
    public boolean willCutOtherWords(int from, int len) {
        //TODO 检查这里的实现是否完美正确
        // from 前面是否切分,前面如果有插板，那么返回false
        if (from != 0 && !bitSet.get(from - 1)) {
            return true;
        }

        //词尾原来是不是又插板，如果true，返回false
        int to = from + len - 1;
        //to 0 1 2 3
        return !bitSet.get(to);
    }

    @Override
    public String toString() {
        int last = bitSet.length() - 1;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < wordnet.length(); i++) {
            sb.append(wordnet.charAt(i));
            if (bitSet.get(i) && i != last) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public void reset() {
        this.bitSet.set(0, length);
    }
}
