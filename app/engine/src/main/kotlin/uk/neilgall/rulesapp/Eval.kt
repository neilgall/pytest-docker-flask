package uk.neilgall.rulesapp

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