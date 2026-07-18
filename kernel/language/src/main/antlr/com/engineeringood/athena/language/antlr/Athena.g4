/*
 * INTERNAL IMPLEMENTATION DETAIL — not part of Athena's public syntax contract.
 *
 * Generated lexer/parser types in this package must not be imported by
 * :kernel:compiler, :kernel:runtime, :ide:*, or any other downstream module.
 * Downstream code must use only com.engineeringood.athena.language contracts.
 *
 * Scope includes M17 syntax plus M18 file-level package and import declarations, and M23
 * system-scoped layout block grammar admission:
 * system, package, import, device, port, connect, qualified names, string literals,
 * property assignments, and layout place/align/group statements only. No expression /
 * macro-use forms.
 */
grammar Athena;

@header {
package com.engineeringood.athena.language.antlr;
}

sourceFile
    : packageDecl? importDecl* systemDecl EOF
    ;

packageDecl
    : PACKAGE packageName
    ;

importDecl
    : IMPORT packageName
    ;

packageName
    : packageNameSegment (DOT packageNameSegment)*
    ;

packageNameSegment
    : ident (MINUS ident)*
    ;

systemDecl
    : SYSTEM ident LBRACE declaration* RBRACE
    ;

declaration
    : deviceDecl
    | portDecl
    | connectDecl
    | layoutDecl
    ;

deviceDecl
    : DEVICE ident LBRACE propertyAssignment* RBRACE
    ;

portDecl
    : PORT twoPartName LBRACE propertyAssignment* RBRACE
    ;

connectDecl
    : CONNECT twoPartName ARROW twoPartName
    ;

layoutDecl
    : LAYOUT viewFamilyName LBRACE layoutStatement* RBRACE
    ;

viewFamilyName
    : ident (MINUS ident)*
    ;

layoutStatement
    : placeStatement
    | alignStatement
    | groupStatement
    ;

placeStatement
    : PLACE ident layoutPlacementRelation ident
    ;

layoutPlacementRelation
    : NEAR
    | BELOW
    ;

alignStatement
    : ALIGN ident ALIGNED_WITH ident AXIS layoutAxis
    ;

layoutAxis
    : HORIZONTAL
    | VERTICAL
    ;

groupStatement
    : GROUP ident GROUPED_WITH ident
    ;

/**
 * Dotted authored name. The grammar accepts one-or-more dotted parts so that
 * over-/under-qualified port and connect endpoints still parse into a tree; the
 * internal ParseAdapter enforces the exact two-part arity and emits the same
 * `owner.port` diagnostics the handwritten parser produced (AD-111: arity is an
 * authored-AST concern, not an ad hoc grammar patch). The rule name is retained
 * for source/tooling continuity.
 */
twoPartName
    : ident (DOT ident)*
    ;

propertyAssignment
    : ident scalarValue
    ;

scalarValue
    : ident
    | STRING
    ;

/**
 * Keywords remain usable as identifiers in property names/values, matching the
 * handwritten tokenizer which treats keywords as contextual IDENTIFIER matches.
 */
ident
    : IDENT
    | SYSTEM
    | DEVICE
    | PORT
    | CONNECT
    | PACKAGE
    | IMPORT
    | LAYOUT
    | PLACE
    | NEAR
    | BELOW
    | ALIGN
    | AXIS
    | HORIZONTAL
    | VERTICAL
    | GROUP
    ;

SYSTEM : 'system' ;
DEVICE : 'device' ;
PORT : 'port' ;
CONNECT : 'connect' ;
PACKAGE : 'package' ;
IMPORT : 'import' ;
LAYOUT : 'layout' ;
PLACE : 'place' ;
NEAR : 'near' ;
BELOW : 'below' ;
ALIGN : 'align' ;
ALIGNED_WITH : 'aligned-with' ;
AXIS : 'axis' ;
HORIZONTAL : 'horizontal' ;
VERTICAL : 'vertical' ;
GROUP : 'group' ;
GROUPED_WITH : 'grouped-with' ;
LBRACE : '{' ;
RBRACE : '}' ;
DOT : '.' ;
ARROW : '->' ;
MINUS : '-' ;

STRING
    : '"' (~["\r\n])* '"'
    ;

IDENT
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

BOM
    : '\uFEFF' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
