package com.mayabot.nlp.segment.perceptron;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.perceptron.FeatureSet;
import com.mayabot.nlp.perceptron.solution.ner.NERPerceptron;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.dictionary.Nature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PerceptronNerService {

    private final PerceptronPosService posService;
    private NERPerceptron perceptron;

    static InternalLogger logger = InternalLoggerFactory.getInstance(PerceptronNerService.class);

    @Inject
    public PerceptronNerService(MynlpEnv mynlp,
                                PerceptronPosService posService
    ) throws Exception {

        this.posService = posService;
        NlpResource parameterResource = mynlp.loadResource("ner/parameter.bin");

        if (parameterResource == null) {
            logger.error("Not found ner/parameter.bin \n" +
                    "add dependencies with gradle\n" +
                    "compile 'com.mayabot.mynlp:mynlp-resource-ner:1.0.0'"
            );
        }

        NlpResource labelResource = mynlp.loadResource("ner/label.txt");
        NlpResource featureResource = mynlp.loadResource("ner/feature.txt");

        File temp = mynlp.getCacheDir();

        File featureDatFile = new File(temp, featureResource.hash() + ".ner.dat");
        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.openInputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = NERPerceptron.load(
                new BufferedInputStream(parameterResource.openInputStream()),
                new BufferedInputStream(new FileInputStream(featureDatFile)),
                new BufferedInputStream(labelResource.openInputStream()));
    }

    /**
     * WordTerm里面准备word和nature
     *
     * @param sentence
     * @return
     */
    public List<String> decode(List<WordTerm> sentence) {
        return perceptron.decode(sentence);
    }

    /**
     * 要求WordTerm已经词性填充完成
     *
     * @param list
     */
    public List<WordTerm> ner(List<WordTerm> list, boolean pos) {

        if (pos) {
            posService.posFromTerm(list);
        }

        List<String> decode = perceptron.decode(list);

        List<WordTerm> result = new ArrayList<>(list.size());
        List<WordTerm> temp = null;

        int i = 0;
        String nerPOS = null;
        for (String label : decode) {
            WordTerm word = list.get(i++);

            if ("O".equals(label) || "S".equals(label)) {
                if (temp != null) {

                    StringBuilder bigName = new StringBuilder();
                    for (WordTerm w : temp) {
                        bigName.append(w.word);
                    }
                    WordTerm group = new WordTerm(bigName.toString(), Nature.valueOf(nerPOS));
                    group.setOffset(temp.get(0).getOffset());
                    group.setSubword(temp);
                    temp = null;
                    nerPOS = null;

                    result.add(group);
                }
                result.add(word);
            } else {
                //B M E
                if (temp == null) {
                    temp = new ArrayList<>();
                    //B-nt
                    nerPOS = label.substring(2);
                }
                temp.add(word);

            }
        }

        if (temp != null) {
            StringBuilder bigName = new StringBuilder();
            for (WordTerm w : temp) {
                bigName.append(w.word);
            }
            WordTerm group = new WordTerm(bigName.toString(), Nature.valueOf(nerPOS));
            group.setSubword(temp);
            temp = null;
            nerPOS = null;

            result.add(group);
        }

        return result;
    }

    /**
     * 简单的接口，对一个分词序列进行NER，内置调用分词
     * String需要归一化处理
     *
     * @param list
     */
    public List<WordTerm> ner(List<String> list) {
        List<WordTerm> list2 = Lists.newArrayListWithCapacity(list.size());
        for (String w : list) {
            list2.add(new WordTerm(w, Nature.x));
        }

        return ner(list2, true);
    }


    public static void main(String[] args) {
        PerceptronNerService service = Mynlps.getInstance(PerceptronNerService.class);
        System.out.println(service.ner(Lists.newArrayList("上海 万行 信息 科技 有限 公司 在 上海 注册 成功".split(" "))));
        System.out.println(service.ner(Lists.newArrayList("上海 华 安 工业 （ 集团 ） 公司 董事长 谭旭光 和 秘书 胡花蕊 来 到 美国 纽约 现代 艺术 博物馆 参观".split(" "))));

    }
}
