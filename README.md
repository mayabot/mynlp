# MYNLP 中文NLP工具包

![License](https://img.shields.io/github/license/mayabot/mynlp.svg)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/mayabot/mynlp/mynlp-core/maven-metadata.xml.svg)](http://mvnrepository.com/artifact/com.mayabot.mynlp)
[![Latest release](https://img.shields.io/github/release/mayabot/mynlp/all.svg)](https://github.com/mayabot/mynlp/releases/latest)

## 一个高性能、模块化、可扩展的中文NLP工具包

mynlp是一个高性能、模块化、可扩展的中文NLP工具包。内容如下：
- 中文分词
    - CORE分词 
        - _二元语言模型 + viterbi解码算法 + 基础规则（数字、英文、日期..。_
        - 高性能：速度200万+字/秒(_2.6GHz Intel i7_)
    - CWS分词 
    
        _基于感知机序列标注算法 + 基础规则。_
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
- 新词发现(未发布)

    _可小内存上运行大数据集，完美超高性能_
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
    - 统一资源加载
    - 可扩展的资源加载API
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
            - 数据为人民日报2014，通过Hanlp、中科院等多种分词器自动切分对比后。通过10人5个月时间修复、校验
            后获得的分词语料库。（准备在mynlp具备一定用户量之后再开源）

## Getting Started
非常易用，只需要1分钟你就可以体验mynlp。没有配置，无需手动下载资源文件，只需要依赖添加依赖包，写两行代码。

### Requirements
您需要 JAVA1.8+ 运行环境

### Installation

mynlp的jar已经发布到Maven中央仓库，在您的项目中依赖mynlp-all.jar最新版本。

GRADLE
```
    compile 'com.mayabot.mynlp:mynlp-all:2.0.0'
```
或者MAVEN
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-all</artifactId>
  <version>2.0.0</version>
</dependency>
```

### 中文分词示例

```java
MynlpTokenizer tokenizer = Tokenizers.coreTokenizer();
Sentence sentence = tokenizer.parse("mynlp是mayabot开源的中文NLP工具包。");
System.out.println(sentence.asWordList());
```
输出：
```text
[mynlp/x, 是/v, mayabot/x, 开源/v, 的/u, 中文/nz, nlp/x, 工具包/n, 。/w]
```

    mynlp的发行jar里面是不包含资源文件的，在运行时如果发现本地mynlp.data目录夹没有对应资源时会自动从cdn上下载资源JAR文件，
    所以第一次运行需要您电脑可以访问互联网。
    默认mynlp.data文件夹的位置在~/.mynlp.data，mac系统下为隐藏文件夹，可以通过Command+Shift+G访问。


详细文档请移步[WIKI](https://github.com/mayabot/mynlp/wiki/Home)

## 联系和交流

MYNLP的发展和持续离不开大家的支持，所以建立了微信群方便大家及时沟通交流，有需要的同学可以添加我微信`jimichan`,备注mynlp。


## 声明和致谢

mynlp开发之初是对Hanlp和ansj项目的重构整理，通过多次迭代和项目需求驱动实践，逐渐发展为目前自有的架构体系。
向HanLP和ansj在中文NLP开源中做出的贡献致敬！

mynlp项目参考了以下项目：
- [HanLP](https://github.com/hankcs/HanLP)
- [ansj_seg](https://github.com/NLPchina/ansj_seg)

