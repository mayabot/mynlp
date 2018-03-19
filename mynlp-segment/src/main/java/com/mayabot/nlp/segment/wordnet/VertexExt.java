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

//
//    public void clear(ObjectPredicate<? super Enum> predicate) {
//        if (cmap != null) {
//            cmap.removeAll(predicate);
//        }
//        if (intmap != null) {
//            intmap.removeAll(predicate);
//        }
//        if (stringmap != null) {
//            stringmap.removeAll(predicate);
//        }
//        if (doublemap != null) {
//            doublemap.removeAll(predicate);
//        }
//        if (objectMap != null) {
//            objectMap.removeAll(predicate);
//        }
//    }
//
//    public <T> T getObj(Enum e) {
//        if (objectMap == null) {
//            return null;
//        }
//        return (T) objectMap.instance(e);
//    }
//
//    public void setObj(Enum e, Object x) {
//        if (objectMap == null) {
//            objectMap = new ObjectObjectScatterMap<Enum, Object>();
//        }
//        objectMap.put(e, x);
//    }
//
//    public double getDouble(Enum e) {
//        if (doublemap == null) {
//            return 0;
//        }
//        return doublemap.instance(e);
//    }
//
//    public void setDouble(Enum e, double x) {
//        if (doublemap == null) {
//            doublemap = new ObjectDoubleScatterMap<>();
//        }
//        doublemap.put(e, x);
//    }
//
//    public String getString(Enum e) {
//        if (stringmap == null) {
//            return null;
//        }
//        return stringmap.instance(e);
//    }
//
//    public void setString(Enum e, String x) {
//        if (stringmap == null) {
//            stringmap = new ObjectObjectScatterMap<>();
//        }
//        stringmap.put(e, x);
//    }
//
//    public char getChar(Enum e) {
//        if (cmap == null) {
//            return (char) 0;
//        }
//        return cmap.instance(e);
//    }
//
//    public char setChar(Enum e, char x) {
//        if (cmap == null) {
//            cmap = new ObjectCharScatterMap<>();
//        }
//        return cmap.put(e, x);
//    }
//
//    public int getInt(Enum e) {
//        if (intmap == null) {
//            return 0;
//        }
//        return intmap.instance(e);
//    }
//
//    public void setInt(Enum e, int x) {
//        if (intmap == null) {
//            intmap = new ObjectIntScatterMap<>();
//        }
//        intmap.put(e, x);
//    }
//
//    enum A {
//        a, b, c
//    }
//
//    enum B {
//        a
//    }
//
//    public static void main(String[] args) {
//
//        try {
//            Thread.sleep(1000 * 10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("start");
//        ObjectPredicate<? super Enum> predicate = (obj) -> obj.getClass() == A.class;
//
//        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; i++) {
//            VertexExt ext = new VertexExt();
//            ext.setChar(A.a, 'x');
//            Enum x = A.a;
//            boolean b = x.getClass() == A.class;
//            ext.setChar(A.a, 'x');
//            ext.setChar(A.c, 'y');
//            ext.getChar(A.b);
//            ext.getChar(A.a);
//
//            ext.clear(predicate);
//
////            ext.tag = 'a';
////            ext.tag = 'a';
////            char x = ext.tag;
////            char y = ext.tag;
//        }
//        System.out.println(System.currentTimeMillis() - t1);
//    }


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
