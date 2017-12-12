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
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

public class FstMainTest {
    // TODO 设计一个复杂的识别模式，如电子邮件、url地址、联系地址等等

    @Test
    public void test1() {
        List<String> list = Lists.newArrayList("我 3 公斤 ipad 30 公斤".split(" "));

        FST<String> fst = new FST<String>();

        fst.start()
                .edge(FstCondition.pattern("\\d+"), "数字")
                .edge(FstCondition.in(Sets.newHashSet("公斤")), "$$");

        fst.start()
                .edge(FstCondition.pattern("\\w+"), "单词").
                edge(FstCondition.pattern("\\d+"), "$$");


        fst.fluze();

        FstMatcher<String, String> m = fst.newMatcher(list);

        while (m.find()) {
            System.out.println(m.getStart() + "-" + m.getLength() + " " + list.subList(m.getStart(), m.getStart() + m.getLength()));
        }

        //
        // long t1 = System.currentTimeMillis();
        // for(int i=0;i<1000000;i++){
        // int j=0;
        // FstMatcher<Integer,Vertex> m1 = fst.newMatcher();
        // for(Vertex v : list){
        // m1.input(j++, v, fp);
        // }
        // }
        // long t2 = System.currentTimeMillis();
        // System.out.println(t2-t1);
    }

    @Test
    public void test2() {
        FST<String> fst = new FST<String>();

        String[] seq = new String[]{"a", "b", "c"};

        FstNode<String> point = fst.start();

        for (int i = 0; i < seq.length; i++) {
            point = point.to("#" + i, FstCondition.eq(seq[i]));
        }

        point.to("$", FstCondition.TRUE());


        List<Integer> list = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }


        FstMatcher<String, Integer> m = fst.newMatcher(list, x -> ((char) x.intValue()) + "");

        while (m.find()) {
            System.out.println(m.getStart() + "-" + m.getLength() + " " + list.subList(m.getStart(), m.getStart() + m.getLength()));
        }
    }


    @Test
    public void test3() {
        FST<String> fst = new FST<String>();

        String[] seq = new String[]{"a", "b+", "c"};

        FstNode<String> point = fst.start();

        for (int i = 0; i < seq.length; i++) {


            boolean jia = false;
            if (seq[i].endsWith("+")) {
                jia = true;
            }
            seq[i] = seq[i].replace("+", "");

            point = point.to("#" + i, FstCondition.eq(seq[i]));

            String m = seq[i];
            if (jia) {
                point.to("#" + i, ((index, obj) -> obj.equals(m)));
            }
        }

        point.to("$", FstCondition.TRUE());


        List<Integer> list = Lists.newArrayList(97, 98, 98, 98, 99);


        FstMatcher<String, Integer> m = fst.newMatcher(list, x -> ((char) x.intValue()) + "");

        while (m.find()) {
            System.out.println(m.getStart() + "-" + m.getLength() + " " + list.subList(m.getStart(), m.getStart() + m.getLength()));
        }
    }


    @Test
    public void testModifyPlus() {
        List<String> list = Lists.newArrayList("w 1 2 3 3 公 斤".split(" "));

        FST<String> fst = new FST<String>();

        fst.start()
                .edge(FstCondition.pattern("\\d")
                        , "$");


        fst.fluze();

        FstMatcher<String, String> m = fst.newMatcher(list);

        while (m.find()) {
            System.out.println(m.getStart() + "-" + m.getLength() + " " + list.subList(m.getStart(), m.getStart() + m.getLength()));
        }

    }


    @Test
    public void testRex() {
        Pattern p = Pattern.compile("sss");
        CharSequence input = "";
        p.matcher(input);


    }


}
