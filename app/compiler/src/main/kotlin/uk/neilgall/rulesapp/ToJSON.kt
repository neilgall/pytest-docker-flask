package uk.neilgall.rulesapp

import org.json.JSONArray
import org.json.JSONObject

private fun attributeToName(attr: Any?): String = when (attr) {
    is Attribute -> attr.name
    else -> attr.toString()
}

fun Attribute.toJSON(): JSONObject = JSONObject(when (this) {
    is Attribute.String -> mapOf(
            "type" to "string",
            "name" to name,
            "value" to value
    )
    is Attribute.Number -> mapOf(
            "type" to "number",
            "name" to name,
            "value" to value
    )
    is Attribute.Request -> mapOf(
            "type" to "request",
            "name" to name,
            "key" to key
    )
    is Attribute.REST<*> -> mapOf(
            "type" to "rest",
            "name" to name,
            "url" to url,
            "method" to method.name,
            "params" to JSONObject(params.mapValues { attributeToName(it.value) })
    )
})

fun Term<Attribute>.toJSON(): JSONObject = JSONObject(when (this) {
    is Term.String -> mapOf(
            "type" to "string",
            "value" to value
    )
    is Term.Number -> mapOf(
            "type" to "number",
            "value" to value
    )
    is Term.Attribute -> mapOf(
            "type" to "attribute",
            "name" to value.name
    )
})

fun Decision.toJSON() = this.name

fun Condition<Attribute>.toJSON(): JSONObject = JSONObject(when (this) {
    is Condition.Not -> mapOf(
            "type" to "not",
            "condition" to condition.toJSON()
    )
    is Condition.And -> mapOf(
            "type" to "and",
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
    is Condition.Or -> mapOf(
            "type" to "or",
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
    is Condition.Equal -> mapOf(
            "type" to "equal",
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
    is Condition.Greater -> mapOf(
            "type" to "greater",
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
})

fun Rule<Attribute>.toJSON(): JSONObject = JSONObject(when (this) {
    is Rule.Always -> mapOf(
            "type" to "always",
            "decision" to decision.toJSON()
    )
    is Rule.Never -> mapOf(
            "type" to "never",
            "decision" to decision.toJSON()
    )
    is Rule.When -> mapOf(
            "type" to "when",
            "condition" to condition.toJSON(),
            "decision" to decision.toJSON()
    )
    is Rule.Guard -> mapOf(
            "type" to "guard",
            "condition" to condition.toJSON(),
            "rule" to rule.toJSON()
    )
    is Rule.Majority -> mapOf(
            "type" to "majority",
            "decision" to decision.toJSON(),
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
    is Rule.All -> mapOf(
            "type" to "all",
            "decision" to decision.toJSON(),
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
    is Rule.Any -> mapOf(
            "type" to "any",
            "decision" to decision.toJSON(),
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
})

fun RuleSet<Attribute>.toJSON(): JSONObject = JSONObject(mapOf(
        "attributes" to JSONArray(attributes.map { it.toJSON() }),
        "rules" to JSONArray(rules.map { it.toJSON() })
))
