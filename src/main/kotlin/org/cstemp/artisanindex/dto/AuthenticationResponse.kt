package org.cstemp.artisanindex.dto

data class AuthenticationResponse(val status: Boolean, val message: String, val jwt: String?)
