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

package com.mayabot.nlp.fst;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Function;

/**
 * 有限自动状态机
 *
 * @param <T>
 * @author jimichan
 */
public class FST<T> {

    public static final String Start_ID = "Start";


    private boolean fluze = false;

    private FstNode<T> startNode;

    private Map<String, FstNode<T>> nodeMap = Maps.newHashMap();


//    private int idCount = 1;
//
//    public int incIdCount() {
//        return idCount++;
//    }

    public FST() {
        startNode = FstNode.createStartNode(this);
    }

    public FstFounder<T> newFounder() {
        return new FstFounder<T>(this);
    }

    public FstMatcher<T, T> newMatcher(Iterable<T> date) {
        return new FstMatcher<T, T>(this, date);
    }

    public <R> FstMatcher<T, R> newMatcher(Iterable<R> date, Function<R, T> function) {
        return new FstMatcher<T, R>(this, date, function);
    }

    public FstMatcher<T, T> newMatcher(T[] date) {
        return new FstMatcher<T, T>(this, date);
    }

    public <R> FstMatcher<T, R> newMatcher(R[] date, Function<R, T> function) {
        return new FstMatcher<T, R>(this, date, function);
    }

    /**
     * 获取一个Node的定义
     *
     * @param nodeId
     * @return
     */
    public FstNode<T> $(String nodeId) {
        if (Start_ID.equals(nodeId) || "^".equals(nodeId)) {
            return startNode;
        }
        if (nodeMap.containsKey(nodeId)) {
            return nodeMap.get(nodeId);
        } else {
            FstNode<T> node = null;
            if (nodeId.startsWith("$$")) {
                node = FstNode.createTerminalNode(this, nodeId, true);
            } else if (nodeId.startsWith("$")) {
                node = FstNode.createTerminalNode(this, nodeId, false);
            } else {
                node = FstNode.createNode(this, nodeId);
            }
            nodeMap.put(nodeId, node);
            return node;
        }
    }

    public FstNode<T> start() {
        return getStartNode();
    }

    public FST<T> link(String from, String to, FstCondition<T> transformCondition) {
        $(from).edge(transformCondition, to);
        return this;
    }

    /**
     * 当读取到了虚拟的END输入
     *
     * @param from
     * @param to
     * @return
     */
    public FST<T> linkIfReadEndFlag(String from, String to) {
        $(from).edge((x, y) -> x == Integer.MAX_VALUE, to);
        return this;
    }


    /**
     * 状态冻结。不能变更
     */
    public void frozen() {
        fluze = true;
    }

    public FstNode<T> getStartNode() {
        return startNode;
    }

}
