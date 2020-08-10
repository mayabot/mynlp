package com.mayabot.nlp.module.lucene

import com.mayabot.nlp.segment.Lexers
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.RAMDirectory

fun main() {

    val directory = RAMDirectory()
    val analyzer = MynlpAnalyzer(
            Lexers.coreBuilder().withPersonName().build().filterReader(true, true)
    )
    val iwc = IndexWriterConfig(analyzer)
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
    val writer = IndexWriter(directory, iwc)

    writer.addDocument(Document().apply {
        add(TextField("title", "俞正声主持召开全国政协第五十三次主席会议", Field.Store.YES))
    })
    writer.commit()
    writer.close()

}