package com.mayabot.nlp;

import com.google.common.collect.Maps;
import com.mayabot.nlp.collection.bintrie.BinTrieTree;
import com.mayabot.nlp.collection.bintrie.BinTrieTreeBuilder;
import com.mayabot.nlp.collection.bintrie.TrieTreeMatcher;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;

import java.util.TreeMap;

public class Test {
    public static void main(String[] args) {
        TreeMap<String,String> map = Maps.newTreeMap();

        map.put("习近平","M");

        final BinTrieTree<String> tree =

                BinTrieTreeBuilder.miniHash.build(map);

        DoubleArrayTrie dat = new DoubleArrayTrieBuilder<String>().build(map);

        final DATMatcher xx = dat.match("我们的习近平是中共的鹅鹅鹅", 0);


        {
            final TrieTreeMatcher<String> matcher = tree.newAllMatcher("我们的习近平是中共的鹅鹅鹅");

            String x = matcher.next();
            while (x != null) {
                System.out.println(x);
                System.out.println(matcher.getOffset());
                System.out.println(matcher.getParams());

                x = matcher.next();
            }
        }

        long t1 = System.currentTimeMillis();
        for (int i = 0; i <1000000; i++) {
            final TrieTreeMatcher<String> matcher = tree.newAllMatcher("我们的习近平是中共的鹅鹅鹅");

            String x = matcher.next();
            while (x != null) {
                x = matcher.next();
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);


    }
}
