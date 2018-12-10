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

import com.mayabot.nlp.utils.CustomCharSequence;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * <p>
 * #S# [0] #Start 品 [1] 品质 质 [2] 质 和 [3] {和、和服} 服 [4] 服务 务 [5] 务 #E# [6] #End
 * <p>
 * <p>
 * sindex表示智能下标索引，用 -1 表示Start length表示End
 * <p>
 * WordNodeLinkedList 是一个优化过的linkedlist，去除了中间node节点,并增量排序和特殊性质
 * <p>
 * 实现CharSequence接口，可以进行当做字符串类型处理
 * O
 *
 * @author jimichan
 */
public class Wordnet implements CharSequence {

    private BestPathAlgorithm bestPathAlgorithm;

    /**
     * 节点，每一行都是前缀词，和 char数字一一对应
     */
    final VertexRow[] slotList;

    /**
     * 启始行 下标 -1
     */
    private final VertexRow begin;

    /**
     * 结尾行 下标 charSize
     */
    private final VertexRow end;

    /**
     * 原始句子对应的数组
     */
    private final char[] charArray;

    /**
     * 字符的数量
     */
    private final int charSize;

    /**
     * 是否工作在优化网络模式
     */
    private boolean optimizeNet = false;


    /**
     * 构建一个空的网，槽的数量是charArray.length
     *
     * @param charArray 字符数组
     */
    public Wordnet(char[] charArray) {
        this.charArray = charArray;

        this.charSize = charArray.length;

        this.begin = new VertexRow(-1, this);
        this.end = new VertexRow(charSize, this);

        // 创建一个空的数组
        slotList = new VertexRow[charSize];

        //初始化数组里面的对象,提前初始化好
        for (int i = 0; i < charSize; i++) {
            slotList[i] = new VertexRow(i, this);
        }
    }


//    static final NatureAttribute df = NatureAttribute.create(Nature.x, 1);

    public void fillNill() {
        for (VertexRow row : slotList) {
            if (row.first == null) {
                Vertex creted = row.getOrCrete(1);
                //creted.natureAttribute = df;
                creted.freq = 1;
            }
        }
    }

    /**
     * 寻找没有被所有路径覆盖的位置
     *
     * @return 返回 false的位置是没有覆盖掉的
     */
    public BitSet findNoOverWords() {

        BitSet noOverWords = new BitSet();

        for (int i = 0; i < charSize; i++) {
            VertexRow row = slotList[i];
            if (row != null) {
                Vertex p = row.first();
                while (p != null) {
                    noOverWords.set(row.rowNum, row.rowNum + p.length);
                    p = p.next;
                }
            }
        }

        return noOverWords;
    }

    /**
     * 寻找 存在跳转到当前row但是当前row没有跳出节点。
     * 或者 根本不存在跳转到当前行的路径(前置条件是没有被路径覆盖)
     * 寻找 悬空行，会导致路径中断
     *
     * @return bitset
     */
    public BitSet findDangling() {
        BitSet bitSet = new BitSet(charSize);
        BitSet noOverWords = findNoOverWords();

        //第一行肯定是跳入的
        bitSet.set(0);

        for (int i = 0; i < charSize; i++) {
            VertexRow row = slotList[i];
            if (row != null) {
                Vertex p = row.first();
                while (p != null) {
                    noOverWords.set(row.rowNum + p.length);
                    p = p.next;
                }
            }
        }

        for (int i = 0; i < charSize; i++) {
            //如果没人跳入，而且也没被覆盖
            if (!(bitSet.get(i) || noOverWords.get(i))) {
                bitSet.set(i);
            }
        }

        //bit set 里面为 false的，那么肯定是孤力
        return null;
    }

    /**
     * 如果对应行不存在，那么会自动创建
     *
     * @param sindex
     * @return
     */
    public final VertexRow getRow(int sindex) {
        return indexAt(sindex);
    }

    public final VertexRow row(int sindex) {
        return indexAt(sindex);
    }


    /**
     * 节点数字，包含了Start，end两个标记节点 动态统计,调用的时候请注意
     *
     * @return 包含了Start，end两个标记节点的数量
     */
    public int size() {
        int count = 0;

        for (int i = charSize - 1; i >= 0; i--) {
            VertexRow r = slotList[i];
            if (r != null) {
                count += r.size();
            }

        }
        count += this.begin.size();
        count += this.end.size();

        return count;
    }

    /**
     * 返回第多少行的链表
     * <p>
     * <pre>
     * 	length = 5
     *  sloat length = 7
     *
     * -1 0 1 2 3 4 5	优化过的智能下标
     *    0 1 2 3 4		char array 下标
     *  0 1 2 3 4 5 6	sloat数组下标
     * </pre>
     *
     * @param sindex SmartIndex -1表示Start
     * @return
     */
    private VertexRow indexAt(int sindex) {

        if (sindex == -1) {
            return begin;
        }
        if (sindex == charSize) {
            return end;
        }
        return slotList[sindex];
    }

