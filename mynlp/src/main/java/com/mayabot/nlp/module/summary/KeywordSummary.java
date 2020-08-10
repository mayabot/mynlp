package com.mayabot.nlp.module.summary;

import com.mayabot.nlp.algorithm.TopMaxK;
import com.mayabot.nlp.common.Pair;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.Lexers;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于TextRank算法的关键字提取，适用于单文档
 * <p>
 * 该对象可复用，多线程安全。
 *
 * @author hankcs
 * @author jimichan
 */
public class KeywordSummary {

    /**
     * 阻尼系数一般取值为0.85
     */
    float d = 0.85f;

    /**
     * 最大迭代次数
     */
    private int maxIter = 200;

    private float minDiff = 0.001f;

    private LexerReader lexerReader = Lexers.core().filterReader(true, true);


    public List<String> keyword(String text, int top) {
        return keywordWithScore(new StringReader(text), top).stream().map(it -> it.first).collect(Collectors.toList());
    }

    public List<String> keyword(Reader text, int top) {
        return keywordWithScore(text, top).stream().map(it -> it.first).collect(Collectors.toList());
    }

    public List<Pair<String, Float>> keywordWithScore(String text, int top) {
        return keywordWithScore(new StringReader(text), top);
    }

    public List<Pair<String, Float>> keywordWithScore(Reader text, int top) {
        TopMaxK<String> topMaxK = new TopMaxK<String>(top, String.class);

        Map<String, Float> rank = getRank(text);

        rank.forEach((k, v) -> topMaxK.push(k, v));

        return topMaxK.result();
    }

    /**
     * 使用已经分好的词来计算rank
     * 该方法复制来自https://github.com/hankcs/HanLP/blob/master/src/main/java/com/hankcs/hanlp/summary/KeywordSummary.java
     *
     * @return
     */
    private Map<String, Float> getRank(Reader reader) {

        Map<String, Set<String>> words = new TreeMap<>();
        Queue<String> que = new LinkedList<>();

        lexerReader.scan(reader).stream().map(it -> it.word).forEach(w -> {
            if (!words.containsKey(w)) {
                words.put(w, new TreeSet<>());
            }
            // 复杂度O(n-1)
            if (que.size() >= 5) {
                que.poll();
            }
            for (String qWord : que) {
                if (w.equals(qWord)) {
                    continue;
                }
                //既然是邻居,那么关系是相互的,遍历一遍即可
                words.get(w).add(qWord);
                words.get(qWord).add(w);
            }
            que.offer(w);
        });


        Map<String, Float> score = new HashMap<>(64);

        //依据TF来设置初值
        for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
            score.put(entry.getKey(), sigmoid(entry.getValue().size()));
        }

        for (int i = 0; i < maxIter; ++i) {
            Map<String, Float> m = new HashMap<>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - d);
                for (String element : value) {
                    int size = words.get(element).size();
                    if (key.equals(element) || size == 0) {
                        continue;
                    }
                    m.put(key, m.get(key) + d / size * (score.get(element) == null ? 0 : score.get(element)));
                }
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= minDiff) {
                break;
            }
        }

        return score;
    }


    private float sigmoid(float value) {
        return (float) (1d / (1d + Math.exp(-value)));
    }

    public LexerReader getLexerReader() {
        return lexerReader;
    }

    /**
     * 设置新的分词器。默认是去除停用词和标点符号的
     *
     * @param lexerReader
     * @return KeywordSummary
     */
    public KeywordSummary setLexerReader(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
        return this;
    }


    public float getD() {
        return d;
    }

    public KeywordSummary setD(float d) {
        this.d = d;
        return this;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public KeywordSummary setMaxIter(int maxIter) {
        this.maxIter = maxIter;
        return this;
    }

    public float getMinDiff() {
        return minDiff;
    }

    public KeywordSummary setMinDiff(float minDiff) {
        this.minDiff = minDiff;
        return this;
    }
}
