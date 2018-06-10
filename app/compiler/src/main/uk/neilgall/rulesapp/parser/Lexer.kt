package uk.neilgall.rulesapp.parser

import org.jparsec.*
import org.jparsec.Parsers.or

private val keywords = listOf(
        // attribute types
        "const", "request", "rest",

        // rest methods
        "GET", "PUT", "POST", "DELETE",

        // decisions
        "permit", "deny",

        // rules
        "always", "never", "when", "guard", "majority", "all", "any",

        // conditions
        "not", "and", "or"
)

private val terminals: Terminals = Terminals
        .operators("=", "!=", ">", ">=", "<", "<=", ",")
        .words(Scanners.IDENTIFIER)
        .caseInsensitiveKeywords(keywords)
        .build()

private val tokens = or(
        Scanners.DOUBLE_QUOTE_STRING,
        Scanners.SINGLE_QUOTE_STRING,
        terminals.tokenizer()
)

private val tokenDelimiter: Parser<Void> = Parsers.or(
        Scanners.WHITESPACES,
        Scanners.JAVA_BLOCK_COMMENT,
        Scanners.JAVA_LINE_COMMENT
).skipMany()

fun <T> parse(p: Parser<T>, s: String): T =
        p.from(tokens, tokenDelimiter).parse(s)

internal fun token(s: String): Parser<Token> = terminals.token(s)

