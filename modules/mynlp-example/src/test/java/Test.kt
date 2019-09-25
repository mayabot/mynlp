import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.Lexers
import java.io.StringReader

object Test {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val builrder = Lexers.coreBuilder()
        val lexer = builrder // core分词构建器
                .withPos() // 开启词性
                				.withNer()
                .withCustomDictionary()
//                .keepOriCharOutput()
                .withPersonName() // 开启人名
                .build() // 创建定制词法分析对象
        val time = System.currentTimeMillis()
//
//        val txt = """"	。
//他	认为	,
//第三	次	表决	时	,
//有	可能	出现	通过	基里延科	总理	人选	的	局面	。
//始终	对	基里延科	持	反对	意见	的	"	人民	政权	"	议员团	领导人	雷日科夫	直言不讳	地	说	:
//"	我	不	想	让	国家杜马	被	解散	,
//这	是	最	主要	的	。
//有关	基里延科	的	问题	我	不	打算	再	谈	了	,
//既然	总统	做出	了	决定	,
//总统	就	该	承担	所有	责任	。	"
//"	亚博卢	"	议员团	领导人	亚夫林斯基	表示	,
//"	‘	亚博卢	’	过去	投	了	反对票	,
//现在	仍然	投	反对票	,
//将来	还要	投	反对票	。	"
//
//俄共	议员团	是	国家杜马	中	最	大	的	议员团	,
//观察家	认为	,
//基里延科	24日	能否	闯	过	杜马	关	,
//完全	取决于	俄共	议员团	的	立场	。
//国家杜马	主席	,	俄共	党员	谢列兹尼奥夫	18日	在	俄罗斯	公共	电视台	发表	谈话	时	说	,
//他	认为	,
//于	4月	23日	举行	的	俄共	中央	全会	将	会	建议	国家杜马	共产党	议员	对	基里延科	任	政府	总理	的	提名	投	赞成票	。
//他	表示	,
//他	将	说服	党内	同志	支持	基里延科	。
//他	认为	,
//解散	国家杜马	的	代价	太	大	。
//
//有	分析	说	,
//如果	俄共	真	的	在	中央	全会	上	做出	有关	转变	立场	的	决定	,
//其	后果	将	严重	影响	俄共	在	选民	中	的	形象	及	威信	。
//这	将	是	俄共	领导人	最	不	愿意	接受	的	现实	。
//
//24日	俄罗斯	政坛	究竟	将	出现	什么样	的	局面	?
//俄罗斯	htb	电视台	新闻	节目	主持人	耐人寻味	地	说	:
//"	对	此	我	不	能	做出	评论	,
//因为	世界	上	任何	一个	国家	的	政局	走向	,
//都	不	像	俄罗斯	政局	这样	令	人	难以	预测	。	"
//(	本报	莫斯科	4月	19日	电	)
//
//
//图片	新闻
//
//1	,	朝鲜	和	韩国	首	次	政府	间	直接	会谈	经过	一	周	没有	成果	的	谈判	,
//4月	18日	在	北京	宣布	破裂	。
//当天	上午	,
//双方	代表团	分别	举行	记者	招待会	。
//图	为	朝鲜	代表团	团长	,	政务院	参事	全今哲	在	记者	招待会	上	回答	记者	的	提问	。
//(	新华社	记者	樊如钧	摄	)
//
//2	,	18日	,
//第二	届	美洲	国家	首脑	"""
//        ////
//        lexer.scan(txt) { term ->
//            println(term)
//        }
//
        val st = File("data.work/test/corpus.txt").readText()

        lexer.reader().scan(
                StringReader(st))
                .forEach { x ->

                }

        println("scan耗时:" + (System.currentTimeMillis() - time))

    }

}
