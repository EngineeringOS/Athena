package com.engineeringood.athena.language

/** Parses the standalone Athena M0 text DSL into a syntax-only AST. */
class AthenaLanguageParser {
    /** Parses [source] from [file] and returns either an AST or syntax diagnostics. */
    fun parse(file: String, source: String): ParseResult {
        return when (val tokenization = AthenaTokenizer(file, source).tokenize()) {
            is TokenizationFailure -> ParseFailure(listOf(tokenization.diagnostic))
            is TokenizationSuccess -> {
                try {
                    ParseSuccess(AthenaParser(file, tokenization.tokens).parseSource())
                } catch (exception: ParseException) {
                    ParseFailure(listOf(exception.diagnostic))
                }
            }
        }
    }
}

/** Converts raw source text into the token stream consumed by the recursive-descent parser. */
private class AthenaTokenizer(
    private val file: String,
    private val source: String,
) {
    fun tokenize(): TokenizationResult {
        val tokens = mutableListOf<Token>()
        var index = 0
        var line = 1
        var column = 1

        fun currentPosition() = SourcePosition(index, line, column)

        fun advance(): Char {
            val char = source[index++]
            if (char == '\n') {
                line += 1
                column = 1
            } else {
                column += 1
            }
            return char
        }

        fun emit(kind: TokenKind, lexeme: String, start: SourcePosition, end: SourcePosition) {
            tokens += Token(kind, lexeme, SourceSpan(start, end))
        }

        while (index < source.length) {
            val char = source[index]

            when {
                index == 0 && char == '\uFEFF' -> {
                    advance()
                }

                char.isWhitespace() -> {
                    advance()
                }

                char == '{' -> {
                    val start = currentPosition()
                    advance()
                    emit(TokenKind.LEFT_BRACE, "{", start, currentPosition())
                }

                char == '}' -> {
                    val start = currentPosition()
                    advance()
                    emit(TokenKind.RIGHT_BRACE, "}", start, currentPosition())
                }

                char == '.' -> {
                    val start = currentPosition()
                    advance()
                    emit(TokenKind.DOT, ".", start, currentPosition())
                }

                char == '-' && source.getOrNull(index + 1) == '>' -> {
                    val start = currentPosition()
                    advance()
                    advance()
                    emit(TokenKind.ARROW, "->", start, currentPosition())
                }

                char == '"' -> {
                    val start = currentPosition()
                    advance()
                    val builder = StringBuilder()
                    while (index < source.length && source[index] != '"') {
                        builder.append(advance())
                    }
                    if (index >= source.length) {
                        return failure("Unterminated string literal", start, currentPosition())
                    }
                    advance()
                    emit(TokenKind.STRING, builder.toString(), start, currentPosition())
                }

                char.isIdentifierStart() -> {
                    val start = currentPosition()
                    val builder = StringBuilder()
                    builder.append(advance())
                    while (index < source.length && source[index].isIdentifierPart()) {
                        builder.append(advance())
                    }
                    emit(TokenKind.IDENTIFIER, builder.toString(), start, currentPosition())
                }

                else -> {
                    val start = currentPosition()
                    advance()
                    return failure("Unexpected character '$char'", start, currentPosition())
                }
            }
        }

        val end = currentPosition()
        tokens += Token(TokenKind.EOF, "", SourceSpan(end, end))
        return TokenizationSuccess(tokens)
    }

    private fun failure(message: String, start: SourcePosition, end: SourcePosition): TokenizationFailure {
        return TokenizationFailure(
            SyntaxDiagnostic(
                file = file,
                line = start.line,
                column = start.column,
                message = message,
                span = SourceSpan(start, end),
            ),
        )
    }
}

