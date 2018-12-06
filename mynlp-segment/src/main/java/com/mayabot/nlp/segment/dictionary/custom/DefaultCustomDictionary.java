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

package com.mayabot.nlp.segment.dictionary.custom;


import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResouceExternalizable;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.dictionary.CustomDictionary;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 用户自定义词典
 * 缓存用户的自定义词典，查询出
 * <p>
 * reload watch
 *
 * @author jimichan
 */
@Singleton
public class DefaultCustomDictionary extends NlpResouceExternalizable implements CustomDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(DefaultCustomDictionary.class);

    private final MynlpEnv mynlp;

    private DoubleArrayTrieMap<Integer> dat;

    private List<String> resourceUrls;

    private boolean isNormalization = false;

    public static Setting<String> dictPathSetting = Setting.string(
            "custom.dictionary.path", "dictionary/CustomDictionary.txt");

    @Inject
    public DefaultCustomDictionary(MynlpEnv mynlp) throws Exception {

        this.mynlp = mynlp;

        List<String> resourceUrls = mynlp.getSettings().getAsList(dictPathSetting);

        if (resourceUrls == null || resourceUrls.isEmpty()) {
            return;
        }

        this.resourceUrls = resourceUrls;

        this.restore(mynlp);
    }

    /**
     * 返回资源的Hash版本，可以不要过长
     *
     * @param mynlp
     * @return 资源版本号
     */
    @Override
    public String sourceVersion(MynlpEnv mynlp) {
        if (resourceUrls.isEmpty()) {
            return "empty";
        }
        TreeSet<String> set = new TreeSet<>();

        for (String url : resourceUrls) {
            NlpResource resource = mynlp.loadResource(url);
            if (resource == null) {
                logger.warn("Not found resource " + url);
                continue;
            }
            set.add(resource.hash());
        }

        String hash = Hashing.md5().hashString(set.toString(), Charsets.UTF_8).toString();

        return hash.substring(0, 6);
    }

    /**
     * 从原始内容加载
     *
     * @param mynlp
     * @throws Exception
     */
    @Override
    public void loadFromSource(MynlpEnv mynlp) throws Exception {
        TreeMap<String, Integer> map = new TreeMap<>();

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
//                    int natureCount = (params.length - 1) / 2;
//
//                    NatureAttribute attribute;
//                    if (natureCount == 0) {
//                        attribute = NatureAttribute.create1000(defaultNature);
//                    } else {
//                        attribute = NatureAttribute.create(params);
//                    }

                    map.put(params[0], 1000);
                }
            }
        }


        if (map.isEmpty()) {
            return;
        }

        dat = new DoubleArrayTrieMap<Integer>(map);
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        this.dat.save(out, DefaultCustomDictionary::writeInt);
    }

    static void writeInt(Integer integer, DataOutput out) {
        try {
            out.writeInt(integer);
        } catch (IOException e) {
        }
    }

    static Integer readInt(DataInput out) {
        try {
            return out.readInt();
        } catch (IOException e) {
        }
        return -1;
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public void readExternal(ObjectInput in) {
        try {
            this.dat = new DoubleArrayTrieMap<Integer>(in, DefaultCustomDictionary::readInt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normalizationString(String text) {
        return text.toLowerCase();
    }

    @Override
    public DoubleArrayTrieMap<Integer> getTrie() {
        return dat;
    }
}
