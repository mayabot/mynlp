package com.mayabot.mynlp.lucene;

import com.mayabot.mylp.lucene.MynlpLuceneAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class UseAnalyzer {
    public static void main(String[] args) throws IOException {
        MynlpLuceneAnalyzer analyzer = new MynlpLuceneAnalyzer();

        TokenStream tokenStream = analyzer.tokenStream("title", "Git有很多优势，其中之一就是远程操作非常简便。本文详细介绍5个Git命令，它们的概念和用法，理解了这些内容，你就会完全掌握Git远程操作。");
        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            System.out.print(tokenStream.getAttribute(CharTermAttribute.class));
            System.out.print("\t");
            System.out.print(tokenStream.getAttribute(OffsetAttribute.class).startOffset());
            System.out.print("\t");
            System.out.print(tokenStream.getAttribute(PositionIncrementAttribute.class).getPositionIncrement());
        }

        analyzer.close();
    }
}
