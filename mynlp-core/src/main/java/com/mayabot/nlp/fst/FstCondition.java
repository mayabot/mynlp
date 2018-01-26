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


import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 状态转移条件
 *
 * @author jimichan
 */
@FunctionalInterface
public interface FstCondition<T> {

    /**
     * 转移状态. 如果返回null表示不转移
     *
     * @param obj
     * @return
     */
    boolean test(int index, T obj);

    default FstCondition<T> not() {
        return (index, obj) -> !FstCondition.this.test(index, obj);
    }


    FstCondition readEndFlag = (index, obj) -> {
        if (index == Integer.MAX_VALUE) { // -2 表示读入了$ 序列终止符号
            return true;
        }
        return false;
    };


    static <T> FstCondition<T> not(FstCondition<T> condition) {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                return !condition.test(index, obj);
            }
        };
    }

    static <Test, R> FstCondition<R> predicate(Test test, BiPredicate<Test, R> m) {
        return new FstCondition<R>() {
            @Override
            public boolean test(int index, R obj) {
                if (test == null) {
                    return false;
                }
                return m.test(test, obj);
            }
        };
    }


    public static <R> FstCondition<R> predicate(Predicate<R> predicate) {
        return new FstCondition<R>() {
            @Override
            public boolean test(int index, R obj) {
                return predicate.test(obj);
            }
        };
    }

    public static <T> FstCondition<T> pattern(Pattern pattern) {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                if (obj == null) {
                    return false;
                }
                return pattern.matcher(obj.toString()).find();
            }
        };
    }


    public static <T> FstCondition<T> pattern(String _pattern) {
        Pattern pattern = Pattern.compile(_pattern);
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                if (obj == null) {
                    return false;
                }
                return pattern.matcher(obj.toString()).find();
            }
        };
    }


    public static <T> FstCondition<T> eq(T testObj) {
        return predicate(testObj, (t, y) -> t.equals(y));
    }

    public static <T> FstCondition<T> in(Set<T> set) {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                if (obj == null) {
                    return false;
                }
                return set.contains(obj);
            }
        };
    }

    public static <T> FstCondition<T> NULL() {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                return obj == null;
            }
        };
    }

    public static <T> FstCondition<T> NotNull() {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                return obj != null;
            }
        };
    }


    public static <T> FstCondition<T> FALSE() {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                return false;
            }
        };
    }

    public static <T> FstCondition<T> TRUE() {
        return new FstCondition<T>() {
            @Override
            public boolean test(int index, T obj) {
                return true;
            }
        };
    }


    /**
     * 多个条件是或的关系
     * <p>
     * Created by jimichan on 2017/7/9.
     */
    class AndConditon<T> implements FstCondition<T> {


        private List<FstCondition<T>> list;

        public AndConditon() {
            list = Lists.newArrayList();
        }

        public AndConditon<T> add(FstCondition<T> condition) {
            list.add(condition);
            return this;
        }


        @Override
        public boolean test(int index, T obj) {

            if (list.isEmpty()) {
                return false;
            }

            for (FstCondition<T> condition : list) {
                if (!condition.test(index, obj)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 或关系
     *
     * @param <T>
     */
    class OrConditon<T> implements FstCondition<T> {

        private List<FstCondition<T>> list;

        public OrConditon() {
            list = Lists.newArrayList();
        }

        public void add(FstCondition<T> condition) {
            list.add(condition);
        }

        @Override
        public boolean test(int index, T obj) {

            if (list.isEmpty()) {
                return false;
            }

            for (FstCondition<T> condition : list) {
                if (condition.test(index, obj)) {
                    return true;
                }
            }
            return false;
        }
    }

}
