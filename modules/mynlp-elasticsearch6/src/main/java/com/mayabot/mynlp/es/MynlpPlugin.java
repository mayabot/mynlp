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

package com.mayabot.mynlp.es;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MynlpPlugin extends Plugin implements AnalysisPlugin {

    /**
     * cws模型比较重，默认不开启。
     */
    private boolean enableCws;

    public MynlpPlugin(Settings settings, Path configPath) {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }

        enableCws = settings.getAsBoolean("mynlp.cws", false);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>(16);

        extra.put("mynlp", MynlpTokenizerFactory::new);
        extra.put("mynlp-core", MynlpTokenizerFactory::new);
        if (enableCws) {
            extra.put("mynlp-cws", MynlpTokenizerFactory::new);
        }

        return extra;
    }


    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {

        //开启异步线程，先执行一个core的分词，试图去下载依赖的资源

        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> map = new HashMap<>();

        map.put("mynlp", MynlpAnalyzerProvider::new);
        map.put("mynlp-core", MynlpAnalyzerProvider::new);
        if (enableCws) {
            map.put("mynlp-cws", MynlpAnalyzerProvider::new);
        }

        return map;
    }

}