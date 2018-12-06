package com.mayabot.nlp.segment.dictionary.core;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.dictionary.Nature;

import java.util.ArrayList;
import java.util.List;

/**
 * 这些抽象的词，使用\u0001开头，保证字典排序在前面。
 * 这样在DAT中的wordId也就提前确定了。
 * 这样Nature和他们的对应的关系也就确定下来了，
 * 比如词性 m对应的等效词必定能快速查询，一个wordId是不是等效词也能快速判断。
 * 这些全部是Static的，程序没有依赖关系
 */
public class CoreDictionaryAbsWords {

//
//    //计算出预编译的量
//    Begin_WORD_ID = getWordID(BIGIN_TAG);
//    End_WORD_ID = getWordID(TAG_END);
//
//    XX_WORD_ID = getWordID(TAG_OTHER);
//    NR_WORD_ID = getWordID(TAG_PEOPLE);
//    NS_WORD_ID = getWordID(TAG_PLACE);
//    NT_WORD_ID = getWordID(TAG_GROUP);
//    T_WORD_ID = getWordID(TAG_TIME);
//    X_WORD_ID = getWordID(TAG_CLUSTER);
//    M_WORD_ID = getWordID(TAG_NUMBER);
//    NX_WORD_ID = getWordID(TAG_NX);

    public static final int MaxId = 9;


    public static final int BIGIN_ID = 0;
    public static final int END_ID = 1;
    public static final int MQ_ID = 2;
    public static final int NX_ID = 3;
    public static final int TIME_ID = 4;
    public static final int X_ID = 5;
    public static final int NT_ID = 6;
    public static final int NS_ID = 7;
    public static final int NR_ID = 8;
    public static final int NUM_ID = 9;

    public static void main(String[] args) {
        System.out.println(allLabel());
    }


    /**
     * 词性和AbsWordId的对应关系
     *
     * @return
     */
    public final static int nature2id(Nature nature) {
        switch (nature) {
            case begin:
                return BIGIN_ID;

            default:
                return -1;
        }
    }

    /**
     * 抽象词，对应的Label
     *
     * @param id
     * @return
     */
    public final static String id2label(int id) {
        switch (id) {
            case BIGIN_ID:
                return BIGIN_TAG;

            default:
                return null;
        }
    }


    /**
     * 构建词典的时候一定把这些放进去
     *
     * @return
     */
    public static List<String> allLabel() {
        ArrayList<String> list = Lists.newArrayList(
                BIGIN_TAG,
                TAG_END,
                TAG_QUANTIFIER,
                TAG_NX,
                TAG_TIME,
                TAG_CLUSTER,
                TAG_GROUP,
                TAG_PLACE,
                TAG_PEOPLE,
                TAG_NUMBER
        );
        return list;
    }

    /**
     * 句子的开始 begin
     */
    public final static String BIGIN_TAG = "\u00010Begin";

    /**
     * 结束 end
     */
    public final static String TAG_END = "\u00011End";

    /**
     * 数量词 mq （现在觉得应该和数词同等处理，比如一个人和一人都是合理的）
     */
    public final static String TAG_QUANTIFIER = "\u00012MQ";

    /**
     * nx 中文语料里面出现了英文单词。
     * 不能叫专有名词
     */
    public final static String TAG_NX = "\u00013NX";

    /**
     * 时间 t
     */
    public final static String TAG_TIME = "\u00014Time";

    /**
     * 字符串 x
     */
    public final static String TAG_CLUSTER = "\u00015String";

    /**
     * 团体名词 组织机构 nt
     */
    public final static String TAG_GROUP = "\u00016NT团体";

    /**
     * 地址 ns
     */
    public final static String TAG_PLACE = "\u00017NS地名";
    /**
     * 人名 nr
     */
    public final static String TAG_PEOPLE = "\u00018NR人名";


    /**
     * 数词 m
     */
    public final static String TAG_NUMBER = "\u00019Num";

//    /**
//     * 其它 XX
//     */
//    public final static String TAG_OTHER = "\u0001AOther";
}
