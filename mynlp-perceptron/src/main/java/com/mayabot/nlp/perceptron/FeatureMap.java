package com.mayabot.nlp.perceptron;

import com.carrotsearch.hppc.ObjectIntHashMap;

import java.util.Map;
import java.util.TreeMap;

public class FeatureMap {
    private ObjectIntHashMap<String> featureMap = new ObjectIntHashMap<>(5000000);
//    public Map<String, Integer> featureMap = new TreeMap<>();

    private int[] tagSet;

    public FeatureMap(int[] tagSet) {
        this.tagSet = tagSet;
//        addTransitionFeatures(tagSet);

    }

    private void addTransitionFeatures(int[] tagSet) {
        String [] temp = {"B","M","E","S"};
        for (int i = 0; i < tagSet.length; i++) {
            idOf("BL=" + temp[i]);
        }
        idOf("BL=_BL_");
    }

    public int tagSize() {
        return tagSet.length;
    }

    public int featureSize() {
        return featureMap.size();
    }


    public ObjectIntHashMap<String> getFeatureMap() {
        return this.featureMap;
    }

    public void setFeatureMap(ObjectIntHashMap<String> featureMap) {
        this.featureMap = featureMap;
    }

    public int[] getTagSet() {
        return tagSet;
    }

    public void setTagSet(int[] tagSet) {
        this.tagSet = tagSet;
    }

    public int idOf(String o) {
        int id = featureMap.get(o);
        if (id != 0) return id;
        else {
            if (featureMap.containsKey(o))
                return id;
            else {
                id = featureMap.size();
                featureMap.put(o, id);
                return id;
            }
        }
    }
}
