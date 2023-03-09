package org.cstemp.artisanindex.user

import jakarta.servlet.http.HttpServletResponse
import org.cstemp.artisanindex.dto.AuthenticationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
class UserController(private val userService: UserService) {

    @PostMapping("/login")
    fun login(@RequestBody credentials: Credentials, response: HttpServletResponse): ResponseEntity<AuthenticationResponse> {
        return userService.authenticate(credentials.email, credentials.password, response)
    }

    @GetMapping("/")
    fun returnAdminDetails(@CookieValue("jwt") jwt: String?): User? {
        return userService.extractUserByJWT(jwt)
    }
}

data class Credentials(val email: String, val password: String)

