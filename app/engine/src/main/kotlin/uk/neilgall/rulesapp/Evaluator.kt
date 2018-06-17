package uk.neilgall.rulesapp

typealias Request = Map<String, String>

data class Eval<out T>(val t: T?, val e: Throwable?, val d: String, val c: List<Eval<*>>) {
    constructor(t: T, d: String = "", c: List<Eval<*>> = listOf()) : this(t, null, d, c)
    constructor(e: Throwable, d: String = "", c: List<Eval<*>> = listOf()) : this(null, e, d, c)

    fun <U> map(f: (T) -> U, d: String = "map"): Eval<U> =
            if (t != null)
                Eval(f(t), d, listOf(this))
            else
                Eval<U>(null, e, d, listOf(this))

    fun <U> flatMap(f: (T) -> Eval<U>, d: String = "flatMap"): Eval<U> =
            if (t != null)
                f(t).addChildren(d, listOf(this))
            else
                Eval<U>(null, e, d, listOf(this))

    fun <U, V> combine(u: Eval<U>, f: (T, U) -> V, d: String = "combine"): Eval<V> =
            flatMap({ t -> u.map({ f(t, it) }, d) }, d)

    fun orThrow(): T = t ?: throw e!!

    private fun addChildren(d: String, c: List<Eval<*>>): Eval<T> {
        return Eval(t, e, if (d == this.d) d else "${this.d}, $d", this.c + c)
    }
}

fun <K, V, U> Map<K, V>.traverse(f: (V) -> Eval<U>): Eval<Map<K, U>> =
        try {
            Eval(mapValues { e -> f(e.value).orThrow() })
        } catch (t: Throwable) {
            Eval(t)
        }

fun Attribute.reduce(r: Request): Eval<Value> = when (this) {
    is Attribute.String -> Eval(Value.String(value))
    is Attribute.Number -> Eval(Value.Number(value))
    is Attribute.Request -> {
        val v = r[key]
        if (v != null)
            Eval<Value>(Value.String(v))
        else
            Eval(NoSuchElementException("Missing parameter $key"))
    }
    is Attribute.REST<*> ->
        this.params.traverse { (it as Attribute).reduce(r) }
                .map({ doREST(url, method, it) })
                .map(Value::String)
}

fun Term<Attribute>.reduce(r: Request): Eval<Value> = when (this) {
    is Term.String -> Eval(Value.String(value))
    is Term.Number -> Eval(Value.Number(value))
    is Term.Attribute -> value.reduce(r)
    is Term.Expr -> when (op) {
        Operator.PLUS -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x + y }, "+")
        Operator.MINUS -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x - y }, "-")
        Operator.MULTIPLY -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x * y }, "*")
        Operator.DIVIDE -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x / y }, "/")
        Operator.REGEX -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x.regexMatch(y) }, "regex")
    }
}

fun Condition<Attribute>.reduce(r: Request): Eval<Boolean> = when (this) {
    is Condition.Not -> condition.reduce(r).map({ !it }, "not")
    is Condition.And -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x && y }, "and")
    is Condition.Or -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x || y }, "or")
    is Condition.Equal -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x.equals(y) }, "==")
    is Condition.Greater -> lhs.reduce(r).combine(rhs.reduce(r), { x, y -> x > y }, ">")
}

fun Rule<Attribute>.reduce(r: Request): Eval<Decision> = when (this) {
    is Rule.Always -> Eval(decision, d="always")
    is Rule.Never -> Eval(Decision.Undecided, d="never")
    is Rule.When -> condition.reduce(r).map({ if (it) decision else Decision.Undecided }, "when")
    is Rule.Branch -> condition.reduce(r).flatMap({ if (it) trueRule.reduce(r) else falseRule.reduce(r) }, "branch")
    is Rule.Majority -> {
        val reductions = rules.map { it.reduce(r) }
        val matches = reductions.filter { it.t == decision }
        Eval(if (matches.size > reductions.size / 2) decision else Decision.Undecided)
    }
    is Rule.All -> Eval(if (rules.all { it.reduce(r).t == decision }) decision else Decision.Undecided)
    is Rule.Any -> Eval(if (rules.any { it.reduce(r).t == decision }) decision else Decision.Undecided)
}

fun Eval<*>.toMap(): Map<String, *> =
        mapOf("value" to orThrow(), "description" to d, "children" to c.map { it.toMap() })

fun RuleSet<Attribute>.evaluate(request: Request) = rules.map { it.reduce(request).toMap() }
