package com.engineeringood.athena.language.antlr

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.DrawingGridPosition
import com.engineeringood.athena.language.ElementAnchorExportDeclaration
import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ElementLabelExportDeclaration
import com.engineeringood.athena.language.ElementNumberField
import com.engineeringood.athena.language.EngineeringFunctionDeclaration
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.BindingSelectorKind
import com.engineeringood.athena.language.ImportDeclaration
import com.engineeringood.athena.language.InstallationChannelDeclaration
import com.engineeringood.athena.language.InstallationClearanceLiteral
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.InstallationDuctDeclaration
import com.engineeringood.athena.language.InstallationEnclosureDeclaration
import com.engineeringood.athena.language.InstallationKind
import com.engineeringood.athena.language.InstallationLengthLiteral
import com.engineeringood.athena.language.InstallationMountDeclaration
import com.engineeringood.athena.language.InstallationMountOrientation
import com.engineeringood.athena.language.InstallationOrientation
import com.engineeringood.athena.language.InstallationPointLiteral
import com.engineeringood.athena.language.InstallationRailDeclaration
import com.engineeringood.athena.language.InstallationRouteDeclaration
import com.engineeringood.athena.language.InstallationSize3Literal
import com.engineeringood.athena.language.InstallationSizeLiteral
import com.engineeringood.athena.language.InstallationSurfaceDeclaration
import com.engineeringood.athena.language.InstallationTerminalGroupDeclaration
import com.engineeringood.athena.language.LayoutAxis
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutOrientation
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.PackageDeclaration
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.ProfileDeclaration
import com.engineeringood.athena.language.ProjectSourceUnit
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.RepresentationDeclaration
import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationResourceKind
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SyntaxDiagnostic
import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolBounds
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolDynamicLabelDeclaration
import com.engineeringood.athena.language.SymbolGraphicDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import com.engineeringood.athena.language.SymbolIdentifierField
import com.engineeringood.athena.language.SymbolPoint
import com.engineeringood.athena.language.SymbolSize
import com.engineeringood.athena.language.SymbolStringField
import com.engineeringood.athena.language.SystemDeclaration
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

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
        val errorListener = AthenaAntlrSyntaxErrorListener(file, source)

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
        val firstDiagnostic = listOfNotNull(
            errorListener.diagnostics.firstOrNull(),
            splitImportTargetDiagnostic(file, tree.importDecl()),
        ).minByOrNull { it.span.start.offset }
        firstDiagnostic?.let { return ParseFailure(listOf(it)) }

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

private fun splitImportTargetDiagnostic(
    file: String,
    imports: List<AthenaParser.ImportDeclContext>,
): SyntaxDiagnostic? {
    imports.forEach { context ->
        val importToken = context.IMPORT()?.symbol ?: return@forEach
        val target = context.packageName()?.start ?: return@forEach
        if (target.line == importToken.line) return@forEach
        return SyntaxDiagnostic(
            file = file,
            line = target.line,
            column = target.charPositionInLine + 1,
            message = "Expected import target after 'import'",
            span = spanOfToken(target),
        )
    }
    return null
}

