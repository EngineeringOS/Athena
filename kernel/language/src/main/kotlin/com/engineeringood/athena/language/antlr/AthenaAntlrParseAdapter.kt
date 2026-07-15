package com.engineeringood.athena.language.antlr

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SyntaxDiagnostic
import com.engineeringood.athena.language.SystemDeclaration
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token

/*
 * INTERNAL IMPLEMENTATION DETAIL — not part of Athena's public syntax contract.
 *
 * This file is the single, isolated bridge between the generated ANTLR4 parse tree
 * (Story 2.1) and Athena's frozen authored AST (`SourceFileAst` and friends). Every
 * type here is `internal`, so no other Gradle module can import ANTLR-facing types
 * (AD-105 / AD-106). Downstream code depends only on `AthenaLanguageParser.parse`
 * and the `com.engineeringood.athena.language` contracts.
 *
 * Span mapping (AD-109): ANTLR's `charPositionInLine` is 0-based and Athena columns
 * are 1-based, so every column adds 1. ANTLR line numbers are already 1-based. Spans
 * are half-open: `end` points immediately after the final authored character, computed
 * from a token's stop index. No token in the current grammar spans a newline, so the
 * end line always equals the start line of the last token.
 */

/** Internal control-flow signal carrying an authored-AST-level syntax diagnostic. */
internal class AthenaAntlrAdapterFailure(val diagnostic: SyntaxDiagnostic) :
    RuntimeException(diagnostic.message)

/** The only supported entry point for the ANTLR-backed parse path; never throws to callers. */
internal object AthenaAntlrParseEngine {
    fun parse(file: String, source: String): ParseResult {
        return try {
            parseInternal(file, source)
        } catch (failure: AthenaAntlrAdapterFailure) {
            ParseFailure(listOf(failure.diagnostic))
        } catch (throwable: RuntimeException) {
            // AD-109: invalid source must never surface as an opaque parser crash.
            ParseFailure(listOf(fallbackDiagnostic(file, throwable)))
        }
    }

    private fun parseInternal(file: String, source: String): ParseResult {
        val errorListener = AthenaAntlrSyntaxErrorListener(file)

        val lexer = AthenaLexer(CharStreams.fromString(source))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = AthenaParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)

        val tree = try {
            parser.sourceFile()
        } catch (exception: RecognitionException) {
            errorListener.diagnostics.firstOrNull()?.let { return ParseFailure(listOf(it)) }
            return ParseFailure(listOf(recognitionDiagnostic(file, exception)))
        }

        // The handwritten parser failed fast on the first syntax error; preserve that single-diagnostic
        // contract by reporting the first error ANTLR recovered from (its message/position are richest).
        errorListener.diagnostics.firstOrNull()?.let { return ParseFailure(listOf(it)) }

        return ParseSuccess(AthenaAntlrAstAdapter(file).adapt(tree))
    }

    private fun recognitionDiagnostic(file: String, exception: RecognitionException): SyntaxDiagnostic {
        val token = exception.offendingToken
        return if (token != null) {
            SyntaxDiagnostic(
                file = file,
                line = token.line,
                column = token.charPositionInLine + 1,
                message = exception.message ?: "Syntax error",
                span = spanOfToken(token),
            )
        } else {
            fallbackDiagnostic(file, exception)
        }
    }

    private fun fallbackDiagnostic(file: String, throwable: Throwable): SyntaxDiagnostic {
        val position = SourcePosition(offset = 0, line = 1, column = 1)
        return SyntaxDiagnostic(
            file = file,
            line = 1,
            column = 1,
            message = throwable.message ?: "Syntax error",
            span = SourceSpan(position, position),
        )
    }
}

/** Records ANTLR syntax errors as Athena-owned diagnostics instead of writing to stderr. */
internal class AthenaAntlrSyntaxErrorListener(private val file: String) : BaseErrorListener() {
    val diagnostics: MutableList<SyntaxDiagnostic> = mutableListOf()

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?,
    ) {
        val column = charPositionInLine + 1
        val span = if (offendingSymbol is Token) {
            spanOfToken(offendingSymbol)
        } else {
            val position = SourcePosition(offset = -1, line = line, column = column)
            SourceSpan(position, position)
        }
        diagnostics += SyntaxDiagnostic(
            file = file,
            line = line,
            column = column,
            message = msg ?: "Syntax error",
            span = span,
        )
    }
}

