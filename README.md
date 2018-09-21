#MYNLP:中文NLP工具包

![License](https://img.shields.io/github/license/mayabot/mynlp.svg)
[![Latest release](https://img.shields.io/github/release/mayabot/mynlp.svg)](https://github.com/mayabot/mynlp/releases/latest)


mynlp包含：中文分词、词性标注、文本分类（情感分析）、拼音转换、简繁体转换、文本摘要等常见NLP功能。
依托灵活的架构设计、柔性API、高效数据结构，mynlp能在复杂环境中，满足业务需求。算法研究者也可以在mynlp基础上快速开发各种新分算法。

运行需求：
- JRE 1.8+


## 在项目中添加mynlp依赖
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

## 如何使用
    
[API教程、架构设计文档请在WIKI中查看。](https://github.com/mayabot/mynlp/wiki)


## 声明和致谢

mynlp开发之初是对Hanlp和ansj的重构整理，通过多次迭代和项目需求驱动实践，逐渐发展为目前的架构体系。
向HanLP和ansj在中文NLP开源中做出的贡献致敬！

mynlp项目引用或参考了以下项目代码和资源：
- [HanLP](https://github.com/hankcs/HanLP)
- [ansj_seg](https://github.com/NLPchina/ansj_seg)
- [trie4j](https://github.com/takawitter/trie4j)

