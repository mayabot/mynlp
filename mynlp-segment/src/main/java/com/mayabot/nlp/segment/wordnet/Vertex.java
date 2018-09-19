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

import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;

import java.util.Map;

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
     * 节点对应的等效词(抽象词)（如未##数）
     * Hanlp 中这个对应的字段是word，有些时候，当没有等效词的时候，这就是实际的词
     */
    public String abstractWord;

    /**
     * 核心词典的ID,也是Attribute的下标. 主要是针对核心词典的ID 或者词ID id之间有ngram统计关系
     */
    public int wordID = -1;

    /**
     * 词的属性
     * 如果要修改，应当new一个Attribute
     */
    public NatureAttribute natureAttribute;

    /////////////////////////////////////////////

    /**
     * 词性分析计算出来的词性,计算后的结果
     */
    private Nature guessNature = null;

    private int natureFreq;


    /////////////////////////////////////////////

    public Vertex(int length) {
        this.length = (short) length;
    }


    public Vertex(int length, int wordID, String abstractWord, NatureAttribute natureAttribute) {
        this.length = (short) length;
        setWordInfo(wordID, abstractWord, natureAttribute);
    }

    /**
     * 复制一个等效的新节点对象，除了length和wordinfo之外没有复制
     *
     * @param node 复制的节点
     */
    public Vertex(Vertex node) {
        this.length = node.length;
        this.abstractWord = node.abstractWord;
        this.wordID = node.wordID;
        this.natureAttribute = node.natureAttribute;
    }

    public static boolean hasNature(Vertex vertex, Nature nature) {
        return vertex != null && vertex.natureAttribute != null && vertex.natureAttribute.hasNature(nature);
    }

    /**
     * copy to new object , in abstractWord length wordinfo
     *
     * @return 新的Vertex对象
     */
    public Vertex copy() {
        return new Vertex(this);
    }

    public char theChar() {
        return this.vertexRow.theChar();
    }

    // for test
    public Vertex(int length, String abstractWord) {
        this.length = (short) length;
        this.abstractWord = abstractWord;
    }

    /**
     * 设置核心词典的属性
     *
     * @param wordID    核心词典的词下标(包括等效词下标)
     * @param word      等效词()
     * @param attribute 属性(包含了词性等信息)
     * @return 自己本身
     */
    public Vertex setWordInfo(int wordID, String word, NatureAttribute attribute) {
        this.wordID = wordID;
        this.abstractWord = word;
        this.natureAttribute = attribute;
        return this;
    }

    /**
     * @param wordID
     * @param attribute
     * @return
     */
    public Vertex setWordInfo(int wordID, NatureAttribute attribute) {
        this.wordID = wordID;
        this.abstractWord = null;
        this.natureAttribute = attribute;
        return this;
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

    public int realWordOffset() {
        return vertexRow.rowNum;
    }

    /**
     * 获取最可能的词性
     * 如果没有调用confirmNature 那么只能获取仅有一个词性的词语
     *
     * @return
     */
    public Nature guessNature() {
        if (guessNature != null) {
            return guessNature;
        }

        if (natureAttribute == null) {
            return null;
        }


        if (natureAttribute.size() == 1) {
            Map.Entry<Nature, Integer> one = natureAttribute.one();
            this.guessNature = one.getKey();
            this.natureFreq = one.getValue();
            return guessNature;
        }

        return null;
    }

    public int natureFreq() {
        return this.natureFreq;
    }

    public boolean confirmNature(Nature nature) {
        if (nature == null) {

            return true;
        }
        if (natureAttribute.size() == 1 && natureAttribute.one().getKey() == nature) {
            this.guessNature = nature;
            this.natureFreq = natureAttribute.one().getValue();
            return true;
        }
        boolean result = true;
        int frequency = natureAttribute.getNatureFrequency(nature);

        if (frequency == 0) {
            frequency = 1000;
            result = false;
        }

        guessNature = nature;
        this.natureFreq = frequency;
        return result;
    }

    public void setNatureAttribute(NatureAttribute attribute) {
        this.natureAttribute = attribute;
    }

    public boolean setNature(Nature nature, boolean updateWord) {
        if (nature == Nature.m) {
            abstractWord = CoreDictionary.TAG_NUMBER;
        } else if (nature == Nature.t) {
            abstractWord = CoreDictionary.TAG_TIME;
        }
        return confirmNature(nature);

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
        return "WordNode [length=" + length + "]" + (abstractWord == null ? "" : abstractWord);
    }

    public Vertex next() {
        return next;
    }

    public int getLength() {
        return length;
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

    public Vertex setAbstractWord(String abstractWord) {
        this.abstractWord = abstractWord;
        return this;
    }

    public Vertex setAbstractWordIfEmpty(String abstractWord) {
        if (this.abstractWord == null) {
            this.abstractWord = abstractWord;
        }
        return this;
    }

    public Vertex setWordID(int wordID) {
        this.wordID = wordID;
        return this;
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
