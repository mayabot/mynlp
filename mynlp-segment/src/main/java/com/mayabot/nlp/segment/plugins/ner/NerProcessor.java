package com.mayabot.nlp.segment.plugins.ner;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.SegmentComponentOrder;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Wordpath;

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

//        service.
//        ArrayList<Vertex> vertices = Lists.newArrayList(wordPath.iteratorVertex());
//        List<Nature> posList = perceptronPosService.posFromVertex(vertices);
//
//        for (int i = 0; i < vertices.size(); i++) {
//            Vertex vertex = vertices.get(i);
//            if (vertex.nature == null) {
//                vertex.nature = (posList.get(i));
//            }
//        }

        return wordPath;
    }
}