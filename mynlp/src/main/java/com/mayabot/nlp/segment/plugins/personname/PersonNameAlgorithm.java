package com.mayabot.nlp.segment.plugins.personname;

import com.mayabot.nlp.injector.Singleton;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordSplitAlgorithm;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.List;
/**
 * 采用感知机或者将来CRF制作的人名识别模型。
 * 这个切分算法，为了配合词典分词算法。
 * 我们在构造词图阶段就提取人名。
 */
@Singleton
public class PersonNameAlgorithm extends BaseSegmentComponent implements WordSplitAlgorithm {

    private final PerceptronPersonNameService service;

    public PersonNameAlgorithm(
            PerceptronPersonNameService service) {
        super(LEVEL3);
        this.service = service;
    }

    @Override
    public void fill(Wordnet wordnet) {

        char[] charArray = wordnet.getCharArray();

        List<PersonName> names = service.findName(charArray);

        wordnet.set(PersonNamePlugin.key,names);

        if (!names.isEmpty()) {
            for (PersonName name : names) {

                // 人名<=3，可能性高，作为初始词汇。防止被切断。陈宝奇怪别人不好
                if (name.getName().length() <= 3) {
                    //如果已经存在
                    if (wordnet.row(name.getOffset()).contains(name.getName().length())) {
                        continue;
                    }
                    Vertex v = new Vertex(name.getName().length());
                    v.setAbsWordNatureAndFreq(Nature.nr);
                    wordnet.put(name.getOffset(), v);
                }
            }
        }

    }
}
