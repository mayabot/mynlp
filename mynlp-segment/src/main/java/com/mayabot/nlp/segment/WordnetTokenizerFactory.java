package com.mayabot.nlp.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mayabot.nlp.MyNlps;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 根据JSON配置文件产生一个WordnetTokenizer对象
 */
@Singleton
public class WordnetTokenizerFactory {

    static InternalLogger logger = InternalLoggerFactory.getInstance(WordnetTokenizerFactory.class);

    private final NamedComponentRegistry registry;
    private final Injector injector;

    @Inject
    WordnetTokenizerFactory(NamedComponentRegistry registry, Injector injector) {
        this.registry = registry;
        this.injector = injector;
    }

    public static WordnetTokenizerFactory get() {
        return MyNlps.getInjector().getInstance(WordnetTokenizerFactory.class);
    }

    public WordnetTokenizer build(String json) {
        logger.debug("Json: \n" + json);
        Map<String, Object> map = new Gson().fromJson(json, Map.class);
        return build(map);
    }

    public WordnetTokenizer build(Map<String, Object> config) {
        WordnetTokenizer wordNetTokenizer = injector.getInstance(WordnetTokenizer.class);

        ConfigReader configReader = new ConfigReader(config);
        //

        wordNetTokenizer.setBestPathComputer(registry.getInstance(configReader.bestPath(), BestPathComputer.class));
        wordNetTokenizer.setWordnetInitializer(registry.getInstance(configReader.wordnetIniter(), WordnetInitializer.class));

        for (PipelineItem item : configReader.pipeline()) {

            WordPathProcessor pro = registry.getInstance(item.type, WordPathProcessor.class);

            if (pro instanceof WordPathProcessorIniter) {
                ((WordPathProcessorIniter) pro).init(item.config);
            }

        }

        wordNetTokenizer.check();

        return wordNetTokenizer;
    }

    static class ConfigReader {

        Map<String, Object> config;

        ConfigReader(Map<String, Object> config) {
            this.config = config;
        }

        public String bestPath() {
            return config.getOrDefault("bestPath", "viterbi").toString();
        }

        public String wordnetIniter() {
            return config.getOrDefault("wordnetIniter", "core").toString();
        }

        public List<PipelineItem> pipeline() {
            List<?> list = (List) config.getOrDefault("pipeline", Lists.newArrayList());

            ArrayList<PipelineItem> result = Lists.newArrayList();

            for (Object obj : list) {
                PipelineItem item = new PipelineItem();
                if (obj instanceof String) {
                    item.type = obj.toString();
                } else if (obj instanceof Map) {
                    Map<String, Object> mini = ((Map) obj);
                    Preconditions.checkArgument(mini.containsKey("type"));

                    item.type = mini.remove("type").toString();
                    item.config = ImmutableMap.copyOf(mini);
                } else {
                    logger.error("obj {} format is error", obj);
                    Preconditions.checkState(false, "obj %s format is error", obj.toString());
                }
                result.add(item);
            }
            return result;
        }
    }

    static class PipelineItem {
        String type;
        Map<String, Object> config = ImmutableMap.of();
    }

}
