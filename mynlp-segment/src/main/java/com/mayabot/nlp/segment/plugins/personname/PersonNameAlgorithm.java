package com.mayabot.nlp.segment.plugins.personname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    @Inject
    public PersonNameAlgorithm(PerceptronPersonNameService service) {
        this.service = service;
    }

    @Override
    public void fill(Wordnet wordnet) {

        char[] charArray = wordnet.getCharArray();

        List<PersonName> names = service.findName(charArray);

        if (!names.isEmpty()) {
            for (PersonName name : names) {
                Vertex v = new Vertex(name.getName().length());
                v.setAbsWordNatureAndFreq(Nature.nr);
                wordnet.put(name.getOffset(), v);
            }
        }

    }
}
