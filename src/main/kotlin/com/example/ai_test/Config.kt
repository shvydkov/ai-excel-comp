package com.example.ai_test

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ParameterizedTypeReference
import java.io.FileInputStream
import java.io.IOException


@Configuration
@RegisterReflectionForBinding(DataBucket::class)
class Config {
    @Bean
    fun vectorStore(olama: OllamaEmbeddingModel): VectorStore {
        return SimpleVectorStore(olama)
    }

    @Bean
    fun applicationRunner(
        vectorStore: VectorStore,
        chatClient: ChatClient
    ): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments? ->

            loadExcel("20241210_azit_risk_assessment_master_old.xlsx", vectorStore)
            loadExcel("20250403_azit_risk_assessment_master_new.xlsx", vectorStore)

//            vectorStore.accept(listOf(Document("name: Barky")))

            val prompt = """
					compare all files with name 'icd', the content of file is stored in 'content'
					""".trimIndent()
//            val content: DataBucket? = chatClient.prompt().user(prompt).call().entity(DataBucket::class.java)
            val content: List<DataBucket?>? = chatClient.prompt().user(prompt).call()
                .entity(object : ParameterizedTypeReference<List<DataBucket?>?>() {})

            System.out.println(content)
        }
    }

    @Bean
    fun chatClient(vs: VectorStore?, builder: ChatClient.Builder): ChatClient {
        val system = """
				Your urpose is to ONLY compare documents that are stored. The name of a file is stored in 'name', and it's content in 'content'.
                Do not use any other information. If you do not know the answer, ONLY answer exactly: UNKNOWN.
				""".trimIndent()
        return builder
            .defaultSystem(system)
            .defaultAdvisors(QuestionAnswerAdvisor(vs, SearchRequest.defaults()))
            .build()
    }

//    @Bean
//    fun uploadFilesIntoVectorStore(vectorStore: VectorStore): VectorStore {
////        val config = PdfDocumentReaderConfig.builder()
////            .withPageExtractedTextFormatter(
////                ExtractedTextFormatter.builder()
////                    .build()
////            ).build()
//
//
//        loadExcel("20241210_azit_risk_assessment_master_old.xlsx", vectorStore)
//        loadExcel("20250403_azit_risk_assessment_master_new.xlsx", vectorStore)
//
////        vectorStore.accept(sheets)
////        val a =
////        writeCSV(data, "/Users/glebshvydkov/projects/personal/ai-excel-comp/src/main/resources/compare1.csv")
//
//
////        val res = ClassPathResource("overivew.pdf")
//
////        val pdfReader = PagePdfDocumentReader(res, config)
//
////        val textSplitter = TokenTextSplitter()
//
////        vectorStore.accept(textSplitter.apply(pdfReader.get()))
////        vectorStore.accept(textSplitter.apply(stringres))
//        return vectorStore
//    }

    private fun loadExcel(fileName: String, vectorStore: VectorStore) {
        val excelName = fileName
        val data: MutableMap<String, Any> =
            iterateAndPrepareData("/Users/glebshvydkov/projects/personal/ai-excel-comp/src/main/resources/${excelName}")
        val sheets = data.map { entry ->
            val castedData = entry.value as List<Array<String?>>
            val sheetString = formatAsCsv(castedData, excelName, entry.key)
            Document(sheetString)
        }
        vectorStore.accept(sheets)
    }

    fun openWorkbook(path: String): Workbook {
        val file = FileInputStream(path)
        return WorkbookFactory.create(file)
    }

    @Throws(IOException::class)
    fun iterateAndPrepareData(filePath: String?): MutableMap<String, Any> {

        val dataSheets = emptyMap<String, Any>().toMutableMap()

        val workbook = openWorkbook(filePath!!)
        workbook.sheetIterator().forEach { sheet: Sheet? ->
            val data: MutableList<Array<String?>> = ArrayList()
            val sheetName = sheet?.sheetName!!
            val formatter = DataFormatter()
            for (row in sheet) {
//                try {
//                    arrayOfNulls<String>(row.lastCellNum.toInt())
//                } catch (e: Exception) {
//                    println(e)
//                }
                val size = row.lastCellNum.toInt()
                val rowData = if (size > 0) {
                    arrayOfNulls<String>(size)
                } else {
                    emptyArray()
                }
                for (cn in 0 until row.lastCellNum) {
                    val cell: Cell? = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    if (cell != null) {
                        rowData[cn] = formatter.formatCellValue(cell)
                    }
                }
                data.add(rowData)
                dataSheets[sheetName] = data
            }
            workbook.close()
        }
        return dataSheets
    }

    @Throws(IOException::class)
    fun formatAsCsv(data: List<Array<String?>>, excelName: String, sheetName: String): String {
        val sb = StringBuffer()
        for (row in data) {
            val rowString = row.joinToString(";") { it.toString() }
            sb.append(rowString + "\n")
        }
        return "name: $sheetName, content: $excelName"
//        return "name: $excelName.$sheetName, content: $sb"
//        "<SHEET_NAME=$sheetName, EXCEL_FILE_NAME=$excelName>\n$sb<END_OF_SHEET>"
    }
}