/** Walks the generated ANTLR parse tree and constructs the authored AST. */
internal class AthenaAntlrAstAdapter(private val file: String) {
    fun adapt(tree: AthenaParser.SourceFileContext): SourceFileAst {
        val systemContext = tree.systemDecl()
        val systemSpan = spanOfContext(systemContext.start, systemContext.stop)
        return SourceFileAst(
            system = SystemDeclaration(
                name = systemContext.ident().text,
                span = systemSpan,
            ),
            declarations = systemContext.declaration().map { adaptDeclaration(it) },
            span = systemSpan,
        )
    }

    private fun adaptDeclaration(context: AthenaParser.DeclarationContext): Declaration {
        context.deviceDecl()?.let { return adaptDevice(it) }
        context.portDecl()?.let { return adaptPort(it) }
        context.connectDecl()?.let { return adaptConnect(it) }
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = context.start.line,
                column = context.start.charPositionInLine + 1,
                message = "Expected 'device', 'port', or 'connect'",
                span = spanOfContext(context.start, context.stop),
            ),
        )
    }

    private fun adaptDevice(context: AthenaParser.DeviceDeclContext): DeviceDeclaration {
        return DeviceDeclaration(
            name = context.ident().text,
            fields = context.propertyAssignment().map { adaptProperty(it) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptPort(context: AthenaParser.PortDeclContext): PortDeclaration {
        val qualifiedName = adaptQualifiedName(
            context.twoPartName(),
            "Expected qualified port name in owner.port form after 'port'",
        )
        return PortDeclaration(
            qualifiedName = qualifiedName,
            fields = context.propertyAssignment().map { adaptProperty(it) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptConnect(context: AthenaParser.ConnectDeclContext): ConnectionDeclaration {
        val from = adaptQualifiedName(
            context.twoPartName(0),
            "Expected qualified source reference in owner.port form after 'connect'",
        )
        val to = adaptQualifiedName(
            context.twoPartName(1),
            "Expected qualified target reference in owner.port form after '->'",
        )
        return ConnectionDeclaration(
            from = from,
            to = to,
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptProperty(context: AthenaParser.PropertyAssignmentContext): PropertyAssignment {
        val value = adaptScalar(context.scalarValue())
        return PropertyAssignment(
            name = context.ident().text,
            value = value,
            span = SourceSpan(startPosition(context.start), value.span.end),
        )
    }

    private fun adaptScalar(context: AthenaParser.ScalarValueContext): ScalarValue {
        val stringNode = context.STRING()
        if (stringNode != null) {
            val token = stringNode.symbol
            val raw = token.text ?: ""
            val content = if (raw.length >= 2) raw.substring(1, raw.length - 1) else ""
            return ScalarValue.StringLiteral(content, spanOfToken(token))
        }
        val identContext = context.ident()
        return ScalarValue.Identifier(
            identContext.text,
            spanOfContext(identContext.start, identContext.stop),
        )
    }

    /**
     * Enforces the exactly-two-dotted-parts arity for `port`/`connect` endpoints inside the adapter,
     * preserving the handwritten parser's `owner.port` diagnostics on over-/under-qualified names.
     */
    private fun adaptQualifiedName(
        context: AthenaParser.TwoPartNameContext,
        qualifiedMessage: String,
    ): QualifiedName {
        val identContexts = context.ident()
        val parts = identContexts.map { it.text }
        if (parts.size != 2) {
            val firstToken = identContexts.first().start
            throw AthenaAntlrAdapterFailure(
                SyntaxDiagnostic(
                    file = file,
                    line = firstToken.line,
                    column = firstToken.charPositionInLine + 1,
                    message = qualifiedMessage,
                    span = spanOfToken(firstToken),
                ),
            )
        }
        return QualifiedName(parts, spanOfContext(context.start, context.stop))
    }
}

/** Athena start position for [token]: 0-based ANTLR column becomes a 1-based Athena column. */
private fun startPosition(token: Token): SourcePosition {
    return SourcePosition(
        offset = token.startIndex,
        line = token.line,
        column = token.charPositionInLine + 1,
    )
}

/**
 * Athena end position for [token], honoring the half-open span contract: `end` points immediately
 * after the token's final character. No current grammar token spans a newline, so the end line
 * equals the token's line and the end column advances by the token's character length.
 */
private fun endPosition(token: Token): SourcePosition {
    val length = token.stopIndex - token.startIndex + 1
    return SourcePosition(
        offset = token.stopIndex + 1,
        line = token.line,
        column = token.charPositionInLine + 1 + length,
    )
}

private fun spanOfToken(token: Token): SourceSpan = SourceSpan(startPosition(token), endPosition(token))

private fun spanOfContext(start: Token, stop: Token): SourceSpan =
    SourceSpan(startPosition(start), endPosition(stop))
