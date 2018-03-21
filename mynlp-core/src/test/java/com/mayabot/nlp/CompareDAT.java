package com.mayabot.nlp;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import org.junit.Test;
import org.trie4j.doublearray.DoubleArray;
import org.trie4j.doublearray.MapDoubleArray;
import org.trie4j.doublearray.TailDoubleArray;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.tail.ConcatTailArrayBuilder;
import org.trie4j.tail.builder.ConcatTailBuilder;
import org.trie4j.tail.builder.SuffixTrieTailBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CompareDAT {

    @Test
    public void test() throws Exception{
        File file = new File("../data/CoreNatureDictionary.txt");

        ArrayList<String> words = (ArrayList<String>)Files.lines(file.toPath(), Charsets.UTF_8).map(x -> x.split("\t")[0]).collect(Collectors.toList());

        Stopwatch stopwatch = Stopwatch.createUnstarted();
        final int times = 1000000;
        final int wordSize = words.size();
        {
            DoubleArrayTrieBuilder builder = new DoubleArrayTrieBuilder();

            TreeMap<String, String> map = new TreeMap<>();
            words.forEach(x->map.put(x,x));
            stopwatch.start();
            DoubleArrayTrie trie = builder.build(map);
            stopwatch.stop();

            System.out.println("Build My DAT use time "+stopwatch.elapsed(MILLISECONDS)+" ms");

            Random random = new Random(0);

            stopwatch.reset().start();
            for (int i = 0; i < times; i++) {
                String word = words.get(random.nextInt(wordSize));
                trie.get(word);
            }

            stopwatch.stop();
            System.out.println("Find DAT use time "+stopwatch.elapsed(MILLISECONDS)+" ms");

        }

//
//        {
//            MapPatriciaTrie trie = new MapPatriciaTrie();
//
//            stopwatch.reset().start();
//            words.forEach(word -> trie.insert(word,word));
//            stopwatch.stop();
//            System.out.println("Build Patricia use time "+stopwatch.elapsed(MILLISECONDS)+" ms");
//
//            stopwatch.reset().start();
//            MapDoubleArray da = new MapDoubleArray(trie);
            //stopwatch.stop();
//            System.out.println("Build DoubleArray use time "+stopwatch.elapsed(MILLISECONDS)+" ms");
//
//            Random random = new Random(0);
//
//            stopwatch.reset().start();
//            for (int i = 0; i < times; i++) {
//                String word = words.get(random.nextInt(wordSize));
//                da.get(word);
//            }
//
//            stopwatch.stop();
//            System.out.println("Find DAT use time "+stopwatch.elapsed(MILLISECONDS)+" ms");
//
//        }


        {
            PatriciaTrie trie = new PatriciaTrie();

            stopwatch.reset().start();
            words.forEach(word -> trie.insert(word));
            stopwatch.stop();
            System.out.println("Build Patricia use time "+stopwatch.elapsed(MILLISECONDS)+" ms");

            stopwatch.reset().start();
            TailLOUDSTrie da = new TailLOUDSTrie(trie);
            stopwatch.stop();
            System.out.println("Build DoubleArray use time "+stopwatch.elapsed(MILLISECONDS)+" ms");

            Random random = new Random(0);

            stopwatch.reset().start();
            for (int i = 0; i < times; i++) {
                String word = words.get(random.nextInt(wordSize));
                da.contains(word);
            }

            stopwatch.stop();
            System.out.println("Find DAT use time "+stopwatch.elapsed(MILLISECONDS)+" ms");

        }

    }


}
