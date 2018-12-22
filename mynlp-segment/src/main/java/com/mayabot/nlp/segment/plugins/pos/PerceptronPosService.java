package com.mayabot.nlp.segment.plugins.pos;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.common.ResourceLastVersion;
import com.mayabot.nlp.segment.wordnet.Vertex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.function.Function;

/**
 * 感知机词性分词服务。
 * 单例服务，pos的资源是依赖mynlp-resouces里面
 * <p>
 * 该服务可以独立调用
 * <p>
 * PerceptronPosService service = Mynlps.getInstance(PerceptronPosService.class);
 */
@Singleton
public class PerceptronPosService {

    private POSPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronPosService.class);

    @Inject
    public PerceptronPosService(MynlpEnv mynlp) throws Exception {
        NlpResource parameterResource = mynlp.loadResource("pos-model/parameter.bin");

        if (parameterResource == null) {
            logger.error("Not found pos/parameter.bin \n");
            logger.error(ResourceLastVersion.show(ResourceLastVersion.pos));
        }

        NlpResource labelResource = mynlp.loadResource("pos-model/label.txt");
        NlpResource featureResource = mynlp.loadResource("pos-model/feature.txt");

        File temp = new File(mynlp.getCacheDir(), "pos");
        File featureDatFile = new File(temp, featureResource.hash() + ".dat");

        Files.createParentDirs(featureDatFile);

        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.openInputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = POSPerceptron.load(
                new BufferedInputStream(parameterResource.openInputStream()),
                new BufferedInputStream(new FileInputStream(featureDatFile)),
                new BufferedInputStream(labelResource.openInputStream()));

    }

    public List<Nature> pos(List<String> words) {
        return perceptron.decode(words);
    }

    public <T> List<Nature> pos(List<T> words, Function<T, String> sink) {
        return perceptron.decode(words, sink);
    }


    static String vertex2String(Vertex vertex) {
        return vertex.realWord();
    }

    public List<Nature> posFromVertex(List<Vertex> words) {
        return perceptron.decode(words, PerceptronPosService::vertex2String);
    }

    public void posFromTerm(List<WordTerm> words) {
        List<Nature> decode = perceptron.decode(words, PerceptronPosService::term2string);
        for (int i = 0; i < words.size(); i++) {
            words.get(i).setNature(decode.get(i));
        }
    }

    static String term2string(WordTerm term) {
        return term.word;
    }


}
