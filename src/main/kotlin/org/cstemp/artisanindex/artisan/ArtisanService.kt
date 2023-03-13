package org.cstemp.artisanindex.artisan

import jakarta.persistence.EntityManager
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.cstemp.artisanindex.dto.ArtisanRequest
import org.cstemp.artisanindex.dto.ArtisanResponse
import org.cstemp.artisanindex.programme.ArtisanProgramme
import org.cstemp.artisanindex.programme.Programme
import org.cstemp.artisanindex.programme.ProgrammeRepo
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.cstemp.artisanindex.programme.ArtisanProgrameRepo


@Service
class ArtisanService(private val artisanRepo: ArtisanRepo, private val programmeRepo: ProgrammeRepo, private val artisanProgrammeRepo: ArtisanProgrameRepo, private val entityManager: EntityManager) {
    fun searchArtisansByProgrammeTitle(programmeTitle: String): List<Artisan> {
        val query = entityManager.createQuery(
            "SELECT a FROM Artisan a JOIN a.programmes p WHERE p.title = :programmeTitle",
            Artisan::class.java
        )
        query.setParameter("programmeTitle", programmeTitle)
        return query.resultList
    }

    @Transactional
    fun saveArtisanWithProgrammes(artisan: Artisan, programmes: List<Programme?>) {
        // Save the Artisan
        val savedArtisan = artisanRepo.save(artisan)
        // Save the Programmes
        val savedProgrammes = programmeRepo.saveAll(programmes)
        // Associate the Artisan with the Programmes
        savedProgrammes.forEach { programme ->
            val artisanProgramme = ArtisanProgramme().apply {
                this.artisan = savedArtisan
                this.programme = programme
            }
            artisanProgrammeRepo.save(artisanProgramme)
        }
    }


    fun create(body: ArtisanRequest): ResponseEntity<*> {
        if(body != null){
            var artisan = Artisan()
            artisan.fullName = body.fullName
            if(body.gender.isNotEmpty() && body.gender.trim().lowercase().startsWith("male")){
                artisan.gender = AppConstants.Gender.MALE
            } else if(body.gender.isNotEmpty() && body.gender.trim().lowercase().startsWith("female")){
                artisan.gender = AppConstants.Gender.MALE
            } else {
                artisan.gender = AppConstants.Gender.OTHER
            }
            artisan.city = body.city
            artisan.state = body.state
            artisan.phoneNumber = body.phoneNumber
            artisan.trade = body.trade

            val programme = Programme()
            programme.title = body.programme
            when {
                Regex("\\w+").matches( body.batchYear.trim()) ->  programme.batch = body.batchYear
                """^\d{4}$""".toRegex().matches( body.batchYear.trim()) ->  programme.year = body.batchYear
            }

            try {
                saveArtisanWithProgrammes(artisan, listOf(programme))

            } catch (e: Exception) {
               return ResponseEntity.badRequest().body( e.message)
            }
            return ResponseEntity.ok().body(artisan)
        }
        return ResponseEntity
            .badRequest()
            .body(null);

    }
    fun fetchAllArtisans(): MutableList<ArtisanResponse> {
        val artisans = artisanRepo.findAll()

        val refinedArtisans = mutableListOf<ArtisanResponse>()
        for(artisan in artisans) {
            val artisanData = ArtisanResponse()
            artisanData.id = artisan.id.toString()
            artisanData.fullName = artisan.fullName.toString()
            artisanData.state = artisan.state.toString()
            artisanData.trade = artisan.trade.toString()
            artisanData.city = artisan.city.toString()
            artisanData.gender = artisan.gender.toString()
            artisanData.phoneNumber = artisan.phoneNumber.toString()


            val stringBuilder = StringBuilder()
            val programmes = artisanProgrammeRepo.findAllByArtisan(artisan)
            for(each in programmes){
                stringBuilder.append(each.programme?.title ?: "")
                stringBuilder.append(",")
                artisanData.batch = each.programme?.batch.toString()
                artisanData.year = each.programme?.year.toString()
            }
            artisanData.programme = stringBuilder.toString()
//            artisanData.batch =

            refinedArtisans.add(artisanData)
        }
        return refinedArtisans
    }

    fun findByPhoneNumber(phoneNumber: String): Artisan? {
        return entityManager.createQuery("SELECT a FROM Artisan a WHERE a.phoneNumber = :phoneNumber", Artisan::class.java)
            .setParameter("phoneNumber", phoneNumber)
            .resultList
            .firstOrNull()
    }

    fun findProgrammeByArtisan(entityManager: EntityManager, artisan: Artisan): List<Programme> {
        val query = entityManager.createQuery("SELECT p FROM Programme p WHERE p.artisan = :artisan", Programme::class.java)
        query.setParameter("artisan", artisan)
        return query.resultList
    }

