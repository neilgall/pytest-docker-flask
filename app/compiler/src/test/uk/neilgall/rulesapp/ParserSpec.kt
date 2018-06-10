package uk.neilgall.rulesapp

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec

class ParserSpec : StringSpec({
    "attribute names" {
        parse(attributeName, "foo") shouldEqual "foo"
        parse(attributeName, "foo_bar") shouldEqual "foo_bar"
        parse(attributeName, "foo123") shouldEqual "foo123"
    }

    "constant attributes" {
        parse(constantAttribute, "foo = const \"bar\"") shouldEqual Attribute.Constant("foo", "bar")
    }

    "request attributes" {
        parse(requestAttribute, "foo = request \"A.B.C\"") shouldEqual Attribute.Request("foo", "A.B.C")
    }

    "rest attributes" {
        parse(restAttribute, "foo = rest GET \"http://foo.bar\" user=UserName, pass=Password") shouldEqual
                Attribute.REST("foo", "http://foo.bar", RESTMethod.GET, mapOf("user" to "UserName", "pass" to "Password"))
    }

    "decisions" {
        parse(decision, "permit") shouldEqual Decision.Permit
        parse(decision, "deny") shouldEqual Decision.Deny
    }

    "conditions" {
        parse(condition(), "foo = bar") shouldEqual Condition.Equal("foo", "bar")
        parse(condition(), "foo > bar") shouldEqual Condition.Greater("foo", "bar")
        parse(condition(), "foo != bar") shouldEqual Condition.Not(Condition.Equal("foo", "bar"))
        parse(condition(), "foo < bar") shouldEqual Condition.Not(
                Condition.Or(listOf(Condition.Equal("foo", "bar"), Condition.Greater("foo", "bar")))
        )
//        parse(condition(), "and foo = bar, qux = xyz") shouldEqual Condition.And(listOf(
//                Condition.Equal("foo", "bar"),
//                Condition.Equal("qux", "xyz")
//        ))
    }

    "rules" {
        parse(rule(), "always permit") shouldEqual Rule.Always<String>(Decision.Permit)
        parse(rule(), "always deny") shouldEqual Rule.Always<String>(Decision.Deny)
        parse(rule(), "never permit") shouldEqual Rule.Never<String>(Decision.Permit)
        parse(rule(), "never deny") shouldEqual Rule.Never<String>(Decision.Deny)
        parse(rule(), "permit when abc = def") shouldEqual Rule.When(
                Condition.Equal("abc", "def"),
                Decision.Permit
        )
        parse(rule(), "deny when ghi > jkl") shouldEqual Rule.When(
                Condition.Greater("ghi", "jkl"),
                Decision.Deny
        )
        parse(rule(), "guard abc = def always deny") shouldEqual Rule.Guard(
                Condition.Equal("abc", "def"),
                Rule.Always(Decision.Deny)
        )
        parse(rule(), "majority permit always permit, always deny") shouldEqual Rule.Majority(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
        parse(rule(), "any permit always permit, always deny") shouldEqual Rule.Any(
                Decision.Permit,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
        parse(rule(), "all deny always permit, always deny") shouldEqual Rule.All(
                Decision.Deny,
                listOf(Rule.Always(Decision.Permit), Rule.Always<String>(Decision.Deny))
        )
    }

    "ruleset" {
        val foo = Attribute.Constant("foo", "foo")
        val bar = Attribute.Request("bar", "bar")
        val qux = Attribute.Constant("const0", "qux")

        parse(ruleSet, """
            foo = const "foo"
            bar = request "bar"

            any permit
                permit when foo = bar,
                permit when foo = "qux"

            """).resolve() shouldEqual RuleSet(
                listOf(foo, bar, qux),
                listOf(Rule.Any(Decision.Permit,
                        listOf(
                                Rule.When(Condition.Equal(foo, bar), Decision.Permit),
                                Rule.When(Condition.Equal(foo, qux), Decision.Permit)
                        ))
                ))
    }

})