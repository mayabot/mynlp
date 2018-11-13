package com.mayabot.nlp.cli

import org.apache.http.NameValuePair
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import java.net.URLEncoder


fun main(args: Array<String>) {
    Help.xx()
}

object Help {

    //Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
//Accept-Encoding: gzip, deflate
//Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
//Cache-Control: max-age=0
//Connection: keep-alive
//Content-Length: 1104
//Content-Type: application/x-www-form-urlencoded
//Cookie: ASP.NET_SessionId=2dpkxqvloauogcpnvbwplu4w; safedog-flow-item=
//Host: corpus.zhonghuayuwen.org
//Origin: http://corpus.zhonghuayuwen.org
//Referer: http://corpus.zhonghuayuwen.org/CnCindex.aspx
//Upgrade-Insecure-Requests: 1
//User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36
    fun xx() {

        val cookie = ArrayList<String>()

        val firstPage = Request.Get("http://corpus.zhonghuayuwen.org/CnCindex.aspx").execute().returnResponse()

        firstPage.allHeaders.forEach {
            if (it.name.startsWith("Set-Cookie")) {
                cookie += it.value.split(";")[0]
            }
        }


        val request = Request.Post("http://corpus.zhonghuayuwen.org/CnCindex.aspx")

        request.addHeader("Cookie", cookie.joinToString(separator = "; "))


//        header.lines()
//                .map { (it.split(": ")[0] to it.split(": ")[1]) }.forEach { pair ->
//                    request.addHeader(pair.first, pair.second)
//                }


        val list = ArrayList<NameValuePair>()


        var target = ""
        var ARGUMENT = ""
        var keyword = "服务"
        var pageSize = 10

//        list += BasicNameValuePair("__EVENTTARGET", encodeUrl(target))
//        list += BasicNameValuePair("__EVENTARGUMENT", encodeUrl(ARGUMENT))
        //跳转到第多少页
        //list += BasicNameValuePair("txtTopage", "") // 这个和target一起出现

        val dom = Jsoup.parse(EntityUtils.toString(firstPage.entity, Charsets.UTF_8))
        for (element in dom.select("input")) {
            val name = element.attr("name")
            val value = element.attr("value")
            if (name == "__VIEWSTATE" || name == "__VIEWSTATEGENERATOR" || name == "__EVENTVALIDATION") {
                list += BasicNameValuePair(name, encodeUrl(value))
            }
        }


        list += BasicNameValuePair("TextBoxCCkeywords", encodeUrl(keyword))
        list += BasicNameValuePair("DropDownListPsize", pageSize.toString())

        list += BasicNameValuePair("Button1", encodeUrl("检  索"))


        //整词匹配
        list += BasicNameValuePair("1", "RBindexword")
        //标注语料
        list += BasicNameValuePair("2", "RadioButton4")


//        list.forEach { println(it) }

        val body = list.joinToString(separator = "&")
        println(body)
        request.bodyString(body, ContentType.create("application/x-www-form-urlencoded", "UTF-8"))


        val response = request.execute()

        println(response.returnContent().asString(Charsets.UTF_8))

    }

    fun encodeUrl(s: String): String {
        return URLEncoder.encode(s, "UTF-8")

    }
}
//__EVENTTARGET: LBnextpageTop
//__EVENTARGUMENT:
//__VIEWSTATE: /wEPDwUKLTQzNTczMjk0OA9kFgICAw9kFgQCEw8PFgIeB1Zpc2libGVnZBYMAgMPDxYCHgRUZXh0BTnnrKwx5YiwMTDmnaHvvIzlhbHmn6Xor6LliLAxODk1MOadoeespuWQiOimgeaxgueahOS+i+WPpSFkZAIFDw8WAh8AaGRkAgcPDxYCHwBoZGQCCQ8PFgIfAGdkZAILDw8WAh8AZ2RkAg0PDxYCHwBnZGQCFg8PFgIfAGdkFg4CAQ8PFgIfAGhkZAIDDw8WAh8AaGRkAgUPDxYCHwBnZGQCBw8PFgIfAGdkZAIJDw8WAh8BBQExZGQCCw8PFgIfAQUEMTg5NWRkAg0PDxYCHwEFBTE4OTUwZGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgoFC1JCaW5kZXh3b3JkBQpSQmxpa2Vtb2RlBQpSQmxpa2Vtb2RlBQ5SQmZ1bGx0ZXh0bW9kZQUOUkJmdWxsdGV4dG1vZGUFDFJhZGlvQnV0dG9uMwUMUmFkaW9CdXR0b24zBQxSYWRpb0J1dHRvbjQFDkNoZWNrQm94Q2h1Y2h1BRBDaGVja0JveEtXSUNtb2RlR0C1odwFlbNfRKY50ekd7qZWXAOQBDm0pIbd+AOEm+w=
//__VIEWSTATEGENERATOR: 3A0BE18D
//__EVENTVALIDATION: /wEWFQLr3s/aCgLYiuv/CwLzuO7zDQL3uO7zDQLV+YmkCgLZ+YmkCgKM54rGBgK8u9naBwKJlM7DBwKAg8rcDgKWzvT1CAKWzuCuBwK2q5qHDgK/xfDTAQLxqL+hAgLCpJSTBQKKn9X3AwKLlOLCBgLc/9LTBQL3t9jyBALZu+PjB4qvaR7I+G0w9e8gY8iZvCgkKojgzCQQ3zAymaQJTUs0
//TextBoxCCkeywords: 没有
//DropDownListPsize: 10
//1: RBindexword
//2: RadioButton4
//txtTopage: