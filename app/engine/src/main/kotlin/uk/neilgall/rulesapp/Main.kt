package uk.neilgall.rulesapp

import org.json.JSONArray
import org.json.JSONObject
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
open class EngineController {

    private var ruleSet: RuleSet<Attribute>? = null

    @RequestMapping("/load", method = [RequestMethod.POST])
    fun load(@RequestBody json: String): String {
        ruleSet = JSONObject(json).toRuleSet().resolve()
        return "ok"
    }

    @RequestMapping("/query", method = [RequestMethod.POST])
    fun execute(@RequestBody attributes: Map<String, Any>): String {
        val request = attributes.mapValues { it.value.toString() }
        val results = ruleSet?.evaluate(request) ?: listOf()
        return JSONArray(results).toString()
    }
}

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
