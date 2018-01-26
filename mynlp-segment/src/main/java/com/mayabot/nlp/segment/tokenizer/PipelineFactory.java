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

package com.mayabot.nlp.segment.tokenizer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.NamedComponentRegistry;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.xprocessor.OptimizeWordPathProcessor;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Singleton
public class PipelineFactory {

    @Inject
    private NamedComponentRegistry registry;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PipelineFactory.class);

    public Pipeline createByName(String name) {
        return createByName(name, PipelineSettings.EMTPY);
    }

    public Pipeline createByName(String name, PipelineSettings settings) {

        List pipeItemList = (List) pipelineConfig.get(name);

        Preconditions.checkNotNull(pipeItemList, "Not found " + name + "pipeline");

        List<WordpathProcessor> wordPathProcessors = Lists.newArrayList();

        for (Object obj : pipeItemList) {
            if (obj instanceof String) {
                wordPathProcessors.add(dd(((String) obj), null));
            } else if (obj instanceof Map) {
                Map<String, Object> mini = ((Map) obj);
                Preconditions.checkArgument(mini.containsKey("type"));

                String type = mini.remove("type").toString();

                wordPathProcessors.add(dd(type, mini));

            } else {
                logger.error("obj {} format is error", obj);
                Preconditions.checkState(false, "obj %s format is error", obj.toString());
            }
        }

        for (WordpathProcessor wordPathProcessor : wordPathProcessors) {
            if (wordPathProcessor instanceof ApplyPipelineSetting) {
                ((ApplyPipelineSetting) wordPathProcessor).apply(settings);
            }
        }


        return new Pipeline(wordPathProcessors);
    }

    private WordpathProcessor dd(String type, Map<String, Object> x) {
        if (type.equals("optimizeNet")) {
            OptimizeWordPathProcessor p = (OptimizeWordPathProcessor) registry.getInstance(type, WordpathProcessor.class);

            p.initConfig(x);

            return p;
        }

        return registry.getInstance(type, WordpathProcessor.class);

    }


    static Map<String, Object> pipelineConfig = Maps.newHashMap();


    static {

        try {
            Enumeration<URL> resources = PipelineFactory.class.getClassLoader().getResources("META-INF/pipeline.json");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                try {
                    String json = Resources.asCharSource(url, Charsets.UTF_8).read();

                    Map<String, Object> map1 = (JSONObject) JSON.parse(json);

                    pipelineConfig.putAll(map1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("find pipeline " + pipelineConfig.keySet());
            }

        } catch (Exception e) {
            logger.error("", e);
        }

    }

}
