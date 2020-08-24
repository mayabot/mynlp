package com.mayabot.nlp.segment.plugins.ner;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.common.Lists;
import com.mayabot.nlp.common.injector.Singleton;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jimichan
 */
@Singleton
public class NerProcessor extends BaseSegmentComponent implements WordpathProcessor, PipelineLexerPlugin {

    private final PerceptronNerService service;

    public NerProcessor(
            PerceptronNerService perceptronPosService
    ) {
        super(LEVEL5);
        this.service = perceptronPosService;
    }

    @Override
    public void init(PipelineLexerBuilder pipelineLexerBuilder) {
        pipelineLexerBuilder.addProcessor(this);
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        ArrayList<Vertex> vertices = Lists.newArrayList(wordPath.iteratorVertex());
        try {


            List<String> tagS = service.getPerceptron().decodeVertexList(vertices);

            int from = -1;
            int length = 0;
            for (int i = 0; i < vertices.size(); i++) {
                String tag = tagS.get(i);
                Vertex vertex = vertices.get(i);

                if ("O".equals(tag) || "S".equals(tag)) {
                    from = -1;
                    length = 0;
                } else if (tag.startsWith("B-")) {
                    from = vertex.offset();
                    length += vertex.length;
                } else if (tag.startsWith("M-")) {
                    length += vertex.length;
                } else if (tag.startsWith("E-")) {
                    length += vertex.length;
                    if (from != -1) {

                        Vertex x = wordPath.combine(from, length);

                        if ("E-nt".equals(tag)) {
                            x.nature = Nature.nt;
                        }
                        if ("E-ns".equals(tag)) {
                            x.nature = Nature.ns;
                        }
                    }

                    //连续出现BMEBME
                    from = -1;
                    length = 0;
                }
            }
        } catch (Exception e) {
            Mynlp.logger.error("",e);
        }

        return wordPath;
    }
}