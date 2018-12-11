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

package com.mayabot.nlp.pinyin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.SettingItem;
import com.mayabot.nlp.pinyin.model.Pinyin;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import javax.inject.Singleton;
import java.util.List;
import java.util.TreeMap;

import static com.mayabot.nlp.SettingItem.string;

/**
 * 拼音的词典
 *
 * @author jimichan
 */
@Singleton
public class PinyinDictionary extends BasePinyinDictionary {


    public final static SettingItem<String> pinyinSetting =
            string("pinyin.dict", "dictionary/pinyin.txt");

    public final static SettingItem<String> pinyinExtDicSetting =
            string("pinyin.ext.dict", null);

    private final MynlpEnv mynlp;

    @Inject
    public PinyinDictionary(MynlpEnv mynlp) {
        super();
        this.mynlp = mynlp;

        rebuild();

    }

    @Override
    TreeMap<String, Pinyin[]> load() {
        List<NlpResource> list = Lists.newArrayList();

        list.add(mynlp.loadResource(pinyinSetting));

        NlpResource ext = mynlp.loadResource(pinyinExtDicSetting);
        if (ext != null) {
            list.add(ext);
        }

        TreeMap<String, Pinyin[]> map = new TreeMap<>();
        for (NlpResource dictResource : list) {

            try (CharSourceLineReader reader = dictResource.openLineReader()) {
                while (reader.hasNext()) {
                    //降龙伏虎=xiang2,long2,fu2,hu3
                    //单=dan1,shan4,chan2
                    String line = reader.next();
                    String[] param = line.split("=");

                    String key = param[0];

                    Pinyin[] pinyins = parse(param[1]);
                    if (pinyins != null) {
                        map.put(key, pinyins);
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return map;
    }

}
