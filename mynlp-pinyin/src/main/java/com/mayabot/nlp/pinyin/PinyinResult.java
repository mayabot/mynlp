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

package com.mayabot.nlp.pinyin;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mayabot.nlp.pinyin.model.Pinyin;
import com.mayabot.nlp.utils.Characters;

import java.util.List;

public class PinyinResult {

    private List<Pinyin> pinyinList;
    private String text;

    private boolean skipNull = false;

    private boolean ignorePunctuation = true;

    PinyinResult(List<Pinyin> pinyinList, String text) {
        this.pinyinList = pinyinList;
        this.text = text;

    }

    public PinyinResult skipNull(boolean skipNull) {
        this.skipNull = skipNull;
        return this;
    }

    public PinyinResult ignorePunctuation(boolean ignore) {
        this.ignorePunctuation = ignorePunctuation;
        return this;
    }

    public String asString() {
        return asString(" ");
    }


    public List<String> asList() {
        List<String> list = Lists.newArrayListWithCapacity(pinyinList.size());
        int i = 0;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5 && !skipNull) {
                char x = text.charAt(i);
                if (ignorePunctuation && Characters.isPunctuation(x)) {

                } else {
                    list.add(text.charAt(i) + "");
                }
            } else {
                list.add(pinyin.getPinyinWithoutTone());
            }
            ++i;
        }
        return list;
    }

    public List<Character> asHeadList() {
        List<Character> list = Lists.newArrayListWithCapacity(pinyinList.size());
        int i = 0;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5 && !skipNull) {
                char x = text.charAt(i);
                if (ignorePunctuation && Characters.isPunctuation(x)) {

                } else {
                    list.add(text.charAt(i));
                }
            } else {
                list.add(pinyin.getFirstChar());
            }
            ++i;
        }
        return list;
    }

    public String asString(String splitter) {

        return Joiner.on(splitter).join(asList());
    }

    public String asHeadString(String splitter) {
        return Joiner.on(splitter).join(asHeadList());
    }

    public String asHeadString() {
        return Joiner.on(" ").join(asHeadList());
    }

}