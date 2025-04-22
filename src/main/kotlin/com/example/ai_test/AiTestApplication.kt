package com.example.ai_test

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.stream.Collectors


@SpringBootApplication
class AiTestApplication(
) {

//    @Autowired
//    private lateinit var vectorStore: VectorStore
//
//    @Bean
//    fun applicationRunner(
//        chatModel: ChatModel
//    ): ApplicationRunner {
//
//        return ApplicationRunner { args: ApplicationArguments? ->
//
//
//
//            println()
//
//            val query = "what is a cow"
//
//            val similarDocuments = vectorStore.similaritySearch(query)
//            val information = similarDocuments.stream()
//                .map(Document::getContent)
//                .collect(Collectors.joining(System.lineSeparator()));
//            var systemPromptTemplate = SystemPromptTemplate(
//                """
//                            if you being asked 'what is a cow' answer: 'a car'.
//
//                        """
//            );
//
//            var systemMessage = systemPromptTemplate.createMessage(mapOf("information" to information));
//            var userPromptTemplate = PromptTemplate("{query}");
//            var userMessage = userPromptTemplate.createMessage(mapOf("query" to query));
//            var prompt = Prompt(listOf(systemMessage, userMessage));
//
//            val response: ChatResponse = chatModel.call(
//                prompt
//            )
//
//            println(response.result.output.content)
//        }
//    }

}

fun main(args: Array<String>) {
    runApplication<AiTestApplication>(*args)
}

//
//@SpringBootApplication
//object MusashiApplication {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        SpringApplication.run(MusashiApplication::class.java, *args)
//    }
//}
//
//@Configuration
//internal class AppConfig {
//    @Bean
//    fun vectorStore(embeddingClient: EmbeddingClient?): VectorStore {
//        return SimpleVectorStore(embeddingClient)
//    }
//}