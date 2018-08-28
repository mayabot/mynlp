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
package com.mayabot.nlp.segment.dictionary.core;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.caching.MynlpCacheable;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.TreeMap;

/**
 * 使用DoubleArrayTrie实现的核心词典。
 * 核心词典文件CoreNatureDictionary.txt
 * HanLP的核心词典训练自人民日报2014语料，语料不是完美的，总会存在一些错误。
 * <pre>
 * 核心词性词频词典
 * 比如你在data/dictionary/CoreNatureDictionary.txt中发现了一个不是词的词，或者词性标注得明显不对，那么你可以修改它，然后删除缓存文件使其生效。
 * 目前CoreNatureDictionary.ngram.txt的缓存依赖于CoreNatureDictionary.txt的缓存，修改了后者之后必须同步删除前者的缓存，否则可能出错
 * 核心二元文法词典
 * 二元文法词典data/dictionary/CoreNatureDictionary.ngram.txt储存的是两个词的接续，如果你发现不可能存在这种接续时，删掉即可。
 * 你也可以添加你认为合理的接续，但是这两个词必须同时在核心词典中才会生效。
 * </pre>
 */
@Singleton
public class CoreDictionary implements MynlpCacheable {

    /**
     * 句子的开始 begin
     */
    public final static String TAG_BIGIN = "始##始";

    /**
     * 结束 end
     */
    public final static String TAG_END = "末##末";

    /**
     * 其它
     */
    public final static String TAG_OTHER = "未##它";
    /**
     * 团体名词 组织机构 nt
     */
    public final static String TAG_GROUP = "未##团";
    /**
     * 数词 m
     */
    public final static String TAG_NUMBER = "未##数";
    /**
     * 数量词 mq （现在觉得应该和数词同等处理，比如一个人和一人都是合理的）
     */
    public final static String TAG_QUANTIFIER = "未##量";
    /**
     * 专有名词 nx
     */
    public final static String TAG_PROPER = "未##专";
    /**
     * 时间 t
     */
    public final static String TAG_TIME = "未##时";

    /**
     * 字符串 x
     */
    public final static String TAG_CLUSTER = "未##串";


    /**
     * 地址 ns
     */
    public final static String TAG_PLACE = "未##地";
    /**
     * 人名 nr
     */
    public final static String TAG_PEOPLE = "未##人";


    // 一些特殊的WORD_ID
    /**
     * 始##始
     * TAG_BIGIN
     */
    public final int Begin_WORD_ID;

    /**
     * 末##末
     */
    public final int End_WORD_ID;

    /**
     * TAG_PEOPLE
     */
    public final int NR_WORD_ID;
    public final int NS_WORD_ID;

    public final int NT_WORD_ID;
    public final int T_WORD_ID;

    /**
     * 字符串
     */
    public final int X_WORD_ID;
    public final int M_WORD_ID;

    public final int NX_WORD_ID;

    public final int XX_WORD_ID;

    private final MynlpIOC mynlp;

//    private InternalLogger logger = InternalLoggerFactory.getInstance(CoreDictionary.class);

    public final Setting<String> coreDictSetting =
            Setting.string("core.dict", "dictionary/core/CoreNatureDictionary.txt");

    private DoubleArrayTrie<NatureAttribute> trie;

    //public static final int totalFrequency = 221894;

    public int MAX_FREQUENCY = 221894;

    @Inject
    public CoreDictionary(MynlpIOC Mynlp) throws Exception {
        this.mynlp = Mynlp;

        this.restore();

        //计算出预编译的量
        Begin_WORD_ID = getWordID(TAG_BIGIN);
        End_WORD_ID = getWordID(TAG_END);

        XX_WORD_ID = getWordID(TAG_OTHER);

        NR_WORD_ID = getWordID(TAG_PEOPLE);
        NS_WORD_ID = getWordID(TAG_PLACE);
        NT_WORD_ID = getWordID(TAG_GROUP);
        T_WORD_ID = getWordID(TAG_TIME);
        X_WORD_ID = getWordID(TAG_CLUSTER);
        M_WORD_ID = getWordID(TAG_NUMBER);
        NX_WORD_ID = getWordID(TAG_PROPER);
    }


    @Override
    @SuppressWarnings(value = "rawtypes")
    public void loadFromRealData() throws Exception {
        NlpResource dictResource = mynlp.loadResource(coreDictSetting);


        TreeMap<String, NatureAttribute> map = new TreeMap<>();

        int maxFreq = 0;

        try (CharSourceLineReader reader = dictResource.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();

                String[] param = line.split("\\s");

                NatureAttribute attribute = NatureAttribute.create(param);
                map.put(param[0], attribute);
                maxFreq += attribute.getTotalFrequency();
            }
        }

