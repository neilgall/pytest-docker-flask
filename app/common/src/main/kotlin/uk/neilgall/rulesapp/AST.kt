package uk.neilgall.rulesapp

enum class RESTMethod {
    GET, PUT, POST, DELETE
}

sealed class Attribute {
    abstract val name: String
    data class Constant(override val name: String, val value: String): Attribute()
    data class Request(override val name: String, val key: String): Attribute()
    data class REST<A>(override val name: String, val url: String, val method: RESTMethod, val params: Map<String, A>): Attribute()

    fun <A, B> map(f: (A) -> B): Attribute = when(this) {
        is Constant -> this
        is Request -> this
        is REST<*> -> REST(name, url, method, params.mapValues { f(it.value as A) })
    }
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

    fun <B> map(f: (A) -> B): Rule<B> = when(this) {
        is Always -> Always(decision)
        is Never -> Never(decision)
        is When -> When(condition.map(f), decision)
        is Guard -> Guard(condition.map(f), rule.map(f))
        is Majority -> Majority(decision, rules.map { it.map(f) })
        is All -> All(decision, rules.map { it.map(f) })
        is Any -> Any(decision, rules.map { it.map(f) })
    }
}

sealed class Condition<A> {
    data class Not<A>(val condition: Condition<A>): Condition<A>()
    data class And<A>(val conditions: List<Condition<A>>): Condition<A>()
    data class Or<A>(val conditions: List<Condition<A>>): Condition<A>()
    data class Equal<A>(val lhs: A, val rhs: A): Condition<A>()
    data class Greater<A>(val lhs: A, val rhs: A): Condition<A>()

    fun <B> map(f: (A) -> B): Condition<B> = when(this) {
        is Not -> Not(condition.map(f))
        is And -> And(conditions.map { it.map(f) })
        is Or -> Or(conditions.map { it.map(f) })
        is Equal -> Equal(f(lhs), f(rhs))
        is Greater -> Greater(f(lhs), f(rhs))
    }

}

data class RuleSet<A>(
        val attributes: List<Attribute>,
        val rules: List<Rule<A>>
) {
    fun <B> map(f: (A) -> B): RuleSet<B> =
            RuleSet(attributes.map { it.map(f) }, rules.map { it.map(f) })

    fun plusAttributes(attrs: List<Attribute>): RuleSet<A> = RuleSet(attributes + attrs, rules)
}
