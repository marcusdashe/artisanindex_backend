package org.cstemp.artisanindex.programme

import org.cstemp.artisanindex.artisan.Artisan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProgrammeRepo : JpaRepository<Programme, Long> {

    fun findByTitleContainingIgnoreCase(title: String?): List<Programme>
//    @Query("SELECT a FROM Artisan a JOIN a.programmes p WHERE p = :programme")
//    fun findArtisansByProgramme(@Param("programme") programme: Programme): List<Artisan>

//    @Query("SELECT a FROM Artisan a WHERE a.programme = :programme")
//    fun findArtisansByProgramme(@Param("programme") programme: Programme): List<Artisan?>

    // Find all programmes for a given artisan
    fun findAllByArtisansContains(artisan: Artisan): List<Programme>
    fun findByTitle(title: String): Programme?

    fun findByYear(year: String): List<Programme>

    fun findByBatch(batch: String): List<Programme>

}