    fun saveExcelDataToDatabase(multipartfile: MultipartFile): ResponseEntity<String> {
        val inputStream = multipartfile.inputStream
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val rowIterator: Iterator<Row> = sheet.iterator()
        // skip header row
        if (rowIterator.hasNext()) {
            rowIterator.next()
        }
        while (rowIterator.hasNext()) {
            val row = rowIterator.next()
            val artisan = Artisan()

            artisan.fullName = row.getCell(0).stringCellValue

            if (row.getCell(5) != null && row.getCell(5).stringCellValue.equals("male", ignoreCase = true)) {
                artisan.gender = AppConstants.Gender.MALE
            } else if (row.getCell(5) != null && row.getCell(5).stringCellValue.equals("female", ignoreCase = true)) {
                artisan.gender = AppConstants.Gender.FEMALE
            } else {
                artisan.gender = AppConstants.Gender.OTHER
            }

            val cell = row.getCell(2)?.cellType
            when (cell) {
                CellType.NUMERIC -> artisan.phoneNumber = row.getCell(2).numericCellValue.toLong().toString()
                CellType.STRING -> artisan.phoneNumber = row.getCell(2).stringCellValue
                else -> artisan.phoneNumber = ""
            }

            artisan.city = row.getCell(4).stringCellValue
            artisan.state = row.getCell(3).stringCellValue
            artisan.trade = row.getCell(1).stringCellValue
            var programmes: MutableList<Programme?> = mutableListOf()
            if (row.getCell(6).stringCellValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray().size > 1) {

                for (s in row.getCell(6).stringCellValue.split(",")) {
                    val programme = Programme()
                    programme.title = s

                    val cell2 = row.getCell(7)?.cellType
                    when (cell2) {
                        CellType.NUMERIC -> {
                            when {
                                Regex("\\w+").matches(
                                    row.getCell(7).numericCellValue.toString().trim()
                                ) -> programme.batch = row.getCell(7).numericCellValue.toString().trim()

                                """^\d{4}$""".toRegex().matches(
                                    (row.getCell(7).numericCellValue.toString().trim())
                                ) -> programme.year = row.getCell(7).numericCellValue.toString().trim()
                            }
                        }

                        CellType.STRING -> {
                            when {
                                Regex("\\w+").matches(row.getCell(7).stringCellValue.trim()) -> programme.batch =
                                    row.getCell(7).stringCellValue.trim()

                                """^\d{4}$""".toRegex()matches((row.getCell(7).stringCellValue.trim())) -> programme.year =
                                    row.getCell(7).stringCellValue.trim()
                            }
                        }

                        else -> {
                            programme.year = ""
                            programme.batch = ""
                        }
                    }
                    programmes.add(programme)
                }

            } else {
                val programme = Programme()
                programme.title = row.getCell(6).stringCellValue

                val cell3 = row.getCell(7)?.cellType
                when (cell3) {
                    CellType.NUMERIC -> {
                        when {
                            Regex("\\w+").matches(
                                row.getCell(7).numericCellValue.toString().trim()
                            ) -> programme.batch = row.getCell(7).numericCellValue.toString().trim()

                            """^\d{4}$""".toRegex().matches(
                                (row.getCell(7).numericCellValue.toString().trim())
                            ) -> programme.year = row.getCell(7).numericCellValue.toString().trim()
                        }
                    }

                    CellType.STRING -> {
                        when {
                            Regex("\\w+").matches(row.getCell(7).stringCellValue.trim()) -> programme.batch =
                                row.getCell(7).stringCellValue.trim()

                            """^\d{4}$""".toRegex().matches((row.getCell(7).stringCellValue.trim())) -> programme.year =
                                row.getCell(7).stringCellValue.trim()
                        }
                    }

                    else -> {
                        programme.year = ""
                        programme.batch = ""
                    }
                }

                programmes.add(programme)
            }
            artisan.trade = row.getCell(1).stringCellValue
            try{
                if (findByPhoneNumber(artisan.phoneNumber!!) != null) {
//                    throw Exception("An Artisan with this phone number already exists")
                }
                saveArtisanWithProgrammes(artisan, programmes)
//                    artisanRepo.save(artisan)

            } catch (e: Exception) {
                continue
            }

        }
        return ResponseEntity.ok().body("Data uploaded successfully")
    }

    fun fetchArtisanBySearch(searchInput: String?, selectedValue: String?): List<Artisan?> {
        if (searchInput.isNullOrBlank() && selectedValue.isNullOrBlank()) {
            return emptyList()
        }

        return  when(selectedValue?.trim()) {
            "trade" -> {
                if(searchInput != null)
                    artisanRepo.findByTradeContainingIgnoreCase(searchInput.trim())
                else
                    artisanRepo.findAll()
            }
            "phone" -> {
                if(searchInput != null)
                    artisanRepo.findByPhoneNumber(searchInput.trim())
                else
                    artisanRepo.findAll()
            }
            "state" -> {
                if(searchInput != null)
                    artisanRepo.findByStateContainingIgnoreCase(searchInput.trim())
                else
                    artisanRepo.findAll()
            }
            "fullname" -> {
                if(searchInput != null)
                    artisanRepo.findByFullNameContainingIgnoreCase(searchInput.trim())
                else
                    artisanRepo.findAll()
            }
            "city" -> {
                if(searchInput != null)
                    artisanRepo.findByCityContainingIgnoreCase(searchInput.trim())
                else
                    artisanRepo.findAll()
            }
            "gender" -> {
                when {
                    searchInput != null && searchInput.trim().lowercase().startsWith("male") ->
                        artisanRepo.findByGender(AppConstants.Gender.MALE)
                    searchInput != null && searchInput.lowercase().trim().startsWith("female") ->
                        artisanRepo.findByGender(AppConstants.Gender.FEMALE)
                    else ->
                        artisanRepo.findAll()
                }
            }

            "programme" -> {
                if (searchInput != null) {
                    searchArtisansByProgrammeTitle(searchInput)
                } else {
                    artisanRepo.findAll()
                }
            }
            else -> emptyList()
        }
    }


    fun  getAllArtisanByTradeName(trade: String): ResponseEntity<List<Artisan?>?>{
        val artisans: List<Artisan?>? = artisanRepo.findByTrade(trade)
        return ResponseEntity.ok().body(artisans)
    }

    fun getAllArtisansaAsExcelSpreadSheet(response: HttpServletResponse) {
        val artisans: List<Artisan> = artisanRepo.findAll()
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Artisans Table")
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Full Name")
        headerRow.createCell(1).setCellValue("Trade")
        headerRow.createCell(2).setCellValue("Phone Number")
        headerRow.createCell(3).setCellValue("State")
        headerRow.createCell(4).setCellValue("City")
        headerRow.createCell(5).setCellValue("Gender")
        headerRow.createCell(6).setCellValue("Training Programme")
        headerRow.createCell(7).setCellValue("Batch/Year")

//        Populate row number
        var rowNum = 1
        if (artisans != null) {
            for (rowData in artisans) {
                val dataRow = sheet.createRow(rowNum++)
                dataRow.createCell(0).setCellValue(rowData.fullName)
                dataRow.createCell(1).setCellValue(rowData.trade)
                dataRow.createCell(2).setCellValue(rowData.phoneNumber)
                dataRow.createCell(3).setCellValue(rowData.state)
                dataRow.createCell(4).setCellValue(rowData.city)
                when(rowData.gender){
                    AppConstants.Gender.MALE -> dataRow.createCell(5).setCellValue("Male")
                    AppConstants.Gender.FEMALE -> dataRow.createCell(5).setCellValue("Female")
                    else -> dataRow.createCell(5).setCellValue("Others")
                }

                val stringBuilder = StringBuilder()
                val stringBuilderBY = StringBuilder()
                val programmes = artisanProgrammeRepo.findAllByArtisan(rowData)
                for(each in programmes){
                    stringBuilder.append(each.programme?.title ?: "")
                    if(each.programme?.batch?.isNotEmpty() == true)
                        stringBuilderBY.append(each.programme?.batch?: "")
                    else
                        stringBuilderBY.append(each.programme?.year?: "")
                }
                dataRow.createCell(6).setCellValue(stringBuilder.toString())
                dataRow.createCell(7).setCellValue(stringBuilderBY.toString())
            }
            // Set response headers for Excel file download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=my-table-data.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();

            }
        }

    fun fetchAllArtisanBySearchFilterAndReturnSpreadsheet(searchInput: String?, selectedValue: String?, response: HttpServletResponse ) {
       val artisans = fetchArtisanBySearch(searchInput, selectedValue)
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Filtered Artisans Table")
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Full Name")
        headerRow.createCell(1).setCellValue("Trade")
        headerRow.createCell(2).setCellValue("Phone Number")
        headerRow.createCell(3).setCellValue("State")
        headerRow.createCell(4).setCellValue("City")
        headerRow.createCell(5).setCellValue("Gender")
        headerRow.createCell(6).setCellValue("Training Programme")
        headerRow.createCell(6).setCellValue("Batch/Year")
        var rowNum = 1
        if (artisans != null) {
            for (rowData in artisans) {
                val dataRow = sheet.createRow(rowNum++)
                if (rowData != null) {
                    dataRow.createCell(0).setCellValue(rowData.fullName)

                dataRow.createCell(1).setCellValue(rowData.trade)
                dataRow.createCell(2).setCellValue(rowData.phoneNumber)
                dataRow.createCell(3).setCellValue(rowData.state)
                dataRow.createCell(4).setCellValue(rowData.city)
                when(rowData.gender){
                    AppConstants.Gender.MALE -> dataRow.createCell(5).setCellValue("Male")
                    AppConstants.Gender.FEMALE -> dataRow.createCell(5).setCellValue("Female")
                    else -> dataRow.createCell(5).setCellValue("Others")
                }
                    val stringBuilder = StringBuilder()
                    val stringBuilderBY = StringBuilder()
                    val programmes = artisanProgrammeRepo.findAllByArtisan(rowData)
                    for(each in programmes){
                        stringBuilder.append(each.programme?.title ?: "")
                        if(each.programme?.batch?.isNotEmpty() == true)
                            stringBuilderBY.append(each.programme?.batch?: "")
                        else
                            stringBuilderBY.append(each.programme?.year?: "")
                }
                    dataRow.createCell(6).setCellValue(stringBuilder.toString())
                    dataRow.createCell(7).setCellValue(stringBuilderBY.toString())
                }}
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=my-table-data.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
    }
    }
}
