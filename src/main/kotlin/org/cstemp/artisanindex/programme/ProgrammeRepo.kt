package org.cstemp.artisanindex.programme

import org.cstemp.artisanindex.artisan.Artisan
import org.springframework.data.jpa.repository.JpaRepository

interface ProgrammeRepo : JpaRepository<Programme, Long> {
    fun findByArtisan(artisan: Artisan): List<Programme>
}