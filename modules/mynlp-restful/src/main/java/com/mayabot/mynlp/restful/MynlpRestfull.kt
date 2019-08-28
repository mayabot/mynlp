package com.mayabot.mynlp.restful

import com.fasterxml.jackson.annotation.JsonInclude
import com.mayabot.nlp.segment.Lexers
import com.mayabot.nlp.segment.WordTerm
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.request.receiveStream
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlin.system.measureNanoTime

fun main() {
    val port = System.getProperty("http.port","6789").toInt()
    val server = embeddedServer(Netty, port) {
        module()
    }

    server.start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        jackson {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    // This feature sets a Date and Server headers automatically.
    install(DefaultHeaders)
    // This feature enables compression automatically when accepted by the client.
    install(Compression)
    // Logs all the requests performed
    install(CallLogging)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for Range, Accept-Range and Content-Range headers
    install(PartialContent)
    // For each GET header, adds an automatic HEAD handler (checks the headers of the requests
    // without actually getting the payload to be more efficient about resources)
    install(AutoHeadResponse)
    // Based on the Accept header, allows to reply with arbitrary objects converting them into JSON
    // when the client accepts it.

    segment()
}

fun Application.segment() {
    routing {

        get("/lexer") {
            val txt = call.parameters["txt"]?:""
            call.respond(
                doSegment(txt)
            )
        }

        post("/lexer") {
            val txt = call.receiveStream().bufferedReader(charset = Charsets.UTF_8).readText()
            call.respond(doSegment(txt))
        }
    }
}

fun doSegment(txt: String) :HttpResult {
    val start = System.nanoTime()
    val res = lexer.scan(txt).toList()
    val end = System.nanoTime()

    val list = res.map {
        val sub = it.subword?.map { Word(it.word, it.offset, it.natureString) }
        Word(it.word, it.offset, it.natureString, sub)
    }

    return HttpResult(true,(end-start)/1000000.0f,list)
}

data class HttpResult(
        val success:Boolean,
        val time:Float,
        val data:Any
)

data class Word(
        val word:String,
        val offset:Int,
        val nature:String,
        val subword: List<Word>? = null
)
val lexer = Lexers.core()