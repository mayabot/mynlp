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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mayabot.nlp.segment.wordnet.Vertex;
import fasttext.FastText;
import fasttext.matrix.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {

        FastText fastText = FastText.loadModel(new File("/Users/jimichan/bin/fasttext/model_min.bin"));


        Vector v1 = fastText.getWordVector("苹果");
        Vector v2 = fastText.getWordVector("香蕉");
        Vector v3 = fastText.getWordVector("国王");
        System.out.println(v1.norm_2());

        System.out.println(Vector.dot(v1,v2));
        System.out.println(Vector.cosine(v1,v2));
        System.out.println(Vector.cosine(v3,v2));
        System.out.println(Vector.dot(v3,v2));

        Vector sv1 = fastText.getSentenceVector(Lists.newArrayList("香蕉 和 苹果 都 是 水果".split(" ")));
        Vector sv2 = fastText.getSentenceVector(Lists.newArrayList("香蕉 苹果 是 常见 的 水果 品种".split(" ")));

       // System.out.println(fastText);
        System.out.println(Vector.cosine(sv1,sv2));
    }
}
