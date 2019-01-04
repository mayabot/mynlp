package com.mayabot.nlp.segment.perceptron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.SettingItem;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.plugins.ner.PerceptronNerService;
import com.mayabot.nlp.utils.CharNormUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;

/**
 * 感知机分词服务
 */
@Singleton
public class PerceptronCwsService {

    private CWSPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    /**
     */
    final SettingItem<String> resourceName =
            SettingItem.string("cws.model.name", "mynlp-resource-cws-1.0.0.jar");

    @Inject
    public PerceptronCwsService(MynlpEnv mynlp) throws Exception {

        NlpResource parameterResource = mynlp.loadResource("cws-model/parameter.bin");

        if (parameterResource == null) {
            logger.info("Not found cws-model in classpath or data dir,Now auto download from server\n");

            String fileName = mynlp.getSettings().get(this.resourceName);
            File jar = mynlp.download(fileName);

            parameterResource = mynlp.loadResource("cws-model/parameter.bin");
            if (jar == null) {
                logger.error("download " + fileName + " fail. see wiki https://github.com/mayabot/mynlp/wiki");
                System.exit(0);
            }
        }

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
