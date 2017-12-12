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

package com.mayabot.nlp.fst;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

/**
 * 状态机的节点
 *
 * @param <R>
 */
public class FstNode<R> {

    public enum Type {
        START, NORMAL, END
    }

    /**
     * 当 输入符号 触发 转移到 END节点，那么当前的输入符号，算不算到结果里面去
     */
    private boolean include = false;

    private Type type = Type.START;

    private String id;

    private List<ConditionPair> conditionList = Lists.newArrayList();

    private FST fst;


    /**
     * $$ 的id 代表 include=true的end节点
     *
     * @param toNodeId
     * @param condition
     * @param <N>
     * @return 返回的是被连接的TO节点
     */
    public <N> FstNode<R> edge(FstCondition<R> condition, String toNodeId) {

        if (isEnd()) {
            throw new RuntimeException("End Node conn't to !!!");
        }


        FstNode<R> to = fst.$(toNodeId);
        conect(condition, to);

        return to;
    }

    private void conect(FstCondition<R> condition, FstNode<R> node) {
        conditionList.add(new ConditionPair(condition, node));
    }


    /**
     * @param toNodeId
     * @param condition
     * @param <N>
     * @return 返回的是被连接的TO节点
     */
    public <N> FstNode<R> to(String toNodeId, FstCondition<R> condition) {
        return edge(condition, toNodeId);
    }

    public void loop(FstCondition<R> condition) {
        conect(condition, this);
    }

    public <N> FstNode<R> linkIfReadEndFlag(String toNodeId) {
        return to(toNodeId, (x, y) -> x == Integer.MAX_VALUE);
    }

    /**
     * 状态转移
     *
     * @param i
     * @param readObj
     * @return
     */
    public Optional<FstNode<R>> transeform(int i, R readObj) {

        for (ConditionPair conditionPair : conditionList) {
            // i = Int.max 表示最后一个
            boolean accept = conditionPair.testMethod.test(i, readObj);
            if (accept) {
                return Optional.of(conditionPair.to);
            }
        }
        return Optional.empty();
    }

    private FstNode(Type type, String name) {
        this.type = type;
        this.id = name;
    }

    static <T> FstNode<T> createNode(FST fst, String name) {
        return new FstNode<T>(Type.NORMAL, name).fst(fst);
    }

    static <T> FstNode<T> createStartNode(FST fst) {
        return new FstNode<T>(Type.START, FST.Start_ID).fst(fst);
    }

    /**
     * @param name
     * @param include 是否接受当前的输入作为匹配的一部分
     * @return
     */
    static <T> FstNode<T> createTerminalNode(FST fst, String name, boolean include) {
        FstNode<T> s = new FstNode<T>(Type.END, name).fst(fst);
        s.include = include;
        return s;
    }


    public boolean isNormal() {
        return Type.NORMAL.equals(type);
    }

    public boolean isStart() {
        return Type.START.equals(type);
    }

    public boolean isEnd() {
        return Type.END.equals(type);
    }

    public String getId() {
        return id;
    }

    public boolean isInclude() {
        return include;
    }

    private FstNode<R> fst(FST<R> fst) {
        this.fst = fst;
        return this;
    }

    public static class ConditionPair<T> {

        FstCondition<T> testMethod;

//     testObj;

        FstNode<T> to;


        public ConditionPair(FstCondition<T> testMethod, FstNode<T> to) {
            this.testMethod = testMethod;
            this.to = to;
        }
    }

}