/** Recursive-descent parser for the minimal M0 declaration grammar. */
private class AthenaParser(
    private val file: String,
    private val tokens: List<Token>,
) {
    private var current = 0

    fun parseSource(): SourceFileAst {
        val systemKeyword = consumeKeyword("system")
        val systemName = consumeIdentifier("Expected system name after 'system'")
        val systemStart = systemKeyword.span.start
        consume(TokenKind.LEFT_BRACE, "Expected '{' after system name")
        val declarations = buildList {
            while (!check(TokenKind.RIGHT_BRACE) && !isAtEnd()) {
                add(parseDeclaration())
            }
        }
        val closingBrace = consume(TokenKind.RIGHT_BRACE, "Expected '}' after system body")
        consume(TokenKind.EOF, "Expected end of file")

        return SourceFileAst(
            system = SystemDeclaration(
                name = systemName.lexeme,
                span = SourceSpan(systemStart, closingBrace.span.end),
            ),
            declarations = declarations,
            span = SourceSpan(systemStart, closingBrace.span.end),
        )
    }

    private fun parseDeclaration(): Declaration {
        return when {
            matchKeyword("device") -> parseDevice(previous())
            matchKeyword("port") -> parsePort(previous())
            matchKeyword("connect") -> parseConnection(previous())
            else -> throw error(peek(), "Expected 'device', 'port', or 'connect'")
        }
    }

    private fun parseDevice(keyword: Token): DeviceDeclaration {
        val name = consumeIdentifier("Expected device name after 'device'")
        consume(TokenKind.LEFT_BRACE, "Expected '{' after device name")
        val fields = parsePropertyAssignments()
        val closingBrace = consume(TokenKind.RIGHT_BRACE, "Expected '}' after device body")
        return DeviceDeclaration(
            name = name.lexeme,
            fields = fields,
            span = SourceSpan(keyword.span.start, closingBrace.span.end),
        )
    }

    private fun parsePort(keyword: Token): PortDeclaration {
        val qualifiedName = parseQualifiedName(
            message = "Expected qualified port name after 'port'",
            minimumParts = 2,
            maximumParts = 2,
            qualifiedMessage = "Expected qualified port name in owner.port form after 'port'",
        )
        consume(TokenKind.LEFT_BRACE, "Expected '{' after port name")
        val fields = parsePropertyAssignments()
        val closingBrace = consume(TokenKind.RIGHT_BRACE, "Expected '}' after port body")
        return PortDeclaration(
            qualifiedName = qualifiedName,
            fields = fields,
            span = SourceSpan(keyword.span.start, closingBrace.span.end),
        )
    }

    private fun parseConnection(keyword: Token): ConnectionDeclaration {
        val from = parseQualifiedName(
            message = "Expected qualified source reference after 'connect'",
            minimumParts = 2,
            maximumParts = 2,
            qualifiedMessage = "Expected qualified source reference in owner.port form after 'connect'",
        )
        consume(TokenKind.ARROW, "Expected '->' between connection endpoints")
        val to = parseQualifiedName(
            message = "Expected qualified target reference after '->'",
            minimumParts = 2,
            maximumParts = 2,
            qualifiedMessage = "Expected qualified target reference in owner.port form after '->'",
        )
        return ConnectionDeclaration(
            from = from,
            to = to,
            span = SourceSpan(keyword.span.start, to.span.end),
        )
    }

    private fun parsePropertyAssignments(): List<PropertyAssignment> {
        return buildList {
            while (!check(TokenKind.RIGHT_BRACE) && !isAtEnd()) {
                val name = consumeIdentifier("Expected property name inside block")
                val value = parseScalarValue()
                add(PropertyAssignment(name.lexeme, value, SourceSpan(name.span.start, value.span.end)))
            }
        }
    }

    private fun parseScalarValue(): ScalarValue {
        return when {
            match(TokenKind.STRING) -> ScalarValue.StringLiteral(previous().lexeme, previous().span)
            match(TokenKind.IDENTIFIER) -> ScalarValue.Identifier(previous().lexeme, previous().span)
            else -> throw error(peek(), "Expected identifier or string literal")
        }
    }

    private fun parseQualifiedName(
        message: String,
        minimumParts: Int = 1,
        maximumParts: Int = Int.MAX_VALUE,
        qualifiedMessage: String = message,
    ): QualifiedName {
        val first = consumeIdentifier(message)
        val parts = mutableListOf(first.lexeme)
        var end = first.span.end
        while (match(TokenKind.DOT)) {
            val part = consumeIdentifier("Expected identifier after '.'")
            parts += part.lexeme
            end = part.span.end
        }
        if (parts.size < minimumParts) {
            throw error(first, qualifiedMessage)
        }
        if (parts.size > maximumParts) {
            throw error(first, qualifiedMessage)
        }
        return QualifiedName(parts, SourceSpan(first.span.start, end))
    }

    private fun consumeKeyword(keyword: String): Token {
        return if (matchKeyword(keyword)) {
            previous()
        } else {
            throw error(peek(), "Expected '$keyword'")
        }
    }

    private fun consumeIdentifier(message: String): Token {
        return consume(TokenKind.IDENTIFIER, message)
    }

    private fun consume(kind: TokenKind, message: String): Token {
        if (check(kind)) {
            return advance()
        }
        throw error(peek(), message)
    }

    private fun match(kind: TokenKind): Boolean {
        if (!check(kind)) {
            return false
        }
        advance()
        return true
    }

    private fun matchKeyword(keyword: String): Boolean {
        if (peek().kind != TokenKind.IDENTIFIER || peek().lexeme != keyword) {
            return false
        }
        advance()
        return true
    }

    private fun check(kind: TokenKind): Boolean {
        if (isAtEnd()) {
            return kind == TokenKind.EOF
        }
        return peek().kind == kind
    }

    private fun advance(): Token {
        if (!isAtEnd()) {
            current += 1
        }
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().kind == TokenKind.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseException {
        return ParseException(
            SyntaxDiagnostic(
                file = file,
                line = token.span.start.line,
                column = token.span.start.column,
                message = message,
                span = token.span,
            ),
        )
    }
}

/** Single lexical token emitted by the Athena tokenizer. */
private data class Token(
    val kind: TokenKind,
    val lexeme: String,
    val span: SourceSpan,
)

/** Token kinds required by the current M0 grammar surface. */
private enum class TokenKind {
    IDENTIFIER,
    STRING,
    LEFT_BRACE,
    RIGHT_BRACE,
    DOT,
    ARROW,
    EOF,
}

/** Internal control-flow exception used to unwind to a parse diagnostic. */
private class ParseException(val diagnostic: SyntaxDiagnostic) : RuntimeException(diagnostic.message)

/** Result of lexical tokenization before grammar parsing begins. */
private sealed interface TokenizationResult

/** Successful tokenization containing the full token stream. */
private data class TokenizationSuccess(val tokens: List<Token>) : TokenizationResult

/** Failed tokenization containing the first syntax diagnostic encountered. */
private data class TokenizationFailure(val diagnostic: SyntaxDiagnostic) : TokenizationResult

private fun Char.isIdentifierStart(): Boolean = isLetter() || this == '_'

private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_'
