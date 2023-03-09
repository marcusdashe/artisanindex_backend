package org.cstemp.artisanindex.artisan

import jakarta.servlet.http.HttpServletResponse
import org.cstemp.artisanindex.dto.ArtisanRequest
import org.cstemp.artisanindex.dto.ArtisanResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@Controller
@RequestMapping("/api/artisan")
class ArtisanApi(private val artisanService: ArtisanService, private val artisanRepo: ArtisanRepo) {

    @PostMapping("/create")
    fun createArtisan(@RequestBody body: ArtisanRequest): ResponseEntity<*> = this.artisanService.create(body)

    @GetMapping("/all")
    fun getAllArtisan(): ResponseEntity<MutableList<ArtisanResponse>> = ResponseEntity.ok().body(artisanService.fetchAllArtisans())

    @GetMapping("/search")
    fun getArtisanData(
        @RequestParam("search") searchInput: String?,
        @RequestParam("filter") selectedValue: String?
    ): ResponseEntity<List<Artisan?>> {
        val artisans: List<Artisan?>? = artisanService.fetchArtisanBySearch(searchInput, selectedValue)
        return ResponseEntity.ok().body(artisans)
    }

    @GetMapping(value = ["/{trade}"])
    fun fetchArtisansByTrade(@PathVariable("trade") tradeName: String): ResponseEntity<List<Artisan?>?> {
        val artisans: List<Artisan?>? = artisanRepo.findByTrade(tradeName)
        return ResponseEntity.ok().body(artisans)
    }

    @PostMapping("/upload-spreadsheet")
    fun uploadSpreadsheet(@RequestBody file: MultipartFile): ResponseEntity<String> {
        return try {
            var response =  this.artisanService.saveExcelDataToDatabase(file)
            response
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @GetMapping("/table/export")
    fun exportToExcel(response: HttpServletResponse) = artisanService.getAllArtisansaAsExcelSpreadSheet(response)


    @GetMapping("/spreadsheet/search")
    fun getArtisanbySearchQSAndReturnSpreadsheet(
        @RequestParam("search") searchInput: String?,
        @RequestParam("filter") selectedValue: String?,
        response: HttpServletResponse
    ) = artisanService.fetchAllArtisanBySearchFilterAndReturnSpreadsheet(searchInput, selectedValue, response)

}