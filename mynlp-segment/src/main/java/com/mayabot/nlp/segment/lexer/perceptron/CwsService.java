package com.mayabot.nlp.segment.lexer.perceptron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.SettingItem;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.plugins.ner.PerceptronNerService;
import com.mayabot.nlp.utils.CharNormUtils;

import java.util.List;

/**
 * 感知机分词服务
 */
@Singleton
public class CwsService {

    private CWSPerceptron perceptron;

    public static final String cswModel = "cws-model";
    public static final String cswHanlpModel = "cws-hanlp-model";

    public static final SettingItem<String> cwsModelItem = SettingItem.string("cws.model", cswModel);


    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    @Inject
    public CwsService(MynlpEnv mynlp,
                      CwsPatch cwsPatch) throws Exception {

        //cws-model or cws-hanlp-model
        String modelName = mynlp.getSettings().get(cwsModelItem);

        long t1 = System.currentTimeMillis();
        NlpResource parameterResource = mynlp.loadResource(modelName + "/parameter.bin");
        NlpResource featureResource = mynlp.loadResource(modelName + "/feature.dat");

        this.perceptron = CWSPerceptron.load(
                parameterResource.inputStream(),
                featureResource.inputStream());

        for (String example : cwsPatch.getExamples()) {
            perceptron.learn(example);
        }

        long t2 = System.currentTimeMillis();

        logger.info("PerceptronCwsService init use " + (t2 - t1) + " ms");
    }

    public List<String> splitWord(String sentence) {
        sentence = CharNormUtils.convert(sentence);
        List<String> strings = perceptron.decodeToWordList(sentence);
        return strings;
    }

    public CWSPerceptron getPerceptron() {
        return perceptron;
    }
}
