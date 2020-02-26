# FastText4j

FastText4j implementing FastText with Kotlin&Java.
[Fasttext](https://github.com/facebookresearch/fastText/) is a library for text representation and classification by facebookresearch.

FastText4j是java&kotlin开发的fasttext算法库。[Fasttext](https://github.com/facebookresearch/fastText/) 是由facebookresearch开发的一个文本分类和词向量的库。

代码迁移至Mynlp项目 [https://github.com/mayabot/mynlp/tree/master/fasttext](https://github.com/mayabot/mynlp/tree/master/fasttext) 。

New code move to Mynlp project [https://github.com/mayabot/mynlp/tree/master/fasttext](https://github.com/mayabot/mynlp/tree/master/fasttext)
   
Features:

 * Implementing with java(kotlin)
 * Well-designed API
 * Compatible with original C++ model file (include quantizer compression model)
 * Provides train、test etc. api (almost the same performance)
 * Support for java file formats( can read file use mmap),read big model file with less memory
 
Features:

 * 100%由kotlin&java实现
 * 良好的API
 * 兼容官方原版的预训练模型
 * 提供所有的包括train、test等api
 * 支持自有模型存储格式，可以使用MMAP快速加载大模型


## Installing

### Gradle
```
compile 'com.mayabot.mynlp:fastText4j:3.1.0'
```

### Maven
```xml
<dependency>
  <groupId>com.mayabot.mynlp</groupId>
  <artifactId>fastText4j</artifactId>
  <version>3.1.0</version>
</dependency>
```

## API

### Train model | 训练模型

#### 1. train Text classification model | 训练文本分类模型

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
训练数据格式:

where train.txt is a text file containing a training sentence per line along with the labels. By default, we assume that labels are words that are prefixed by the string __label__. This will output two files: model.bin and model.vec. Once the model was trained, you can evaluate it by computing the precision and recall at k (P@k and R@k) on a test set using:

训练数据是个纯文本文件，每一行一条数据，词之间使用空格分开，每一行必须包含至少一个label标签。默认
情况下，是一个带`__label__`前缀的字符串。
> __label__tag1  saints rally to beat 49ers the new orleans saints survived it all hurricane ivan
> 
> __label__积极  这个 商品 很 好 用 。 



#### 2. word representation learning |  词向量学习 

支持cow和Skipgram两种模型

```java
FastText.trainCow(file,inputArgs)
//Or
FastText.trainSkipgram(file,inputArgs)
```

### Test model
```java
File trainFile = new File("data/agnews/ag.train");
InputArgs inputArgs = new InputArgs();
inputArgs.setLoss(LossName.softmax);
inputArgs.setLr(0.1);
inputArgs.setDim(100);

FastText model = FastText.trainSupervised(trainFile, inputArgs);

model.test(new File("data/agnews/ag.test"),1,0,true);
```

output:

```
F1-Score : 0.968954 Precision : 0.960683 Recall : 0.977368  __label__2
F1-Score : 0.882043 Precision : 0.882508 Recall : 0.881579  __label__3
F1-Score : 0.890173 Precision : 0.888772 Recall : 0.891579  __label__4
F1-Score : 0.917353 Precision : 0.926463 Recall : 0.908421  __label__1
N	7600
P@1	0.915
R@1	0.915
```


### Save model | 保存模型文件

```java
FastText model = FastText.trainSupervised(trainFile, inputArgs);
model.saveModel(new File("path/data.model"));
```

### Load model | 加载模型

```java
//load from java format 
FastText model = FastText.Companion.loadModel(new File(""),false);
```

```java
//load from c++ format
FastText model = FastText.Companion.loadCppModel(new File("path/wiki.bin"))
```

### Quantizer compression | 乘积量化压缩
    分类的模型可以压缩模型体积

```java
//load from java format 
FastText qmodel = model.quantize(2, false, false);
```


### Predict | 预测分类
```java
List<ScoreLabelPair> result = model.predict(Arrays.asList("fastText 在 预测 标签 时 使用 了 非线性 激活 函数".split(" ")), 5,0);
```

### Nearest Neighbor Search | 词向量近邻
```java
List<ScoreLabelPair> result = model.nearestNeighbor("中国",5);
```

### Analogies | 类比
By giving three words A, B and C, return the nearest words in terms of semantic distance and their similarity list, under the condition of (A - B + C).
```java
List<ScoreLabelPair> result = fastText.analogies("国王","皇后","男",5);
```

### Parameter | 参数
`InputArgs`可以设置各种参数，兼容fasttext原版参数。


```
$ ./fasttext supervised
Empty input or output path.

The following arguments are mandatory:
  -input              training file path
  -output             output file path

The following arguments are optional:
  -verbose            verbosity level [2]

The following arguments for the dictionary are optional:
  -minCount           minimal number of word occurrences [1]
  -minCountLabel      minimal number of label occurrences [0]
  -wordNgrams         max length of word ngram [1]
  -bucket             number of buckets [2000000]
  -minn               min length of char ngram [0]
  -maxn               max length of char ngram [0]
  -t                  sampling threshold [0.0001]
  -label              labels prefix [__label__]

The following arguments for training are optional:
  -lr                 learning rate [0.1]
  -lrUpdateRate       change the rate of updates for the learning rate [100]
  -dim                size of word vectors [100]
  -ws                 size of the context window [5]
  -epoch              number of epochs [5]
  -neg                number of negatives sampled [5]
  -loss               loss function {ns, hs, softmax} [softmax]
  -thread             number of threads [12]
  -pretrainedVectors  pretrained word vectors for supervised learning []
  -saveOutput         whether output params should be saved [0]

The following arguments for quantization are optional:
  -cutoff             number of words and ngrams to retain [0]
  -retrain            finetune embeddings if a cutoff is applied [0]
  -qnorm              quantizing the norm separately [0]
  -qout               quantizing the classifier [0]
  -dsub               size of each sub-vector [2]
```

Defaults may vary by mode. (Word-representation modes `skipgram` and `cbow` use a default `-minCount` of 5.)


## Resource
### Official pre-trained model
- Recent state-of-the-art [English word vectors](https://fasttext.cc/docs/en/english-vectors.html).
- Word vectors for [157 languages trained on Wikipedia and Crawl](https://github.com/facebookresearch/fastText/blob/master/docs/crawl-vectors.md).
- Models for [language identification](https://fasttext.cc/docs/en/language-identification.html#content) and [various supervised tasks](https://fasttext.cc/docs/en/supervised-models.html#content).
