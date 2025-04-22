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

//        first line of a file is a sheet name, the
//        second line is a columns name of a sheet, and after that comes data.

        val systemPromptTemplate = SystemPromptTemplate(
            """
                            You are a software which purpose is to compare excels files that are stored in CSV format.
                            Sheet name is stored in a tag '<SHEET_NAME>' also as EXCEL_FILE_NAME. Multiple Sheets may 
                            belong to one EXCEL file.                
                            On the next line stored column names separated by ';' and on the next lines sored data for
                             the sheet separated by ';'. Every sheet of each file is separated by a tag <END_OF_SHEET>.
                            You should respond with data in ASCII table structure formatting.
                            Do not use any other information. If you do not know the answer, only answer exactly: UNKNOWN.
                            Use ONLY the following information to find a difference:
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