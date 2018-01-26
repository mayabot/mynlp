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

import java.util.Optional;

/**
 * FST匹配器
 *
 * @author jimichan
 */
public class FstFounder<T> {

    /**
     * 当前状态指针
     */
    private FstNode<T> point;

    private FST<T> fst;

    private int start = -1; // 起点
    private T startObj;
    private int lastIndex = -1;
//    private LinkedList<T> path;


    FstFounder(FST<T> fst) {
        this.fst = fst;
        reset();
    }

    public static class FoundResult<T> {
        boolean movieToNext = true;
        boolean found;

        int start;
        T startObj;
        //T endObj;
        int length;

        String endNodeName;

//        private LinkedList<T> path;

        public FoundResult(boolean movieToNext, boolean found, String endNodeName) {
            this.movieToNext = movieToNext;
            this.found = found;
            this.endNodeName = endNodeName;
        }

        public boolean isFound() {
            return found;
        }

        public int getStart() {
            return start;
        }

        public int getLength() {
            return length;
        }

        public T getStartObj() {
            return startObj;
        }

        public String getEndNodeName() {
            return endNodeName;
        }

        /*public T getEndObj() {
            return endObj;
        }*/
    }

    private FoundResult notFound_continue = new FoundResult(true, false, null);


    /**
     * 输入一个序列状态。返回是否运行到最终节点
     *
     * @return FoundResult
     */
    public FoundResult<T> input(int index, T currentObj) {

        try {

            Optional<FstNode<T>> go = point.transeform(index, currentObj);

            FstNode<T> lastPoint = null;

            // 重置
            if (!go.isPresent()) { // 没有跳到任何节点，默认就当跳转到start节点
                point = fst.getStartNode();
                lastPoint = fst.getStartNode();
            } else {
                lastPoint = point;
                point = go.get();
            }


            if (point.isStart()) {

                // 还是跳转到Start
                reset();
                return notFound_continue;

            } else if (point.isNormal()) {

                //第一次发生转移
                if (lastPoint.isStart()) {
                    resetData();
                    // 从开始节点转移到其他节点
                    this.start = index; // 记录开始的位置
                    this.startObj = currentObj;

                }

                return notFound_continue;

            } else { //is End node

                final boolean includeCurrentObj = point.isInclude(); //是否当前读入的值，放到结果里面去

                if (index == Integer.MAX_VALUE) {
                    // 如果是读入了一个虚拟的终止节点
                    // 那么肯定不能包含这个虚拟节点

                    FoundResult result = null;

                    //读入了一个include的节点
                    if (includeCurrentObj) {
                        result = notFound_continue;

                    } else {
                        result = new FoundResult(false, true, point.getId());

                        result.start = this.start;
                        result.startObj = this.startObj;
                        result.length = this.lastIndex - this.start + 1;

                    }

                    return result;

                } else {

                    FoundResult result = null;

                    if (includeCurrentObj) {
                        result = new FoundResult(true, true, point.getId());
                        if (this.start == -1) {
                            // 特殊情况，第一次遇到，就结束了
                            //
                            this.start = index;
                            this.startObj = currentObj;

                        }
                        result.start = this.start;
                        result.startObj = this.startObj;
                        result.length = index - this.start + 1;
                    } else {
                        if (this.start == -1) {
                            result = notFound_continue;
                        } else {
                            result = new FoundResult(false, true, point.getId());

                            result.start = this.start;
                            result.startObj = this.startObj;
                            result.length = index - this.start;
                        }
                    }

                    return result;
                }

            }

        } finally {
            lastIndex = index;
        }

    }

    public void reset() {
        this.point = fst.getStartNode();
        resetData();
    }

    private void resetData() {
        this.start = -1;
        this.startObj = null;
//        this.path = null;
        lastIndex = -1;
    }

}
