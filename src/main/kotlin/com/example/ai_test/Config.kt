package com.example.ai_test

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.IOException
import javax.print.Doc


@Configuration
class Config {

    @Bean
    fun vectorStore(olama: OllamaEmbeddingModel): VectorStore {
        return SimpleVectorStore(olama)
    }

    @Bean
    fun uploadFilesIntoVectorStore(vectorStore: VectorStore): VectorStore {
        val config = PdfDocumentReaderConfig.builder()
            .withPageExtractedTextFormatter(
                ExtractedTextFormatter.builder()
                    .build()
            ).build()


        val excelName = "compare1.xlsx"
        val data: MutableMap<String, Any> =
            iterateAndPrepareData("/Users/glebshvydkov/projects/personal/ai-excel-comp/src/main/resources/${excelName}")
        val sheets = data.map { entry ->
            val castedData = entry.value as List<Array<String?>>
            val sheetString = formatAsCsv(castedData, excelName, entry.key)
            Document(sheetString)
        }

        vectorStore.accept(sheets)

//        vectorStore.accept(sheets)
//        val a =
//        writeCSV(data, "/Users/glebshvydkov/projects/personal/ai-excel-comp/src/main/resources/compare1.csv")


//        val res = ClassPathResource("overivew.pdf")

//        val pdfReader = PagePdfDocumentReader(res, config)

//        val textSplitter = TokenTextSplitter()

//        vectorStore.accept(textSplitter.apply(pdfReader.get()))
//        vectorStore.accept(textSplitter.apply(stringres))
        return vectorStore
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
            for (row in sheet!!) {
                val rowData = arrayOfNulls<String>(row.lastCellNum.toInt())
                for (cn in 0 until row.lastCellNum) {
                    val cell: Cell = row.getCell(cn)
                    rowData[cn] = formatter.formatCellValue(cell)
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
        return "<SHEET_NAME=$sheetName, EXCEL_FILE_NAME=$excelName>\n$sb<END_OF_SHEET>"
    }
}