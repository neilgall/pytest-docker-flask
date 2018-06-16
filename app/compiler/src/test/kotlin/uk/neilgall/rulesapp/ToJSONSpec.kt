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
                Term.String("bar"),
                Term.String("xyz")
        ).toJSON() should beJSON("""
            {"type":"equal",
            "lhs":{"type":"string","value":"bar"},
            "rhs":{"type":"string","value":"xyz"}}
        """)
    }

    "greater condition" {
        Condition.Greater<Attribute>(
                Term.Number(100),
                Term.Number(99)
        ).toJSON() should beJSON("""
            {"type":"greater",
            "lhs":{"type":"number","value":100},
            "rhs":{"type":"number","value":99}}
        """)
    }

    "not condition" {
        Condition.Not(
                Condition.Equal<Attribute>(
                        Term.Attribute(Attribute.String("foo", "bar")),
                        Term.Attribute(Attribute.String("qux", "xyz"))
                )
        ).toJSON() should beJSON("""
            {"type":"not","condition":{
                "type":"equal",
                "lhs":{"type":"attribute","name":"foo"},
                "rhs":{"type":"attribute","name":"qux"}
            }}
        """)
    }

    "and condition" {
        Condition.And<Attribute>(
                Condition.Equal(
                        Term.String("foo"),
                        Term.String("qux")
                ),
                Condition.Greater(
                        Term.Number(42),
                        Term.Number(43)
                )
        ).toJSON() should beJSON("""
            {"type":"and",
            "lhs":{"type":"equal","lhs":{"type":"string","value":"foo"},"rhs":{"type":"string","value":"qux"}},
            "rhs":{"type":"greater","lhs":{"type":"number","value":42},"rhs":{"type":"number","value":43}}
            }
        """)
    }

    "or condition" {
        Condition.Or<Attribute>(
                Condition.Equal(
                        Term.String("foo"),
                        Term.String("qux")
                ),
                Condition.Greater(
                        Term.Number(42),
                        Term.Number(43)
                )
        ).toJSON() should beJSON("""
            {"type":"or",
            "lhs":{"type":"equal","lhs":{"type":"string","value":"foo"},"rhs":{"type":"string","value":"qux"}},
            "rhs":{"type":"greater","lhs":{"type":"number","value":42},"rhs":{"type":"number","value":43}}
            }
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
        Rule.When(
                Condition.Equal<Attribute>(
                        Term.Attribute(Attribute.Request("foo", "q")),
                        Term.String("bar")
                ),
                Decision.Permit
        ).toJSON() should beJSON("""
            {"type":"when","decision":"Permit",
            "condition":{
                "type":"equal",
                "rhs":{"type":"string","value":"bar"},
                "lhs":{"type":"attribute","name":"foo"}
            }}
        """)
    }

    "guard rule" {
        Rule.Guard<Attribute>(
                Condition.Equal(
                        Term.Number(123),
                        Term.Number(234)
                ),
                Rule.Always(Decision.Permit)
        ).toJSON() should beJSON("""
            {"type":"guard","rule":{"type":"always","decision":"Permit"},
            "condition":{
                "type":"equal",
                "lhs":{"type":"number","value":123},
                "rhs":{"type":"number","value":234}
            }}
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