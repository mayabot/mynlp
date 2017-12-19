package com.mayabot.nlp.segment.tokenizer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mayabot.nlp.MyNlps;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.NamedComponentRegistry;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 根据JSON配置文件产生一个WordnetTokenizer对象
 * @author jimichan
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

    /**
     * Just building 延迟初始化，所以向NamedComponentRegistry注册新的注解需要asEagerSingleton
     *
     * @return
     */
    public static WordnetTokenizerFactory get() {
        return MyNlps.getInjector().getInstance(WordnetTokenizerFactory.class);
    }

    public WordnetTokenizer build(String json) {
        logger.debug("Json: \n" + json);

        Map<String, Object> map = (JSONObject) JSON.parse(json);
        return build(map);
    }

    public WordnetTokenizer build(WordnetTokenizerBuilder builder) {
        WordnetTokenizer wordNetTokenizer = injector.getInstance(WordnetTokenizer.class);


        wordNetTokenizer.setBestPathComputer(registry.getInstance(builder.getBestPath(), BestPathComputer.class));
        wordNetTokenizer.setWordnetInitializer(registry.getInstance(builder.getWordnetIniter(), WordnetInitializer.class));

        final List<WordpathProcessor> wordpathProcessors = Lists.newArrayList();

        for (PipelineItem item : builder.getPipelineItem()) {

            WordpathProcessor pro = registry.getInstance(item.type, WordpathProcessor.class);
            pro.initConfig(item.config);

            wordpathProcessors.add(pro);
        }

        wordNetTokenizer.setWordPathProcessors(ImmutableList.copyOf(wordpathProcessors));

        wordNetTokenizer.check();

        return wordNetTokenizer;
    }


    /**
     * bestPath=viterbi
     * wordnetIniter=core
     * pipeline.0.type=abc
     * pipeline.0.config.a=b
     * pipeline.0.config.b=b
     *
     * @param pstring
     * @return
     */
    public WordnetTokenizer buildProperties(String pstring) {
//        logger.debug("Json: \n" + json);
//        Map<String, Object> map = new Gson().fromJson(json, Map.class);
//        return build(map);

        return null;
    }

    public WordnetTokenizer build(Map<String, Object> config) {
        ConfigReader configReader = new ConfigReader(config);

        WordnetTokenizerBuilder builder = WordnetTokenizerBuilder.create();
        builder.setWordnetIniter(configReader.wordnetIniter());
        builder.setBestPath(configReader.bestPath());
        builder.setPipelineItem(configReader.pipeline());

        return build(builder);
    }

    /**
     *
     */
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

        public PipelineItem() {

        }

        public PipelineItem(String type) {
            this.type = type;
        }

        public PipelineItem(String type, Map<String, Object> config) {
            this.type = type;
            this.config = config;
        }
    }

}
