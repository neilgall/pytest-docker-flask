package uk.neilgall.rulesapp

enum class RESTMethod {
    GET, PUT, POST, DELETE
}

enum class ValueType {
    STRING, NUMBER, BOOLEAN
}

typealias kString = kotlin.String

data class Attribute(val name: String, val value: Term<*>) {
    fun <A, B> map(f: (A) -> B): Attribute =
            Attribute(name, (value as Term<A>).map(f))
}

enum class Operator(val s: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    REGEX("~=")
}

sealed class Term<A> {
    data class String<A>(val value: kString): Term<A>()
    data class Number<A>(val value: Int): Term<A>()
    data class Request<A>(val key: kString): Term<A>()
    data class REST<A>(val url: kString, val method: RESTMethod, val params: Map<kString, A>): Term<A>()
    data class Attribute<A>(val value: A): Term<A>()
    data class Expr<A>(val lhs: Term<A>, val op: Operator, val rhs: Term<A>): Term<A>()
    data class Coerce<A>(val value: Term<A>, val toType: ValueType): Term<A>()

    fun <A, B> map(f: (A) -> B): Term<B> = when(this) {
        is String -> String(value)
        is Number -> Number(value)
        is Request -> Request(key)
        is REST<*> -> REST(url, method, params.mapValues{ f(it.value as A) })
        is Attribute -> Attribute(f(value as A))
        is Expr -> Expr(lhs.map(f), op, rhs.map(f))
        is Coerce -> Coerce(value.map(f), toType)
    }
}

enum class Decision {
    Permit, Deny, Undecided
}

sealed class Rule<A> {
    data class Never<A>(val decision: Decision = Decision.Undecided): Rule<A>()
    data class Always<A>(val decision: Decision): Rule<A>()
    data class When<A>(val condition: Condition<A>, val decision: Decision): Rule<A>()
    data class Branch<A>(val condition: Condition<A>, val trueRule: Rule<A>, val falseRule: Rule<A>): Rule<A>()
    data class Majority<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()
    data class Any<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()
    data class All<A>(val decision: Decision, val rules: List<Rule<A>>): Rule<A>()

    fun <B> map(f: (A) -> B): Rule<B> = when(this) {
        is Always -> Always(decision)
        is Never -> Never(decision)
        is When -> When(condition.map(f), decision)
        is Branch -> Branch(condition.map(f), trueRule.map(f), falseRule.map(f))
        is Majority -> Majority(decision, rules.map { it.map(f) })
        is All -> All(decision, rules.map { it.map(f) })
        is Any -> Any(decision, rules.map { it.map(f) })
    }
}

sealed class Condition<A> {
    data class Not<A>(val condition: Condition<A>): Condition<A>()
    data class And<A>(val lhs: Condition<A>, val rhs: Condition<A>): Condition<A>()
    data class Or<A>(val lhs: Condition<A>, val rhs: Condition<A>): Condition<A>()
    data class Equal<A>(val lhs: Term<A>, val rhs: Term<A>): Condition<A>()
    data class Greater<A>(val lhs: Term<A>, val rhs: Term<A>): Condition<A>()

    fun <B> map(f: (A) -> B): Condition<B> = when(this) {
        is Not -> Not(condition.map(f))
        is And -> And(lhs.map(f), rhs.map(f))
        is Or -> Or(lhs.map(f), rhs.map(f))
        is Equal -> Equal(lhs.map(f), rhs.map(f))
        is Greater -> Greater(lhs.map(f), rhs.map(f))
    }

}

data class RuleSet<A>(
        val attributes: List<Attribute>,
        val rules: List<Rule<A>>
) {
    fun <B> map(f: (A) -> B): RuleSet<B> =
            RuleSet(attributes.map { it.map(f) },
                    rules.map { it.map(f) })
}
