package com.example.ai_test

import com.lowagie.text.pdf.PdfWriter
import org.jsoup.Jsoup
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.FileOutputStream
import java.net.URI
import java.util.stream.Collectors


@RestController
class RagController {

    @Autowired
    private lateinit var chatModel: ChatModel

    @Autowired
    private lateinit var vectorStore: VectorStore


    fun t2(): String {
        val document: com.lowagie.text.Document = com.lowagie.text.Document()
        PdfWriter.getInstance(document, FileOutputStream("HtmlToPdf.pdf"))

        document.open()

        val html = getHtml()

        val parse = Jsoup.parse(html)
        val select = parse.getElementsByClass("website-content")

        return select.text()

    }

    fun getHtml(): String {
        val url = URI.create("https://www.tiobe.com/tiobe-index/").toURL()
        val `is` = url.openStream()
        var ptr = 0
        val buffer = StringBuffer()
        while ((`is`.read().also { ptr = it }) != -1) {
            buffer.append(ptr.toChar())
        }
        return buffer.toString()
    }

    @GetMapping("/lama/rag")
    fun generateAnswer(@RequestParam query: String): ResponseEntity<String> {
        val similarDocuments: List<Document> = vectorStore.similaritySearch(query)
        val information = similarDocuments.stream()
            .map(Document::getContent)
            .collect(Collectors.joining(System.lineSeparator()));

        val systemPromptTemplate = SystemPromptTemplate(
            """
                            Your name is Bjoern, always introduce yourself at the beginning of a reply.
                            You are a medical advisor. You should give an overview of an information provided to you.
                            Do not use any other information. If you do not know the answer, only answer exactly: UNKNOWN.
                            Use ONLY the following information to answer the question:
                            {information}
                        """.trimIndent()
        )
        var systemMessage = systemPromptTemplate.createMessage(mapOf("information" to information));
        var userPromptTemplate = PromptTemplate("{query}");
        var userMessage = userPromptTemplate.createMessage(mapOf("query" to query));
        var prompt = Prompt(listOf(systemMessage, userMessage));

        return ResponseEntity.ok(chatModel.call(prompt).getResult().output.content)
    }

}