package uk.neilgall.rulesapp

enum class RESTMethod {
    GET, PUT, POST, DELETE
}

sealed class Attribute {
    abstract val name: String
    data class Constant(override val name: String, val value: String): Attribute()
    data class Request(override val name: String, val key: String): Attribute()
    data class REST<A>(override val name: String, val url: String, val method: RESTMethod, val params: Map<String, A>): Attribute()
}

enum class Decision {
    Permit, Deny, Undecided
}

sealed class Rule<A> {
    data class Always<A>(val decision: Decision): Rule<A>()
    data class Never<A>(val decision: Decision): Rule<A>()
    data class When<A>(val condition: Condition<A>, val decision: Decision): Rule<A>()
    data class Guard<A>(val condition: Condition<A>, val rule: Rule<A>): Rule<A>()
    data class Majority<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()
    data class Any<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()
    data class All<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()
}

sealed class Condition<A> {
    data class Not<A>(val condition: Condition<A>): Condition<A>()
    data class And<A>(val conditions: List<Condition<A>>): Condition<A>()
    data class Or<A>(val conditions: List<Condition<A>>): Condition<A>()
    data class Equal<A>(val lhs: A, val rhs: A): Condition<A>()
    data class Greater<A>(val lhs: A, val rhs: A): Condition<A>()
}

data class RuleSet<A>(
        val attributes: List<Attribute>,
        val rules: List<Rule<A>>
)

fun <A, B> Attribute.map(f: (A) -> B): Attribute = when(this) {
    is Attribute.Constant -> Attribute.Constant(name, value)
    is Attribute.Request -> Attribute.Request(name, key)
    is Attribute.REST<*> -> Attribute.REST(name, url, method, params.mapValues { f(it.value as A) })
}

fun <A, B> Condition<A>.map(f: (A) -> B): Condition<B> = when(this) {
    is Condition.Not -> Condition.Not(condition.map(f))
    is Condition.And -> Condition.And(conditions.map { it.map(f) })
    is Condition.Or -> Condition.Or(conditions.map { it.map(f) })
    is Condition.Equal -> Condition.Equal(f(lhs), f(rhs))
    is Condition.Greater -> Condition.Greater(f(lhs), f(rhs))
}

fun <A, B> Rule<A>.map(f: (A) -> B): Rule<B> = when(this) {
    is Rule.Always -> Rule.Always(decision)
    is Rule.Never -> Rule.Never(decision)
    is Rule.When -> Rule.When(condition.map(f), decision)
    is Rule.Guard -> Rule.Guard(condition.map(f), rule.map(f))
    is Rule.Majority -> Rule.Majority(decision, rules.map { it.map(f) })
    is Rule.All -> Rule.All(decision, rules.map { it.map(f) })
    is Rule.Any -> Rule.Any(decision, rules.map { it.map(f) })
}

fun <A, B> RuleSet<A>.map(f: (A) -> B): RuleSet<B> =
        RuleSet(attributes.map { it.map(f) }, rules.map { it.map(f) })
