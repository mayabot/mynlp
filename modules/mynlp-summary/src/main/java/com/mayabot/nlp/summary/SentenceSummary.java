package com.mayabot.nlp.summary;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.Lexers;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文本摘要
 *
 * @author jimichan
 */
public class SentenceSummary {

    private static Splitter lineSplitter = Splitter.on(Pattern.compile("[\r\n]")).omitEmptyStrings().trimResults();
    private static Splitter sentenceSplitter = Splitter.on(Pattern.compile("[，,。:：“”？?！!；;]")).omitEmptyStrings().trimResults();

    private LexerReader lexerReader = Lexers.core().filterReader(true, true);

    /**
     * 对文章进行摘要
     *
     * @param document 目标文档
     * @param max      需要摘要的长度
     * @return 摘要文本
     */
    public String summary(String document, int max) {
        List<String> sentenceList = splitSentence(document);
        if (sentenceList.isEmpty()) {
            return "";
        }

        int sentence_count = sentenceList.size();
        int document_length = document.length();
        int sentence_length_avg = document_length / sentence_count;
        int size = max / sentence_length_avg + 1;

        List<List<String>> docs = toDocument(sentenceList);
        TextRankSentence textRank = new TextRankSentence(docs);
        int[] topSentence = textRank.getTopSentence(size);
        List<String> resultList = new LinkedList<String>();
        for (int i : topSentence) {
            resultList.add(sentenceList.get(i));
        }

        resultList = permutation(resultList, sentenceList);
        resultList = pickSentences(resultList, max);

        return Joiner.on('。').join(resultList);
    }

    /**
     * 对文章进行摘要
     *
     * @param document 文档
     * @param top      需要的关键句的个数
     * @return 关键句列表
     */
    public List<String> summarySentences(String document, int top) {

        List<String> sentences = splitSentence(document);
        List<List<String>> docs = toDocument(sentences);

        TextRankSentence textRank = new TextRankSentence(docs);
        int[] topSentence = textRank.getTopSentence(top);

        List<String> resultList = new LinkedList<String>();
        for (int i : topSentence) {
            resultList.add(sentences.get(i));
        }

        return resultList;
    }


    private List<String> permutation(List<String> resultList, final List<String> sentenceList) {
        Collections.sort(resultList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Integer num1 = sentenceList.indexOf(o1);
                Integer num2 = sentenceList.indexOf(o2);
                return num1.compareTo(num2);
            }
        });
        return resultList;
    }

    private List<String> pickSentences(List<String> resultList, int max_length) {
        List<String> summary = new ArrayList<String>();
        int count = 0;
        for (String result : resultList) {
            if (count + result.length() <= max_length) {
                summary.add(result);
                count += result.length();
            }
        }
        return summary;
    }


    private List<String> splitSentence(String document) {
        List<String> sentences = Lists.newArrayList();

        lineSplitter.split(document).forEach(line ->
                sentences.addAll(sentenceSplitter.splitToList(line))
        );

        return sentences;
    }

    private List<List<String>> toDocument(List<String> setences) {
        List<List<String>> sentences = Lists.newArrayList();

        setences.forEach(sentence -> {
            sentences.add(Lists.newArrayList(lexerReader.scan(sentence).toWordSequence()));
        });

        return sentences;
    }

    public static Splitter getLineSplitter() {
        return lineSplitter;
    }

    public static void setLineSplitter(Splitter lineSplitter) {
        SentenceSummary.lineSplitter = lineSplitter;
    }

    public static Splitter getSentenceSplitter() {
        return sentenceSplitter;
    }

    public static void setSentenceSplitter(Splitter sentenceSplitter) {
        SentenceSummary.sentenceSplitter = sentenceSplitter;
    }

    public LexerReader getLexerReader() {
        return lexerReader;
    }

    public SentenceSummary setLexerReader(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
        return this;
    }
}
