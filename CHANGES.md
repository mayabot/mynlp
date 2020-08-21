# 3.2.1
- kotlin update to 1.4.0
- fixbug： injector 单例在key不一致的情况下单例实例化两个对象
- 清理了Setting，彻底去除配置文件
- fix junit test内存溢出的问题

# 3.2.0

- 代码结构做出改变，合并到mynlp单一项目
- fasttext也合并到mynlp中
- mynlp不再自动依赖词典资源，需要独立引入资源
- mynlp-with-res这个自动引入常用资源，可以通过exclude排除不需要的资源
- elasticsearch-plugin将独立项目，支持7.0以上的版本


# 3.1.5
去除Gauva依赖，mynlp只依赖kotlin运行时

# 3.1.2
- FastText 模型保存为单个文件，也可以从单个文件加载
```kotlin
fastText.saveModelToSingleFile(File("fastText4j/data/model.fjbin"))

FastText.loadModelFromSingleFile(File("fastText4j/data/model.fjbin"))
```

# 3.1.1
- fix 标点符号过滤bug

# v3.1.0
- 合并了mynlp-core,mynlp-perceptron,mynlp-segment模块
- 重构了感知机模块，自定义感知机只需要实现一个接口定义
- 感知机分词、词性分析使用新的感知机API
- 开放词性分析在线学习接口；简化词性感知机特征提取函数
- 在规则层面提高人名识别准确性
- 合并fastText4j代码到mynlp项目
    - 按照最新C语言版本fastText重构
    - 新增OneVsAiLoss损失函数
    - 新增test接口
    - fix预测结果数量少一个的bug
    


