package com.mayabot.nlp.segment.perceptron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.plugins.ner.PerceptronNerService;
import com.mayabot.nlp.utils.CharNormUtils;

import java.io.BufferedInputStream;
import java.util.List;

/**
 * 感知机分词服务
 */
@Singleton
public class PerceptronCwsService {

    private CWSPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    @Inject
    public PerceptronCwsService(MynlpEnv mynlp) throws Exception {

        NlpResource parameterResource = mynlp.loadResource("cws-model/parameter.bin");
        NlpResource featureResource = mynlp.loadResource("cws-model/feature.dat");

        this.perceptron = CWSPerceptron.load(
                new BufferedInputStream(parameterResource.openInputStream()),
                new BufferedInputStream(featureResource.openInputStream()));

    }

    public List<String> splitWord(String sentence) {
        sentence = CharNormUtils.convert(sentence);
        List<String> strings = perceptron.decodeToWordList(sentence);
        return strings;
    }
}
