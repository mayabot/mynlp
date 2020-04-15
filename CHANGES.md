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
    
# 3.1.1
- fix 标点符号过滤bug