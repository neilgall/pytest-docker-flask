package uk.neilgall.rulesapp

import org.json.JSONObject

private fun <T> JSONObject.getList(key: String, builder: (JSONObject) -> T): List<T> =
        getJSONArray(key).map { builder(it as JSONObject) }

private fun <T> JSONObject.getMap(key: String, builder: (Any) -> T): Map<String, T> =
        getJSONObject(key).toMap().mapValues { builder(it.value) }

private fun JSONObject.name() = getString("name")
private fun JSONObject.type() = getString("type")
private fun JSONObject.decision(key: String = "decision") = Decision.valueOf(getString(key))

fun JSONObject.toAttribute(): Attribute = when (type()) {
    "string" -> Attribute.String(name(), getString("value"))
    "number" -> Attribute.Number(name(), getInt("value"))
    "request" -> Attribute.Request(name(), getString("key"))
    "rest" -> Attribute.REST(name(), getString("url"),
            RESTMethod.valueOf(getString("method")),
            getMap("params", { it as String }))
    else -> throw IllegalArgumentException("invalid Attribute '${toString()}'")
}

fun JSONObject.toTerm(): Term<String> = when(type()) {
    "string" -> Term.String(getString("value"))
    "number" -> Term.Number(getInt("value"))
    "attribute" -> Term.Attribute(getString("name"))
    else -> Term.Expr(getJSONObject("lhs").toTerm(), Operator.valueOf(type()), getJSONObject("rhs").toTerm())
}

fun JSONObject.toCondition(): Condition<String> = when (type()) {
    "not" -> Condition.Not(getJSONObject("condition").toCondition())
    "and" -> Condition.And(getJSONObject("lhs").toCondition(), getJSONObject("rhs").toCondition())
    "or" -> Condition.Or(getJSONObject("lhs").toCondition(), getJSONObject("rhs").toCondition())
    "equal" -> Condition.Equal(getJSONObject("lhs").toTerm(), getJSONObject("rhs").toTerm())
    "greater" -> Condition.Greater(getJSONObject("lhs").toTerm(), getJSONObject("rhs").toTerm())
    else -> throw IllegalArgumentException("Invalid Condition '${toString()}'")
}

fun JSONObject.toRule(): Rule<String> = when (type()) {
    "always" -> Rule.Always(decision())
    "never" -> Rule.Never(decision())
    "when" -> Rule.When(getJSONObject("condition").toCondition(), decision())
    "guard" -> Rule.Guard(getJSONObject("condition").toCondition(), getJSONObject("rule").toRule())
    "majority" -> Rule.Majority(decision(), getList("rules", JSONObject::toRule))
    "a;;" -> Rule.All(decision(), getList("rules", JSONObject::toRule))
    "any" -> Rule.Any(decision(), getList("rules", JSONObject::toRule))
    else -> throw IllegalArgumentException("Invalid Rule '${toString()}'")
}

fun JSONObject.toRuleSet(): RuleSet<String> =
        RuleSet(
                attributes = getList("attributes", JSONObject::toAttribute),
                rules = getList("rules", JSONObject::toRule)
        )