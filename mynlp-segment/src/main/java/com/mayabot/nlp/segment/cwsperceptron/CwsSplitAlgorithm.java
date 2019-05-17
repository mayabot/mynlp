package com.mayabot.nlp.segment.cwsperceptron;

import com.google.inject.Inject;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.SegmentComponentOrder;
import com.mayabot.nlp.segment.WordSplitAlgorithm;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import static com.mayabot.nlp.segment.cwsperceptron.CWSPerceptron.E;
import static com.mayabot.nlp.segment.cwsperceptron.CWSPerceptron.S;

/**
 * 基于核心词典的基础切词器
 *
 * @author jimichan
 */
public class CwsSplitAlgorithm extends BaseSegmentComponent implements WordSplitAlgorithm {

    private final CWSPerceptron perceptron;
    private CwsService service;
    private CoreDictionary coreDictionary;

    @Inject
    public CwsSplitAlgorithm(CwsService service, CoreDictionary coreDictionary) {
        this.service = service;
        this.coreDictionary = coreDictionary;
        perceptron = service.getPerceptron();
        setOrder(SegmentComponentOrder.FIRST);
    }

    @Override
    public void fill(Wordnet wordnet) {
        char[] text = wordnet.getCharArray();

        int[] decode = perceptron.decode(text, false);

        int p = 0;
        for (int i = 0; i < decode.length; i++) {
            int x = decode[i];
            if (x == S || x == E) {
                combine(wordnet, text, p, i - p + 1);
                p = i + 1;
            }
        }

        if (p < text.length) {
            combine(wordnet, text, p, text.length - p);
        }
    }

    private void combine(Wordnet wordnet, char[] text, int offset, int length) {
        Vertex vertex = wordnet.put(offset, length);
        int wordId = coreDictionary.wordId(text, offset, length);
        if (wordId >= 0) {
            int freq = coreDictionary.wordFreq(wordId);
            vertex.wordID = wordId;
            vertex.freq = freq;
        } else {
            vertex.setAbsWordNatureAndFreq(Nature.newWord);
        }
    }

}