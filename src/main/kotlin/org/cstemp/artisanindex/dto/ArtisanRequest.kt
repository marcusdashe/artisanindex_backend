package org.cstemp.artisanindex.dto

data class ArtisanRequest(
    var id: String = "",
    var city: String = "",
    var fullName: String = "",
    var gender: String = "",
    var phoneNumber: String = "",
    var state: String = "",
    var trade: String = "",
    var programme: String = "",
)
