package com.mayabot.nlp.segment.wordnet;

/**
 * Wordnet是一个在分词使用的数据结构。
 * 所谓词图，指的是句子中所有词可能构成的图。
 *
 * 这里提供了优化的Wordnet的实现，更快的速度、更低的内存、尽量zero-copy。
 *
 * 还提供了Wordpath数据结构，wordpath采用bitSet去实现对选中路径的描述，避免和wordnet数据结构的纠缠，
 * 让规则程序更容易去进行重新划分词语，为识别器和业务规则的编码带来便利，降低了程序复杂度。
 * @author jimichan 
 **/