package com.mayabot.nlp.perceptron;

import com.carrotsearch.hppc.ObjectIntHashMap;

public class FeatureMap{
    private ObjectIntHashMap<String> featureMap = new ObjectIntHashMap<>(5000000);
//    private HashMap<T, Integer> featureMap = new HashMap<>(5000000);

    private int[] tagSet;

    public FeatureMap(int[] tagSet) {
        this.tagSet = tagSet;
    }

    public int tagSize() {
        return tagSet.length;
    }

    public int featureSize() {
        return featureMap.size();
    }

    public ObjectIntHashMap<String> getFeatureMap(){
        return this.featureMap;
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

//    public int idOf(T string)
//    {
//        Integer id = featureMap.get(string);
//        if (id == null)
//        {
//            id = featureMap.size();
//            featureMap.put(string, id);
//        }
//        return id;
//    }
}
