package com.mayabot.nlp.segment.tokenizer.collector;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 对长词进行全切分操作,结果保存在WordTerm的subword里面
 *
 * @author jimichan
 */
public class SentenceIndexWordCollector implements WordTermCollector {

    private int minWordLength = 2;


    @Override
    public void collect(Wordnet wordnet, Wordpath wordPath, Consumer<WordTerm> consumer) {
        Iterator<Vertex> vertexIterator = wordPath.iteratorVertex();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            WordTerm term = new WordTerm(vertex.realWord(), vertex.guessNature());
            term.setOffset(vertex.getRowNum());

            //默认把空白的字符去除掉
            if (StringUtils.isWhiteSpace(term.word)) {
                continue;
            }


            if (vertex.length > 2) {

                List<WordTerm> subwords = Lists.newArrayList();
                term.setSubword(subwords);

                final int lastIndex = vertex.length + vertex.getRowNum();

                int from = vertex.getRowNum();
                int to = from + vertex.length;

                for (int i = from; i < to; i++) {
                    VertexRow row = wordnet.getRow(i);

                    Vertex small = row.first();
                    while (small != null) {
                        try {

                            if (small.length >= minWordLength && i + small.length() <= lastIndex && small != vertex) {

                                WordTerm smallterm = new WordTerm(small.realWord(), small.guessNature());
                                term.setOffset(small.getRowNum());
                                //大于1的词，并且在范围内
                                subwords.add(smallterm);

                            }

                        } finally {
                            small = small.next();
                        }

                    }
                }
            }
        }
    }
}
