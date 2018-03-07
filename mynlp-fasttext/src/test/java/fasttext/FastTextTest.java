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

package fasttext;

import com.google.common.collect.Lists;
import fasttext.matrix.Vector;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class FastTextTest {

    static FastText fastText;

    @BeforeClass
    public static void prepare() throws Exception{
        fastText = FastText.loadModel(new File("/Users/jimichan/bin/fasttext/wiki.zh.bin"));
    }

    @Test
    public void testWordVec() {
        Vector v1 = fastText.getWordVector("苹果");
        Vector v2 = fastText.getWordVector("香蕉");
        Vector v3 = fastText.getWordVector("国王");
        System.out.println(v1.norm_2());

        System.out.println(Vector.dot(v1,v2));
        System.out.println(Vector.cosine(v1,v2));
        System.out.println(Vector.cosine(v3,v2));
        System.out.println(Vector.dot(v3,v2));
    }

    @Test
    public void testSenVec() {
        Vector sv1 = fastText.getSentenceVector(Lists.newArrayList("香蕉 和 苹果 都 是 水果".split(" ")));
        Vector sv2 = fastText.getSentenceVector(Lists.newArrayList("香蕉 苹果 是 常见 的 水果 品种".split(" ")));
        System.out.println(Vector.cosine(sv1,sv2));

    }

    @Test
    public void testNN() throws Exception{

        FastText.NearestNeighbor nearestNeighbor = fastText.nearestNeighbor();

        nearestNeighbor.nn("香蕉", 11).forEach(System.out::println);

        System.out.println("---------------------");

        nearestNeighbor.nn("发动机", 11).forEach(System.out::println);
    }

    @Test
    public void testAnalogies() throws Exception{

        FastText.Analogies analogies = fastText.analogies();

        analogies.analogies("国王","皇后","男",10).stream().filter(x->x.key>0.7f).forEach(System.out::println);
        System.out.println("---------------------");
        analogies.analogies("国王","皇后","公",10).stream().filter(x->x.key>0.7f).forEach(System.out::println);
        System.out.println("---------------------");
        analogies.analogies("国王","皇后","皇帝",10).stream().filter(x->x.key>0.7f).forEach(System.out::println);
    }
}
