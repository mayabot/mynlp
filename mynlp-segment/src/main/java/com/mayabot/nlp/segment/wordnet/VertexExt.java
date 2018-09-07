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


/**
 * 各种扩展属性. 将来可以设计动态扩展属性
 */
@SuppressWarnings("unchecked")
public class VertexExt {
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

//    public boolean change = false;


//    private ObjectDoubleMap<Vertex> cache = new ObjectDoubleScatterMap(8);

//
//    private ObjectCharMap<Enum> cmap = null;
//    private ObjectIntMap<Enum> intmap = null;
//    private ObjectObjectMap<Enum, String> stringmap = null;
//    private ObjectDoubleMap<Enum> doublemap = null;
//    private ObjectObjectMap<Enum, Object> objectMap = null;

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
