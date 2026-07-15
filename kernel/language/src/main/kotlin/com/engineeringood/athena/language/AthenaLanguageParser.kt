package com.engineeringood.athena.language

import com.engineeringood.athena.language.antlr.AthenaAntlrParseEngine

/**
 * Parses the standalone Athena M0 text DSL into a syntax-only AST.
 *
 * This facade is the only supported entry point for obtaining Athena-owned syntax contracts
 * ([ParseResult]). Callers must depend on the returned Athena-owned types and must not depend
 * on how parsing is implemented internally.
 *
 * As of M17 Epic 2 (Stories 2.2/2.3), parsing runs end to end through the generated ANTLR4
 * lexer/parser and the internal `ParseAdapter` in `com.engineeringood.athena.language.antlr`,
 * which are implementation details behind this unchanged facade (AD-105). The handwritten
 * recursive-descent parser has been removed so there is only one live production parse path.
 */
class AthenaLanguageParser {
    /**
     * Parses [source] from [file] and returns either an AST or syntax diagnostics.
     *
     * The return type [ParseResult] is the only supported way for callers to obtain a
     * syntax-owned AST or syntax failure. Callers must not depend on the ANTLR lexer/parser,
     * parse-tree, or adapter internals behind this method. Invalid source always returns a
     * typed [ParseFailure] with real provenance rather than throwing to the caller (AD-109).
     */
    fun parse(file: String, source: String): ParseResult = AthenaAntlrParseEngine.parse(file, source)
}
