package com.mayabot.nlp.segment.plugins.ner;

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
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.plugins.pos.PerceptronPosService;
import com.mayabot.nlp.utils.CharNormUtils;

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
        NlpResource parameterResource = mynlp.loadResource("ner-model/parameter.bin");
        NlpResource labelResource = mynlp.loadResource("ner-model/label.txt");
        NlpResource featureResource = mynlp.loadResource("ner-model/feature.txt");

        File temp = mynlp.getCacheDir();

        File featureDatFile = new File(temp, featureResource.hash() + ".ner.dat");
        if (!featureDatFile.exists()) {
            FeatureSet featureSet = FeatureSet.readFromText(new BufferedInputStream(featureResource.openInputStream()));
            featureSet.save(featureDatFile, null);
        }

        this.perceptron = NERPerceptron.load(
                parameterResource.openInputStream(),
                new BufferedInputStream(new FileInputStream(featureDatFile)),
                labelResource.openInputStream());
    }

    /**
     * 返回双层嵌套结构的句子列表
     * @param list
     * @return
     */
    public static List<WordTerm> toNerComposite(List<WordTerm> list) {

        boolean findLab = false;
        for (WordTerm x : list) {
            if (x.getCustomFlag() != null) {
                findLab = true;
                break;
            }
        }
        if (!findLab) {
            return list;
        }


        List<WordTerm> result = new ArrayList<>(list.size());
        List<WordTerm> temp = null;

        String nerPOS = null;
        for (WordTerm word : list) {
            String label = word.getCustomFlag();

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

            result.add(group);
        }

        return result;
    }

    /**
     *
     * @param list
     * @param pos 是否需要计算词性
     */
    public List<WordTerm> ner(List<WordTerm> list, boolean pos) {

        if (pos) {
            posService.posFromTerm(list);
        }

        perceptron.decode(list);

        return toNerComposite(list);
    }

    /**
     * 简单的接口，对一个分词序列进行NER，内置调用分词
     * String需要归一化处理
     *
     * @param list
     */
    public Sentence ner(List<String> list) {
        List<WordTerm> list2 = Lists.newArrayListWithCapacity(list.size());
        for (String w : list) {
            list2.add(new WordTerm(CharNormUtils.convert(w), Nature.x));
        }

        return Sentence.of(ner(list2, true));
    }


    public NERPerceptron getPerceptron() {
        return perceptron;
    }

    public static void main(String[] args) {
        PerceptronNerService service = Mynlps.instanceOf(PerceptronNerService.class);
        System.out.println(service.ner(Lists.newArrayList("上海 万行 信息 科技 有限 公司 在 上海 注册 成功".split(" "))));
        System.out.println(service.ner(Lists.newArrayList("上海 华 安 工业 （ 集团 ） 公司 董事长 谭旭光 和 秘书 胡花蕊 来 到 美国 纽约 现代 艺术 博物馆 参观".split(" "))));
        System.out.println(service.ner(Lists.newArrayList("这|是|上海|万|行|信息|科技|有限公司|的|财务|报表".split("\\|"))));
    }
}
