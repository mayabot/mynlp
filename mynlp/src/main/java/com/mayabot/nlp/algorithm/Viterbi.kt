package com.mayabot.nlp.algorithm

/**
 * 实现一个通用的Viterbi算法框架。
 *
 * window决定输出结果的观察窗口的大小.如果window=Integer.Max，那么表示需要观察到最后一个元素才能出结果
 *
 * 输入一个有限长度的序列
 *
 * 通过Collector接口发射结果，可以和Flow冷流结合产生一个新的接口。
 */

abstract class Viterbi<Obs, HidStatus>(
    val window: Int = Int.MAX_VALUE
) {

    init {
        if (window != Int.MAX_VALUE) {
            check(window > 0)
        }
    }

    /**
     * block版本，迭代所有obs列表里面的数据，decode方法才返回
     */
    fun decode(obsList: Iterable<Obs>, collector: Collector<HidStatus>) {
        var cost1 = CostRow<HidStatus, Obs>()
        var cost2 = CostRow<HidStatus, Obs>()

        val obsIterator = obsList.iterator()


        // day 0 初始权重
        fillDay0Weight(hidden(obsIterator.next())).forEach { s, weight ->
            cost1[s] = Path(s, weight)
        }

        var time = 0

        while (obsIterator.hasNext()) {
            val obs = obsIterator.next()

            val t_1_weight = cost1
            val t_weight = cost2

            // 今日有哪些可选状态
            val todayStatus = hidden(obs)

            for (t_s in todayStatus) {
                // 今天的某个状态和前一天的每个状态去比较，选出今天当前状态t_s，和前一天哪个状态距离最短
                var minWeight = Double.MAX_VALUE
                var bestLastDayStatus: HidStatus? = null
                for (t_1_s in t_1_weight.keys()) {
                    val t_1_weight = t_1_weight[t_1_s].weight
                    val weight = weight(t_1_weight, obs, t_s, t_1_s)
                    if (weight < minWeight) {
                        minWeight = weight
                        bestLastDayStatus = t_1_s
                    }
                }
                t_weight[t_s] = t_1_weight[bestLastDayStatus!!].append(t_s, minWeight)
            }


            time++

            //swap
            val temp = cost1
            cost1 = cost2
            cost2 = temp
            cost2.clear()

            // 0    1   2   3   4   5   6   7   8   9
            // *    ~   ~   ↑

            // 判断是不是可以出结果
            if (window != Int.MAX_VALUE) {

            }
        }

        if (window == Int.MAX_VALUE) {
            // 选择cost1,输出Path全部内容

        } else {
            // 把剩余的path里面的内容输出，不用一个一个PoP
        }
    }

    class CostRow<HidStatus, Obs>() {

        // 可以是基于Map的实现，也可以是基于Array的实现
        operator fun get(status: HidStatus): Path<HidStatus> {
            TODO()
        }

        operator fun set(status: HidStatus, path: Path<HidStatus>) {
            TODO()
        }

        fun keys(): List<HidStatus> {
            TODO()
        }

        fun shotPath():Path<HidStatus> {
            var weight = Double.MAX_VALUE
            var minPath: Path<HidStatus>? = null
            for (key in keys()) {
                val path = this[key]
                if(path.weight<weight){
                    minPath = path
                    weight = path.weight
                }
            }
            return minPath!!
        }

        fun clear() {

        }
    }

    class Path<H>(
        lastValue: H,
        val weight: Double,
        prefix: Node<H>? = null
    ) {

        private val tail: Node<H> = Node(lastValue,prefix)

        /**
         * 返回并删除第一个Node
         */
        fun popFirst():H{
            if(tail.last == null){
                error("最后一个Node不能PoP")
            }

            var point1: Node<H> = tail.last!!
            var point2 = tail
            while(point1.last!=null){
                point2 = point1
                point1 = point1.last!!
            }
            // point1.last == null
            point2.last = null
            return  point1?.value
        }

        /**
         * 返回一个新的Path对象
         */
        fun append(v: H, weight: Double): Path<H> {
            return Path(v, weight, this.tail)
        }

        /**
         * 正向的LIST
         */
        fun toList(size: Int): List<H> {
            val list = ArrayList<H>(size)

            var point = tail
            for (i in size - 1..0) {
                list[i] = point.value
                point = point.last!!
            }
            return list
        }

        class Node<V>(
            val value: V,
            var last: Node<V>? = null
        )
    }



    abstract fun weight(t_1_weight: Double, obs: Obs, t_s: HidStatus, t_1_s: HidStatus): Double


    /**
     * 首日，所有状态的权重。对应HMM为隐藏状态初始概率
     */
    abstract fun fillDay0Weight(status: List<HidStatus>): Map<HidStatus, Double>

    /**
     * 当前观察的状态下，对应所有可能的隐藏状态。
     * 在经典HMM模型下，可选的隐藏状态和当前OBS无关。
     */
    abstract fun hidden(obs: Obs): List<HidStatus>

    interface Collector<HidStatus> {
        fun status(status: HidStatus)
    }
}

