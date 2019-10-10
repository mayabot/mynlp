package com.mayabot.nlp.segment.plugins.pos;

import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.injector.Singleton;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 感知机词性分词服务。
 * 单例服务，pos的资源是依赖mynlp-resouces里面
 * <p>
 * 该服务可以独立调用
 * <p>
 * PerceptronPosService service = Mynlps.instanceOf(PerceptronPosService.class);
 */
@Singleton
public class PerceptronPosService {

    private POSPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronPosService.class);

    public PerceptronPosService(MynlpEnv mynlp) throws Exception {
        NlpResource parameterResource = mynlp.loadResource("pos-model/parameter.bin");
        NlpResource featureResource = mynlp.loadResource("pos-model/feature.dat");
        NlpResource labelResource = mynlp.loadResource("pos-model/label.txt");

        long t1 = System.currentTimeMillis();

        this.perceptron = POSPerceptron.load(
                parameterResource.inputStream(),
                featureResource.inputStream(),
                labelResource.inputStream());

        long t2 = System.currentTimeMillis();

        logger.info("PerceptronPosService Load use " + (t2 - t1) + " ms");
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
        ArrayList<String> stList = new ArrayList<>(words.size());
        for (Vertex word : words) {
            stList.add(word.realWord());
        }
        return perceptron.decode(stList);
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
