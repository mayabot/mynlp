package com.mayabot.nlp.segment.plugins.personname;

import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.injector.Singleton;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.resources.NlpResource;
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

    public PerceptronPersonNameService(MynlpEnv mynlp) throws Exception {

        long t1 = System.currentTimeMillis();
        NlpResource parameterResource = mynlp.loadResource("person-name-model/parameter.bin");
        NlpResource featureResource = mynlp.loadResource("person-name-model/feature.txt");

        File temp = new File(mynlp.getCacheDir(), "ner");

        File featureDatFile = new File(temp, featureResource.hash() + ".personName.dat");
        if (!featureDatFile.getParentFile().exists()) {
            featureDatFile.getParentFile().mkdirs();
        }

        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.inputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = PersonNamePerceptron.load(
                parameterResource.inputStream(),
                new FileInputStream(featureDatFile));

        long t2 = System.currentTimeMillis();

        logger.info("PerceptronPersonNameService load use " + (t2 - t1) + " ms");

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
