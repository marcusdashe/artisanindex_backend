package org.cstemp.artisanindex.artisan

import jakarta.persistence.EntityManager
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.cstemp.artisanindex.programme.Programme
import org.cstemp.artisanindex.programme.ProgrammeRepo
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


@Service
class ArtisanService(private val artisanRepo: ArtisanRepo, private val programmeRepo: ProgrammeRepo, private val entityManager: EntityManager) {

    fun fetchArtisanBySearch(searchInput: String?, selectedValue: String?): List<Artisan?>? {
        if (searchInput.isNullOrBlank() && selectedValue.isNullOrBlank()) {
            return null
        }
        var response =  when(selectedValue?.trim()) {
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
                    if(searchInput != null && searchInput.lowercase().trim().startsWith("male"))
                        artisanRepo.findByGender(AppConstants.Gender.MALE)
                    else if(searchInput != null && searchInput.lowercase().trim().startsWith("female"))
                        artisanRepo.findByGender(AppConstants.Gender.FEMALE)
                else
                    artisanRepo.findAll()
            }

            else -> return null
        }
        return response
    }

    fun fetchAllArtisans(): List<Artisan?> {
        return artisanRepo.findAll()
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

            val cell = row.getCell(2).cellType
            when (cell) {
                CellType.NUMERIC -> artisan.phoneNumber = row.getCell(2).numericCellValue.toLong().toString()
                CellType.STRING -> artisan.phoneNumber = row.getCell(2).stringCellValue
                else -> artisan.phoneNumber = ""
            }

            artisan.city = row.getCell(4).stringCellValue
            artisan.state = row.getCell(3).stringCellValue

            if (row.getCell(6).stringCellValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray().size > 1) {
                var programme: Programme? = null
                for (s in row.getCell(6).stringCellValue.split(",")) {
                    programme = Programme()
                    programme.title = s
                    artisan.addProgramme(programme)
                }
            } else {
                val programme = Programme()
                programme.title = row.getCell(6).stringCellValue
                artisan.addProgramme(programme)
            }
            artisan.trade = row.getCell(1).stringCellValue
            try{
                if (findByPhoneNumber(artisan.phoneNumber!!) != null) {
                    throw Exception("An Artisan with this phone number already exists")
                }
                artisanRepo.save(artisan)
            } catch (e: Exception) {
                continue
//                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
//                return ResponseEntity.ok().body("Uploaded Artisanal Spreadsheet records successfully")
            }


        }
        return ResponseEntity.ok().body("Uploaded Artisanal Spreadsheet records successfully")
        }

    fun  getAllArtisanByTradeName(trade: String): ResponseEntity<List<Artisan?>?>{
        val artisans: List<Artisan?>? = artisanRepo.findByTrade(trade)
        return ResponseEntity.ok().body(artisans)
    }

    fun getAllArtisansaAsExcelSpreadSheet(response: HttpServletResponse) {
        val artisans: List<Artisan>? = artisanRepo.findAll()
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
                val programmes = programmeRepo.findByArtisan(rowData)
                for(programme in programmes){
                    stringBuilder.append(programme.title)
                }
                dataRow.createCell(6).setCellValue(stringBuilder.toString())
            }
            // Set response headers for Excel file download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=my-table-data.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
            //        return artisans
            }
        }
}