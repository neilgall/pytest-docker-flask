package uk.neilgall.rulesapp

typealias Request = Map<String, String>

fun Attribute.reduce(r: Request): Value = when (this) {
    is Attribute.String -> Value.String(value)
    is Attribute.Number -> Value.Number(value)
    is Attribute.Request -> Value.String(r[key] ?: throw NoSuchElementException("Missing parameter $key"))
    is Attribute.REST<*> -> Value.String(doREST(url, method, params.mapValues { (it.value as Attribute).reduce(r) }))
}

fun Term<Attribute>.reduce(r: Request): Value = when (this) {
    is Term.String -> Value.String(value)
    is Term.Number -> Value.Number(value)
    is Term.Attribute -> value.reduce(r)
    is Term.Expr -> when (op) {
        Operator.PLUS -> lhs.reduce(r) + rhs.reduce(r)
        Operator.MINUS -> lhs.reduce(r) - rhs.reduce(r)
        Operator.MULTIPLY -> lhs.reduce(r) * rhs.reduce(r)
        Operator.DIVIDE -> lhs.reduce(r) * rhs.reduce(r)
        Operator.REGEX -> lhs.reduce(r).regexMatch(rhs.reduce(r))
    }
}

fun Condition<Attribute>.reduce(r: Request): Boolean = when (this) {
    is Condition.Not -> !condition.reduce(r)
    is Condition.And -> lhs.reduce(r) && rhs.reduce(r)
    is Condition.Or -> lhs.reduce(r) || rhs.reduce(r)
    is Condition.Equal -> lhs.reduce(r) == rhs.reduce(r)
    is Condition.Greater -> lhs.reduce(r) > rhs.reduce(r)
}

fun Rule<Attribute>.reduce(r: Request): Decision = when (this) {
    is Rule.Always -> decision
    is Rule.Never -> decision
    is Rule.When -> if (condition.reduce(r)) decision else Decision.Undecided
    is Rule.Guard -> if (condition.reduce(r)) rule.reduce(r) else Decision.Undecided
    is Rule.Majority -> {
        val reductions = rules.map { it.reduce(r) }
        val matches = reductions.filter { it == decision }
        if (matches.size > reductions.size / 2) decision else Decision.Undecided
    }
    is Rule.All -> if (rules.all { it.reduce(r) == decision }) decision else Decision.Undecided
    is Rule.Any -> if (rules.any { it.reduce(r) == decision }) decision else Decision.Undecided
}

fun RuleSet<Attribute>.evaluate(request: Request) = rules.map { it.reduce(request) }
