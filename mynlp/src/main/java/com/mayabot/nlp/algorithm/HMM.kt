//package com.mayabot.nlp.algorithm
//
//import com.mayabot.nlp.algorithm.Activity.*
//import kotlin.math.ln
//
///**
// * HMM 5元组模型
// */
//class HMMModel<Obs, Hid>(
//    obsEnum: Class<Obs>, hiddenEnum: Class<Hid>,
//    /**
//     * 隐藏状态初始概率
//     */
//    startP: DoubleArray,
//    /**
//     * 隐藏状态转移概率
//     */
//    transP: DoubleArray,
//    /**
//     * 发射概率 （隐状态表现为显状态的概率）
//     */
//    emitP: DoubleArray
//) where Obs : Enum<*>, Hid : Enum<*> {
//
//    private val hiddenLabels = hiddenEnum.enumConstants.toList().sortedBy { it.ordinal }
//
//    private val observeLabels = obsEnum.enumConstants.toList().sortedBy { it.ordinal }
//
//    private val obsStateSize = observeLabels.size
//    private val hidStateSize = hiddenLabels.size
//
//    private val startProb: Array<Double> = startP.map { -ln(it) }.toTypedArray()
//
//    /**
//     * hidSize * hidSize
//     */
//    private val transProb = Array(hidStateSize){ row->
//        DoubleArray(hidStateSize){ cel->
//            -ln(transP[row*hidStateSize+cel])
//        }
//    }
//
//    /**
//     * hidSize * obsSize
//     *      O1  O2  O3
//     * H0:
//     * H1:
//     */
//    private val emitProb =
//        Array(hidStateSize){ row->
//            DoubleArray(obsStateSize){ cel->
//                -ln(emitP[row*obsStateSize+cel])
//            }
//        }
//
//
//    fun decode(obsList: List<Obs>): List<Hid> {
//       //路径概率表 V[时间][隐状态] = 概率
//        val VALUE = Array(obsList.size) { DoubleArray(hidStateSize ) }
//
//        var valueArray1 = DoubleArray(hidStateSize )
//        var valueArray2 = DoubleArray(hidStateSize )
//
//        fun swap(){
//            val temp = valueArray1
//            valueArray1 = valueArray2
//            valueArray2 = temp
//        }
//
//        //# 一个中间变量，代表当前状态是哪个隐状态
//        var path = Array(hidStateSize ) { IntArray(obsList.size) }
//
//        for( y in 0 until hidStateSize){
//            // 初始概率 * 第一天观察状态emit到y概率
//            VALUE[0][y] =  startProb[y] + emitProb[y][obsList[0].ordinal]
//            path[y][0] = y
//        }
//
//        for (t in 1 until obsList.size) {
//            // 行表示隐藏状态，列表示时间
//            val newPath = Array(hidStateSize ) { IntArray(obsList.size) }
//
//            // 双层循环的意思是，每个隐藏状态和昨天的每个隐藏状态去比较，笛卡尔积
//            // 找出今天选择那个隐藏状态，使得到目前为止，概率最大，并记录状态
//            for(y in 0 until hidStateSize){
//                var prob = Double.MAX_VALUE
//                for(yestdayHS in 0 until hidStateSize){
//
//                    //昨天的某个概率 * 发射 * 转移概率
//                    val nprob = VALUE[t-1][yestdayHS] + transProb[yestdayHS][y] + emitProb[y][obsList[t].ordinal]
//                    // 因为 -ln(prob) 所以变成数字越小越好
//                    if (nprob < prob) {
//                        prob = nprob
//
//                        //记录今天如果是y的概率
//                        VALUE[t][y] = prob // V[t][y] 肯定会被赋值，保存了今天每个隐藏状态最佳概率
//
//                        // 记录概率最大情况下，把昨天为止状态复制下来
//                        System.arraycopy(path[yestdayHS],0,newPath[y],0,t)
//                        newPath[y][t] = y
//
//                    }
//                }
//            }
//
//            path = newPath
//
//        }
//
//        var prob = Double.MAX_VALUE
//        var state = 0
//        for(y in 0 until hidStateSize){
//            if(VALUE[obsList.size-1][y]<prob){
//                prob = VALUE[obsList.size-1][y]
//                state = y
//            }
//        }
//        // 6.02431837197906
//        println(prob)
//        return path[state].map { hiddenLabels[it] }
//
//    }
//}
//
////观察
//internal enum class Activity {
//    walk, shop, clean
//}
//
//internal enum class Weather {
//    Rainy, Sunny;
//
//    companion object {
//        fun startP(): DoubleArray {
//            return doubleArrayOf(0.6, 0.4)
//        }
//
//        fun transP(): DoubleArray {
//            return doubleArrayOf(
//                0.7, 0.3,
//                0.4, 0.6
//            )
//        }
//
//        fun emitP(): DoubleArray {
//            return doubleArrayOf(
//                0.1, 0.4, 0.5,
//                0.6, 0.3, 0.1
//            )
//        }
//    }
//
//}
//
//
//fun main() {
//    val model = HMMModel(
//        Activity::class.java,
//        Weather::class.java,
//        Weather.startP(),
//        Weather.transP(),
//        Weather.emitP()
//    )
//    val decode = model.decode(listOf(walk, shop, clean,walk))
//    println(decode)
//}