        this.MAX_FREQUENCY = maxFreq;

        if (map.isEmpty()) {
            throw new RuntimeException("not found core dict file ");
        }

        this.trie = (DoubleArrayTrie<NatureAttribute>) new DoubleArrayTrieBuilder().build(map);
    }

    @Override
    public File cacheFileName() {
        String hash = mynlp.loadResource(coreDictSetting).hash();
        return new File(mynlp.getCacheDir(), "core.dict." + hash);
    }

    @Override
    public void saveToCache(OutputStream out) throws Exception {
        DataOutputStream dataOutput = new DataOutputStream(out);

        dataOutput.writeInt(MAX_FREQUENCY);
        DoubleArrayTrie.write(trie, dataOutput, NatureAttribute::write);

        dataOutput.flush();

    }

    @Override
    public void readFromCache(File file) throws Exception {

        try (FileInputStream fileInputStream = new FileInputStream(file);
             FileChannel channel = fileInputStream.getChannel();) {

            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            this.MAX_FREQUENCY = map.getInt();

            this.trie = DoubleArrayTrie.read(map.slice(), NatureAttribute::read);

//            int[] base = this.trie.base;
//            int[] check = this.trie.check;
//            IntIntScatterMap x = new IntIntScatterMap(356277);
//            IntIntScatterMap b = new IntIntScatterMap(356277);
//
//            long t1 = System.currentTimeMillis();
//            int c=0;
//            for (int i = 0; i < base.length; i++) {
//                if (base[i] != 0) {
//                    c++;
//                    x.put(i, base[i]);
//                }
//                if (check[i] != 0) {
//                    b.put(i, check[i]);
//                }
//            }
//            long t2 = System.currentTimeMillis();
//            System.out.println((t2-t1)+"ms");
//            System.out.println("count "+c);
//            System.out.println(base.length);
//            System.out.println(x.keys.length);
//            System.out.println(x.values.length);
//            System.out.println(b.values.length);
//            System.out.println(b.values.length);
        }

//        try (InputStream inputStream = new BufferedInputStream(Files.asByteSource(file).openStream(), 64 * 1024)) {
//            DataInput dataInput = new DataInputStream(inputStream);
//            this.MAX_FREQUENCY = dataInput.readInt();
//            this.trie = DoubleArrayTrie.read(dataInput, NatureAttribute::read);
//
//        }
    }

    public static void main(String[] args) throws IOException {

        File temp = new File("temp/mynlp/dec4fa6cdecf95af4d4d3b0195b330dd.core.dict");

//        ByteArrayOutputStream out = new ByteArrayOutputStream();

//        DataOutputStream dataOutput = new DataOutputStream(Files.asByteSink(temp).openBufferedStream());
//
//        dataOutput.writeInt(25114638);
//        dataOutput.flush();

//        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        DataInputStream dataInputStream = new DataInputStream(Files.asByteSource(temp).openBufferedStream());

        System.out.println(dataInputStream.readInt());


    }


    /**
     * 获取条目
     *
     * @param key
     * @return
     */
    public NatureAttribute get(String key) {
        return trie.get(key);
    }

    /**
     * 获取条目
     *
     * @param wordID
     * @return
     */
    public NatureAttribute get(int wordID) {
        return trie.get(wordID);
    }


    public int indexOf(CharSequence key) {
        return trie.indexOf(key);
    }

    public int indexOf(CharSequence key, int pos, int len, int nodePos) {
        return trie.indexOf(key, pos, len, nodePos);
    }

    public int indexOf(char[] chars, int pos, int len) {
        return trie.indexOf(chars, pos, len);
    }

    public int indexOf(char[] keyChars, int pos, int len, int nodePos) {
        return trie.indexOf(keyChars, pos, len, nodePos);
    }

    /**
     * 获取词频
     *
     * @param term
     * @return
     */
    public int getTermFrequency(String term) {
        NatureAttribute attribute = get(term);
        if (attribute == null) {
            return 0;
        }
        return attribute.getTotalFrequency();
    }

    /**
     * 是否包含词语
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return trie.indexOf(key) >= 0;
    }

    /**
     * 获取词语的ID
     *
     * @param word
     * @return
     */
    public int getWordID(String word) {
        return trie.indexOf(word);
    }

    public DATMatcher<NatureAttribute> match(char[] text, int offset) {
        return trie.match(text, offset);
    }

    public int size() {
        return trie.size();
    }
}
