package uk.neilgall.rulesapp.parser

import org.jparsec.Parser
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Terminals

internal val quotedString: Parser<String> =
        Terminals.StringLiteral.PARSER.map { it.removeSurrounding("\"") }

internal val attributeName: Parser<String> = or(
        Terminals.Identifier.PARSER,
        Terminals.StringLiteral.PARSER
)

internal val constantAttribute: Parser<Attribute> =
        sequence(
                attributeName,
                token("=").followedBy(token("const")).next(quotedString),
                Attribute::Constant
        )

internal val requestAttribute: Parser<Attribute> =
        sequence(
                attributeName,
                token("=").followedBy(token("request")).next(quotedString),
                Attribute::Request
        )

internal val restMethod = or(
        token("GET").retn(RESTMethod.GET),
        token("PUT").retn(RESTMethod.PUT),
        token("POST").retn(RESTMethod.POST),
        token("DELETE").retn(RESTMethod.DELETE)
)

internal val restParam: Parser<Pair<String, String>> =
        sequence(
                attributeName,
                token("=").next(attributeName),
                { t, u -> t to u }
        )

internal val restParams: Parser<Map<String, String>> =
        restParam.sepBy(token(",")).map { it.associate { it } }

internal val restAttribute: Parser<Attribute> =
        sequence(
                attributeName,
                token("=").followedBy(token("rest")).next(restMethod),
                quotedString,
                restParams,
                { n: String, m: RESTMethod, u: String, p: Map<String, String> -> Attribute.REST(n, u, m, p) }
        )

internal val attribute: Parser<Attribute> = or(
        constantAttribute,
        requestAttribute,
        restAttribute
)

internal val decision: Parser<Decision> = or(
        token("permit").retn(Decision.Permit),
        token("deny").retn(Decision.Deny)
)

internal val notCondition: Parser<Condition<String>> =
        token("not").next(condition()).map { c -> Condition.Not(c) }

internal val andCondition: Parser<Condition<String>> =
        token("and").next(condition().sepBy1(token(","))).map { cs -> Condition.And(cs) }

internal val orCondition: Parser<Condition<String>> =
        token("or").next(condition().sepBy1(token(","))).map { cs -> Condition.Or(cs) }

internal val equalCondition: Parser<Condition<String>> =
        sequence(
                attributeName,
                token("=").next(attributeName),
                { lhs: String, rhs: String -> Condition.Equal(lhs, rhs) }
        )

internal val notEqualCondition: Parser<Condition<String>> =
        sequence(
                attributeName,
                token("!=").next(attributeName),
                { lhs: String, rhs: String -> Condition.Not(Condition.Equal(lhs, rhs)) }
        )

internal val greaterCondition: Parser<Condition<String>> =
        sequence(
                attributeName,
                token(">").next(attributeName),
                { lhs: String, rhs: String -> Condition.Greater(lhs, rhs) }
        )

internal val lessCondition: Parser<Condition<String>> =
        sequence(
                attributeName,
                token("<").next(attributeName),
                { lhs: String, rhs: String ->
                    Condition.Not(
                            Condition.Or(listOf(
                                    Condition.Equal(lhs, rhs),
                                    Condition.Greater(lhs, rhs)
                            ))
                    )
                }
        )

internal fun condition(): Parser<Condition<String>> = or(
        equalCondition,
        notEqualCondition,
        greaterCondition,
        lessCondition,
        andCondition,
        orCondition,
        notCondition
)

internal val alwaysRule: Parser<Rule<String>> =
        token("always").next(decision).map { d -> Rule.Always<String>(d) }

internal val neverRule: Parser<Rule<String>> =
        token("never").next(decision).map { d -> Rule.Never<String>(d) }

internal val whenRule: Parser<Rule<String>> =
        sequence(
                decision,
                token("when").next(condition()),
                { d, c -> Rule.When(c, d) }
        )

internal val guardRule: Parser<Rule<String>> =
        sequence(
                token("guard").next(condition()),
                rule(),
                { c, r -> Rule.Guard(c, r) }
        )

internal val majorityRule: Parser<Rule<String>> =
        sequence(
                token("majority").next(decision),
                rule().sepBy1(token(",")),
                { d, rs -> Rule.Majority(d, rs) }
        )

internal val allRule: Parser<Rule<String>> =
        sequence(
                token("all").next(decision),
                rule().sepBy1(token(",")),
                { d, rs -> Rule.All(d, rs) }
        )

internal val anyRule: Parser<Rule<String>> =
        sequence(
                token("any").next(decision),
                rule().sepBy1(token(",")),
                { d, rs -> Rule.Any(d, rs) }
        )

internal fun rule(): Parser<Rule<String>> = or(
        alwaysRule,
        neverRule,
        whenRule,
        guardRule,
        majorityRule,
        allRule,
        anyRule
)

val ruleSet: Parser<RuleSet<String>> =
        sequence(
                attribute.many(),
                rule().many(),
                { a, r -> RuleSet(a, r) }
        )
