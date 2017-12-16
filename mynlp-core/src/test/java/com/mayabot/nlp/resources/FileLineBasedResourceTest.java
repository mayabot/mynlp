package com.mayabot.nlp.resources;

import com.google.common.collect.Iterators;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

public class FileLineBasedResourceTest {

    @Test
    public void test() {

        FileMynlpResource f = new FileMynlpResource(
                new File("/Users/jimichan/project-new/mynlp/mynlp-segment/src/main/resources/maya_data/dictionary/core/CoreNatureDictionary.ngram.txt.zip"));


        Iterator<String> x = Iterators.filter(f.openLineReader(), line -> line.startsWith("陈"));

        Iterators.limit(x, 100).forEachRemaining(
                line -> System.out.println(line)
        );


    }
}