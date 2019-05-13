package com.mayabot.nlp.segment.plugins.ner;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.SegmentComponentOrder;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class NerProcessor extends BaseSegmentComponent implements WordpathProcessor {

    private final PerceptronNerService service;

    @Inject
    NerProcessor(
            PerceptronNerService perceptronPosService
    ) {
        setOrder(SegmentComponentOrder.LASTEST);
        this.service = perceptronPosService;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        ArrayList<Vertex> vertices = Lists.newArrayList(wordPath.iteratorVertex());

        List<String> tagS = service.getPerceptron().decodeVertexList(vertices);

        int from = -1;
        int lenght = 0;
        for (int i = 0; i < vertices.size(); i++) {
            String tag = tagS.get(i);
            Vertex vertex = vertices.get(i);

            if ("O".equals(tag) || "S".equals(tag)) {
                from = -1;
                lenght = 0;
            } else if (tag.startsWith("B-")) {
                from = vertex.offset();
                lenght += vertex.length;
            } else if (tag.startsWith("M-")) {
                lenght += vertex.length;
            } else if (tag.startsWith("E-")) {
                lenght += vertex.length;
                if (from != -1) {
                    Vertex x = wordPath.combine(from, lenght);

                    if ("E-nt".equals(tag)) {
                        x.nature = Nature.nt;
                    }
                    if ("E-ns".equals(tag)) {
                        x.nature = Nature.ns;
                    }
                }
            }
        }

        return wordPath;
    }
}