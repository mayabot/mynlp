package com.mayabot.nlp.segment.perceptron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.resources.NlpResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * 感知机分词服务
 */
@Singleton
public class PerceptronCwsService {

    private CWSPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    @Inject
    public PerceptronCwsService(MynlpEnv mynlp) throws Exception {
        NlpResource parameterResource = mynlp.loadResource("cws/parameter.bin");

        if (parameterResource == null) {
            logger.error("Not found cws/parameter.bin \n" +
                    "add dependencies with gradle\n" +
                    "compile 'com.mayabot.mynlp:mynlp-resource-cws:1.0.0'"
            );
        }

        NlpResource featureResource = mynlp.loadResource("cws/feature.txt");

        File temp = mynlp.getCacheDir();

        File featureDatFile = new File(temp, featureResource.hash() + ".cws.dat");
        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.openInputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = CWSPerceptron.load(
                new BufferedInputStream(parameterResource.openInputStream()),
                new BufferedInputStream(new FileInputStream(featureDatFile)));

    }
}
