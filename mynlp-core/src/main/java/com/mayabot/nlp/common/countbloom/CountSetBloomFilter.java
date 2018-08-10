package com.mayabot.nlp.common.countbloom;

import com.google.common.hash.Funnel;
import com.google.common.primitives.Ints;

import java.util.*;
import java.util.stream.Collectors;

public class CountSetBloomFilter<T> implements Iterable<T>{

    CountBloomFilter<T> bf;

    List<T> list;

    int expectedInsertions;

    int keep;

    int fz;

    public CountSetBloomFilter(Funnel<T> funnel,int keep){
        this.expectedInsertions = expectedInsertions;
        bf = CountBloomFilter.create(funnel, keep*3, 0.01);
        list= new ArrayList<>();
        this.keep = keep;
        fz = keep*2;
    }

    public void put(T object) {
        boolean c = bf.put(object);
        if (!c) {
            list.add(object);
        }

        if (list.size() > fz) {
            compact(.2f);
        }

    }

    public int count(T object) {
        return bf.mayCount(object);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    /**
     * 压缩，去除80的数据，保留最高的20%
     */
    public void compact(float k){
        //int keep = (int)(list.size()*k);
        TreeSet<TP> top = new TreeSet<>();
        top.add(new TP("",-1));

        for (T word : list) {
            int count = bf.mayCount(word);
            final TP tp = top.last();
            if (count > tp.count) {
                tp.count = count;
                tp.value = word;
                top.add(new TP(word,count));
            }

            if (top.size()>keep){
                top.remove(top.last());
            }
        }

        List<T> topList = top.stream().map(it -> (T) it.value).collect(Collectors.toList());
        Set<T> set = topList.stream().collect(Collectors.toSet());

        for (T x : list) {
            if(!set.contains(x)){
                bf.removeCount(x);
            }
        }

        this.list = topList;
    }

    public static void main(String[] args) {

    }

    static class TP implements Comparable<TP>{
        Object value;
        int count = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TP tp = (TP) o;
            return Objects.equals(value, tp.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public int compareTo(TP o) {
            return Ints.compare(o.count,count);
        }

        public TP(Object value, int count) {
            this.value = value;
            this.count = count;
        }

        @Override
        public String toString() {
            return "TP{" +
                    "value=" + value +
                    ", count=" + count +
                    '}';
        }
    }

}
