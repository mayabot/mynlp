package com.mayabot.nlp.collection.dat;

import com.carrotsearch.hppc.IntIntHashMap;
import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Stream;

public class DoubleArrayTrieBuildSpeedTest {

    @Test
    public void test() throws IOException {
        File file = new File("../data/CoreNatureDictionary.txt");
        List<String> strings = Files.readAllLines(file.toPath(), Charsets.UTF_8);

        TreeMap<String, String> treeMap = new TreeMap<>();

        for (String line : strings) {
            String word = line.substring(0, line.indexOf("\t"));
            treeMap.put(word, word);
        }

        long t1 = System.currentTimeMillis();
        DoubleArrayTrieBuilder builder = new DoubleArrayTrieBuilder();
        DoubleArrayTrie trie = builder.build(treeMap);
        long t2 = System.currentTimeMillis();

        Stream<String> lines = Files.lines(file.toPath());

    }

    @Test
    public void buildBigTrie() throws IOException {
        File file = new File("../data/crf_funtions.txt");

        final String prefix = "U05:，";

            Stream<String> lines = Files.lines(file.toPath());

            TreeMap<String, String> treeMap = new TreeMap<>();
            lines
                    .map(x -> x.split(" ")[1])
                    .filter(x->x.startsWith(prefix))
                    .forEach(
                            x -> treeMap.put(x.substring(prefix.length()), x)
                    );
            System.out.println(prefix+":"+treeMap.size());
            long t1 = System.currentTimeMillis();
            DoubleArrayTrieBuilder builder = new DoubleArrayTrieBuilder();
            DoubleArrayTrie trie = builder.build(treeMap);
            long t2 = System.currentTimeMillis();



            System.out.println("base len "+trie.base.length);


            int[] base = trie.base;
            int c =0;
            for (int i = base.length-1; i >=0 ; i--) {
                if (0== base[i]) {
                    c++;
                }else{
                    System.out.println("+"+i);
                    break;
                }
            }

        System.out.println("tail empty count"+c);
        IntIntHashMap map = new IntIntHashMap();
        for (int i = 0; i < trie.base.length; i++) {
            if (trie.base[i] != 0) {
                map.put(i, trie.base[i]);
            }
        }

        Random random = new Random();
        {
            long tt1 = System.currentTimeMillis();
            final int[] base_ = trie.base;
            for (int i = 0; i < 1000000; i++) {
                int an = random.nextInt(base_.length);
                int i1 = base_[an];
            }
            long tt2 = System.currentTimeMillis();

            System.out.println("assess array use time " + (tt2 - tt1) + "ms");
        }

        {
            long tt1 = System.currentTimeMillis();
            final int[] base_ = trie.base;
            for (int i = 0; i < 1000000; i++) {
                int an = random.nextInt(base_.length);
                int i1 = map.get(an);
            }
            long tt2 = System.currentTimeMillis();

            System.out.println("assess map use time " + (tt2 - tt1) + "ms");
        }

        System.out.println("keys="+map.keys.length);
        System.out.println(map.keys.length*1.0/trie.base.length);

            System.out.println(prefix+" use time"+(t2-t1));
    }
//
//    @Test
//    public void testFindCommonPrefix() throws IOException {
//        File file = new File("../data/crf_funtions.txt");
//
//
//        Stream<String> lines = Files.lines(file.toPath());
//
//        CountBloomFilter<CharSequence> filter = CountBloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 50000);
//
//        AtomicInteger c = new AtomicInteger();
//        lines
//                .map(x -> x.split(" ")[1])
//                .forEach(
//                        x -> {
//                            int count = c.incrementAndGet();
//
//                            for (int i = 1; i <= x.length(); i++) {
//                                filter.put(x.substring(0, i));
//                            }
//
//                            if (count % 10000 == 0) {
//                                filter.resetLowCount(5000);
//                            }
//                        }
//                );
//
//        filter.resetLowCount(5000);
//        Map<String, Integer> stringIntegerMap = filter.exportHighRepeatKeys();
//
//        System.out.println(stringIntegerMap.size());
//
//        stringIntegerMap.forEach((x,y)->{
//            System.out.println(x+" -> "+y);
//        });
//
//
//        Map<String,Integer> map = Maps.newTreeMap();
//        Files.lines(file.toPath())
//                .map(x -> x.split(" ")[1])
//                .forEach(
//                        x -> {
//                            for (int i = 1; i <= x.length(); i++) {
//                                String sub = x.substring(0, i);
//
//                                int i1 = filter.mayCount(sub);
//                                if (i1 > 5000) {
//                                    map.putIfAbsent(sub,i1);
//                                }
//                            }
//
//                        }
//                );
//
//        map.forEach((x,y)->{
//            System.out.println(x+" -> "+y);
//        });
//    }


}
