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

import java.util.Iterator;
import java.util.function.Function;

public class FstMatcher<T, R> {

    FstFounder<T> founder = null;

    int start = -1;

    int length = -1;

    T startObj;
    T endObj;

    String endNodeId;

    Iterator<R> iterator2;
    R[] iteratorData;


    Function<R, T> function;

    FstMatcher(FST<T> fst, Iterable<R> date, Function<R, T> function) {
        founder = fst.newFounder();
        this.iterator2 = date.iterator();
        this.function = function;
    }

    FstMatcher(FST<T> fst, Iterable<R> date) {
        founder = fst.newFounder();
        this.iterator2 = date.iterator();
        this.function = x -> (T) x;
    }

    FstMatcher(FST<T> fst, R[] date, Function<R, T> function) {
        founder = fst.newFounder();
        this.iteratorData = date;
        this.function = function;
    }

    FstMatcher(FST<T> fst, R[] date) {
        founder = fst.newFounder();
        this.iteratorData = date;
        this.function = x -> (T) x;
    }


    int index = -1;

    boolean movieToNext = true;

    private boolean run = true;

    T _preRead = null;
    T _nowRead = null;

    int movePoint = -1;

    public boolean find() {

        start = -1;
//		T preRead = null;
//		T nowRead = null;

        do {
            boolean isNotMovie = false;

            if (movieToNext == false) {
                //恢复场景
                isNotMovie = true;
                movieToNext = true;
//				preRead = _preRead;
//				nowRead = _nowRead;

            } else {
                if (iterator2 != null) {
                    if (iterator2.hasNext()) {
                        index++;
                        _preRead = _nowRead;
                        _nowRead = function.apply(iterator2.next());
                    } else {
                        index = Integer.MAX_VALUE;
                        _preRead = _nowRead;
                        _nowRead = null;
                        run = false;
                    }
                } else {
                    movePoint++;
                    if (movePoint < iteratorData.length) {
                        index++;
                        _preRead = _nowRead;
                        _nowRead = function.apply(iteratorData[movePoint]);
                    } else {
                        index = Integer.MAX_VALUE;
                        _preRead = _nowRead;
                        _nowRead = null;
                        run = false;
                    }
                }

            }

            FstFounder.FoundResult<T> fr = founder.input(index, _nowRead);
            movieToNext = fr.movieToNext;

//			if(!movieToNext){
//				//保存现场
//				_preRead = preRead;
//				_nowRead = nowRead;
//			}

            if (isNotMovie && !movieToNext) {
                //上次是不移动，这次还是不移动，那么就不对了，强制移动
                System.err.println("上次是不移动，这次还是不移动，那么就不对了，强制移动");
                movieToNext = true;
            }

            if (index == Integer.MAX_VALUE) {
                movieToNext = true; //强制跳转
            }

            if (fr.found) {

                //特殊情况，第一次就找到并返回
                this.start = fr.start;
                this.length = fr.length;
                this.endNodeId = fr.endNodeName;

                startObj = fr.startObj;
                endObj = fr.movieToNext ? _nowRead : _preRead;


                this.founder.reset();

                return true;
            }

        } while (run);

        return false;
    }

    public int getStart() {
        return start;
    }

    public FstFounder<T> getFounder() {
        return founder;
    }

    public int getLength() {
        return length;
    }

    public String getEndNodeId() {
        return endNodeId;
    }

    public T getStartObj() {
        return startObj;
    }

    public T getEndObj() {
        return endObj;
    }
}
