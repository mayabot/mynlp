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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mayabot.nlp.pinyin.model.Pinyin;
import com.mayabot.nlp.utils.Characters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jimichan
 */
public class PinyinResult {

    private List<Pinyin> pinyinList;
    private String text;

    private boolean notSkipNull = true;

    private boolean ignorePunctuation = true;

    /**
     * 模糊拼音.
     * as list 后，把
     * z = zh
     * c ch
     * s sh
     * an ang
     * en eng
     * in ing
     * ian iang
     * uan uang
     * 把拼音的都归一化
     */
    private boolean fuzzy;

    PinyinResult(List<Pinyin> pinyinList, String text) {
        this.pinyinList = pinyinList;
        this.text = text;

    }

    public PinyinResult fuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
        return this;
    }

    public PinyinResult skipNull(boolean skipNull) {
        this.notSkipNull = !skipNull;
        return this;
    }

    public PinyinResult ignorePunctuation(boolean ignore) {
        this.ignorePunctuation = ignore;
        return this;
    }

    public String asString() {
        return asString(" ");
    }

    private static Pattern pattern = Pattern.compile("(^zh|^ch|^sh|iang$|ang$|ing$|eng$|uang$)");
    private static ImmutableMap<String, String> fuzzyMap = ImmutableMap.<String, String>builder()
            .put("zh", "z")
            .put("ch", "c")
            .put("sh", "s")
            .put("eng", "en")
            .put("ang", "an")
            .put("ing", "in")
            .put("iang", "ian")
            .put("uang", "uan").build();

    public static void main(String[] args) {

    }

    public List<String> asList() {
        List<String> list = Lists.newArrayListWithCapacity(pinyinList.size());
        int i = 0;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5 && notSkipNull) {
                char x = text.charAt(i);
                if (ignorePunctuation && Characters.isPunctuation(x)) {

                } else {
                    list.add(text.charAt(i) + "");
                }
            } else {
                String withoutTone = pinyin.getPinyinWithoutTone();

                if (fuzzy) {
                    Matcher matcher = pattern.matcher(withoutTone);
                    StringBuffer sb = new StringBuffer();
                    if (matcher.find()) {
                        String part = matcher.group();
                        matcher.appendReplacement(sb, fuzzyMap.get(part));
                    }
                    matcher.appendTail(sb);
                    list.add(sb.toString());
                } else {
                    list.add(withoutTone);
                }


            }
            ++i;
        }
        return list;
    }

    public List<Character> asHeadList() {
        List<Character> list = Lists.newArrayListWithCapacity(pinyinList.size());
        int i = 0;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5 && notSkipNull) {
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

    @Override
    public String toString() {
        return asString();
    }
}