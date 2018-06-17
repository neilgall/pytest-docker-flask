package uk.neilgall.rulesapp

import org.json.JSONArray
import org.json.JSONObject

fun Attribute.toJSON(): JSONObject = JSONObject(mapOf(
        "name" to name,
        "value" to (value as Term<Attribute>).toJSON()
))

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
    is Term.Expr -> mapOf(
            "type" to op.s,
            "lhs" to lhs.toJSON(),
            "rhs" to rhs.toJSON()
    )
    is Term.Coerce -> mapOf(
            "type" to "coerce",
            "from" to value.toJSON(),
            "to" to toType.name
    )
    is Term.Request -> mapOf(
            "type" to "request",
            "key" to key
    )
    is Term.REST<*> -> mapOf(
            "type" to "rest",
            "method" to method.name,
            "url" to url,
            "params" to JSONObject(params.mapValues { (it.value as Attribute).name })
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
    is Rule.Branch -> mapOf(
            "type" to "branch",
            "condition" to condition.toJSON(),
            "true" to trueRule.toJSON(),
            "false" to falseRule.toJSON()
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
    is Rule.OneOf -> mapOf(
            "type" to "one-of",
            "rules" to JSONArray(rules.map { it.toJSON() })
    )
})

fun RuleSet<Attribute>.toJSON(): JSONObject = JSONObject(mapOf(
        "attributes" to JSONArray(attributes.map { it.toJSON() }),
        "rules" to JSONArray(rules.map { it.toJSON() })
))
