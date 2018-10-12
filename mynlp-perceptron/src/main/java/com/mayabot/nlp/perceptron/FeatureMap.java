package com.mayabot.nlp.perceptron;

import com.carrotsearch.hppc.ObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FeatureMap {
    private ObjectIntHashMap<String> featureMap = new ObjectIntHashMap<>(5000000);
//    public Map<String, Integer> featureMap = new TreeMap<>();
    private List<String> labelSet;
    private int[] tagSet;

    public FeatureMap(List<String> labelSet) {
        this.labelSet = labelSet;
        tagSet = new int[labelSet.size()];
        for (int i = 0; i < labelSet.size(); i++){
            tagSet[i] = i;
        }
        addTransitionFeatures();

    }

    private void addTransitionFeatures() {
        for (int i = 0; i < tagSet.length; i++)
        {
            idOf("BL=" + labelSet.get(i));
        }
        idOf("BL=_BL_");
//        String [] temp = {"B","M","E","S"};
//        for (int i = 0; i < tagSet.length; i++) {
//            idOf("BL=" + temp[i]);
//        }
//        idOf("BL=_BL_");
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

    public List<String> getLabelSet() {
        return labelSet;
    }

    public void setLabelSet(List<String> labelSet) {
        this.labelSet = labelSet;
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
