

# Mynlp: 高性能、可扩展的中文NLP工具包

![License](https://img.shields.io/github/license/mayabot/mynlp.svg)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/mayabot/mynlp/mynlp/maven-metadata.xml.svg)](http://mvnrepository.com/artifact/com.mayabot.mynlp)
[![Latest release](https://img.shields.io/github/release/mayabot/mynlp/all.svg)](https://github.com/mayabot/mynlp/releases/latest)


![logo](https://cdn.mayabot.com/mynlp/mynlp-banner.png)

[在线文档 mynlp.mayabot.com（编辑中）](https://mynlp.mayabot.com/)

**项目推广中：如果您喜欢、使用、Clone、发ISSUE时，请先STAR鼓励，谢谢**
> 本项目大部分代码使用kotlin编写，[kotlin语法](https://www.kotlincn.net/docs/reference/basic-syntax.html)简单易上手,从java上手很快
> 采用kotlin的唯一原因是代码可以被大量精简，节省时间！

## 功能与特点：
- 中文分词
    - 感知机分词
    - Core：二元语言模型&词典分词
- 词性标注
- 命名实体识别（人名、地名、组织机构名）
- fastText（100% kotlin实现）
- 文本分类
- 新词发现
- 拼音转换&切分
- 简繁体转换
- Elasticsearch&Lucene插件支持
- 支持用户自主训练模型
- 柔性API --- 自定义&插件机制、支持各种业务场景
- 开发语言
    - JDK 1.8+
    - kotlin
    - java

## 安装

GRADLE
```groovy
compile 'com.mayabot.mynlp:mynlp:3.1.0'
```
MAVEN
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp</artifactId>
  <version>3.1.0</version>
</dependency>
```

> 'com.mayabot.mynlp:mynlp:3.1.0' 中包含主要功能和部分词典资源的依赖。
>
> 其他功能如拼音、fasttext等被独立打包、按需依赖。

## 功能概览

### 词法分析

词法分析包括分词、词性标注、实体识别等过程。

主分词算法包括二元语言模型词典分词、感知机分词器。在Pipeline中额外有
规则切分、词性分析、人名识别、命名实体识别、自定义词典等插件支持。
Pipeline提供强大的柔性API，你可以方便扩展pipeline中的功能、也可以为之开发
全新的基础分词算法。使用Pipeline你可以完全自定义一个适合你需求的分词器。

#### Core分词器
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

#### 感知机分词器：
需要导入模型依赖
<dependency>
  <groupId>com.mayabot.mynlp.resource</groupId>
  <artifactId>mynlp-resource-cws</artifactId>
  <version>1.0.0</version>
</dependency>

```java
Lexer lexer = Lexers
                .perceptronBuilder()
                .withPos()
                .withPersonName()
                .withNer()
                .build();

// or Lexer lexer = Lexers.perceptron(); 返回一个默认的分析器

System.out.println(lexer.scan("2001年，他还在纽约医学院工作时，在英国学术刊物《自然》上发表一篇论文"));

```

输出：
```text
2001年/t ,/w 他/r 还/d 在/p 纽约医学院/nt 工作/n 时/t ,/w 在/p 英国/ns 学术/n 刊物/n 《/w 自然/d 》/w 上/f 发表/v 一/m 篇/q 论文/n
```


#### 示例Pipeline和动态自定义分词
```java
MemCustomDictionary dictionary = new MemCustomDictionary();
dictionary.addWord("逛吃");

//词典生效
dictionary.rebuild();

FluentLexerBuilder builder = Lexers.coreBuilder()
        .withPos()
        .withPersonName();
        
builder.with(new CustomDictionaryPlugin(dictionary));

Lexer lexer = builder.build();

System.out.println(lexer.scan("逛吃行动小组成立"));
```

### 拼音

```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-pinyin</artifactId>
  <version>3.1.0</version>
</dependency>
```

#### 转换中文到对应的拼音
```java
PinyinResult result = Pinyins.convert("招商银行,推出朝朝盈理财产品");

System.out.println(result.asString());
System.out.println(result.asHeadString(","));

// 输出模糊拼音
result.fuzzy(true);
System.out.println(result.fuzzy(true).asString());

//保留标点
result.keepPunctuation(true);
//result.keepAlpha(true);
//result.keepNum(true);
//result.keepOthers(true);

System.out.println(result.asString());
```

输出：
```text
zhao shang yin hang tui chu chao chao ying li cai chan pin
z,s,y,h,t,c,c,c,y,l,c,c,p
zao sang yin han tui cu cao cao yin li cai can pin
zao sang yin han , tui cu cao cao yin li cai can pin
```

> 其他更多自定义拼音词典等功能请参加具体文档。

#### 拼音流切分
把连续输入的拼音切分出来
```java
System.out.println(PinyinSplits.split("nizhidaowozaishuoshenmema"));
```
输出:
```text
[ni, zhi, dao, wo, zai, shuo, shen, me, ma]
```

### FastText
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>fastText4j</artifactId>
  <version>3.1.0</version>
</dependency>
```


```java
File trainFile = new File("data/agnews/ag.train");
InputArgs inputArgs = new InputArgs();
inputArgs.setLoss(LossName.softmax);
inputArgs.setLr(0.1);
inputArgs.setDim(100);
inputArgs.setEpoch(20);

FastText model = FastText.trainSupervised(trainFile, inputArgs);
```

主要参数说明：
- loss 损失函数
    - hs 分层softmax.比完全softmax慢一点。
      分层softmax是完全softmax损失的近似值，它允许有效地训练大量类。
      还请注意，这种损失函数被认为是针对不平衡的label class，即某些label比其他label更多出现在样本。
       如果您的数据集每个label的示例数量均衡，则值得尝试使用负采样损失（-loss ns -neg 100）。
    - ns NegativeSamplingLoss 负采样
    - softmax default for Supervised model
    - ova  one-vs-all 可用于多分类.“OneVsAll” loss function for multi-label classification, which corresponds to the sum of binary cross-entropy computed independently for each label.
- lr 学习率learn rate 
- dim 向量维度
- epoch 迭代次数

> 更多功能参加相关文档。

### 文本分类
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-classification</artifactId>
  <version>3.1.0</version>
</dependency>
```
mynlp采用fasttext算法提供文本分类功能，你可以训练、评估自己的分类模型。

训练数据是个纯文本文件，每一行一条数据，词之间使用空格分开，每一行必须包含至少一个label标签。默认
情况下，是一个带`__label__`前缀的字符串。
> __label__tag1  saints rally to beat 49ers the new orleans saints survived it all hurricane ivan
> 
> __label__积极  这个 商品 很 好 用 。 

所以你的训练语料需要提前进行分词预处理。

在这里查看[一个完整的酒店评论的代码示例](https://github.com/mayabot/mynlp/blob/master/modules/mynlp-classification/src/test/java/com/mayabot/mynlp/HotelCommentExampleTrain.java)

 ```java
// 训练参数
InputArgs trainArgs = new InputArgs();
trainArgs.setLoss(LossName.hs);
trainArgs.setEpoch(10);
trainArgs.setDim(100);
trainArgs.setLr(0.2);

//训练一个分类模型
FastText fastText = FastText.trainSupervised(trainFile, trainArgs);

//使用乘积量化压缩模型
FastText qFastText = fastText.quantize();

//fastText.saveModel("example.data/hotel.model");

//使用测试数据评估模型
fastText.test(testFile,1,0.0f,true);
System.out.println("--------------");
qFastText.test(testFile,1,0.0f,true);
```

```text
Read file build dictionary ...
Read 0M words

Number of words:  14339
Number of labels: 2
Number of wordHash2Id: 19121
Progress: 100.00% words/sec/thread: Infinity arg.loss: 0.22259
Train use time 790 ms
pq 100%
compute_codes...
compute_codes success
F1-Score : 0.915167 Precision : 0.903553 Recall : 0.927083  __label__neg
F1-Score : 0.919708 Precision : 0.931034 Recall : 0.908654  __label__pos
N	400
P@1	0.918
R@1	0.918

--------------

F1-Score : 0.917526 Precision : 0.908163 Recall : 0.927083  __label__neg
F1-Score : 0.922330 Precision : 0.931373 Recall : 0.913462  __label__pos
N	400
P@1	0.920
R@1	0.920
```

### 简繁转换
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-transform</artifactId>
  <version>3.1.0</version>
</dependency>
```

```java
Simplified2Traditional s2t = TransformService.simplified2Traditional();
System.out.println(s2t.transform("软件和体育的艺术"));

Traditional2Simplified t2s = TransformService.traditional2Simplified();
System.out.println(t2s.transform("軟件和體育的藝術"));

```

```text
軟件和體育的藝術
软件和体育的艺术
```

### 简单文本摘要
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-summary</artifactId>
  <version>3.1.0</version>
</dependency>

文本摘要包含了两个简单TextRank的实现。
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>mynlp-summary</artifactId>
  <version>3.1.0</version>
</dependency>
```


关键字摘要

```java
KeywordSummary keywordSummary = new KeywordSummary();
keywordSummary.keyword("text",10);
```

句子摘要
```java
SentenceSummary sentenceSummary = new SentenceSummary();
List<String> result = sentenceSummary.summarySentences(document, 10);
```

KeywordSummary和SentenceSummary内置了默认的分词实现，你可以配置自定义的Lexer对象,参加具体文档。

## 功能模块、词典&模型资源

### Jar
模块 | 功能描述 
------ | ------------
`com.mayabot.mynlp:mynlp:{{<version>}}` | mynlp主要功能和基础算法包
`com.mayabot.mynlp:fastText4j:{{<version>}}` | fastText算法包
`com.mayabot.mynlp:mynlp-classification:{{<version>}}` | 文本分类
`com.mayabot.mynlp:mynlp-lucene:{{<version>}}` | lucene 分析器接口实现
`com.mayabot.mynlp:mynlp-pinyin:{{<version>}}` | 文字转拼音
`com.mayabot.mynlp:mynlp-summary:{{<version>}}` | 文本摘要
`com.mayabot.mynlp:mynlp-transform:{{<version>}}` | 繁简体互转

### 词典和模型资源

mynlp中模型和词典文件被封装在jar中，发布到maven中央仓库。你可以使用`unzip`命令解压查看里面的内容。
> 由于感知机分词模型太大，所以没有被默认依赖。

Gradle地址 |依赖| 大小 |功能描述 
------ | --- | -- | ----
`com.mayabot.mynlp.resource:mynlp-resource-coredict:1.0.0`| Y | 18.2M | 核心词典（词典和二元语言统计|格式：明文）
`com.mayabot.mynlp.resource:mynlp-resource-pos:1.0.0`| Y | 17.5M | 词性标注模型（感知机模型）
`com.mayabot.mynlp.resource:mynlp-resource-ner:1.0.0`| Y | 13.4M | 命名实体识别（感知机模型）
`com.mayabot.mynlp.resource:mynlp-resource-cws:1.0.0`| *N* | 62.4M | 感知机分词模型
`com.mayabot.mynlp.resource:mynlp-resource-custom:1.0.0`| *N* | 2.2M | 补充自定义词库

> 依赖为Y: 表示这些jar资源被`com.mayabot.mynlp:mynlp:{{<version>}}`自动依赖。<br>
> 依赖为N: 如果需要，你可以在maven或者Gradle中引入。 




### 添加微信公众号：加入微信交流群、获取最新NLP相关文章和动态。

![weixin](https://cdn.mayabot.com/mynlp/mayajimi.jpg)

## 声明和致谢
早期在使用ansj和hanlp过程中，遇到很多问题无法解决和扩展，mynlp开发之初借鉴了算法实现，通过多次迭代和项目需求驱动实践，逐渐发展为目前自有的架构体系。
也向HanLP和ansj在中文NLP开源中做出的贡献和努力致敬！

mynlp项目参考了以下项目：
- [HanLP](https://github.com/hankcs/HanLP)
- [ansj_seg](https://github.com/NLPchina/ansj_seg)


