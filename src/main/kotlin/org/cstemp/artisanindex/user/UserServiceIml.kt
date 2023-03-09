package org.cstemp.artisanindex.user

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.cstemp.artisanindex.artisan.Artisan
import org.cstemp.artisanindex.dto.AuthenticationResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    companion object {
        private val key: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        private val secretString: String = Encoders.BASE64.encode(key.getEncoded())
    }

    override fun extractUserByJWT(jwt: String?): User? {
        if (jwt.isNullOrBlank()) {
            return null
        }
        try {
            val body = Jwts.parser().setSigningKey(secretString).parseClaimsJws(jwt).getBody()
            val userId = body.get("id", Long::class.java) ?: return null
            val user = userRepository.findById(userId).orElse(null)
            if (user != null) {
                return user
            }
        } catch (e: JwtException) {
            return null
        } catch (e: Exception) {
           return null
        }
        return null
    }


    override fun authenticate(email: String, password: String, response: HttpServletResponse): ResponseEntity<AuthenticationResponse> {
        val user = findByEmail(email) ?:
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse(status = false, message = "User not found", null)) //throw AuthenticationException("Invalid credentials")
        return if (!user.comparePassword(password)) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthenticationResponse(status = false, message = "Invalid credentials", null))
        } else {
            val issuer = user.id.toString()
            val jwt = Jwts.builder().setIssuer(issuer).setExpiration(Date(System.currentTimeMillis() + 60 * 24 * 1000)) // a day
                .signWith(SignatureAlgorithm.HS512, secretString).compact()
            val cookie = Cookie("jwt", jwt)
            cookie.isHttpOnly = true
            response.addCookie(cookie)
            ResponseEntity.ok().body(AuthenticationResponse(status = true, message = "login successfully", jwt = jwt))
        }
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    private fun generateToken(user: User): String {
        val issuer = user.id.toString()

        return Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + 60 * 24 * 1000))
            .signWith(SignatureAlgorithm.HS512, secretString)
            .compact()
    }

    private fun secretKey(): SecretKey {
        return Keys.hmacShaKeyFor("your-secret-key".toByteArray())
    }

    protected fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
