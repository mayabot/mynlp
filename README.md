# MYNLP 中文NLP工具包

![License](https://img.shields.io/github/license/mayabot/mynlp.svg)
[![Latest release](https://img.shields.io/github/release/mayabot/mynlp/all.svg)](https://github.com/mayabot/mynlp/releases/latest)


mynlp包含：中文分词、词性标注、文本分类（情感分析）、拼音转换、简繁体转换、文本摘要等常见NLP功能。
依托灵活的架构设计、柔性API、高效数据结构，mynlp能在复杂环境中，满足业务需求。算法研究者也可以在mynlp基础上快速开发各种新分算法。

环境需求：
- JRE 1.8+
- Gradle or Maven


## 项目中引入mynlp
Mynlp的Maven gourp ID是 `com.mayabot.mynlp` ,不同的功能被分拆在各个artifact里面。

- mynlp-segment 分词
- mynlp-classification 文本分类
- mynlp-pinyin 拼音转换
- mynlp-summary 文本摘要
- mynlp-transform 繁简体转换
- mynlp-core 基本数据结构（被其他模块依赖）

（Fasttext的Java版本实现迁移到独立的项目 [fastText4j](https://github.com/mayabot/fastText4j)）

如需要分词功能就在Mavne中增加如下配置：

```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-segment</artifactId>
  <version>2.0.0-BETA10</version>
</dependency>
```

Gradle:

```gradle
dependencies {
  compile 'com.mayabot.mynlp:mynlp-segment:2.0.0-BETA10'
}
```

## 文档目录
    
* [首页](https://github.com/mayabot/mynlp/wiki/Home)
  * [项目结构](https://github.com/mayabot/mynlp/wiki/Home#项目结构)
  * [Mynlp项目介绍](https://github.com/mayabot/mynlp/wiki/Home#Mynlp项目介绍)
* [5分钟教程](https://github.com/mayabot/mynlp/wiki/QuickTutorial)
  * [分词](https://github.com/mayabot/mynlp/wiki/QuickTutorial#分词)
  * [文本分类](https://github.com/mayabot/mynlp/wiki/QuickTutorial#文本分类)
  * [拼音](https://github.com/mayabot/mynlp/wiki/QuickTutorial#拼音)
  * [简繁转换](https://github.com/mayabot/mynlp/wiki/QuickTutorial#简繁转换)
  * [文本摘要](https://github.com/mayabot/mynlp/wiki/QuickTutorial#文本摘要)
* [中文分词](https://github.com/mayabot/mynlp/wiki/segment)
  * [开箱即用分词器](https://github.com/mayabot/mynlp/wiki/TokenizerBuilderList)
    * [Core分词](https://github.com/mayabot/mynlp/wiki/TokenizerBuilderList#Core分词器)
    * [CRF分词](https://github.com/mayabot/mynlp/wiki/TokenizerBuilderList#CRF分词)
  * [索引分词](https://github.com/mayabot/mynlp/wiki/index)
  * [MynlpAnalyzer](https://github.com/mayabot/mynlp/wiki/MynlpAnalyzer)
  * [基于Wordnet的分词架构图](https://github.com/mayabot/mynlp/wiki/WordnetFramework)
  * [Wordnet和wordpath](https://github.com/mayabot/mynlp/wiki/Wordnet)
  * [分词器Builder API](https://github.com/mayabot/mynlp/wiki/WordnetTokenizerBuilder)
  * [基础组件](https://github.com/mayabot/mynlp/wiki/Component)
      * [CharNormalize](https://github.com/mayabot/mynlp/wiki/Component#CharNormalize)
      * [WordnetInitializer](https://github.com/mayabot/mynlp/wiki/Component#WordnetInitializer)
      * [WordpathProcessor](https://github.com/mayabot/mynlp/wiki/Component#WordpathProcessor)
      * [OptimizeWordPathProcessor](https://github.com/mayabot/mynlp/wiki/Component#OptimizeWordPathProcessor)
      * [WordTermCollector](https://github.com/mayabot/mynlp/wiki/Component#WordTermCollector)
  * [常见案例和解决方案](https://github.com/mayabot/mynlp/wiki/Recipes)
  * [感知机(开发中)](https://github.com/mayabot/mynlp/wiki/perceptron)
* [文本分类](https://github.com/mayabot/mynlp/wiki/classification)
  * [酒店评论情感分析实例](https://github.com/mayabot/mynlp/wiki/classification#酒店评论情感分析实例)
* [词嵌入Fasttext](https://github.com/mayabot/mynlp/wiki/Fasttext)
* [拼音转换](https://github.com/mayabot/mynlp/wiki/Pinyin)
* [资源加载](https://github.com/mayabot/mynlp/wiki/Resouce)
* [GUICE和系统设置](https://github.com/mayabot/mynlp/wiki/Guice)
* [日志](https://github.com/mayabot/mynlp/wiki/Logger)
* [mynlp-cli命令行工具(开发中)](https://github.com/mayabot/mynlp/wiki/CLI)
* [性能对比](https://github.com/mayabot/mynlp/wiki/performance)
* [如何贡献](https://github.com/mayabot/mynlp/wiki/HowToContribute)


如果你对mynlp有任何疑问或建议请加入微信群一起来进行讨论，以帮助Mynlp改进。<br>
<img src="https://cdn.mayabot.com/nlp/wiki-images/wechat.jpg" width="250">


## 声明和致谢

mynlp开发之初是对Hanlp和ansj的重构整理，通过多次迭代和项目需求驱动实践，逐渐发展为目前的架构体系。
向HanLP和ansj在中文NLP开源中做出的贡献致敬！

mynlp项目引用或参考了以下项目代码和资源：
- [HanLP](https://github.com/hankcs/HanLP)
- [ansj_seg](https://github.com/NLPchina/ansj_seg)
- [trie4j](https://github.com/takawitter/trie4j)

