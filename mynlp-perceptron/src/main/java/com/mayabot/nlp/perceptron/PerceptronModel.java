package com.mayabot.nlp.perceptron;

import java.io.File;

/**
 * 感知机模型
 *
 * @author jimichan
 */
public interface PerceptronModel<E extends Enum<E>> {
    /**
     * 保存感知机模型实例
     *
     * @param file 模型文件的路径
     */
    void save(File file);

    /**
     * 从文件中加载模型
     *
     * @param file
     */
    void load(File file);


    /**
     * 对序列进行解码
     * sequence 如果出现 -1 表示元素不存在
     *
     * @param sequence
     * @return 解码后的label序列
     */
    E[] decode(int[] sequence);
}
