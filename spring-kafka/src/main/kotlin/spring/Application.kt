package spring

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
internal open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}