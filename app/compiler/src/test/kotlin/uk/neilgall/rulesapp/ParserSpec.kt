package uk.neilgall.rulesapp

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec

class AttributeParserSpec : StringSpec({
    "attribute names" {
        parse(attributeName, "foo") shouldEqual "foo"
        parse(attributeName, "foo_bar") shouldEqual "foo_bar"
        parse(attributeName, "foo123") shouldEqual "foo123"
    }

    "string constant" {
        parse(attribute, "foo = string \"bar\"") shouldEqual Attribute.String("foo", "bar")
    }

    "number constant" {
        parse(attribute, "foo = number 123") shouldEqual Attribute.Number("foo", 123)
    }

    "request attributes" {
        parse(attribute, "foo = request \"A.B.C\"") shouldEqual Attribute.Request("foo", "A.B.C")
    }

    "rest attributes" {
        parse(attribute, "foo = rest GET \"http://foo.bar\" user=UserName, pass=Password") shouldEqual
                Attribute.REST("foo", "http://foo.bar", RESTMethod.GET, mapOf("user" to "UserName", "pass" to "Password"))
    }
})

class DecisionParserSpec : StringSpec({
    "decisions" {
        parse(decision, "permit") shouldEqual Decision.Permit
        parse(decision, "deny") shouldEqual Decision.Deny
    }
})

class ConditionParserSpec : StringSpec({
    "equal" {
        parse(condition, "foo = bar") shouldEqual Condition.Equal("foo", "bar")
    }

    "greater" {
        parse(condition, "foo > bar") shouldEqual Condition.Greater("foo", "bar")
    }

    "not equal" {
        parse(condition, "foo != bar") shouldEqual Condition.Not(Condition.Equal("foo", "bar"))
    }

    "less" {
        parse(condition, "foo < bar") shouldEqual Condition.Not(
                Condition.Or(Condition.Equal("foo", "bar"), Condition.Greater("foo", "bar"))
        )
    }

    "not" {
        parse(condition, "not a = b") shouldEqual Condition.Not(Condition.Equal("a", "b"))
    }

    "and" {
        parse(condition, "foo = bar and qux = xyz") shouldEqual Condition.And(
                Condition.Equal("foo", "bar"),
                Condition.Equal("qux", "xyz")
        )
    }
})

class RuleParserSpec : StringSpec({
    "always permit" {
        parse(rule(), "always permit") shouldEqual Rule.Always<String>(Decision.Permit)
    }

    "always deny" {
        parse(rule(), "always deny") shouldEqual Rule.Always<String>(Decision.Deny)
    }

    "never" {
        parse(rule(), "never permit") shouldEqual Rule.Never<String>(Decision.Permit)
        parse(rule(), "never deny") shouldEqual Rule.Never<String>(Decision.Deny)
    }

    "when" {
        parse(rule(), "permit when abc = def") shouldEqual Rule.When(
                Condition.Equal("abc", "def"),
                Decision.Permit
        )
        parse(rule(), "deny when ghi > jkl") shouldEqual Rule.When(
                Condition.Greater("ghi", "jkl"),
                Decision.Deny
        )
    }

    "guard" {
        parse(rule(), "if abc = def always deny") shouldEqual Rule.Guard(
                Condition.Equal("abc", "def"),
                Rule.Always(Decision.Deny)
        )
    }

    "majority" {
        parse(rule(), "majority permit always permit, always deny.") shouldEqual Rule.Majority(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
    }

    "any" {
        parse(rule(), "any permit always permit, always deny.") shouldEqual Rule.Any(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
    }

    "all" {
        parse(rule(), "all deny always permit, always deny.") shouldEqual Rule.All(
                Decision.Deny,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
    }
})

class RuleSetParserSpec : StringSpec({
    "ruleset" {
        val foo = Attribute.String("foo", "foo")
        val bar = Attribute.Request("bar", "bar")
        val qux = Attribute.Number("const0", 123)

        parse(ruleSet, """
            foo = string "foo"
            bar = request "bar"

            any permit
                permit when foo = bar,
                permit when foo = 123
                .

            """).resolve() shouldEqual RuleSet(
                listOf(foo, bar, qux),
                listOf(Rule.Any(Decision.Permit,
                        listOf(
                                Rule.When(Condition.Equal(foo, bar), Decision.Permit),
                                Rule.When(Condition.Equal(foo, qux), Decision.Permit)
                        ) as List<Rule<Attribute>>)
                ))
    }

})