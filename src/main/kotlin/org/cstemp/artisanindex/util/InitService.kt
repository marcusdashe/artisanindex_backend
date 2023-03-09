package org.cstemp.artisanindex.util

import jakarta.validation.ConstraintViolationException
import org.cstemp.artisanindex.user.User
import org.cstemp.artisanindex.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.sql.SQLIntegrityConstraintViolationException

@Service
class InitService(private val userRepository: UserRepository) : CommandLineRunner {

    @Value("\${spring.security.email}")
    private val adminEmail :String? = null

    @Value("\${spring.security.user.password}")
    private val adminPassword: String? = null

    override fun run(vararg args: String?) {
        val user = User()
        user.email = adminEmail
        user.password = adminPassword
        try{
            userRepository.save(user)
        } catch (e: ConstraintViolationException) {
          println(e.message)
        } catch (e: SQLIntegrityConstraintViolationException) {
            println(e.message)
        }

    }
}