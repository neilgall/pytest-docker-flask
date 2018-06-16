package uk.neilgall.rulesapp

import org.jparsec.OperatorTable
import org.jparsec.Parser
import org.jparsec.Parsers.or
import org.jparsec.Parsers.sequence
import org.jparsec.Terminals
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

// Strange issue with Java/Kotlin lambdas+generics interop needs these adapters
private fun <T> uop(t: String, f: (T) -> T): Parser<UnaryOperator<T>> = token(t).retn(object : UnaryOperator<T> {
    override fun apply(t: T): T = f(t)
})

private fun <T> bop(t: String, f: (T, T) -> T): Parser<BinaryOperator<T>> = token(t).retn(object : BinaryOperator<T> {
    override fun apply(t: T, u: T): T = f(t, u)
})

internal val quotedString: Parser<String> =
        Terminals.StringLiteral.PARSER.map { it.removeSurrounding("\"") }

internal val integer: Parser<Int> =
        Terminals.IntegerLiteral.PARSER.map(String::toInt)

internal fun <T> commaListOf(p: Parser<T>): Parser<List<T>> =
        p.sepBy1(token(",")).followedBy(token("."))

internal val attributeName: Parser<String> = or(
        Terminals.Identifier.PARSER,
        Terminals.StringLiteral.PARSER
)

internal val constantStringAttribute: Parser<Attribute> =
        sequence(
                attributeName,
                token("=").next(quotedString),
                Attribute::String
        )

internal val constantIntegerAttribute: Parser<Attribute> =
        sequence(
                attributeName,
                token("=").next(integer),
                Attribute::Number
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
        constantIntegerAttribute,
        constantStringAttribute,
        requestAttribute,
        restAttribute
)

private val term_constant: Parser<Term<String>> = or(
        integer.map { n -> Term.Number<String>(n) },
        quotedString.map { s -> Term.String<kString>(s) },
        attributeName.map { a -> Term.Attribute<String>(a) }
)

internal val term: Parser<Term<String>> = OperatorTable<Term<String>>()
        .infixl(bop("+", { lhs, rhs -> Term.Expr(lhs, Operator.PLUS, rhs) }), 20)
        .infixl(bop("-", { lhs, rhs -> Term.Expr(lhs, Operator.MINUS, rhs) }), 20)
        .infixl(bop("*", { lhs, rhs -> Term.Expr(lhs, Operator.MULTIPLY, rhs) }), 21)
        .infixl(bop("/", { lhs, rhs -> Term.Expr(lhs, Operator.DIVIDE, rhs) }), 21)
        .infixl(bop("~=", { lhs, rhs -> Term.Expr(lhs, Operator.REGEX, rhs) }), 22)
        .build(term_constant)

internal val decision: Parser<Decision> = or(
        token("permit").retn(Decision.Permit),
        token("deny").retn(Decision.Deny)
)

internal val equalCondition: Parser<Condition<String>> =
        sequence(
                term,
                token("=").next(term),
                { lhs, rhs -> Condition.Equal(lhs, rhs) }
        )

internal val notEqualCondition: Parser<Condition<String>> =
        sequence(
                term,
                token("!=").next(term),
                { lhs, rhs -> Condition.Not(Condition.Equal(lhs, rhs)) }
        )

internal val greaterCondition: Parser<Condition<String>> =
        sequence(
                term,
                token(">").next(term),
                { lhs, rhs -> Condition.Greater(lhs, rhs) }
        )

internal val lessCondition: Parser<Condition<String>> =
        sequence(
                term,
                token("<").next(term),
                { lhs, rhs ->
                    Condition.Not(
                            Condition.Or(
                                    Condition.Equal(lhs, rhs),
                                    Condition.Greater(lhs, rhs)
                            )
                    )
                }
        )

internal fun compareCondition(): Parser<Condition<String>> = or(
        equalCondition,
        notEqualCondition,
        greaterCondition,
        lessCondition
)

internal val condition = OperatorTable<Condition<String>>()
        .prefix(uop("not", { c -> Condition.Not(c) }), 11)
        .infixl(bop("and", { l, r -> Condition.And(l, r) }), 10)
        .infixl(bop("or", { l, r -> Condition.Or(l, r) }), 9)
        .build(compareCondition())

internal val alwaysRule: Parser<Rule<String>> =
        token("always").next(decision).map { d -> Rule.Always<String>(d) }

internal val neverRule: Parser<Rule<String>> =
        token("never").retn(Rule.Never())

internal val whenRule: Parser<Rule<String>> =
        sequence(
                decision,
                token("when").next(condition),
                { d, c -> Rule.When(c, d) }
        )

internal val branchRule: Parser<Rule<String>> =
        sequence(
                token("if").next(condition),
                rule(),
                (token("else").next(rule())).optional(Rule.Never()),
                { c, tr, fr -> Rule.Branch(c, tr, fr) }
        )

internal val majorityRule: Parser<Rule<String>> =
        sequence(
                token("majority").next(decision),
                commaListOf(rule()),
                { d, rs -> Rule.Majority(d, rs) }
        )

internal val allRule: Parser<Rule<String>> =
        sequence(
                token("all").next(decision),
                commaListOf(rule()),
                { d, rs -> Rule.All(d, rs) }
        )

internal val anyRule: Parser<Rule<String>> =
        sequence(
                token("any").next(decision),
                commaListOf(rule()),
                { d, rs -> Rule.Any(d, rs) }
        )

internal fun rule(): Parser<Rule<String>> = or(
        alwaysRule,
        neverRule,
        whenRule,
        branchRule,
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
