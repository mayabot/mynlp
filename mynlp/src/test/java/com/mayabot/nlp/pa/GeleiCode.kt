package com.mayabot.nlp.pa

import kotlin.math.pow


//void GrayCode(int n, string *data)
//{
//    if (n == 1)//终止条件，先生成1位的格雷码
//    {
//        data[0] = "0";
//        data[1] = "1";
//        return;
//    }
//    GrayCode(n - 1, data);//生成n位的格雷码首先需要生成n-1的格雷码
//    int len = (int)pow(2, n);
//    for (int i = len / 2; i < len; i++)//先处理后半部分，注意对称
//    {
//        data[i] = "1" + data[len - i - 1];
//    }
//    for (int i = 0; i < len / 2; i++)//对于前半部分直接+'0'
//    {
//        data[i] = "0" + data[i];
//    }

fun grapCode(n: Int, data: Array<String?>) {
    if (n == 1) {
        data[0] = "0"
        data[1] = "1"
        return
    }
    grapCode(n - 1, data)
    val len = 2.0.pow(n).toInt()
    for (i in len / 2 until len) {
        data[i] = "1" + data[len - i - 1]
    }
    for (i in 0 until len / 2) {
        data[i] = "0" + data[i]
    }
}

fun main() {
    val n = 5
    val data = Array<String?>(2.0.pow(n * 1.0).toInt()) { null }
    grapCode(n, data)

    var i = 0
    for (line in data) {
        println("$i\t" + line!!.padStart(n, '0'))
        i++
    }
}