/** Records ANTLR syntax errors as Athena-owned diagnostics instead of writing to stderr. */
internal class AthenaAntlrSyntaxErrorListener(
    private val file: String,
    private val source: String,
) : BaseErrorListener() {
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
            val offset = sourceOffset(source, line, column)
            SourceSpan(
                SourcePosition(offset = offset, line = line, column = column),
                SourcePosition(
                    offset = (offset + 1).coerceAtMost(source.length),
                    line = line,
                    column = column + if (offset < source.length) 1 else 0,
                ),
            )
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

private fun sourceOffset(source: String, line: Int, column: Int): Int {
    var lineStart = 0
    repeat((line - 1).coerceAtLeast(0)) {
        val newline = source.indexOf('\n', lineStart)
        if (newline < 0) return source.length
        lineStart = newline + 1
    }
    return (lineStart + column - 1).coerceIn(0, source.length)
}

/** Walks the generated ANTLR parse tree and constructs the authored AST. */
internal class AthenaAntlrAstAdapter(private val file: String) {
    fun adapt(tree: AthenaParser.SourceFileContext): SourceFileAst {
        val packageDeclaration = tree.packageDecl()?.let { adaptPackage(it) }
        val imports = adaptImports(tree.importDecl())
        val systemContext = tree.systemDecl()
        val unit = if (systemContext != null) {
            ProjectSourceUnit(
                system = SystemDeclaration(
                    name = systemContext.ident().text,
                    span = spanOfContext(systemContext.start, systemContext.stop),
                ),
                declarations = systemContext.declaration().map { adaptDeclaration(it) },
            )
        } else {
            RepresentationSourceUnit(tree.representationDecl().map(::adaptRepresentation))
        }
        val unitStart = systemContext?.start ?: tree.representationDecl().first().start
        val unitStop = systemContext?.stop ?: tree.representationDecl().last().stop
        val unitSpan = spanOfContext(unitStart, unitStop)
        val fileStart = packageDeclaration?.span?.start ?: imports.firstOrNull()?.span?.start ?: unitSpan.start
        return SourceFileAst(
            unit = unit,
            span = SourceSpan(fileStart, unitSpan.end),
            packageDeclaration = packageDeclaration,
            imports = imports,
        )
    }

    private fun adaptPackage(context: AthenaParser.PackageDeclContext): PackageDeclaration {
        val nameContext = context.packageName()
        return PackageDeclaration(
            name = adaptHeaderQualifiedName(nameContext, "package name"),
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptImport(context: AthenaParser.ImportDeclContext): ImportDeclaration {
        val targetContext = context.packageName()
        return ImportDeclaration(
            target = adaptHeaderQualifiedName(targetContext, "import target"),
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptImports(contexts: List<AthenaParser.ImportDeclContext>): List<ImportDeclaration> {
        val seenTargets = mutableSetOf<List<String>>()
        return contexts.map { context ->
            val declaration = adaptImport(context)
            if (!seenTargets.add(declaration.target.parts)) {
                val importToken = context.IMPORT().symbol
                throw AthenaAntlrAdapterFailure(
                    SyntaxDiagnostic(
                        file = file,
                        line = importToken.line,
                        column = importToken.charPositionInLine + 1,
                        message = "Duplicate import target '${declaration.target.parts.joinToString(".")}'",
                        span = spanOfToken(importToken),
                    ),
                )
            }
            declaration
        }
    }

    private fun adaptHeaderQualifiedName(
        context: AthenaParser.PackageNameContext,
        description: String,
    ): QualifiedName {
        rejectQualifiedNameTrivia(context, description)
        return QualifiedName(
            parts = context.packageNameSegment().map { it.text },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun rejectQualifiedNameTrivia(
        context: AthenaParser.PackageNameContext,
        description: String,
    ) {
        val gap = terminalTokens(context).zipWithNext().firstOrNull { (left, right) ->
            left.stopIndex + 1 != right.startIndex
        } ?: return
        val gapStart = endPosition(gap.first)
        val gapEnd = startPosition(gap.second)
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = gapStart.line,
                column = gapStart.column,
                message = "Whitespace is not allowed inside the $description",
                span = SourceSpan(gapStart, gapEnd),
            ),
        )
    }

    private fun adaptDeclaration(context: AthenaParser.DeclarationContext): Declaration {
        context.deviceDecl()?.let { return adaptDevice(it) }
        context.portDecl()?.let { return adaptPort(it) }
        context.connectGroupDecl()?.let { return adaptConnectGroup(it) }
        context.connectDecl()?.let { return adaptConnect(it) }
        context.layoutDecl()?.let { return adaptLayout(it) }
        context.installationDecl()?.let { return adaptInstallation(it) }
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = context.start.line,
                column = context.start.charPositionInLine + 1,
                message = "Expected 'device', 'port', 'connect', 'layout', or 'installation'",
                span = spanOfContext(context.start, context.stop),
            ),
        )
    }

    private fun adaptDevice(context: AthenaParser.DeviceDeclContext): DeviceDeclaration {
        val deviceName = context.ident().text
        val members = context.deviceMember()
        return DeviceDeclaration(
            name = deviceName,
            fields = members.mapNotNull { it.propertyAssignment()?.let { property -> adaptProperty(property) } },
            span = spanOfContext(context.start, context.stop),
            nestedPorts = members.mapNotNull { it.nestedPortDecl()?.let { port -> adaptNestedPort(deviceName, port) } },
            nestedFunctions = members.mapNotNull { member ->
                member.functionDecl()?.let { function -> adaptFunction(function) }
            },
        )
    }

    private fun adaptFunction(context: AthenaParser.FunctionDeclContext): EngineeringFunctionDeclaration {
        val members = context.functionMember()
        val role = singletonMember(
            members.mapNotNull { member -> member.functionRoleDecl() },
            "role",
            "Function",
        ) ?: missingFunctionMember(context, "role")
        val ports = singletonMember(
            members.mapNotNull { member -> member.functionPortsDecl() },
            "ports",
            "Function",
        ) ?: missingFunctionMember(context, "ports")
        return EngineeringFunctionDeclaration(
            name = context.ident().text,
            role = SymbolIdentifierField(
                value = role.ident().text,
                span = spanOfContext(role.ident().start, role.ident().stop),
            ),
            portReferences = ports.functionPortReference().map { reference ->
                QualifiedName(
                    parts = reference.ident().map { part -> part.text },
                    span = spanOfContext(reference.start, reference.stop),
                )
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun missingFunctionMember(
        context: AthenaParser.FunctionDeclContext,
        member: String,
    ): Nothing = throw AthenaAntlrAdapterFailure(
        SyntaxDiagnostic(
            file = file,
            line = context.start.line,
            column = context.start.charPositionInLine + 1,
            message = "Function '${context.ident().text}' requires one '$member' declaration",
            span = spanOfContext(context.start, context.stop),
        ),
    )

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

    private fun adaptNestedPort(
        ownerName: String,
        context: AthenaParser.NestedPortDeclContext,
    ): PortDeclaration {
        val nameContext = context.ident()
        return PortDeclaration(
            qualifiedName = QualifiedName(
                parts = listOf(ownerName, nameContext.text),
                span = spanOfContext(nameContext.start, nameContext.stop),
            ),
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
            alias = context.ident().text,
            aliasSpan = spanOfContext(context.ident().start, context.ident().stop),
            from = from,
            to = to,
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptConnectGroup(context: AthenaParser.ConnectGroupDeclContext): ConnectionGroupDeclaration {
        return ConnectionGroupDeclaration(
            name = context.ident().text,
            connections = context.connectGroupEdge().map { edge -> adaptConnectGroupEdge(edge) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptConnectGroupEdge(context: AthenaParser.ConnectGroupEdgeContext): ConnectionDeclaration {
        val from = adaptQualifiedName(
            context.twoPartName(0),
            "Expected qualified source reference in owner.port form after 'connect'",
        )
        val to = adaptQualifiedName(
            context.twoPartName(1),
            "Expected qualified target reference in owner.port form after '->'",
        )
        return ConnectionDeclaration(
            alias = context.ident().text,
            aliasSpan = spanOfContext(context.ident().start, context.ident().stop),
            from = from,
            to = to,
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptLayout(context: AthenaParser.LayoutDeclContext): LayoutDeclaration {
        return LayoutDeclaration(
            viewFamily = context.viewFamilyName().text,
            statements = context.layoutStatement().map { adaptLayoutStatement(it) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptLayoutStatement(context: AthenaParser.LayoutStatementContext): LayoutStatement {
        context.placeStatement()?.let { return adaptPlaceStatement(it) }
        context.alignStatement()?.let { return adaptAlignStatement(it) }
        context.groupStatement()?.let { return adaptGroupStatement(it) }
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = context.start.line,
                column = context.start.charPositionInLine + 1,
                message = "Expected layout statement",
                span = spanOfContext(context.start, context.stop),
            ),
        )
    }

    private fun adaptPlaceStatement(context: AthenaParser.PlaceStatementContext): LayoutStatement {
        context.authoredLayoutReference()?.let { reference ->
            val position = context.drawingGridPosition()
            val coordinates = position.positiveInteger().map { integer -> integer.text.toInt() }
            return LayoutStatement.PlaceAt(
                subject = QualifiedName(
                    parts = reference.ident().map { part -> part.text },
                    span = spanOfContext(reference.start, reference.stop),
                ),
                position = DrawingGridPosition(
                    column = coordinates[0],
                    row = coordinates[1],
                    span = spanOfContext(position.start, position.stop),
                ),
                orientation = when (context.layoutOrientation().text) {
                    "horizontal" -> LayoutOrientation.Horizontal
                    "vertical" -> LayoutOrientation.Vertical
                    else -> throw AthenaAntlrAdapterFailure(
                        SyntaxDiagnostic(
                            file = file,
                            line = context.layoutOrientation().start.line,
                            column = context.layoutOrientation().start.charPositionInLine + 1,
                            message = "Expected layout orientation 'horizontal' or 'vertical'",
                            span = spanOfContext(context.layoutOrientation().start, context.layoutOrientation().stop),
                        ),
                    )
                },
                span = spanOfContext(context.start, context.stop),
            )
        }
        val subject = context.ident(0).text
        val target = context.ident(1).text
        val span = spanOfContext(context.start, context.stop)
        return when (context.layoutPlacementRelation().text) {
            "near" -> LayoutStatement.PlaceNear(subject, target, span)
            "below" -> LayoutStatement.PlaceBelow(subject, target, span)
            else -> throw AthenaAntlrAdapterFailure(
                SyntaxDiagnostic(
                    file = file,
                    line = context.layoutPlacementRelation().start.line,
                    column = context.layoutPlacementRelation().start.charPositionInLine + 1,
                    message = "Expected layout placement relation 'near' or 'below'",
                    span = spanOfContext(context.layoutPlacementRelation().start, context.layoutPlacementRelation().stop),
                ),
            )
        }
    }

    private fun adaptAlignStatement(context: AthenaParser.AlignStatementContext): LayoutStatement.AlignWith {
        return LayoutStatement.AlignWith(
            subject = context.ident(0).text,
            target = context.ident(1).text,
            axis = when (context.layoutAxis().text) {
                "horizontal" -> LayoutAxis.Horizontal
                "vertical" -> LayoutAxis.Vertical
                else -> throw AthenaAntlrAdapterFailure(
                    SyntaxDiagnostic(
                        file = file,
                        line = context.layoutAxis().start.line,
                        column = context.layoutAxis().start.charPositionInLine + 1,
                        message = "Expected layout axis 'horizontal' or 'vertical'",
                        span = spanOfContext(context.layoutAxis().start, context.layoutAxis().stop),
                    ),
                )
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptGroupStatement(context: AthenaParser.GroupStatementContext): LayoutStatement.GroupWith {
        return LayoutStatement.GroupWith(
            subject = context.ident(0).text,
            target = context.ident(1).text,
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptInstallation(context: AthenaParser.InstallationDeclContext): InstallationDeclaration {
        val members = context.installationMember()
        val enclosures = members.mapNotNull { it.enclosureDecl()?.let(::adaptInstallationEnclosure) }
        val surfaces = members.mapNotNull { it.installationSurfaceDecl()?.let(::adaptInstallationSurface) }
        val rails = members.mapNotNull { it.installationRailDecl()?.let(::adaptInstallationRail) }
        val ducts = members.mapNotNull { it.installationDuctDecl()?.let(::adaptInstallationDuct) }
        val channels = members.mapNotNull { it.installationChannelDecl()?.let(::adaptInstallationChannel) }
        val terminalGroups = members.mapNotNull {
            it.installationTerminalGroupDecl()?.let(::adaptInstallationTerminalGroup)
        }
        val mounts = members.mapNotNull { it.installationMountDecl()?.let(::adaptInstallationMount) }
        rejectDuplicateInstallationMemberIds(
            enclosures.map { it.id to it.span } +
                surfaces.map { it.id to it.span } +
                rails.map { it.id to it.span } +
                ducts.map { it.id to it.span } +
                channels.map { it.id to it.span } +
                terminalGroups.map { it.id to it.span } +
                mounts.map { it.id to it.span },
        )
        return InstallationDeclaration(
            name = context.ident().text,
            kind = InstallationKind.Cabinet,
            enclosures = enclosures,
            surfaces = surfaces,
            rails = rails,
            ducts = ducts,
            channels = channels,
            terminalGroups = terminalGroups,
            mounts = mounts,
            routes = members.mapNotNull { it.installationRouteDecl()?.let(::adaptInstallationRoute) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun rejectDuplicateInstallationMemberIds(ids: List<Pair<String, SourceSpan>>) {
        val seen = mutableSetOf<String>()
        ids.forEach { (id, span) ->
            if (!seen.add(id)) {
                throw AthenaAntlrAdapterFailure(
                    SyntaxDiagnostic(
                        file = file,
                        line = span.start.line,
                        column = span.start.column,
                        message = "Duplicate installation member id '$id'",
                        span = span,
                    ),
                )
            }
        }
    }

    private fun adaptInstallationEnclosure(context: AthenaParser.EnclosureDeclContext): InstallationEnclosureDeclaration =
        InstallationEnclosureDeclaration(
            id = context.ident().text,
            size = adaptLengthTuple3(context.lengthTuple3()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationSurface(context: AthenaParser.InstallationSurfaceDeclContext): InstallationSurfaceDeclaration =
        InstallationSurfaceDeclaration(
            id = context.ident(0).text,
            enclosureId = context.ident(1).text,
            at = adaptLengthPoint(context.lengthPoint()),
            size = adaptLengthSize(context.lengthSize()),
            acceptedMountingTypes = adaptIdentList(context.identList()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationRail(context: AthenaParser.InstallationRailDeclContext): InstallationRailDeclaration =
        InstallationRailDeclaration(
            id = context.ident(0).text,
            surfaceId = context.ident(1).text,
            at = adaptLengthPoint(context.lengthPoint()),
            length = adaptLength(context.lengthLiteral()),
            orientation = adaptInstallationOrientation(context.installationOrientation()),
            mountingType = context.ident(2).text,
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationDuct(context: AthenaParser.InstallationDuctDeclContext): InstallationDuctDeclaration =
        InstallationDuctDeclaration(
            id = context.ident(0).text,
            enclosureId = context.ident(1).text,
            at = adaptLengthPoint(context.lengthPoint()),
            size = adaptLengthSize(context.lengthSize()),
            orientation = adaptInstallationOrientation(context.installationOrientation()),
            wall = adaptLength(context.lengthLiteral()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationChannel(context: AthenaParser.InstallationChannelDeclContext): InstallationChannelDeclaration =
        InstallationChannelDeclaration(
            id = context.ident(0).text,
            ductId = context.ident(1).text,
            at = adaptLengthPoint(context.lengthPoint()),
            size = adaptLengthSize(context.lengthSize()),
            lanes = context.positiveInteger().text.toInt(),
            margin = adaptLength(context.lengthLiteral()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationTerminalGroup(
        context: AthenaParser.InstallationTerminalGroupDeclContext,
    ): InstallationTerminalGroupDeclaration =
        InstallationTerminalGroupDeclaration(
            id = context.ident(0).text,
            enclosureId = context.ident(1).text,
            at = adaptLengthPoint(context.lengthPoint()),
            size = adaptLengthSize(context.lengthSize()),
            orientation = adaptInstallationOrientation(context.installationOrientation()),
            acceptedMountingTypes = adaptIdentList(context.identList()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationMount(context: AthenaParser.InstallationMountDeclContext): InstallationMountDeclaration =
        context.installationMountMember().let { members ->
            InstallationMountDeclaration(
                deviceId = context.ident(0).text,
                id = context.ident(1).text,
                targetId = context.ident(2).text,
                at = adaptLengthPoint(context.lengthPoint()),
                footprint = adaptLengthTuple3(
                    requiredMountMember(context, members.mapNotNull { it.installationFootprintDecl() }, "footprint").lengthTuple3(),
                ),
                mountingType = requiredMountMember(
                    context,
                    members.mapNotNull { it.installationMountingDecl() },
                    "mounting",
                ).ident().text,
                orientation = adaptInstallationMountOrientation(
                    requiredMountMember(
                        context,
                        members.mapNotNull { it.installationMountOrientationDecl() },
                        "orientation",
                    ).installationMountOrientation(),
                ),
                allowedOrientations = requiredMountMember(
                    context,
                    members.mapNotNull { it.installationAllowedOrientationsDecl() },
                    "allowed-orientations",
                ).installationMountOrientationList().installationMountOrientation().map(::adaptInstallationMountOrientation),
                clearance = adaptInstallationClearance(
                    requiredMountMember(
                        context,
                        members.mapNotNull { it.installationClearanceDecl() },
                        "clearance",
                    ).lengthTuple4(),
                ),
                compatibleContainerKinds = adaptIdentList(
                    requiredMountMember(
                        context,
                        members.mapNotNull { it.installationCompatibleContainersDecl() },
                        "compatible-containers",
                    ).identList(),
                ),
                span = spanOfContext(context.start, context.stop),
            )
        }

    private fun <T : ParserRuleContext> requiredMountMember(
        mount: AthenaParser.InstallationMountDeclContext,
        members: List<T>,
        name: String,
    ): T {
        if (members.size == 1) return members.single()
        val issue = if (members.isEmpty()) "requires one" else "must not repeat"
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = mount.start.line,
                column = mount.start.charPositionInLine + 1,
                message = "Mount '${mount.ident(1).text}' $issue '$name' declaration",
                span = spanOfContext(mount.start, mount.stop),
            ),
        )
    }

    private fun adaptInstallationMountOrientation(
        context: AthenaParser.InstallationMountOrientationContext,
    ): InstallationMountOrientation = when (context.text) {
        "deg0" -> InstallationMountOrientation.Deg0
        "deg90" -> InstallationMountOrientation.Deg90
        "deg180" -> InstallationMountOrientation.Deg180
        "deg270" -> InstallationMountOrientation.Deg270
        else -> error("ANTLR admitted unsupported mount orientation '${context.text}'.")
    }

    private fun adaptInstallationClearance(
        context: AthenaParser.LengthTuple4Context,
    ): InstallationClearanceLiteral = InstallationClearanceLiteral(
        top = adaptLength(context.lengthLiteral(0)),
        right = adaptLength(context.lengthLiteral(1)),
        bottom = adaptLength(context.lengthLiteral(2)),
        left = adaptLength(context.lengthLiteral(3)),
        span = spanOfContext(context.start, context.stop),
    )

    private fun adaptInstallationRoute(context: AthenaParser.InstallationRouteDeclContext): InstallationRouteDeclaration =
        InstallationRouteDeclaration(
            connectionAlias = context.ident().text,
            channelIds = adaptIdentList(context.identList()),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptInstallationOrientation(context: AthenaParser.InstallationOrientationContext): InstallationOrientation =
        when (context.text) {
            "horizontal" -> InstallationOrientation.Horizontal
            "vertical" -> InstallationOrientation.Vertical
            else -> throw AthenaAntlrAdapterFailure(
                SyntaxDiagnostic(
                    file = file,
                    line = context.start.line,
                    column = context.start.charPositionInLine + 1,
                    message = "Expected installation orientation 'horizontal' or 'vertical'",
                    span = spanOfContext(context.start, context.stop),
                ),
            )
        }

    private fun adaptIdentList(context: AthenaParser.IdentListContext): List<String> =
        context.ident().map { it.text }

    private fun adaptLengthPoint(context: AthenaParser.LengthPointContext): InstallationPointLiteral =
        InstallationPointLiteral(
            x = adaptLength(context.lengthLiteral(0)),
            y = adaptLength(context.lengthLiteral(1)),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptLengthSize(context: AthenaParser.LengthSizeContext): InstallationSizeLiteral =
        InstallationSizeLiteral(
            width = adaptLength(context.lengthLiteral(0)),
            height = adaptLength(context.lengthLiteral(1)),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptLengthTuple3(context: AthenaParser.LengthTuple3Context): InstallationSize3Literal =
        InstallationSize3Literal(
            width = adaptLength(context.lengthLiteral(0)),
            height = adaptLength(context.lengthLiteral(1)),
            depth = adaptLength(context.lengthLiteral(2)),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptLength(context: AthenaParser.LengthLiteralContext): InstallationLengthLiteral =
        InstallationLengthLiteral(
            value = adaptNumber(context.number()),
            unit = context.MM().text,
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptSymbol(context: AthenaParser.SymbolDeclContext): SymbolDeclaration {
        val members = context.symbolMember()
        return SymbolDeclaration(
            name = context.ident().text,
            identity = singletonMember(members.mapNotNull { it.identityDecl() }, "identity")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            version = singletonMember(members.mapNotNull { it.versionDecl() }, "version")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            resources = members.mapNotNull { it.resourceDecl()?.let(::adaptResource) },
            graphic = singletonMember(members.mapNotNull { it.graphicDecl() }, "graphic")?.let(::adaptGraphic),
            anchors = members.mapNotNull { it.anchorDecl()?.let(::adaptAnchor) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptRepresentation(context: AthenaParser.RepresentationDeclContext): RepresentationDeclaration {
        context.symbolDecl()?.let { return adaptSymbol(it) }
        context.elementDecl()?.let { return adaptElement(it) }
        context.profileDecl()?.let { return adaptProfile(it) }
        context.bindingDecl()?.let { return adaptBinding(it) }
        throw AthenaAntlrAdapterFailure(
            SyntaxDiagnostic(
                file = file,
                line = context.start.line,
                column = context.start.charPositionInLine + 1,
                message = "Expected 'symbol', 'element', 'profile', or 'binding'",
                span = spanOfContext(context.start, context.stop),
            ),
        )
    }

    private fun adaptProfile(context: AthenaParser.ProfileDeclContext): ProfileDeclaration {
        val members = context.profileMember()
        return ProfileDeclaration(
            name = context.ident().text,
            projection = singletonMember(members.mapNotNull { it.projectionDecl() }, "projection", "Profile")?.let { declaration ->
                SymbolIdentifierField(declaration.profileValueName().text, spanOfContext(declaration.start, declaration.stop))
            },
            standard = singletonMember(members.mapNotNull { it.standardDecl() }, "standard", "Profile")?.let { declaration ->
                SymbolIdentifierField(declaration.profileValueName().text, spanOfContext(declaration.start, declaration.stop))
            },
            style = singletonMember(members.mapNotNull { it.styleDecl() }, "style", "Profile")?.let { declaration ->
                SymbolIdentifierField(declaration.profileValueName().text, spanOfContext(declaration.start, declaration.stop))
            },
            fallback = singletonMember(members.mapNotNull { it.fallbackDecl() }, "fallback", "Profile")?.let { declaration ->
                SymbolIdentifierField(declaration.FAIL_CLOSED().text, spanOfContext(declaration.start, declaration.stop))
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptBinding(context: AthenaParser.BindingDeclContext): BindingDeclaration {
        val members = context.bindingMember()
        val useElement = singletonMember(members.mapNotNull { it.useElementDecl() }, "use element", "Binding")
        val selector = singletonMember(members.mapNotNull { it.selectSubjectWhereDecl() }, "select subject where", "Binding")
        return BindingDeclaration(
            name = context.ident().text,
            profile = singletonMember(members.mapNotNull { it.bindingProfileDecl() }, "profile", "Binding")?.let { declaration ->
                SymbolIdentifierField(declaration.ident().text, spanOfContext(declaration.start, declaration.stop))
            },
            priority = singletonMember(members.mapNotNull { it.priorityDecl() }, "priority", "Binding")?.let { declaration ->
                ElementNumberField(adaptNumber(declaration.number()), spanOfContext(declaration.start, declaration.stop))
            },
            selectorKind = selector?.bindingSubjectKind()?.text?.let { kind ->
                when (kind) {
                    "device" -> BindingSelectorKind.Device
                    "function" -> BindingSelectorKind.Function
                    else -> error("ANTLR binding subject kind escaped authored AST boundary: $kind")
                }
            },
            selectorFacts = selector?.propertyAssignment()
                ?.map { adaptProperty(it) }
                .orEmpty(),
            useElement = useElement?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING(0).text), spanOfToken(declaration.STRING(0).symbol))
            },
            useVersion = useElement?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING(1).text), spanOfToken(declaration.STRING(1).symbol))
            },
            variant = singletonMember(members.mapNotNull { it.variantDecl() }, "variant", "Binding")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptElement(context: AthenaParser.ElementDeclContext): ElementDeclaration {
        val members = context.elementMember()
        return ElementDeclaration(
            name = context.ident().text,
            identity = singletonMember(members.mapNotNull { it.identityDecl() }, "identity", "Element")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            version = singletonMember(members.mapNotNull { it.versionDecl() }, "version", "Element")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            bounds = singletonMember(members.mapNotNull { it.boundsDecl() }, "bounds", "Element")?.let(::adaptBounds),
            resources = members.mapNotNull { it.resourceDecl()?.let(::adaptResource) },
            graphic = singletonMember(members.mapNotNull { it.graphicDecl() }, "graphic", "Element")?.let(::adaptGraphic),
            children = members.mapNotNull { it.elementChildDecl()?.let(::adaptElementChild) },
            exportedAnchors = members.mapNotNull { it.exportAnchorDecl()?.let(::adaptElementAnchorExport) },
            exportedLabels = members.mapNotNull { it.exportLabelDecl()?.let(::adaptElementLabelExport) },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptElementChild(context: AthenaParser.ElementChildDeclContext): ElementChildDeclaration {
        val members = context.elementChildMember()
        return ElementChildDeclaration(
            id = context.ident().text,
            headerSpan = spanOfContext(context.start, context.ident().stop),
            symbolIdentity = singletonMember(members.mapNotNull { it.symbolRefDecl() }, "symbol", "Element child")?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            translate = singletonMember(members.mapNotNull { it.translateDecl() }, "translate", "Element child")?.let { declaration ->
                adaptPoint(declaration.pointTuple())
            },
            rotate = singletonMember(members.mapNotNull { it.rotateDecl() }, "rotate", "Element child")?.let { declaration ->
                ElementNumberField(adaptNumber(declaration.number()), spanOfContext(declaration.start, declaration.stop))
            },
            scale = singletonMember(members.mapNotNull { it.scaleDecl() }, "scale", "Element child")?.let { declaration ->
                adaptPoint(declaration.pointTuple())
            },
            zOrder = singletonMember(members.mapNotNull { it.zOrderDecl() }, "zOrder", "Element child")?.let { declaration ->
                ElementNumberField(adaptNumber(declaration.number()), spanOfContext(declaration.start, declaration.stop))
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptElementAnchorExport(context: AthenaParser.ExportAnchorDeclContext): ElementAnchorExportDeclaration =
        ElementAnchorExportDeclaration(
            id = context.ident(0).text,
            childId = SymbolIdentifierField(
                context.ident(1).text,
                spanOfContext(context.ident(1).start, context.ident(1).stop),
            ),
            childAnchorId = SymbolIdentifierField(
                context.ident(2).text,
                spanOfContext(context.ident(2).start, context.ident(2).stop),
            ),
            referenceSpan = SourceSpan(
                startPosition(context.ident(1).start),
                endPosition(context.ident(2).stop),
            ),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptElementLabelExport(context: AthenaParser.ExportLabelDeclContext): ElementLabelExportDeclaration =
        ElementLabelExportDeclaration(
            id = context.ident(0).text,
            childId = SymbolIdentifierField(
                context.ident(1).text,
                spanOfContext(context.ident(1).start, context.ident(1).stop),
            ),
            childLabelId = SymbolIdentifierField(
                context.ident(2).text,
                spanOfContext(context.ident(2).start, context.ident(2).stop),
            ),
            referenceSpan = SourceSpan(
                startPosition(context.ident(1).start),
                endPosition(context.ident(2).stop),
            ),
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptGraphic(context: AthenaParser.GraphicDeclContext): SymbolGraphicDeclaration {
        if (context.RESOURCE() != null) {
            return SymbolGraphicDeclaration(
                bounds = null,
                primitives = emptyList(),
                labels = emptyList(),
                svgResource = SymbolIdentifierField(
                    context.ident().text,
                    spanOfContext(context.ident().start, context.ident().stop),
                ),
                span = spanOfContext(context.start, context.stop),
            )
        }
        val statements = context.graphicStatement()
        val bounds = singletonMember(statements.mapNotNull { it.boundsDecl() }, "bounds")?.let { declaration ->
            adaptBounds(declaration)
        }
        return SymbolGraphicDeclaration(
            bounds = bounds,
            primitives = statements.mapNotNull(::adaptGraphicPrimitive),
            labels = statements.mapNotNull { statement -> statement.labelSlotDecl()?.let(::adaptDynamicLabel) },
            svgResource = null,
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptResource(context: AthenaParser.ResourceDeclContext): RepresentationResourceDeclaration {
        val members = context.resourceMember()
        val kindDecl = singletonMember(members.mapNotNull { it.kindDecl() }, "kind", "Resource")
        val pathDecl = singletonMember(members.mapNotNull { it.pathDecl() }, "path", "Resource")
        if (kindDecl == null || pathDecl == null) {
            throw AthenaAntlrAdapterFailure(
                SyntaxDiagnostic(
                    file = file,
                    line = context.start.line,
                    column = context.start.charPositionInLine + 1,
                    message = "Resource requires both kind and path declarations",
                    span = spanOfContext(context.start, context.stop),
                ),
            )
        }
        return RepresentationResourceDeclaration(
            id = context.ident().text,
            kind = RepresentationResourceKind.SVG,
            path = SymbolStringField(unquote(pathDecl.STRING().text), spanOfToken(pathDecl.STRING().symbol)),
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptGraphicPrimitive(
        statement: AthenaParser.GraphicStatementContext,
    ): SymbolGraphicPrimitiveDeclaration? {
        statement.linePrimitiveDecl()?.let { declaration ->
            return SymbolGraphicPrimitiveDeclaration.Line(
                id = declaration.ident().text,
                from = adaptPoint(declaration.pointTuple(0)),
                to = adaptPoint(declaration.pointTuple(1)),
                style = declaration.styleValueName().text,
                span = spanOfContext(declaration.start, declaration.stop),
            )
        }
        statement.polylinePrimitiveDecl()?.let { declaration ->
            return SymbolGraphicPrimitiveDeclaration.Polyline(
                id = declaration.ident().text,
                points = declaration.pointList().pointTuple().map(::adaptPoint),
                style = declaration.styleValueName().text,
                span = spanOfContext(declaration.start, declaration.stop),
            )
        }
        statement.arcPrimitiveDecl()?.let { declaration ->
            return SymbolGraphicPrimitiveDeclaration.Arc(
                id = declaration.ident().text,
                center = adaptPoint(declaration.pointTuple()),
                radius = adaptNumber(declaration.number(0)),
                startAngleDegrees = adaptNumber(declaration.number(1)),
                sweepAngleDegrees = adaptNumber(declaration.number(2)),
                style = declaration.styleValueName().text,
                span = spanOfContext(declaration.start, declaration.stop),
            )
        }
        statement.circlePrimitiveDecl()?.let { declaration ->
            return SymbolGraphicPrimitiveDeclaration.Circle(
                id = declaration.ident().text,
                center = adaptPoint(declaration.pointTuple()),
                radius = adaptNumber(declaration.number()),
                style = declaration.styleValueName().text,
                span = spanOfContext(declaration.start, declaration.stop),
            )
        }
        statement.rectanglePrimitiveDecl()?.let { declaration ->
            return SymbolGraphicPrimitiveDeclaration.Rectangle(
                id = declaration.ident().text,
                origin = adaptPoint(declaration.pointTuple()),
                size = adaptSize(declaration.sizeTuple()),
                style = declaration.styleValueName().text,
                span = spanOfContext(declaration.start, declaration.stop),
            )
        }
        return null
    }

    private fun adaptDynamicLabel(context: AthenaParser.LabelSlotDeclContext): SymbolDynamicLabelDeclaration =
        SymbolDynamicLabelDeclaration(
            id = context.ident().text,
            origin = adaptPoint(context.pointTuple()),
            size = adaptSize(context.sizeTuple()),
            role = SymbolIdentifierField(
                context.profileValueName().text,
                spanOfContext(context.profileValueName().start, context.profileValueName().stop),
            ),
            style = context.styleValueName().text,
            span = spanOfContext(context.start, context.stop),
        )

    private fun adaptSize(context: AthenaParser.SizeTupleContext): SymbolSize = SymbolSize(
        width = adaptNumber(context.number(0)),
        height = adaptNumber(context.number(1)),
        span = spanOfContext(context.start, context.stop),
    )

    private fun adaptAnchor(context: AthenaParser.AnchorDeclContext): SymbolAnchorDeclaration {
        val members = context.anchorMember()
        val ref = singletonMember(members.mapNotNull { it.refDecl() }, "ref")
        val anchorPort = singletonMember(members.mapNotNull { it.anchorPortDecl() }, "port")
        val directions = members.mapNotNull { it.directionDecl() }
        val signals = members.mapNotNull { it.signalDecl() }
        val point = singletonMember(members.mapNotNull { it.pointDecl() }, "point")
        val role = singletonMember(members.mapNotNull { it.roleDecl() }, "role")
        return SymbolAnchorDeclaration(
            id = context.ident().text,
            ref = ref?.let { declaration ->
                SymbolStringField(unquote(declaration.STRING().text), spanOfToken(declaration.STRING().symbol))
            },
            port = anchorPort?.let { declaration -> adaptQualifiedName(declaration.twoPartName(), "Expected qualified port reference in owner.port form after 'port'") },
            directions = directions.map { declaration ->
                SymbolIdentifierField(declaration.directionPredicate().text, spanOfContext(declaration.start, declaration.stop))
            },
            signals = signals.map { declaration -> adaptQualifiedName(declaration.twoPartName(), "Expected qualified signal reference in owner.signal form after 'signal'") },
            point = point?.let { declaration -> adaptPoint(declaration.pointTuple()) },
            role = role?.let { declaration ->
                SymbolIdentifierField(declaration.ident().text, spanOfContext(declaration.start, declaration.stop))
            },
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptPoint(context: AthenaParser.PointTupleContext): SymbolPoint {
        val values = context.number().map(::adaptNumber)
        return SymbolPoint(values[0], values[1], spanOfContext(context.start, context.stop))
    }

    private fun adaptBounds(context: AthenaParser.BoundsDeclContext): SymbolBounds {
        val values = context.numberTuple4().number().map(::adaptNumber)
        return SymbolBounds(
            x = values[0],
            y = values[1],
            width = values[2],
            height = values[3],
            span = spanOfContext(context.start, context.stop),
        )
    }

    private fun adaptNumber(context: AthenaParser.NumberContext): Double = context.text.toDouble()

    private fun <T : ParseTree> singletonMember(members: List<T>, name: String, owner: String = "Symbol"): T? {
        if (members.size > 1) {
            val duplicate = members[1] as org.antlr.v4.runtime.ParserRuleContext
            throw AthenaAntlrAdapterFailure(
                SyntaxDiagnostic(
                    file = file,
                    line = duplicate.start.line,
                    column = duplicate.start.charPositionInLine + 1,
                    message = "Duplicate $owner '$name' declaration",
                    span = spanOfContext(duplicate.start, duplicate.stop),
                ),
            )
        }
        return members.firstOrNull()
    }

    private fun unquote(raw: String): String = raw.removePrefix("\"").removeSuffix("\"")

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

private fun terminalTokens(tree: ParseTree): List<Token> = buildList {
    fun collect(node: ParseTree) {
        if (node is TerminalNode) {
            add(node.symbol)
            return
        }
        repeat(node.childCount) { index -> collect(node.getChild(index)) }
    }
    collect(tree)
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
