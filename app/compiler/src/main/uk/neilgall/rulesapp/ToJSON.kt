package uk.neilgall.rulesapp

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONString

fun Attribute.toJSON(): JSONObject = JSONObject(when (this) {
    is Attribute.Constant -> mapOf(
            "type" to "constant",
            "value" to value
    )
    is Attribute.Request -> mapOf(
            "type" to "request",
            "key" to key
    )
    is Attribute.REST<*> -> mapOf<String, Any>(
            "type" to "rest",
            "url" to url,
            "method" to method.name,
            "params" to JSONObject(params.mapValues { (it as Attribute).toJSON() })
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
            "conditions" to JSONArray(conditions.map { it.toJSON() })
    )
    is Condition.Or -> mapOf(
            "type" to "or",
            "conditions" to JSONArray(conditions.map { it.toJSON() })
    )
    is Condition.Equal -> mapOf(
            "type" to "equal",
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
    is Condition.Greater -> mapOf(
            "type" to "greater",
            "lhs " to lhs.toJSON(),
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
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
    is Rule.All -> mapOf(
            "type" to "all",
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
    is Rule.Any -> mapOf(
            "type" to "any",
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
})

fun RuleSet<Attribute>.toJSON(): JSONObject = JSONObject(mapOf(
        "attributes" to JSONArray(attributes.map { it.toJSON() }),
        "rules" to JSONArray(rules.map { it.toJSON() })
))