    /**
     * 非空的行数
     *
     * @return
     */
    public int notNullRowNums() {
        int count = 0;
        for (int i = charSize - 1; i >= 0; i--) {
            VertexRow r = slotList[i];
            if (r != null && !r.isEmpty()) {
                count++;
            }
        }

        if (!this.begin.isEmpty()) {
            count++;
        }
        if (!this.end.isEmpty()) {
            count++;
        }

        return count;
    }

    ////////////// 顶点操作////////////////

    /**
     * 添加顶点,重复添加就忽略 返回被替换的节点
     *
     * @param charOffset sindex 下标和char对应
     * @param vertex     顶点
     * @return 返回被替换的节点, 新增节点返回null
     */
    public Vertex put(int charOffset, Vertex vertex) {
        return getRow(charOffset).put(vertex);
    }

    /**
     * put一个 ，但是返回的是一个最新的Vertext对象，然后可以继续设置属性
     *
     * @param offset
     * @param length
     * @return
     */
    public Vertex put(int offset, int length) {
        Vertex vertex = new Vertex(length);
        getRow(offset).put(vertex);
        return vertex;
    }

    /**
     * 行首节点
     *
     * @param sindex
     * @return
     */
    public Vertex getRowFirst(int sindex) {
        VertexRow row = row(sindex);
        return row.getFirst();
    }

    /**
     * 获取某一行长度为length的节点 没有就返回null
     *
     * @param sindex
     * @param length
     * @return
     */
    public Vertex getVertex(int sindex, int length) {
        VertexRow row = row(sindex);
        return row.get(length);
    }

    /**
     * 检查是否包含 offset-length的vertext
     *
     * @param sindex
     * @param length
     * @return
     */
    public boolean isNotContains(int sindex, int length) {
        VertexRow row = row(sindex);
        return !row.contains(length);
    }

    /**
     * 返回一行里面有几个节点
     *
     * @param sindex
     * @return
     */
    public int sizeInRow(int sindex) {
        VertexRow row = row(sindex);
        if (row == null) {
            return 0;
        }
        return row.size();
    }

    /**
     * 访问网络里面所有的Vertex节点. 从后向前了
     *
     * @param consumer
     */
    public final void accessAllVertex(Consumer<Vertex> consumer) {
        for (int i = slotList.length - 1; i >= 0; i--) {
            VertexRow row = slotList[i];
            if (row != null) {
                for (Vertex v = row.first(); v != null; v = v.next()) {
                    consumer.accept(v);
                }
            }
        }
    }


    /**
     * 标记优化网络的路径，根据目前的最优路径来标记
     * 目前最有路径上的点，标记为优化网络
     */
    public void tagOptimizeNetVertex(Wordpath wordPath) {
        Iterator<Vertex> ite = wordPath.iteratorVertex();
        while (ite.hasNext()) {
            Vertex node = ite.next();
            node.setOptimize(true);
        }
    }

    public void tagOptimizeNetVertex(Vertex[] wordPath) {
        for (Vertex node : wordPath) {
            node.setOptimize(true);
        }
    }

    /**
     * 根据当前的最优路径，设定已经选择的最优路径中的Vertex的最优网络标记为true
     */
    @Override
    public String toString() {
        return new WordNetToStringBuilder(this, false).toString();
    }

    public String toMoreString() {
        return new WordNetToStringBuilder(this, true).toString();
    }

    public VertexRow getBeginRow() {
        return begin;
    }

    public VertexRow getEndRow() {
        return end;
    }


    /**
     * 比如字符串长度为5，那么这个length返回5
     *
     * @return
     */
    public int getCharSizeLength() {
        return charSize;
    }

    /**
     * 原始的
     *
     * @return
     */
    public char[] getCharArray() {
        return charArray;
    }

    @Override
    public int length() {
        return charArray.length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0) {
            return ' ';
        }
        if (index >= charSize) {
            return ' ';
        }
        return charArray[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CustomCharSequence(charArray, start, end - start);
    }

    public boolean isOptimizeNet() {
        return optimizeNet;
    }

    public void setOptimizeNet(boolean optimizeNet) {
        this.optimizeNet = optimizeNet;
        //重置所有节点的optimize标记为false
        accessAllVertex(it -> {
            if (it.isOptimize()) {
                it.setOptimize(false);
                it.setOptimizeNewNode(false);
            }
        });

    }

    public VertexRow[] getSlotList() {
        return slotList;
    }

    public BestPathAlgorithm getBestPathAlgorithm() {
        return bestPathAlgorithm;
    }

    public Wordnet setBestPathAlgorithm(BestPathAlgorithm bestPathAlgorithm) {
        this.bestPathAlgorithm = bestPathAlgorithm;
        return this;
    }
}
