package com.mayabot.nlp.segment.core;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.Nature;

import java.util.List;

/**
 * 这些抽象的词，使用\u0001开头，保证字典排序在前面。
 * 这样在DAT中的wordId也就提前确定了。
 * 这样Nature和他们的对应的关系也就确定下来了，
 * 比如词性 m对应的等效词必定能快速查询，一个wordId是不是等效词也能快速判断。
 * 这些全部是Static的，程序没有依赖关系
 */
public class DictionaryAbsWords {

    public static final int MaxId = 9;

    public static final int BEGIN_ID = 0;
    public static final int END_ID = 1;
    public static final int MQ_ID = 2;
    public static final int NX_ID = 3;
    public static final int TIME_ID = 4;
    public static final int X_ID = 5;
    public static final int NT_ID = 6;
    public static final int NS_ID = 7;
    public static final int NR_ID = 8;
    public static final int M_ID = 9;

    public static void main(String[] args) {
        System.out.println(allLabel());
    }


    /**
     * 词性和AbsWordId的对应关系
     *
     * @return abs word id
     */
    public static int nature2id(Nature nature) {
        switch (nature) {
            case begin:
                return BEGIN_ID;
            case end:
                return END_ID;
            case mq:
                return MQ_ID;
            case nx:
                return NX_ID;
            case t:
                return TIME_ID;
            case x:
                return X_ID;
            case nt:
                return NT_ID;
            case ns:
                return NS_ID;
            case nr:
                return NR_ID;
            case m:
                return M_ID;
            default:
                return -1;
        }
    }

    /**
     * 抽象词，对应的Label
     *
     * @param id abs word id
     * @return abs label
     */
    public static String id2label(int id) {
        switch (id) {
            case BEGIN_ID:
                return BEGIN_TAG;
            case END_ID:
                return END_TAG;
            case MQ_ID:
                return MQ_TAG;
            case NX_ID:
                return NX_TAG;
            case TIME_ID:
                return TIME_TAG;
            case X_ID:
                return X_TAG;
            case NT_ID:
                return NT_TAG;
            case NS_ID:
                return NS_TAG;
            case NR_ID:
                return NR_TAG;
            case M_ID:
                return M_TAG;
            default:
                return null;
        }
    }


    /**
     * 构建词典的时候一定把这些放进去
     *
     * @return all label list
     */
    public static List<String> allLabel() {
        return Lists.newArrayList(
                BEGIN_TAG,
                END_TAG,
                MQ_TAG,
                NX_TAG,
                TIME_TAG,
                X_TAG,
                NT_TAG,
                NS_TAG,
                NR_TAG,
                M_TAG
        );
    }

    /**
     * 句子的开始 begin
     */
    public final static String BEGIN_TAG = "\u00010Begin";

    /**
     * 结束 end
     */
    public final static String END_TAG = "\u00011End";

    /**
     * 数量词 mq （现在觉得应该和数词同等处理，比如一个人和一人都是合理的）
     */
    public final static String MQ_TAG = "\u00012MQ";

    /**
     * nx 中文语料里面出现了英文单词。
     * 不能叫专有名词
     */
    public final static String NX_TAG = "\u00013NX";

    /**
     * 时间 t
     */
    public final static String TIME_TAG = "\u00014Time";

    /**
     * 字符串 x
     */
    public final static String X_TAG = "\u00015String";

    /**
     * 团体名词 组织机构 nt
     */
    public final static String NT_TAG = "\u00016NT团体";

    /**
     * 地址 ns
     */
    public final static String NS_TAG = "\u00017NS地名";
    /**
     * 人名 nr
     */
    public final static String NR_TAG = "\u00018NR人名";


    /**
     * 数词 m
     */
    public final static String M_TAG = "\u00019Num";

}
