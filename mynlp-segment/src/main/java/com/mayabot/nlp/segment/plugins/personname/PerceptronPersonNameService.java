package com.mayabot.nlp.segment.plugins.personname;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.common.ResourceLastVersion;
import com.mayabot.nlp.segment.plugins.ner.PerceptronNerService;
import com.mayabot.nlp.utils.CharNormUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * 感知机分词服务
 */
@Singleton
public class PerceptronPersonNameService {

    private PersonNamePerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    @Inject
    public PerceptronPersonNameService(MynlpEnv mynlp) throws Exception {
        NlpResource parameterResource = mynlp.loadResource("person-name-model/parameter.bin");

        if (parameterResource == null) {
            logger.error("Not found nr-ner/parameter.bin \n");
            logger.error(ResourceLastVersion.show(ResourceLastVersion.ner));
        }

        NlpResource featureResource = mynlp.loadResource("person-name-model/feature.txt");

        File temp = new File(mynlp.getCacheDir(), "ner");

        File featureDatFile = new File(temp, featureResource.hash() + ".personName.dat");
        Files.createParentDirs(featureDatFile);

        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.openInputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = PersonNamePerceptron.load(
                new BufferedInputStream(parameterResource.openInputStream()),
                new BufferedInputStream(new FileInputStream(featureDatFile)));

    }

    public List<PersonName> findName(String sentence) {
        char[] chars = sentence.toCharArray();
        CharNormUtils.convert(chars);
        return perceptron.findPersonName(chars);
    }

    public List<PersonName> findName(char[] sentence) {
        return perceptron.findPersonName(sentence);
    }

}
