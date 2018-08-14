package com.mayabot.nlp;

import org.junit.Test;
import org.trie4j.io.TrieWriter;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.patricia.PatriciaTrie;

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SuxTest {

    @Test
    public void test() throws Exception{
        File file = new File("../data/crf_funtions.txt");

        Stream<String> lines = Files.lines(file.toPath());

        PatriciaTrie pat = new PatriciaTrie();

        long t1 = System.currentTimeMillis();
        lines
                .map(x -> x.split(" ")[1])
                .forEach(
                        x -> pat.insert(x)
                );

        System.out.println(pat.size());
//        long t1 = System.currentTimeMillis();
//        DoubleArrayTrieBuilder builder = new DoubleArrayTrieBuilder();
//        DoubleArrayTrie trie = builder.build(treeMap);
//        long t2 = System.currentTimeMillis();

//        System.out.println(t2-t1);



        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);

        TailLOUDSTrie lt = new TailLOUDSTrie(pat);

        System.out.println(System.currentTimeMillis()-t2);
        BufferedOutputStream out
                = new BufferedOutputStream(Files.newOutputStream(Paths.get("../data/trie.out")));
        TrieWriter writer = new TrieWriter(out);

        writer.writeTailLOUDSTrie(lt);

        out.close();

//        DoubleArray da = new DoubleArray(pat); // construct DoubleArray from existing Trie
//        da.contains("World"); // -> true

//        t1 = System.currentTimeMillis();
//        TailLOUDSTrie lt = new TailLOUDSTrie(pat); // construct LOUDS succinct Trie with ConcatTailBuilder(default)
//        t2 = System.currentTimeMillis();
//        System.out.println(t2-t1);
//
//        System.out.println(lt.contains("U05:～/num4/元"));
    }

    @Test
    public void test2() {
        PatriciaTrie pat = new PatriciaTrie();
        pat.insert("人民");
        pat.insert("人民大学");
        pat.insert("我");
        pat.insert("上班");
        pat.contains("Hello"); // -> true

        StringBuilder sb = new StringBuilder();

        pat.predictiveSearch("我在人民大学上班的人").forEach(System.out::println);

        System.out.println(sb);

        //ConcatTailBuilder
//
//
//        pat.predictiveSearch("Wo").forEach(System.out::println); // -> {"Wonder", "Wonderful!", "World"} as Iterable<String>
//
//        DoubleArray da = new DoubleArray(pat); // construct DoubleArray from existing Trie
//        System.out.println(da.contains("World"));
//
//        TailLOUDSTrie lt = new TailLOUDSTrie(pat); // construct LOUDS succinct Trie with ConcatTailBuilder(default)
//        lt.contains("Wonderful!"); // -> true
//        lt.commonPrefixSearch("Wonderful!"); // -> {"Wonder", "Wonderful!"} as Iterable<String>
    }

}
