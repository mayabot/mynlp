package com.mayabot.nlp.dictionary.core;

import com.google.inject.Injector;
import com.mayabot.nlp.MyNlps;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import org.junit.Test;

public class CoreDictionaryTest {


    @Test
    public void get() throws Exception {

        System.setProperty(Settings.KEY_WORK_DIR,"temp");

        CoreDictionary dictionary = MyNlps.getInstance(CoreDictionary.class);
        System.out.println(dictionary.get("普查"));

//        long t1 = System.currentTimeMillis();
//        System.out.println(dictionary.indexOf("人口"));
//        for (int i = 0; i < 10000000; i++) {
//
//            dictionary.get("普查");
//            dictionary.indexOf("人口");
//
//
//        }
//        long t2 = System.currentTimeMillis();
//
//        System.out.println(t2-t1);
    }

}