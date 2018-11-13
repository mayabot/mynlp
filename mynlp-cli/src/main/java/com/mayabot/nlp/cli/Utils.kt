package com.mayabot.nlp.cli

import com.google.common.base.Splitter
import java.io.File

fun String.toFile() = File(this)
/**
 * 返回文件夹下所有非隐藏文件
 */
fun File.allFiles() = this.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

fun List<File>.allFiles() = this.flatMap { it.allFiles() }.toList()
/**
 * 一个词项的VO
 */
class Word(var word: String, var pos: String) {

    var subWord: ArrayList<Word> = ArrayList()

    fun hasSub() = !subWord.isEmpty()

    override fun toString(): String {
        return if (hasSub()) {
            val sb = StringBuilder()
            sb.append("[")
            sb.append(subWord.joinToString(separator = " "))
            sb.append("]").append(pos)
            sb.toString()
        } else {
            "$word/$pos"
        }
    }

}

//陈/nr 汝烨/nr 压题/vn 照片/n ：/w [绵阳/ns 高新技术/n 产业/n 开发区/n]nt 内/f [长虹/nz 家电城/n]ns 一角/n 。/w

fun String.parseToFlatWords(): List<Word> {
    return this.parseToWords().flatMap { if (it.hasSub()) it.subWord else listOf(it) }
}

val splitter = Splitter.on(" ").omitEmptyStrings().trimResults()
fun String.parseToWords(): List<Word> {
    if (this.isEmpty()) {
        return listOf()
    }
    val result = ArrayList<Word>()
    val words = splitter.split(this)
    var bigWord: Word? = null

    for (word in words) {
        val x = word.lastIndexOf(']')
        if (word.startsWith("[") && word != "[/w") {
            bigWord = Word("", "")
            val rw = word.substring(1)
            bigWord.subWord.add(rw.simpleWord())


            //开始就结束    [８６３计划/nz]nz
            if (x > 0 && x < word.length) {
                bigWord = null
                val lsp = rw.lastIndexOf("/")
                val siWord = Word(rw.substring(0, lsp), word.substring(lsp + 2, x))
                val siWord2 = Word(rw.substring(0, lsp), word.substring(x + 1))
                siWord2.subWord.add(siWord)
                result.add(siWord2)
            }

        } else if (bigWord != null && x > 0 && x < word.length) {
            val rw = word.substring(0, x)
            bigWord.subWord.add(rw.simpleWord())
            var ppos = word.substring(x + 1)
            if (ppos.startsWith("/")) {
                ppos = ppos.substring(1)
            }
            bigWord.pos = ppos

            result.add(bigWord)
            bigWord = null

        } else {
            if (bigWord != null) {
                bigWord.subWord.add(word.simpleWord())
            } else {
                result.add(word.simpleWord())
            }
        }
    }
    return result
}

/**
 * 压题/vn
 */
fun String.simpleWord(): Word {
    val sp = this.lastIndexOf('/')
    if (sp < 0) {
        return Word(this, "")
    }
    if (sp == 0) {
        return Word("", this.substring(1))
    }
    return Word(this.substring(0, sp), this.substring(sp + 1, this.length))
}

//不能解析 兵马俑/nn 网站/nn ：/w http/nn :/w ///w www/nn ./w qin/nn ./w com/nn ./w tw/nn
//不能解析 啊/ij ，/w 借用/vv 一下/ad 李长春/nr 先生/nn 那个/dt 三/cd 个/m ，/w 贴近/nn //w 更加/ad 贴近/vv 台湾/nr 的/deg 客观/jj 实际/nn ，/w 更加/ad 贴近/vv 台湾/nr 的/deg 历史/nn 真实/nn ，/w 更加/ad 贴近/vv 或者/cc 试图/vv 贴近/vv 台湾/nr 的/deg 主流/jj 民意/nn ，/w
//不能解析 没有/ve 打折/jj 票/nn 这/dt 一/cd 板块/nn 的/deg 整体/nn 向上/nn 和/cc 底部/nn 抬高/nn 什么/dt ，/w //w 什么/dt 上涨/nn 都/ad 是/vc 空中楼/nn ，/w 水中月/nn ，/w 基础/nn 不/ad 会/vv 牢/va 的/sp ，/w
//不能解析 [breath_noise]/w 你/pn 应该/vv 有/ve 一个/pn ./w
//不能解析 更/ad 详细/jj 资料/nn 请/vv 上网/vv 查询/vv http/nn :/w ///w www/nn ./w pali/nn ./w tpc/nn ./w gov/nn ./w tw/nn //w
//不能解析 [background_speech_/nn 收到/vv _/w 了/as ,/w _/w 收到/vv _/w 了/as ./w ]/nn 哦/ij ,/w 你/pn ,/w 第九/od 号/m ,/w 好象/ad ./w
fun main(args: Array<String>) {
    val line = "因为/c 谁/r 都/d 知道/v ，/w \u007F/ws 按照/p 惯例/n ，/w 每个/r 学期/nt 得/v 大奖/n 的/u 人/n 下/nd 学期/nt 一定/d 会/vu 得到/v 全额/n 奖学金/n 。/w"

    println(line)
    println(line.parseToWords().joinToString(separator = " "))
}