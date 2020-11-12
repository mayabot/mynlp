package com.mayabot.nlp.common;

/**
 * @author jimichan
 */
public class TagAndScore {

    private String tag;

    private float score;

    public TagAndScore(String tag, float score) {
        this.tag = tag;
        this.score = score;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
