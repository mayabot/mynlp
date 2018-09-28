package com.mayabot.nlp.perceptron.lmpl;

import com.mayabot.nlp.perceptron.FeatureExtractor;
import com.mayabot.nlp.perceptron.FeatureMap;

import java.util.LinkedList;
import java.util.List;

public class DefaultFeatureExtractor implements FeatureExtractor<Character> {
    private static final char CHAR_BEGIN = '\u0001';
    private static final char CHAR_END = '\u0002';

    private int featureNumber;
//    public DefalutFeatureExtractor(int featureNumber, FeatureMap featureMap){
//        this.featureNumber = featureNumber;
//    }

    @Override
    public int[] featureExtract(Character[] sentence, int position, FeatureMap featureMap) {
        List<Integer> featureVec = new LinkedList<>();

        char pre2Char = position >= 2 ? sentence[position - 2] : CHAR_BEGIN;
        char preChar = position >= 1 ? sentence[position - 1] : CHAR_BEGIN;
        char curChar = sentence[position];
        char nextChar = position < sentence.length - 1 ? sentence[position + 1]: CHAR_END;
        char next2Char = position < sentence.length - 2 ? sentence[position + 2] : CHAR_END;

        StringBuilder sbFeature = new StringBuilder();
        //char unigram feature
        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(preChar).append('1');
        addFeature(sbFeature, featureVec, featureMap);

        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(curChar).append('2');
        addFeature(sbFeature, featureVec, featureMap);

        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(nextChar).append('3');
        addFeature(sbFeature, featureVec, featureMap);

        //char bigram feature
        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(pre2Char).append("/").append(preChar).append('4');
        addFeature(sbFeature, featureVec, featureMap);

        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(preChar).append("/").append(curChar).append('5');
        addFeature(sbFeature, featureVec, featureMap);

        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(curChar).append("/").append(nextChar).append('6');
        addFeature(sbFeature, featureVec, featureMap);

        sbFeature.delete(0, sbFeature.length());
        sbFeature.append(nextChar).append("/").append(next2Char).append('7');
        addFeature(sbFeature, featureVec, featureMap);

        return toFeatureArray(featureVec);
    }


    private void addFeature(CharSequence rawFeature, List<Integer> featureVector, FeatureMap featureMap)
    {
        int id = featureMap.idOf(rawFeature.toString());
        if (id != -1)
        {
            featureVector.add(id);
        }
    }

    private int[] toFeatureArray(List<Integer> featureVector)
    {
        int[] featureArray = new int[featureVector.size() + 1];   // 最后一列留给转移特征
        int index = -1;
        for (Integer feature : featureVector)
        {
            featureArray[++index] = feature;
        }

        return featureArray;
    }

}
