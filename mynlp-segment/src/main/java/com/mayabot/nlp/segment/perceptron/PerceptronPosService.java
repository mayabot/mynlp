package com.mayabot.nlp.segment.perceptron;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.dictionary.Nature;
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
        NlpResource parameterResource = mynlp.loadResource("pos/parameter.bin");

        if (parameterResource == null) {
            logger.error("Not found pos/parameter.bin \n" +
                    "add dependencies with gradle\n" +
                    "compile 'com.mayabot.mynlp:mynlp-resource-pos:1.0.0'"
            );
        }

        NlpResource labelResource = mynlp.loadResource("pos/label.txt");
        NlpResource featureResource = mynlp.loadResource("pos/feature.txt");

        File temp = mynlp.getCacheDir();

        File featureDatFile = new File(temp, featureResource.hash() + ".pos.dat");
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


    static String vertex2Strng(Vertex vertex) {
        return vertex.realWord();
    }

    public List<Nature> posFromVertex(List<Vertex> words) {
        return perceptron.decode(words, PerceptronPosService::vertex2Strng);
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

    public static void main(String[] args) {
        PerceptronPosService service = Mynlps.getInstance(PerceptronPosService.class);

        List<Nature> natureList = service.pos(Lists.newArrayList("22 23456 中国 的 土地，".split(" ")));

        System.out.println(natureList);
    }

}
