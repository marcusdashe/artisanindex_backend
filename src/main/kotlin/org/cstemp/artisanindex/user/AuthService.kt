package org.cstemp.artisanindex.user

import jakarta.servlet.http.HttpServletResponse
import org.cstemp.artisanindex.artisan.Artisan
import org.cstemp.artisanindex.dto.AuthenticationResponse
import org.cstemp.artisanindex.user.User
import org.springframework.http.ResponseEntity

interface UserService {
    fun authenticate(email: String, password: String, response: HttpServletResponse): ResponseEntity<AuthenticationResponse>
    fun findByEmail(email: String): User?
    fun extractUserByJWT(jwt: String?): User?
}

