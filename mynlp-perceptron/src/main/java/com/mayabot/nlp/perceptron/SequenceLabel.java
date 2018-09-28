package com.mayabot.nlp.perceptron;

/**
 * 序列和标注。作为训练器输入。
 *
 * @author jimichan
 */
public class SequenceLabel<T>  {

    public T[] sequence;

    public int[] label;

    public SequenceLabel(T[] sequence, int[] label) {
        this.sequence = sequence;
        this.label = label;
    }

    public int length(){
        return sequence.length;
    }

    public T[] getSequence() {
        return sequence;
    }

    public SequenceLabel setSequence(T[] sequence) {
        this.sequence = sequence;
        return this;
    }

    public int[] getLabel() {
        return label;
    }

    public SequenceLabel setLabel(int[] label) {
        this.label = label;
        //label[0].ordinal()
        return this;
    }

}

