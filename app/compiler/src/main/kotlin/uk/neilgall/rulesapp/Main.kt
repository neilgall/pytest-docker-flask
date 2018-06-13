package uk.neilgall.rulesapp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
open class CompilerController {

    @RequestMapping("/compile", method = [RequestMethod.POST])
    fun compile(@RequestBody source: String): String {
        val parsed = parse(ruleSet, source).resolve()
        return parsed.toJSON().toString()
    }

}

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}