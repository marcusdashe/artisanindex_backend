package org.cstemp.artisanindex.programme

import org.cstemp.artisanindex.artisan.Artisan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArtisanProgrameRepo : JpaRepository<ArtisanProgramme, Long> {
    fun findAllByArtisan(artisan: Artisan): List<ArtisanProgramme>
}