///*
// * Copyright © 2017 mayabot.com. All rights reserved.
// *
// */
//package com.mayabot.nlp.segment.corpus.occ;
//
//import java.util.List;
//
//import com.mayabot.nlp.collection.trietree.BinTrieTree;
//import com.mayabot.nlp.segment.corpus.occ.Occurrence.TermFrequency;
//
///**
// * 词频统计
// * @author hankcs
// */
//public class TermOccurrence
//{
//    /**
//     * 词频统计用的储存结构
//     */
//    BinTrieTree<TermFrequency> trieSingle;
//    int totalTerm;
//
//    public TermOccurrence()
//    {
//        trieSingle =  BinTrieTree.build();
//    }
//
//    public void add(String term)
//    {
//        TermFrequency value = trieSingle.get(term);
//        if (value == null)
//        {
//            value = new TermFrequency(term);
//            trieSingle.put(term, value);
//        }
//        else
//        {
//            value.increase();
//        }
//        ++totalTerm;
//    }
//
//    public void addAll(List<String> termList)
//    {
//        for (String s : termList)
//        {
//            add(s);
//        }
//    }
//}
