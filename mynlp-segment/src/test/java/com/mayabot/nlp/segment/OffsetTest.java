package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

/**
 * 检测WordTerm的offset的正确性，因为换行符有可能导致
 */
public class OffsetTest {

    String text = "词汇缓存即词汇表缓存\n,是Deeplearning4j用于处理TF-IDF、词向量和某些信息提取方法等自然语言分析任务的一种通用机制。词汇缓存的作用是充当文本向量化的一站式存储组件,保存词袋和词向量等模型通常需要使用的一些信息。\n" +
            "\n" +
            "词汇缓存用一个倒排索引来存储词例、词频概率、逆文档概率和词在文档中的出现次数,其参考实现为InMemoryLookupCache。\n" +
            "\n" +
            "为了在对文本和索引词例进行迭代时使用词汇缓存,您需要明确词例是否应当纳入词汇表中。一般的标准是词例在语料库中出现次数是否超过预设频率。如词例的出现频率低于预设值,则不把该词例纳入词汇表,而是仅仅作为词例处理。";

    @Test
    public void test() {
        List<WordTerm> wordTerms = MynlpTokenizers.coreTokenizer().parse(text).asWordList();


        long count = wordTerms.stream().
                filter(it -> !text.substring(it.getOffset(), it.getOffset() + it.length())
                        .toLowerCase()
                        .equals(it.word))
                .count();

        Assert.assertTrue(count == 0);

    }

    @Test
    public void test21() {
        Stream<WordTerm> wordTerms = new StandardMynlpAnalyzer().stream(new StringReader(text));


        long count = wordTerms.
                filter(it -> !text.substring(it.getOffset(), it.getOffset() + it.length())
                        .toLowerCase()
                        .equals(it.word))
                .count();

        Assert.assertTrue(count == 0);

    }

    @Test
    public void test2() {
        Stream<WordTerm> wordTerms = new StandardMynlpAnalyzer().stream(text);


        long count = wordTerms.
                filter(it -> !text.substring(it.getOffset(), it.getOffset() + it.length())
                        .toLowerCase()
                        .equals(it.word))
                .count();

        Assert.assertTrue(count == 0);

    }

    @Test
    public void test3() {
        Stream<WordTerm> wordTerms = new StandardMynlpAnalyzer().stream("");

        long count = wordTerms.count();

        Assert.assertTrue(count == 0);

    }
}
