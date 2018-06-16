package uk.neilgall.rulesapp

import io.kotlintest.matchers.Matcher
import io.kotlintest.matchers.Result
import io.kotlintest.matchers.should
import io.kotlintest.specs.StringSpec
import org.json.JSONObject

private fun beJSON(s: String) = object : Matcher<JSONObject> {
    override fun test(value: JSONObject): Result {
        val expJson = JSONObject(s)
        return if (value.toMap() == expJson.toMap()) {
            Result(passed = true, message = "match")
        } else {
            Result(passed = false, message = "expected\n${expJson.toString(2)}\nbut got\n${value.toString(2)}")
        }
    }
}

class AttributeToJSONSpec : StringSpec({
    "string attribute" {
        Attribute.String("foo", "bar").toJSON() should beJSON("""
            {"type":"string","name":"foo","value":"bar"}
        """)
    }

    "number attribute" {
        Attribute.Number("foo", 132).toJSON() should beJSON("""
            {"type":"number","name":"foo","value":132}
        """)
    }

    "request attribute" {
        Attribute.Request("foo", "bar").toJSON() should beJSON("""
            {"type":"request","name":"foo","key":"bar"}
        """)
    }

    "simple REST" {
        Attribute.REST("foo",
                "http://foo/bar/",
                RESTMethod.PUT,
                mapOf("abc" to "xyz")
        ).toJSON() should beJSON("""
            {"name":"foo","type":"rest","method":"PUT","params":{"abc":"xyz"},"url":"http://foo/bar/"}
        """)
    }
})

class ConditionToJSONSpec : StringSpec({
    "equals condition" {
        Condition.Equal<Attribute>(
                Attribute.String("foo", "bar"),
                Attribute.String("qux", "xyz")
        ).toJSON() should beJSON("""
            {"type":"equal","rhs":"qux","lhs":"foo"}
        """)
    }

    "greater condition" {
        Condition.Greater<Attribute>(
                Attribute.String("foo", "bar"),
                Attribute.String("qux", "xyz")
        ).toJSON() should beJSON("""
            {"type":"greater","rhs":"qux","lhs":"foo"}
        """)
    }

    "not condition" {
        Condition.Not(
                Condition.Equal<Attribute>(
                        Attribute.String("foo", "bar"),
                        Attribute.String("qux", "xyz")
                )
        ).toJSON() should beJSON("""
            {"type":"not","condition":{"type":"equal","rhs":"qux","lhs":"foo"}}
        """)
    }

    "and condition" {
        Condition.And(
                Condition.Equal<Attribute>(
                        Attribute.String("foo", "bar"),
                        Attribute.String("qux", "xyz")
                ),
                Condition.Greater<Attribute>(
                        Attribute.String("abc", "ghi"),
                        Attribute.String("def", "jkl")
                )
        ).toJSON() should beJSON("""
            {"type":"and",
            "lhs":{"type":"equal","rhs":"qux","lhs":"foo"},
            "rhs":{"type":"greater","rhs":"def","lhs":"abc"}}
        """)
    }

    "or condition" {
        Condition.Or(
                Condition.Equal<Attribute>(
                        Attribute.String("foo", "bar"),
                        Attribute.String("qux", "xyz")
                ),
                Condition.Greater<Attribute>(
                        Attribute.String("abc", "ghi"),
                        Attribute.String("def", "jkl")
                )
        ).toJSON() should beJSON("""
            {"type":"or",
            "lhs":{"type":"equal","rhs":"qux","lhs":"foo"},
            "rhs":{"type":"greater","rhs":"def","lhs":"abc"}}
        """)
    }
})

class RuleToJSONSpec : StringSpec({
    "always rule" {
        Rule.Always<Attribute>(Decision.Permit).toJSON() should beJSON("""
            {"type":"always","decision":"Permit"}
        """)
    }

    "never rule" {
        Rule.Never<Attribute>(Decision.Permit).toJSON() should beJSON("""
            {"type":"never","decision":"Permit"}
        """)
    }

    "when rule" {
        Rule.When<Attribute>(
                Condition.Equal(
                        Attribute.String("foo", "123"),
                        Attribute.String("bar", "234")
                ),
                Decision.Permit
        ).toJSON() should beJSON("""
            {"type":"when","decision":"Permit",
            "condition":{"type":"equal","rhs":"bar","lhs":"foo"}}
        """)
    }

    "guard rule" {
        Rule.Guard<Attribute>(
                Condition.Equal(
                        Attribute.String("foo", "123"),
                        Attribute.String("bar", "234")
                ),
                Rule.Always(Decision.Permit)
        ).toJSON() should beJSON("""
            {"type":"guard","rule":{"type":"always","decision":"Permit"},
            "condition":{"type":"equal","rhs":"bar","lhs":"foo"}}
        """)
    }

    "majority rule" {
        Rule.Majority<Attribute>(
                Decision.Deny,
                listOf(Rule.Always(Decision.Permit), Rule.Always(Decision.Deny))
        ).toJSON() should beJSON("""
            {"type":"majority","decision":"Deny","rules":[
            {"type":"always","decision":"Permit"},{"type":"always","decision":"Deny"}]}
        """)
    }

    "all rule" {
        Rule.All<Attribute>(
                Decision.Deny,
                listOf(Rule.Always(Decision.Permit), Rule.Always(Decision.Deny))
        ).toJSON() should beJSON("""
            {"type":"all","decision":"Deny","rules":[
            {"type":"always","decision":"Permit"},{"type":"always","decision":"Deny"}]}
        """)
    }

    "any rule" {
        Rule.Any<Attribute>(
                Decision.Deny,
                listOf(Rule.Always(Decision.Permit), Rule.Always(Decision.Deny))
        ).toJSON() should beJSON("""
            {"type":"any","decision":"Deny","rules":[
            {"type":"always","decision":"Permit"},{"type":"always","decision":"Deny"}]}
        """)
    }
})