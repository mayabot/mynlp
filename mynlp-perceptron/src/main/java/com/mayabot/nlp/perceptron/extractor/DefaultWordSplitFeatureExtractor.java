package com.mayabot.nlp.perceptron.extractor;

import com.mayabot.nlp.perceptron.FeatureMap;

import java.util.LinkedList;
import java.util.List;

public class DefaultWordSplitFeatureExtractor extends DefaultFeatureExtractor<Character> {
    private static final char CHAR_BEGIN = '\u0001';
    private static final char CHAR_END = '\u0002';

    @Override
    public int[] extractFeature(Character[] sentence, int position, FeatureMap featureMap) {
        List<Integer> featureVec = new LinkedList<>();

        char pre2Char = position >= 2 ? sentence[position - 2] : CHAR_BEGIN;
        char preChar = position >= 1 ? sentence[position - 1] : CHAR_BEGIN;
        char curChar = sentence[position];
        char nextChar = position < sentence.length - 1 ? sentence[position + 1]: CHAR_END;
        char next2Char = position < sentence.length - 2 ? sentence[position + 2] : CHAR_END;

        StringBuilder stringBuilder = new StringBuilder();
        //char unigram feature
        stringBuilder.append(preChar).append('1');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        stringBuilder.append(curChar).append('2');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        stringBuilder.append(nextChar).append('3');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        //char bigram feature
        stringBuilder.append(pre2Char).append("/").append(preChar).append('4');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        stringBuilder.append(preChar).append("/").append(curChar).append('5');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        stringBuilder.append(curChar).append("/").append(nextChar).append('6');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        stringBuilder.append(nextChar).append("/").append(next2Char).append('7');
        addFeatureThenClear(stringBuilder, featureVec, featureMap);

        return toFeatureArray(featureVec);
    }

}
