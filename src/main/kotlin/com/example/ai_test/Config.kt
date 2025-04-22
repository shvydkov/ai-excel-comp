package com.example.ai_test

import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.net.URI


@Configuration
class Config {

    @Bean
    fun vectorStore(olama: OllamaEmbeddingModel): VectorStore {
        return SimpleVectorStore(olama)
    }

    @Bean
    fun f1(vectorStore: VectorStore): VectorStore {
        val config = PdfDocumentReaderConfig.builder()
            .withPageExtractedTextFormatter(
                ExtractedTextFormatter.builder()
                    .build()
            ).build()

        val res = ClassPathResource("over2.pdf")
//        val res = ClassPathResource("overivew.pdf")

        val pdfReader = PagePdfDocumentReader(res, config)

        val textSplitter = TokenTextSplitter()

        vectorStore.accept(textSplitter.apply(pdfReader.get()))
//        vectorStore.accept(textSplitter.apply(stringres))
        return vectorStore
    }


}