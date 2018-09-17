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
package com.mayabot.nlp.segment;

import com.google.common.primitives.Ints;

/**
 * 分词组件需要有个Name和设置是否启用的
 * <p>
 * Name : 组件的名称
 * Enable : 是否启用
 * Order ： 排序。 越小越靠前。
 *
 * @author jimichan
 */
public interface MynlpComponent extends Comparable<MynlpComponent> {

    int ORDER_FIRST = Integer.MIN_VALUE;
    int ORDER_MIDDLE = 0;
    int ORDER_LASTEST = Integer.MAX_VALUE;

    /**
     * @return
     */
    String getName();

    /**
     * 组件是否启用。默认返回true，启用
     *
     * @return
     */
    boolean isEnabled();

    void setEnabled(boolean enable);

    void enable();

    void disable();

    int getOrder();

    void setOrder(int order);

    @Override
    default int compareTo(MynlpComponent o) {
        return Ints.compare(this.getOrder(), o.getOrder());
    }
}
