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
package com.mayabot.nlp.segment.crf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特征模板
 *
 * @author hankcs
 */
public class FeatureTemplate implements Serializable {

    public static int serialVersionUID = 1;

    /**
     * 用来解析模板的正则表达式
     */
    static final Pattern pattern = Pattern.compile("%x\\[(-?\\d*),(\\d*)]");


    String template;
    /**
     * 每个部分%x[-2,0]的位移，其中int[0]储存第一个数（-2），int[1]储存第二个数（0）
     */
    ArrayList<int[]> offsetList;

    List<String> delimiterList;

    public FeatureTemplate() {
    }

    public static FeatureTemplate create(String template) {
        FeatureTemplate featureTemplate = new FeatureTemplate();
        featureTemplate.delimiterList = new LinkedList<String>();
        featureTemplate.offsetList = new ArrayList<int[]>(3);
        featureTemplate.template = template;
        Matcher matcher = pattern.matcher(template);
        int start = 0;
        while (matcher.find()) {
            featureTemplate.delimiterList.add(template.substring(start, matcher.start()));
            start = matcher.end();
            featureTemplate.offsetList.add(new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))});
        }
        return featureTemplate;
    }

    public char[] generateParameter(Table table, int current) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String d : delimiterList) {
            sb.append(d);
            int[] offset = offsetList.get(i++);
            sb.append(table.get(current + offset[0], offset[1]));
        }

        char[] o = new char[sb.length()];
        sb.getChars(0, sb.length(), o, 0);

        return o;
    }


    @Override
    public String toString() {
        String sb = "FeatureTemplate{" + "template='" + template + '\'' +
                ", delimiterList=" + delimiterList +
                '}';
        return sb;
    }
}
