package com.mayabot.nlp.perceptron.extractor;

import com.mayabot.nlp.perceptron.FeatureMap;

import java.util.ArrayList;
import java.util.List;

public class DefaultPosTagFeatureExtractor extends DefaultFeatureExtractor<String> {
    public int[] extractFeature(String[] words, int position, FeatureMap featureMap) {
        List<Integer> featureVector = new ArrayList<>();

//        String pre2Word = position >= 2 ? words[position - 2] : "_B_";
        String preWord = position >= 1 ? words[position - 1] : "_B_";
        String curWord = words[position];

        //		System.out.println("cur: " + curWord);
        String nextWord = position <= words.length - 2 ? words[position + 1] : "_E_";
//        String next2Word = position <= words.length - 3 ? words[position + 2] : "_E_";

        StringBuilder sbFeature = new StringBuilder();
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("U[-2,0]=").append(pre2Word);
//        addFeature(sbFeature, featureVector, featureMap);

        sbFeature.append(preWord).append('1');
        addFeatureThenClear(sbFeature, featureVector, featureMap);

        sbFeature.append(curWord).append('2');
        addFeatureThenClear(sbFeature, featureVector, featureMap);

        sbFeature.append(nextWord).append('3');
        addFeatureThenClear(sbFeature, featureVector, featureMap);

//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("U[2,0]=").append(next2Word);
//        addFeature(sbFeature, featureVector, featureMap);

        // wiwi+1(i = − 1, 0)
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("B[-1,0]=").append(preWord).append("/").append(curWord);
//        addFeature(sbFeature, featureVector, featureMap);
//
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("B[0,1]=").append(curWord).append("/").append(nextWord);
//        addFeature(sbFeature, featureVector, featureMap);
//
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("B[-1,1]=").append(preWord).append("/").append(nextWord);
//        addFeature(sbFeature, featureVector, featureMap);

        // last char(w−1)w0
//        String lastChar = position >= 1 ? "" + words[position - 1].charAt(words[position - 1].length() - 1) : "_BC_";
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("CW[-1,0]=").append(lastChar).append("/").append(curWord);
//        addFeature(sbFeature, featureVector, featureMap);
//
//        // w0 ﬁrst_char(w1)
//        String nextChar = position <= words.length - 2 ? "" + words[position + 1].charAt(0) : "_EC_";
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("CW[1,0]=").append(curWord).append("/").append(nextChar);
//        addFeature(sbFeature, featureVector, featureMap);
//
        int length = curWord.length();
//
//        // ﬁrstchar(w0)lastchar(w0)
//        sbFeature.delete(0, sbFeature.length());
//        sbFeature.append("BE=").append(curWord.charAt(0)).append("/").append(curWord.charAt(length - 1));
//        addFeature(sbFeature, featureVector, featureMap);

        // prefix
        sbFeature.append(curWord.substring(0, 1)).append('4');
        addFeatureThenClear(sbFeature, featureVector, featureMap);

        if (length > 1) {
            sbFeature.append(curWord.substring(0, 2)).append('4');
            addFeatureThenClear(sbFeature, featureVector, featureMap);
        }

        if (length > 2) {
            sbFeature.append(curWord.substring(0, 3)).append('4');
            addFeatureThenClear(sbFeature, featureVector, featureMap);
        }

        // sufﬁx(w0, i)(i = 1, 2, 3)
        sbFeature.append(curWord.charAt(length - 1)).append('5');
        addFeatureThenClear(sbFeature, featureVector, featureMap);

        if (length > 1) {
            sbFeature.append(curWord.substring(length - 2)).append('5');
            addFeatureThenClear(sbFeature, featureVector, featureMap);
        }

        if (length > 2) {
            sbFeature.append(curWord.substring(length - 3)).append('5');
            addFeatureThenClear(sbFeature, featureVector, featureMap);
        }

        return toFeatureArray(featureVector);
    }
}
