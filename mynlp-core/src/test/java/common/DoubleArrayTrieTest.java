package common;

import org.junit.Test;
import org.trie4j.doublearray.DoubleArray;
import org.trie4j.patricia.PatriciaTrie;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Stream;

public class DoubleArrayTrieTest {

    @Test
    public void test() {
        PatriciaTrie trie = new PatriciaTrie();
        String[] list  = (
                        "一举\n" +
                        "一举一动\n" +
                        "一举成名\n" +
                        "一举成名天下知\n" +
                        "五谷\n" +
                        "五谷丰登\n" +
                        "万万").split("\n");

        for (int i = list.length - 1; i >= 0; i--) {
            trie.insert(list[i]);
        }

        DoubleArray da = new DoubleArray(trie);

        da.trimToSize();
        System.out.println("Index" + "\t" + "Base" + "\t" + "Check");

        //IntIntHashMap intIntMap = new IntIntHashMap();

        for (int i = 0; i < da.getBase().length; i++) {
            if (da.getBase()[i] != 0 || da.getCheck()[i] != 0) {
                System.out.println(i + "\t\t" + da.getBase()[i] + "\t\t" +da.getCheck()[i]);
            }
        }

        da.getNodeId("一举成名");

    }

    @Test
    public void tes2t() throws Exception{
        File file = new File("../data/crf_funtions.txt");

        final String prefix = "U05:，";

        Stream<String> lines = Files.lines(file.toPath());
        PatriciaTrie trie = new PatriciaTrie();
        lines
                .map(x -> x.split(" ")[1])
                .filter(x->x.startsWith(prefix) || x.startsWith("U01"))
                .forEach(
                        x -> trie.insert(x.substring(prefix.length()))
//                        x -> trie.insert(x)
                );
        System.out.println(prefix+":"+trie.size());
        long t1 = System.currentTimeMillis();
        DoubleArray da = new DoubleArray(trie);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);

        da.trimToSize();
        System.out.println(da.getBase());
    }
}
