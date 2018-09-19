package com.mayabot.nlp.perceptron;

/**
 * 序列和标注。作为训练器输入。
 *
 * @author jimichan
 */
public class SequenceLabel {

    public int[] sequence;

    public int[] label;

    public SequenceLabel(int[] sequence, int[] label) {
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

    public int[] getLabel() {
        return label;
    }

    public SequenceLabel setLabel(int[] label) {
        this.label = label;
        return this;
    }
}
