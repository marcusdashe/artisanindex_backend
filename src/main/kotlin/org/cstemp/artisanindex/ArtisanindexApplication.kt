package org.cstemp.artisanindex

import org.cstemp.artisanindex.user.User
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class ArtisanindexApplication
	fun main(args: Array<String>) {
		runApplication<ArtisanindexApplication>(*args)
	}





