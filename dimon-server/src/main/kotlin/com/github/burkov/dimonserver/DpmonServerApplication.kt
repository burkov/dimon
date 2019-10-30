package com.github.burkov.dimonserver

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DpmonServerApplication {
//    override fun run(vararg args: String?) {
//        println("here")
//    }

}

fun main(args: Array<String>) {
    runApplication<DpmonServerApplication>(*args)
}
