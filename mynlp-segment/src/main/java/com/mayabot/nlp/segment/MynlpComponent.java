package com.mayabot.nlp.segment;

/**
 * 分词组件需要有个Name和设置是否启用的
 * @author jimichan
 */
public interface MynlpComponent {

    /**
     * @return
     */
    String getName();

    /**
     * 组件是否启用。默认返回true，启用
     *
     * @return
     */
    boolean isEnabled();

    void setEnabled(boolean enable);
}
