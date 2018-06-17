package uk.neilgall.rulesapp

import javafx.scene.chart.NumberAxisBuilder

sealed class Value {
    data class String(val value: kotlin.String) : Value() {
        override fun equals(that: Value): Boolean = when (that) {
            is String -> value == that.value
            is Number -> value == that.value.toString()
        }

        override operator fun compareTo(that: Value): Int = when (that) {
            is String -> value.compareTo(that.value)
            is Number -> value.compareTo(that.value.toString())
        }

        override operator fun plus(that: Value): Value = when (that) {
            is String -> Value.String(value + that.value)
            is Number -> Value.String(value + that.value.toString())
        }

        override fun minus(that: Value): Value = throw IllegalArgumentException()
        override fun times(that: Value): Value = throw IllegalArgumentException()
        override fun div(that: Value): Value = throw IllegalArgumentException()

        override fun regexMatch(that: Value): Value = when (that) {
            is String -> Value.String(Regex(that.value).find(value)?.value ?: "")
            else -> throw IllegalArgumentException()
        }

        override fun coerce(toType: ValueType): Value = when (toType) {
            ValueType.STRING -> this
            ValueType.NUMBER -> Value.Number(value.toInt())
            ValueType.BOOLEAN -> Value.Number(value.toInt()) //TODO
        }

        override fun toString(): kotlin.String = value
    }

    data class Number(val value: Int) : Value() {
        override fun equals(that: Value): Boolean = when (that) {
            is Number -> value == that.value
            is String -> value == that.value.toInt()
        }

        override operator fun compareTo(that: Value): Int = when (that) {
            is Number -> value.compareTo(that.value)
            is String -> value.compareTo(that.value.toInt())
        }

        override fun plus(that: Value): Value = when (that) {
            is Number -> Value.Number(value + that.value)
            is String -> Value.Number(value + that.value.toInt())
        }

        override fun minus(that: Value): Value = when (that) {
            is Number -> Value.Number(value - that.value)
            is String -> Value.Number(value - that.value.toInt())
        }

        override fun times(that: Value): Value = when (that) {
            is Number -> Value.Number(value * that.value)
            is String -> Value.Number(value * that.value.toInt())
        }

        override fun div(that: Value): Value = when (that) {
            is Number -> Value.Number(value / that.value)
            is String -> Value.Number(value / that.value.toInt())
        }

        override fun coerce(toType: ValueType): Value = when (toType) {
            ValueType.STRING -> Value.String(value.toString())
            ValueType.NUMBER -> this
            ValueType.BOOLEAN -> this //TODO
        }

        override fun regexMatch(that: Value): Value = throw IllegalArgumentException()

        override fun toString(): kotlin.String = value.toString()
    }

    abstract fun equals(that: Value): Boolean
    abstract operator fun compareTo(that: Value): Int
    abstract operator fun plus(that: Value): Value
    abstract operator fun minus(that: Value): Value
    abstract operator fun times(that: Value): Value
    abstract operator fun div(that: Value): Value
    abstract fun regexMatch(that: Value): Value
    abstract fun coerce(toType: ValueType): Value
}
