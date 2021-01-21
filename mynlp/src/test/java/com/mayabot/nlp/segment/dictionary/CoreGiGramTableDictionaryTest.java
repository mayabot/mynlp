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

package com.mayabot.nlp.segment.dictionary;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.lexer.bigram.BiGramTableDictionaryImpl;
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary;
import org.junit.Test;

public class CoreGiGramTableDictionaryTest {

    @Test
    public void test() {
        BiGramTableDictionaryImpl table = Mynlp.instance().getInstance(BiGramTableDictionaryImpl.class);
        CoreDictionary dic = Mynlp.instance().getInstance(CoreDictionary.class);

        int v1 = dic.getWordID("君主制");//48801
        int v2 = dic.getWordID("国家");//55157
        int v3 = dic.getWordID("我");
        int v4 = dic.getWordID("是");

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);
        System.out.println(v4);

        System.out.println("---------");

        System.out.println(table.getBiFrequency(v1, v2));
        System.out.println(table.getBiFrequency(v3, v4));

        for (int i = 0; i < 100000; i++) {
            table.getBiFrequency(v1, v2);
        }


        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            table.getBiFrequency(v1, v2);
            table.getBiFrequency(v3, v4);
            table.getBiFrequency(1, v4);
            table.getBiFrequency(0, v4);
            table.getBiFrequency(3, v4);
            table.getBiFrequency(v1, v4);
        }

        long t2 = System.currentTimeMillis();

        System.out.println((t2 - t1) + "ms");
    }

}
