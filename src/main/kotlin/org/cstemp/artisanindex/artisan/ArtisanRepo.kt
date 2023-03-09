package org.cstemp.artisanindex.artisan

import org.springframework.data.jpa.repository.JpaRepository

interface ArtisanRepo : JpaRepository<Artisan, Long> {
    fun findByTrade(trade: String): List<Artisan?>?

    fun findByFullNameContainingIgnoreCase(fullName: String): List<Artisan?>

    fun findByTradeContainingIgnoreCase(trade: String): List<Artisan?>

    fun findByPhoneNumber(phoneNumber: String): List<Artisan?>

    fun findByGender(gender: AppConstants.Gender): List<Artisan?>

    fun findByCityContainingIgnoreCase(city: String): List<Artisan?>

    fun findByStateContainingIgnoreCase(state: String): List<Artisan?>

//    fun existsByEmail(email: String): Boolean
}