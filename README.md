# MYNLP 中文NLP工具包

![License](https://img.shields.io/github/license/mayabot/mynlp.svg)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/mayabot/mynlp/mynlp-core/maven-metadata.xml.svg)](http://mvnrepository.com/artifact/com.mayabot.mynlp)
[![Latest release](https://img.shields.io/github/release/mayabot/mynlp/all.svg)](https://github.com/mayabot/mynlp/releases/latest)

## 一个高性能、模块化、可扩展的中文NLP工具包

[**Wiki文档**](https://github.com/mayabot/mynlp/wiki/Home)

* [Lexer](https://github.com/mayabot/mynlp/wiki/lexer)
  * [Lexer基础架构](https://github.com/mayabot/mynlp/wiki/lexer)
  * 基础分词算法
    * [CORE分词](https://github.com/mayabot/mynlp/wiki/Core)
    * [感知机分词](https://github.com/mayabot/mynlp/wiki/CWS)
    * [原子分词](https://github.com/mayabot/mynlp/wiki/AtomSplitAlgorithm)
  * [Wordnet & Path](https://github.com/mayabot/mynlp/wiki/wordnet)
  * [词性分析](https://github.com/mayabot/mynlp/wiki/POS)
  * [人名识别](https://github.com/mayabot/mynlp/wiki/PersonName)
  * [NER命名实体识别](https://github.com/mayabot/mynlp/wiki/NER)
  * [自定义词典](https://github.com/mayabot/mynlp/wiki/CustomDict)
  * [分词纠错](https://github.com/mayabot/mynlp/wiki/correction)
  * [收集器与索引分词](https://github.com/mayabot/mynlp/wiki/index)
  * [词典和模型资源](https://github.com/mayabot/mynlp/wiki/resources)
  * [自定义分词粒度](https://github.com/mayabot/mynlp/wiki/自定义分词粒度)
* [ElasticSearch插件](https://github.com/mayabot/mynlp/wiki/elasticsearch)
---
* [感知机](https://github.com/mayabot/mynlp/wiki/perceptron)
* [文本分类 & fastText](https://github.com/mayabot/mynlp/wiki/classification)
* [拼音转换](https://github.com/mayabot/mynlp/wiki/Pinyin)
---
* [Guice和Mynlp设置](https://github.com/mayabot/mynlp/wiki/system)
* [日志](https://github.com/mayabot/mynlp/wiki/Logger)
* [资源加载器](https://github.com/mayabot/mynlp/wiki/loader)



**如果你认可MYNLP，欢迎CLONE、FORK、ISSUE，请STAR鼓励一下 ：）**


mynlp是一个开源高性能、模块化、可扩展的中文NLP工具包。

### 设计目标：
- 企业级
- 高性能
- 模块化
- 可扩展
- 多场景
- 柔性API


## Getting Started
只需要导入maven依赖，无需配置和额外资源下载。

### Requirements
您需要 JAVA1.8+ 运行环境

### Installation

`mynlp多个功能被划分在不同的模块中，下面演示分词模块：

GRADLE
```
compile 'com.mayabot.mynlp:mynlp-segment:3.0.1'
```
或者MAVEN
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-segment</artifactId>
  <version>3.0.1</version>
</dependency>
```

模块（artifactId） | 功能 
------ | ------------
mynlp-core | 基础功能 Guice、logger、资源、基础数据结构和算法
mynlp-perceptron | 通用序列标注感知机
mynlp-segment | 分词
mynlp-classification | 文本分类
mynlp-lucene | lucene 分析器接口实现
mynlp-pinyin | 文字转拼音
mynlp-summary | 文本摘要
mynlp-transform | 繁简体互转`

### 中文分词示例

Kotlin:
```kotlin
println("mynlp是mayabot开源的中文NLP工具包。".lexer().toList())
```

Java:
```java
Lexer lexer = Lexers.coreBuilder()      //core分词构建器
                     .withPos()         //开启词性
                     .withPersonName()  //开启人名
                     .build();          // 创建定制词法分析对象
                     
Sentence sentence = lexer.scan("mynlp是mayabot开源的中文NLP工具包。");

System.out.println(sentence.toList());
```

输出：
```text
[mynlp/x, 是/v, mayabot/x, 开源/v, 的/u, 中文/nz, nlp/x, 工具包/n, 。/w]
```

## 添加微信公众号：加入微信交流群、获取最新NLP相关文章和动态。

![weixin](https://cdn.mayabot.com/mynlp/mayajimi.jpg)

## 包含功能和资源如下：
- 中文分词
    - CORE分词 
        - 二元语言模型 + viterbi解码算法 + 基础规则（数字、英文、日期..。_
        - 高性能：速度200万+字/秒(_2.6GHz Intel i7_)
    - 感知机分词 
    
        基于感知机序列标注算法 + 基础规则。_
    - CRF分词 
    
        _高性能CRF解码器 + 基础规则。_
    - 人名识别
    
        _基于感知机实现，高准确率，高性能，自动排除歧义。_
    - NER命名实体识别
    - 自定义功能 
    
        _自定义词库、分词纠错_
    - 柔性API和PIPELINE设计模式
    
        _自由组合和扩展分词逻辑需求_
- 词性标注

    _基于感知机的词性标注_

- 拼音转换

    _文字转拼音，好用的API_
- 文本分类

    _基于[fastText](https://github.com/mayabot/fastText4j) Java原生实现_
- 简繁体转换
- 感知机通用接口

    _提供一个通用的感知机基础API_
- 文本摘要

    _简单的文本摘要实现_
    
- 基础架构
    - 基于GUICE的IOC实现可插拔的组件开发
    - 高性能、易扩展的基础数据结构

- 词典和语料库资源
    - 二元模型
        - 二元模型词数量达20万+
        - 二元接续词配对数量485万+(Hanlp对应资源大概290万+)
    - 感知机模型
        - 训练语料库字数7000万+
    - 词性标注
        - 训练语料库字数3000万+
    - 人名识别
        - 训练语料库字数3000万+
    - 地名、组织机构名
        - 训练语料库字数3000万+ 
    - 语料库来源
        - 公开语料库 
            - 收集的互联网资源
        - 自建语料库
            - 基础数据为人民日报2014全年内容，通过Hanlp、中科院等多种分词器自动切分对比差异，再经过50人月时间修复、校验
            后获得的分词语料库。



## 声明和致谢
早期在使用ansj和hanlp过程中，遇到很多问题无法解决和扩展，mynlp开发之初借鉴了算法实现，通过多次迭代和项目需求驱动实践，逐渐发展为目前自有的架构体系。
也向HanLP和ansj在中文NLP开源中做出的贡献和努力致敬！

mynlp项目参考了以下项目：
- [HanLP](https://github.com/hankcs/HanLP)
- [ansj_seg](https://github.com/NLPchina/ansj_seg)


## 计划

### 跨平台
- Kotlin多平台特性,为 Android、iOS、Linux、Windows、 Mac支持
- 跨平台动态库文件
- 为Python RUST 语言提供支持接口
