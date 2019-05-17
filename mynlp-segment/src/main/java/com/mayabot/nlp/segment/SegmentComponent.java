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
public interface SegmentComponent extends Comparable<SegmentComponent> {


    /**
     * return component name
     * @return name
     */
    String getName();

    /**
     * 组件是否启用。默认返回true，启用
     *
     * @return enabled
     */
    boolean isEnabled();

    void setEnabled(boolean enable);

    void enable();

    void disable();

    int getOrder();

    void setOrder(int order);

    @Override
    default int compareTo(SegmentComponent o) {
        return Ints.compare(this.getOrder(), o.getOrder());
    }
}
