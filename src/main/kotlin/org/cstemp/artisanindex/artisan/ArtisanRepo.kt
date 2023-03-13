package org.cstemp.artisanindex.artisan

import org.cstemp.artisanindex.programme.Programme
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ArtisanRepo : JpaRepository<Artisan, Long> {

    fun save(artisan: Artisan): Artisan

    override fun findById(id: Long): Optional<Artisan>

    override fun findAll(): List<Artisan>

    fun findByTrade(trade: String): List<Artisan?>?

    fun findByFullNameContainingIgnoreCase(fullName: String): List<Artisan?>

    fun findByTradeContainingIgnoreCase(trade: String): List<Artisan?>

    fun findByPhoneNumber(phoneNumber: String): List<Artisan?>

    fun findByGender(gender: AppConstants.Gender): List<Artisan?>

    fun findByCityContainingIgnoreCase(city: String): List<Artisan?>

    fun findByStateContainingIgnoreCase(state: String): List<Artisan?>

    override fun count(): Long

    override fun deleteById(id: Long)

    override fun existsById(id: Long): Boolean

//    fun findByProgrammes(programmes: List<Programme>): List<Artisan>
}