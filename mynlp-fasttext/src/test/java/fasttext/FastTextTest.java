///*
// * Copyright 2018 mayabot.com authors. All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fasttext;
//
//import com.google.common.collect.Lists;
//import com.mayabot.mynlp.fasttext.FastText;
//import fasttext.matrix.Vector;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.io.File;
//
//public class FastTextTest {
//
//    static FastText fastText;
//
//    @BeforeClass
//    public static void prepare() throws Exception{
//        fastText = FastText.loadC++Model("/Users/jimichan/project-new/mynlp/mynlp-fasttext/data/wiki.zh.bin");
//    }
//
//    @Test
//    public void testWordVec() {
//        Vector v1 = fastText.getWordVector("信用卡 注销");
//        Vector v2 = fastText.getWordVector("香蕉");
//        Vector v3 = fastText.getWordVector("国王");
//        System.out.println(v1.norm());
//
//        System.out.println(Vector.dot(v1,v2));
//        System.out.println(Vector.cosine(v1,v2));
//        System.out.println(Vector.cosine(v3,v2));
//        System.out.println(Vector.dot(v3,v2));
//    }
//
//    @Test
//    public void testWordVec2() {
//        Vector v1 = fastText.getSentenceVector(Lists.newArrayList("注销".split(" ")));
//        Vector v2 = fastText.getSentenceVector(Lists.newArrayList("挂失".split(" ")));
//
//        System.out.println(Vector.cosine(v1,v2));
//
//
//        FastText.NearestNeighbor nearestNeighbor = fastText.nearestNeighbor();
//
//        nearestNeighbor.nn("挂失", 50).forEach(System.out::println);
//    }
//
//    @Test
//    public void testSenVec() {
//        String[] x =new String[]{
//        "检查 故障车 左边 后排 4 分 座椅 时",
//        "发现 后排 4 分 座椅 坐垫 右侧 骨架 上 有 明显 的 突出 铁刺",
//        "铁刺 长约 2cm",
//        "在 后排 4 分 座椅 靠背 向前 折叠 放下 再 翻起 时",
//        "座椅 头枕 皮套 直接 被 突出 铁刺 划破",
//                "加速 时 比较 明显",
//                "检查 后桥 油 足够",
//                "干净 和 味道",
//                "苹果 和 香蕉"
//        };
//        for (int i = 0; i < x.length; i++) {
//            for (int j = 0; j < x.length; j++) {
//                if (i != j) {
//                    Vector sv1 = fastText.getSentenceVector(Lists.newArrayList(x[i].split(" ")));
//                    Vector sv2 = fastText.getSentenceVector(Lists.newArrayList(x[j].split(" ")));
//                    System.out.println(x[i]);
//                    System.out.println(sv1);
//                    System.out.println(x[j]);
//                    System.out.println(sv2);
//                    System.out.println(Vector.cosine(sv1,sv2));
//                    System.out.println("==============");
//                }
//            }
//        }
//
//
//    }
//
//    @Test
//    public void testNN() throws Exception{
//
//        FastText.NearestNeighbor nearestNeighbor = fastText.nearestNeighbor();
//
//        nearestNeighbor.nn("注销", 11).forEach(System.out::println);
//
////        System.out.println("---------------------");
////
////        nearestNeighbor.nn("发动机", 11).forEach(System.out::println);
//    }
//
//    @Test
//    public void testAnalogies() throws Exception{
//
//        FastText.Analogies analogies = fastText.analogies();
//
//        analogies.analogies("国王","皇后","男",10).stream().filter(x->x.first >0.7f).forEach(System.out::println);
//        System.out.println("---------------------");
//        analogies.analogies("国王","皇后","公",10).stream().filter(x->x.first >0.7f).forEach(System.out::println);
//        System.out.println("---------------------");
//        analogies.analogies("国王","皇后","皇帝",10).stream().filter(x->x.first >0.7f).forEach(System.out::println);
//    }
//}
