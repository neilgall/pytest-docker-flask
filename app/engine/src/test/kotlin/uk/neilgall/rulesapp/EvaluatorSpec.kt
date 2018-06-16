package uk.neilgall.rulesapp

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec

class TermEvaluatorSpec : StringSpec({
    "string" {
        Term.String<Attribute>("foo").reduce(mapOf()) shouldEqual Value.String("foo")
    }

    "number" {
        Term.Number<Attribute>(123).reduce(mapOf()) shouldEqual Value.Number(123)
    }

    "string attribute" {
        Term.Attribute<Attribute>(Attribute.String("foo", "bar")).reduce(mapOf()) shouldEqual Value.String("bar")
    }

    "number attribute" {
        Term.Attribute<Attribute>(Attribute.Number("qux", 42)).reduce(mapOf()) shouldEqual Value.Number(42)
    }

    "request attribute" {
        Term.Attribute<Attribute>(Attribute.Request("foo", "xyz")).reduce(mapOf("xyz" to "42")) shouldEqual Value.String("42")
    }

    "arithmetic" {
        Term.Expr<Attribute>(Term.Number(6), Operator.MULTIPLY, Term.Number(7)).reduce(mapOf()) shouldEqual Value.Number(42)
    }
})

class ConditionEvaluatorSpec : StringSpec({
    "equals" {
        Condition.Equal<Attribute>(Term.Number(42), Term.Number(42)).reduce(mapOf()) shouldBe true
        Condition.Equal<Attribute>(Term.Number(42), Term.Number(43)).reduce(mapOf()) shouldBe false
        Condition.Equal<Attribute>(Term.Number(42), Term.String("42")).reduce(mapOf()) shouldBe false
    }

    "greater" {
        Condition.Greater<Attribute>(Term.Number(42), Term.Number(9)).reduce(mapOf()) shouldBe true
        Condition.Greater<Attribute>(Term.Number(42), Term.Number(75)).reduce(mapOf()) shouldBe false
    }

    "not" {
        Condition.Not(
                Condition.Equal<Attribute>(Term.Number(42), Term.Number(42))
        ).reduce(mapOf()) shouldBe false
    }

    "and" {
        Condition.And<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Condition.Equal(Term.String("foo"), Term.String("foo"))
        ).reduce(mapOf()) shouldBe true

        Condition.And<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Condition.Equal(Term.String("foo"), Term.String("foo"))
        ).reduce(mapOf()) shouldBe false

        Condition.And<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Condition.Equal(Term.String("foo"), Term.String("bar"))
        ).reduce(mapOf()) shouldBe false

        Condition.And<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Condition.Equal(Term.String("foo"), Term.String("bar"))
        ).reduce(mapOf()) shouldBe false
    }

    "or" {
        Condition.Or<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Condition.Equal(Term.String("foo"), Term.String("foo"))
        ).reduce(mapOf()) shouldBe true

        Condition.Or<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Condition.Equal(Term.String("foo"), Term.String("foo"))
        ).reduce(mapOf()) shouldBe true

        Condition.Or<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Condition.Equal(Term.String("foo"), Term.String("bar"))
        ).reduce(mapOf()) shouldBe true

        Condition.Or<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Condition.Equal(Term.String("foo"), Term.String("bar"))
        ).reduce(mapOf()) shouldBe false
    }
})

class RuleEvaluatorSpec : StringSpec({
    "always" {
        Rule.Always<Attribute>(Decision.Permit).reduce(mapOf()) shouldEqual Decision.Permit
        Rule.Always<Attribute>(Decision.Deny).reduce(mapOf()) shouldEqual Decision.Deny
    }

    "never" {
        Rule.Never<Attribute>(Decision.Permit).reduce(mapOf()) shouldEqual Decision.Undecided
        Rule.Never<Attribute>(Decision.Deny).reduce(mapOf()) shouldEqual Decision.Undecided
    }

    "when" {
        Rule.When<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Decision.Permit
        ).reduce(mapOf()) shouldEqual Decision.Permit

        Rule.When<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Decision.Permit
        ).reduce(mapOf()) shouldEqual Decision.Undecided
    }

    "branch" {
        Rule.Branch<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(42)),
                Rule.Always(Decision.Permit),
                Rule.Always(Decision.Deny)
        ).reduce(mapOf()) shouldEqual Decision.Permit

        Rule.Branch<Attribute>(
                Condition.Equal(Term.Number(42), Term.Number(43)),
                Rule.Always(Decision.Permit),
                Rule.Always(Decision.Deny)
        ).reduce(mapOf()) shouldEqual Decision.Deny
    }

    "majority" {
        Rule.Majority<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Deny)
                )
        ).reduce(mapOf()) shouldEqual Decision.Permit

        Rule.Majority<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Deny),
                        Rule.Always(Decision.Deny)
                )
        ).reduce(mapOf()) shouldEqual Decision.Undecided
    }

    "all" {
        Rule.All<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Permit)
                )
        ).reduce(mapOf()) shouldEqual Decision.Permit

        Rule.All<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Deny)
                )
        ).reduce(mapOf()) shouldEqual Decision.Undecided
    }

    "any" {
        Rule.Any<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Deny),
                        Rule.Always(Decision.Deny),
                        Rule.Always(Decision.Deny)
                )
        ).reduce(mapOf()) shouldEqual Decision.Undecided

        Rule.Any<Attribute>(
                Decision.Permit,
                listOf(Rule.Always(Decision.Deny),
                        Rule.Always(Decision.Permit),
                        Rule.Always(Decision.Deny)
                )
        ).reduce(mapOf()) shouldEqual Decision.Permit
    }
})