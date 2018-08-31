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


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.util.List;
import java.util.TreeMap;

/**
 * 用户自定义词典
 * 缓存用户的自定义词典，查询出
 * <p>
 * reload watch
 * @author jimichan
 */
@Singleton
public class CustomDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(CustomDictionary.class);

    private final MynlpEnv mynlp;

    private DoubleArrayTrie<NatureAttribute> dat;

    private List<String> resourceUrls;

    private boolean isNormalization = false;

    public static Setting<String> customDictSetting = Setting.string("custom.dictionary.path", null);

    //dictionary/custom/*

    @Inject
    public CustomDictionary(MynlpEnv mynlp) throws Exception {

        this.mynlp = mynlp;

        List<String> resourceUrls = mynlp.getSettings().getAsList(customDictSetting);

        if (resourceUrls == null || resourceUrls.isEmpty()) {
            return;
        }
        // dictionary/custom/abc.txt
        // db://dictionary/custom/abc.txt

        this.resourceUrls = resourceUrls;
        isNormalization = mynlp.getSettings().getAsBoolean("custom.dictionary.normalization", Boolean.FALSE);

        loadFromRealData(resourceUrls);
    }

//    @Override
//    public File cacheFileName() {
//        if (resourceUrls.isEmpty()) {
//            return null;
//        }
//        TreeSet<String> set = new TreeSet<>();
//
//        for (String url : resourceUrls) {
//            NlpResource resource = mynlp.loadResource(url);
//
//            set.add(resource.hash());
//        }
//
//        String hash = Hashing.md5().hashString(set.toString(), Charsets.UTF_8).toString();
//
//        return new File(mynlp.getCacheDir(), hash + ".custom.dict");
//    }
//
//    @Override
//    public void saveToCache(OutputStream out) throws Exception {
//        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
//
//        DoubleArrayTrie.write(dat, dataOutput, NatureAttribute::write);
//
//        out.write(dataOutput.toByteArray());
//    }
//
//    @Override
//    public void readFromCache(File file) throws Exception {
//        try (InputStream inputStream = new BufferedInputStream(Files.asByteSource(file).openStream(), 64 * 1024)) {
//            DataInput dataInput = new DataInputStream(inputStream);
//            this.dat = DoubleArrayTrie.read(dataInput, NatureAttribute::read);
//        }
//    }

    //    @Override
    public void loadFromRealData(List<String> resourceUrls) throws Exception {
        TreeMap<String, NatureAttribute> map = new TreeMap<>();

        Nature defaultNature = Nature.n;
        for (String url : resourceUrls) {
            NlpResource resource = mynlp.loadResource(url);

            try (CharSourceLineReader reader = resource.openLineReader()) {
                while (reader.hasNext()) {
                    String line = reader.next();

                    String[] params = line.split("\\s");


                    if (isNormalization) {
                        params[0] = normalizationString(params[0]);
                    }
                    int natureCount = (params.length - 1) / 2;

                    NatureAttribute attribute;
                    if (natureCount == 0) {
                        attribute = NatureAttribute.create1000(defaultNature);
                    } else {
                        attribute = NatureAttribute.create(params);
                    }

                    map.put(params[0], attribute);
                }
            }
        }


        if (map.isEmpty()) {
            return;
        }

        dat = new DoubleArrayTrieBuilder<NatureAttribute>().build(map);

    }

    public DoubleArrayTrie<NatureAttribute> getDat() {
        return dat;
    }

    //FIXME 此处将parms[0]正规化
    private static String normalizationString(String text) {
        return text;
    }
//
//    /**
//     * 提供给项目中使用的工具方法
//     *
//     * @param iterable
//     * @return
//     */
//    public DoubleArrayTrie<NatureAttribute> prepareProjectDict(Iterable<Tuple<String, NatureAttribute>> iterable) {
//
//        TreeMap<String, NatureAttribute> map = new TreeMap<>();
//
//        for (Tuple<String, NatureAttribute> tuple : iterable) {
//            map.put(isNormalization ? normalizationString(tuple.t1) : tuple.t1, tuple.t2);
//        }
//
//        return new DoubleArrayTrieBuilder<NatureAttribute>().build(map);
//
//    }
//
//    /**
//     * 一个通用的二元组
//     * @author jimichan
//     * @param <K>
//     * @param <V>
//     */
//    public static class Tuple<K, V> {
//        public final K t1;
//        public final V t2;
//
//        public Tuple(K t1, V t2) {
//            super();
//            this.t1 = t1;
//            this.t2 = t2;
//        }
//
//
//        public static class IntTuple{
//            public int v1;
//            public int v2;
//
//            public IntTuple(int t1, int t2) {
//                this.v1 = t1;
//                this.v2 = t2;
//            }
//        }
//    }
}
