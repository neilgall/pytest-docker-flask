package uk.neilgall.rulesapp

import org.jparsec.*
import org.jparsec.Parsers.or

private val keywords = listOf(
        // attribute types
        "string", "number", "request", "rest",

        // rest methods
        "GET", "PUT", "POST", "DELETE",

        // decisions
        "permit", "deny",

        // rules
        "always", "never", "when", "if", "else", "majority", "all", "any",

        // conditions
        "not", "and", "or"
)

private val terminals: Terminals = Terminals
        .operators("=", "!=", ">", ">=", "<", "<=", ",", ".", "+", "-", "*", "/", "~=")
        .words(Scanners.IDENTIFIER)
        .caseInsensitiveKeywords(keywords)
        .build()

private val tokens = or(
        Terminals.IntegerLiteral.TOKENIZER,
        Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
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

