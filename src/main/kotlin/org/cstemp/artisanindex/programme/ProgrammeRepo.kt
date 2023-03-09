package org.cstemp.artisanindex.programme

import org.cstemp.artisanindex.artisan.Artisan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProgrammeRepo : JpaRepository<Programme, Long> {
    fun findByArtisan(artisan: Artisan): List<Programme>
    fun findByTitleContainingIgnoreCase(title: String?): List<Programme>

//    @Query("SELECT a FROM Artisan a WHERE a.programme = :programme")
//    fun findArtisansByProgramme(@Param("programme") programme: Programme): List<Artisan?>

}