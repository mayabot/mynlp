package com.mayabot.nlp.perceptron;

/**
 * 序列和标注。作为训练器输入。
 *
 * @author jimichan
 */
public class SequenceLabel<E extends Enum<E>> {

    public int[] sequence;

    public E[] label;

    public SequenceLabel(int[] sequence, E[] label) {
        this.sequence = sequence;
        this.label = label;
    }

    public int[] getSequence() {
        return sequence;
    }

    public SequenceLabel setSequence(int[] sequence) {
        this.sequence = sequence;
        return this;
    }

    public E[] getLabel() {
        return label;
    }

    public SequenceLabel setLabel(E[] label) {
        this.label = label;
        //label[0].ordinal()
        return this;
    }

    public static void main(String[] args) {

        System.out.println(TAG.B.ordinal());
        System.out.println(TAG.M.ordinal());
        System.out.println(TAG.E.ordinal());
        System.out.println(TAG.S.ordinal());
    }

    enum TAG {
        B, M, E, S
    }
}
