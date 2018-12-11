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

import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.dictionary.DictionaryAbsWords;

/**
 * WordNode中的一个节点。顶点 它同时实现了LinkedList中节点的功能指针
 * <p>
 *
 * @author jimichan
 */
public class Vertex {

    /**
     * 词的长度
     */
    public final int length;

    /**
     * 归属的链表
     */
    VertexRow vertexRow;

    // 链表使用的相关属性

    Vertex next;
    Vertex prev;

    ////////////////////////////////////////////

    /**
     * 核心词典的ID,主要是针对核心词典的ID 或者词ID id之间有ngram统计关系
     */
    public int wordID = -1;

    /**
     * 词性
     */
    public Nature nature;

    /**
     * 二元模型分词时，需要从里面放入词频
     */
    public int freq = 0;


    /////////////////////////////////////////////

    public Vertex(int length) {
        this.length = (short) length;
    }


    public Vertex(int length, int wordID, int freq) {
        this.length = (short) length;
        this.wordID = wordID;
        this.freq = freq;
    }

    /**
     * 复制一个等效的新节点对象，除了length和wordinfo之外没有复制
     *
     * @param node 复制的节点
     */
    public Vertex(Vertex node) {
        this.length = node.length;
        this.wordID = node.wordID;
    }


    /**
     * copy to new object , in abstractWord length wordinfo
     *
     * @return 新的Vertex对象
     */
    public Vertex copy() {
        return new Vertex(this);
    }

//    public char theChar() {
//        return this.vertexRow.theChar();
//    }

    /**
     * 设定抽象词词性和对应的频率.
     *
     * @param nature
     * @param freq
     * @return
     */
    public Vertex setAbsWordNatureAndFreq(Nature nature, int freq) {
        this.wordID = DictionaryAbsWords.nature2id(nature);
        if (wordID >= 0 && wordID <= DictionaryAbsWords.MaxId) {
            this.nature = nature;
            this.freq = freq;
        }

        return this;
    }

    public Vertex setAbsWordNatureAndFreq(Nature nature) {
        return setAbsWordNatureAndFreq(nature, 10000);
    }

    /**
     * 是否抽象词
     *
     * @return
     */
    public boolean isAbsWord() {
        return wordID <= DictionaryAbsWords.MaxId;
    }

    /**
     * 如果是抽象词的标签,不是返回null
     *
     * @return
     */
    public String absWordLabel() {
        if (isAbsWord()) {
            return DictionaryAbsWords.id2label(wordID);
        } else {
            return null;
        }
    }

    public boolean isNature(Nature nature) {
        return this.nature != null && nature == this.nature;
    }


    /**
     * 接续行.接续是End就返回null
     *
     * @return
     */
    public VertexRow follow() {
        if (length == 0) {
            return null;
        }

        VertexRow follow = vertexRow.wordnet.getRow(vertexRow.rowNum + length);

        if (follow == null) {
            return null;
        }

        if (follow.isEmpty()) {
            return null;
        }

        return follow;
    }

    /**
     * 返回在原始文本中真实的词
     *
     * @return
     */
    private String realWord;


    public String realWord() {
        if (vertexRow.rowNum == -1) {
            return "";
        }
        if (realWord == null) {
            realWord = new String(vertexRow.wordnet.getCharArray(), vertexRow.rowNum, length);
        }
        return realWord;
    }

    public int offset() {
        return vertexRow.rowNum;
    }

    // hash 和 eq 只认 length

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vertex other = (Vertex) obj;
        return length == other.length;
    }

    @Override
    public String toString() {
        return "Vertex [length=" + length + "]";
    }

    public Vertex next() {
        return next;
    }

    public int length() {
        return length;
    }

    public VertexRow getVertexRow() {
        return vertexRow;
    }

    public int getRowNum() {
        return vertexRow.rowNum;
    }

    public Vertex getNext() {
        return next;
    }


    // ########################################//
    // 在最短路相关计算中用到的几个变量 //
    // ########################################//

    /**
     * 到该节点的最短路径的前驱节点
     */
    public Vertex from;

    /**
     * 最短路径对应的权重
     */
    public double weight;

//    /**
//     * 调节的权重。人工可以控制强制选择或不选择一些节点. 好像是越小越好
//     */
//    public double adjustWeight;

    /**
     * 优化网络的节点标记.
     */
    private boolean optimize = false;

    /**
     * 当网络状态为优化网络。那么之后新增的词，比较为true
     */
    private boolean optimizeNewNode = false;

    private Object tempObj;

    private char tempChar;

    public void clearTemp() {
        tempChar = 0;
        tempObj = null;
    }


    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public boolean isOptimizeNewNode() {
        return optimizeNewNode;
    }

    /**
     * 当网络状态为优化网络。那么之后新增的词，比较为true
     *
     * @param optimizeNewNode
     */
    public void setOptimizeNewNode(boolean optimizeNewNode) {
        this.optimizeNewNode = optimizeNewNode;
    }

    public <T> T getTempObj() {
        return (T) tempObj;
    }

    public void setTempObj(Object tempObj) {
        this.tempObj = tempObj;
    }

    public char getTempChar() {
        return tempChar;
    }

    public void setTempChar(char tempChar) {
        this.tempChar = tempChar;
    }

}
