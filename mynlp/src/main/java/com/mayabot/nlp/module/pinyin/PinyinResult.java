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

package com.mayabot.nlp.module.pinyin;

import com.mayabot.nlp.common.Lists;
import com.mayabot.nlp.common.utils.Characters;
import com.mayabot.nlp.module.pinyin.model.Pinyin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jimichan
 */
public class PinyinResult {

    private List<Pinyin> pinyinList;
    private String text;


    private boolean keepPunctuation = false;
    private boolean keepNum = true;
    private boolean keepAlpha = true;
    private boolean keepOthers = false;

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

    public List<Pinyin> getPinyinList() {
        return pinyinList;
    }

    public PinyinResult fuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
        return this;
    }

    public PinyinResult keepPunctuation(boolean keep) {
        this.keepPunctuation = keep;
        return this;
    }

    public PinyinResult keepNum(boolean keep) {
        this.keepNum = keep;
        return this;
    }

    public PinyinResult keepAlpha(boolean keep) {
        this.keepAlpha = keep;
        return this;
    }

    public PinyinResult keepOthers(boolean keep) {
        this.keepOthers = keep;
        return this;
    }

    public String asString() {
        return asString(" ");
    }

    private static Pattern pattern = Pattern.compile("(^zh|^ch|^sh|iang$|ang$|ing$|eng$|uang$)");

    private static Map<String, String> fuzzyMap = fmap();

    private static Map<String, String> fmap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("zh", "z");
        map.put("ch", "c");
        map.put("sh", "s");
        map.put("eng", "en");
        map.put("ang", "an");
        map.put("ing", "in");
        map.put("iang", "ian");
        map.put("uang", "uan");
        return map;
    }


    public List<String> asList() {
        List<String> list = Lists.newArrayListWithCapacity(pinyinList.size());
        int i = 0;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5) {
                // 数字 + 字符
                char x = text.charAt(i);
                String target = null;

                if (keepPunctuation && Characters.isPunctuation(x)) {
                    target = "" + x;
                } else if (keepNum && (x >= '0' && x <= '9')) {
                    target = "" + x;
                } else if (keepAlpha && ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z'))) {
                    target = "" + x;
                } else if (keepOthers) {
                    target = "" + x;
                }

                // skip null
                list.add(target);


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

            if (pinyin == Pinyin.none5) {
                char x = text.charAt(i);

                boolean out = false;

                if (keepPunctuation && Characters.isPunctuation(x)) {
                    out = true;
                } else if (keepNum && (x >= '0' && x <= '9')) {
                    out = true;
                } else if (keepAlpha && ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z'))) {
                    out = true;
                } else if (keepOthers) {
                    out = true;
                }

                // skip null
                if (out) {
                    list.add(x);
                } else {
                    list.add(null);
                }


            } else {
                list.add(pinyin.getFirstChar());
            }
            ++i;
        }
        return list;
    }

    public String asString(String splitter) {
        return joinSkipNull(asList(), splitter);
    }

    public String asHeadString(String splitter) {
        return joinSkipNull(asHeadList(), splitter);
    }

    private String joinSkipNull(List list, String splitter) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Object x : list) {
            if (x != null) {
                if (first) {
                    first = false;
                } else {
                    if (splitter != "") {
                        sb.append(splitter);
                    }
                }
                sb.append(x);
            }
        }
        return sb.toString();
    }


    public String asHeadString() {
        return asHeadString(" ");
    }

    @Override
    public String toString() {
        return asString();
    }
}