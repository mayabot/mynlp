package com.mayabot.nlp.segment.plugins.pos;

import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.common.injector.Singleton;
import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.PerceptronFileFormat;
import com.mayabot.nlp.perceptron.PerceptronModel;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Vertex;

import java.util.ArrayList;
import java.util.List;

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
        long t1 = System.currentTimeMillis();

        PerceptronModel model = PerceptronFileFormat.loadFromNlpResource("pos-model", mynlp);
        this.perceptron = new POSPerceptron(model);
        long t2 = System.currentTimeMillis();

        logger.info("PerceptronPosService Load use " + (t2 - t1) + " ms");
    }

    public List<Nature> pos(List<String> words) {

        List<Nature> nrList = perceptron.decodeNature(words);

//        // 单字 人名 特殊处理一下
//        for (int i = 0; i < nrList.size(); i++) {
//            if (nrList.get(i) == Nature.nr && words.get(i).length() == 1) {
//                nrList.set(i,Nature.n);
//            }
//        }

        return nrList;
    }

    /**
     * @param sample   word/x word/b
     */
    public void learn(String sample) {
        perceptron.learn(sample);
    }

    /**
     * 定制版本，我们做一些特殊处理
     * @param words
     * @return List<Nature>
     */
    public List<Nature> posFromVertex(List<Vertex> words) {
        ArrayList<String> stList = new ArrayList<>(words.size());
        boolean findIndex =  false;
        for (Vertex word : words) {
            String x = word.realWord();
            if (word.nature == Nature.m) {
                stList.add("["+word.nature+"]");
                findIndex = true;
            } else {
                stList.add(x);
            }
        }

        List<Nature> result = pos(stList);
        if (findIndex) {
            for (int i = 0; i < words.size(); i++) {
                if(words.get(i).nature == Nature.m){
                    result.set(i, words.get(i).nature);
                }
            }
        }else{
            return result;
        }

        return result;
    }

    public void posFromTerm(List<WordTerm> words) {
        ArrayList<String> stList = new ArrayList<>(words.size());
        for (WordTerm word : words) {
            String x = word.word;
            if (word.getNature() == Nature.m) {
                stList.add("["+word.getNatureName()+"]");
            } else {
                stList.add(x);
            }
        }

        List<Nature> result = pos(stList);

        for (int i = 0; i < words.size(); i++) {
            Nature na = result.get(i);
            WordTerm word = words.get(i);
            if (word.getNature() == Nature.m) {
                na = word.getNature();
            }
            word.setNature(na);
        }

    }

}
