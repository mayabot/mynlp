MYNLP是个自然语言处理工具包

#### MYNLP的功能模块

* 中文分词
* 拼音转换
* 简繁体转换
* Fasttext的Java高性能实现（支持GPU,兼容原版C语言的模型文件）[fasttext](https://fasttext.cc/)
* mynlp-news-classification 新闻分类器
* mynlp-lang-detection 语言探测器
* mynlp-cli 命令行工具
    * CRF语料工具
    * 分词工具

MYNLP设计之初的一个重要目标就是基于类似Elasticsearch项目的模块化和插件化体系，来构建整个NLP工具组。
n-Gram+HMM的中文分词
* CRF中文分词
* 命名实体识别（人名、地名、组织机构）
* 词性识别
* 基于WordNet和WordPath的分词体系
* 基于Pipleline的设计，你可以自由组合各种分词插件和逻辑，满足不同场景的需求
* 自由的插件体系，可以根据业务控制分词逻辑
* FST工具，让你简化模式识别的代码逻辑
* 内置歧义词典，高效纠错
* 正则表达式分词逻辑
* 自由的词典和自定义词典加载方式：动态、增量从数据库或者其他资源系统加载数据
* 项目使用Guice进行模块依赖和反转注入，代码逻辑清晰，降低开发人员阅读代码和编写插件的难度

另外MYNLP提供开箱即用的Elasticsearch、Solr插件，all-in-one的jar在Spark等环境中也同样适用      

#Getting Started

分词系统特点
    1. 使用Guice进行组件反转注入
    2. 开发的插件体系，可以控制分词过程，安装业务要求影响分词结果
    3. 基于FST（有限自动状态机）

```java
int a = 0;

```
