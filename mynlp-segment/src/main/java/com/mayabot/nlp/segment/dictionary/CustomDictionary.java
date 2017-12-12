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

package com.mayabot.nlp.segment.dictionary;


import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieSerializer;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.corpus.tag.Nature;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * 用户自定义词典
 * 缓存用户的自定义词典，查询出
 */
public class CustomDictionary {

    private final ResourceLoader resourceLoader;
    private final Environment environment;
    private InternalLogger logger = InternalLoggerFactory.getInstance(CustomDictionary.class);

    private DoubleArrayTrie<NatureAttribute> dat;

    /**
     * 可以由外部程序设定的
     */
    private DoubleArrayTrie<NatureAttribute> projectDAT;

    private Settings setting;

    private String[] paths = new String[0];
    private boolean isNormalization = false;

    @Inject
    public CustomDictionary(Settings setting, ResourceLoader resourceLoader, Environment environment) {
        this.setting = setting;
        this.resourceLoader = resourceLoader;
        this.environment = environment;

        List<String> asList = setting.getAsList("customDictionary.path");
        if (asList == null) {
            asList = Lists.newArrayList();
        }

        paths = asList.toArray(new String[0]);
        isNormalization = setting.getAsBoolean("customDictionary.normalization", Boolean.FALSE);

        init();
    }

    /**
     * 开机自动加载词典
     */
    public void init() {
        loadLocalFileDictionary();
    }

    public DoubleArrayTrie<NatureAttribute>[] allDict() {
        return new DoubleArrayTrie[]{dat, projectDAT};
    }


    private void loadLocalFileDictionary() {

        // 文件没有发生变化，而且缓存文件存在
        File cacheBinFile = new File(environment.getWorkDir(), hashDict());

        if (cacheBinFile.exists() && cacheBinFile.canRead()) {

            dat = loadDataFromBin(cacheBinFile);

            if (dat == null) {
                logger.warn("restore custom dic from bin cache fail");
                cacheBinFile.delete();
            }
            if (dat != null) {
                logger.info("load custom dict from cache bin file");
                return;
            }
        }

        dat = loadDataFromTxt(paths);

        if (dat == null) {
            logger.warn("fail to load {} dictionary " + Arrays.toString(paths));
        }

    }

    /**
     * 从txt文件中加载自定义词典，并且做好序列化和加载tire树
     *
     * @param paths
     * @return 返回是否成功load
     */
    private DoubleArrayTrie<NatureAttribute> loadDataFromTxt(String... paths) {
        try {
            TreeMap<String, NatureAttribute> wordMap = new TreeMap<>();

            for (String path : paths) {
                ByteSource file = resourceLoader.loadDictionary(path);

                if (file.isEmpty()) {
                    logger.warn("can not find custom dict file {} " + path);
                    continue;
                }
                logger.info("loading custom dict file " + path);

                Nature defaultNature = Nature.n;

                // 这里忽略了对默认词性的解析

                boolean success = load(file, defaultNature, wordMap);
                if (!success) {
                    logger.warn("fail to load file: {} " + path);
                }
            }

            if (wordMap.size() == 0) {
                logger.warn("未加载到任何词条");
                wordMap.put(CoreDictionary.TAG_OTHER, null);
            }

            logger.info("load custom dict file ok");

            DoubleArrayTrie<NatureAttribute> tireTree = new DoubleArrayTrieBuilder<NatureAttribute>().build(wordMap);

            //保存到cache文件
            try {
                File cacheBinFile = new File(environment.getWorkDir(), hashDict());
                DoubleArrayTrieSerializer<NatureAttribute> datSerializer = new DoubleArrayTrieSerializer<>();
                datSerializer.setSerializer(NatureAttribute.valueSerializer);
                datSerializer.write(tireTree, cacheBinFile);
            } catch (Exception e) {
                logger.error("can't save custom cache");
            }

            return tireTree;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("");
        }

        return null;
    }

    private Splitter splitter = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

    /**
     * 填充map和customNatureCollector
     *
     * @param path
     * @param defaultNature
     * @param map
     * @return
     */
    private boolean load(ByteSource path, Nature defaultNature, TreeMap<String, NatureAttribute> map) {

        try {
            String[] empty = new String[0];
            path.asCharSource(Charsets.UTF_8)
                    .readLines().stream().map(line -> splitter.splitToList(line).toArray(empty))
                    .filter(x -> x.length != 0)
                    .forEach(params -> {
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
                    });

            return true;
        } catch (Exception e) {
            logger.warn("read custom {} dictionary occurs error " + path);
        }
        return false;
    }


    //FIXME 此处将parms[0]正规化
    private static String normalizationString(String text) {
        return text;
    }


    /**
     * 提供给项目中使用的工具方法
     *
     * @param iterable
     * @return
     */
    public DoubleArrayTrie<NatureAttribute> prepareProjectDict(Iterable<Tuple<String, NatureAttribute>> iterable) {

        TreeMap<String, NatureAttribute> map = new TreeMap<>();

        for (Tuple<String, NatureAttribute> tuple : iterable) {
            map.put(isNormalization ? normalizationString(tuple.t1) : tuple.t1, tuple.t2);
        }

        return new DoubleArrayTrieBuilder<NatureAttribute>().build(map);

    }


    /**
     * 读取指向的所有相关词典文件，计算内容的hash
     *
     * @return
     */
    private String hashDict() {

        Hasher hasher = Hashing.murmur3_128().newHasher();

        for (String file : Lists.newArrayList(paths)) {
            try {
                String _hash = resourceLoader.loadDictionary(file).hash(Hashing.murmur3_128()).toString();
                hasher.putString(_hash, Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return hasher.hash().toString();
    }


    private DoubleArrayTrie<NatureAttribute> loadDataFromBin(File filePath) {
        try {
            DoubleArrayTrieSerializer<NatureAttribute> ds = new DoubleArrayTrieSerializer<>();
            ds.setSerializer(NatureAttribute.valueSerializer);
            return ds.read(filePath);
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
    }

    public DoubleArrayTrie<NatureAttribute> getProjectDAT() {
        return projectDAT;
    }

    public void setProjectDAT(DoubleArrayTrie<NatureAttribute> projectDAT) {
        this.projectDAT = projectDAT;
    }


    /**
     * 一个通用的二元组
     * @author jimichan
     * @param <K>
     * @param <V>
     */
    public static class Tuple<K, V> {
        public final K t1;
        public final V t2;

        public Tuple(K t1, V t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }


        public static class IntTuple{
            public int v1;
            public int v2;

            public IntTuple(int t1, int t2) {
                this.v1 = t1;
                this.v2 = t2;
            }
        }
    }
}
