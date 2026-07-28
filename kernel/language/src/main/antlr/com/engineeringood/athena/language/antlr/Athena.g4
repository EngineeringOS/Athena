/*
 * INTERNAL IMPLEMENTATION DETAIL — not part of Athena's public syntax contract.
 *
 * Generated lexer/parser types in this package must not be imported by
 * :kernel:compiler, :kernel:runtime, :ide:*, or any other downstream module.
 * Downstream code must use only com.engineeringood.athena.language contracts.
 *
 * Scope includes M17 syntax plus M18 file-level package and import declarations, M23
 * system-scoped layout block grammar admission, M28 nested device-owned ports, and
 * compact grouped connect authoring, M34 standalone typed Symbol/Element declarations, and
 * M35 typed installation cabinet declarations:
 * system, package, import, device, port, connect, grouped connect, qualified names,
 * string literals, property assignments, layout place/align/group statements, M34
 * symbol/element/profile/binding/resource declarations, and installation cabinet source only.
 * No expression / macro-use forms.
 */
grammar Athena;

@header {
package com.engineeringood.athena.language.antlr;
}

sourceFile
    : packageDecl? importDecl* (systemDecl | representationDecl+) EOF
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
    | connectGroupDecl
    | connectDecl
    | layoutDecl
    | installationDecl
    ;

deviceDecl
    : DEVICE ident LBRACE deviceMember* RBRACE
    ;

deviceMember
    : propertyAssignment
    | nestedPortDecl
    | functionDecl
    ;

nestedPortDecl
    : PORT ident LBRACE propertyAssignment* RBRACE
    ;

functionDecl
    : FUNCTION ident LBRACE functionMember* RBRACE
    ;

functionMember
    : functionRoleDecl
    | functionPortsDecl
    ;

functionRoleDecl
    : ROLE ident
    ;

functionPortsDecl
    : PORTS LPAREN functionPortReference (COMMA functionPortReference)* RPAREN
    ;

functionPortReference
    : ident (DOT ident)?
    ;

portDecl
    : PORT twoPartName LBRACE propertyAssignment* RBRACE
    ;

connectDecl
    : CONNECT ident twoPartName ARROW twoPartName
    ;

connectGroupDecl
    : CONNECT ident LBRACE connectGroupEdge* RBRACE
    ;

connectGroupEdge
    : ident twoPartName ARROW twoPartName
    ;

layoutDecl
    : LAYOUT viewFamilyName LBRACE layoutStatement* RBRACE
    ;

viewFamilyName
    : ident (MINUS ident)*
    ;

profileValueName
    : ident (MINUS ident)*
    ;

layoutStatement
    : placeStatement
    | alignStatement
    | groupStatement
    ;

placeStatement
    : PLACE ident layoutPlacementRelation ident
    | PLACE authoredLayoutReference AT drawingGridPosition ORIENTATION layoutOrientation
    ;

authoredLayoutReference
    : ident (DOT ident)?
    ;

drawingGridPosition
    : LPAREN positiveInteger COMMA positiveInteger RPAREN
    ;

positiveInteger
    : POSITIVE_INTEGER
    ;

layoutOrientation
    : HORIZONTAL
    | VERTICAL
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

installationDecl
    : INSTALLATION CABINET ident LBRACE installationMember* RBRACE
    ;

installationMember
    : enclosureDecl
    | installationSurfaceDecl
    | installationRailDecl
    | installationDuctDecl
    | installationChannelDecl
    | installationTerminalGroupDecl
    | installationMountDecl
    | installationRouteDecl
    ;

enclosureDecl
    : ENCLOSURE ident SIZE lengthTuple3
    ;

installationSurfaceDecl
    : SURFACE ident IN ident AT lengthPoint SIZE lengthSize ACCEPTS identList
    ;

installationRailDecl
    : RAIL ident ON ident AT lengthPoint LENGTH lengthLiteral ORIENTATION installationOrientation MOUNTING ident
    ;

installationDuctDecl
    : DUCT ident IN ident AT lengthPoint SIZE lengthSize ORIENTATION installationOrientation WALL lengthLiteral
    ;

installationChannelDecl
    : CHANNEL ident IN ident AT lengthPoint SIZE lengthSize LANES positiveInteger MARGIN lengthLiteral
    ;

installationTerminalGroupDecl
    : TERMINAL_GROUP ident IN ident AT lengthPoint SIZE lengthSize ORIENTATION installationOrientation ACCEPTS identList
    ;

installationMountDecl
    : MOUNT ident DEVICE ident ON ident AT lengthPoint ORIENTATION installationOrientation
    ;

installationRouteDecl
    : ROUTE ident THROUGH identList
    ;

installationOrientation
    : HORIZONTAL
    | VERTICAL
    ;

identList
    : LBRACK ident (COMMA ident)* RBRACK
    ;

lengthPoint
    : LPAREN lengthLiteral COMMA lengthLiteral RPAREN
    ;

lengthSize
    : LPAREN lengthLiteral COMMA lengthLiteral RPAREN
    ;

lengthTuple3
    : LPAREN lengthLiteral COMMA lengthLiteral COMMA lengthLiteral RPAREN
    ;

lengthLiteral
    : number MM
    ;

symbolDecl
    : SYMBOL ident LBRACE symbolMember* RBRACE
    ;

representationDecl
    : symbolDecl
    | elementDecl
    | profileDecl
    | bindingDecl
    ;

profileDecl
    : PROFILE ident LBRACE profileMember* RBRACE
    ;

profileMember
    : projectionDecl
    | standardDecl
    | styleDecl
    | fallbackDecl
    ;

projectionDecl
    : PROJECTION profileValueName
    ;

standardDecl
    : STANDARD profileValueName
    ;

styleDecl
    : STYLE profileValueName
    ;

fallbackDecl
    : FALLBACK FAIL_CLOSED
    ;

bindingDecl
    : BINDING ident LBRACE bindingMember* RBRACE
    ;

bindingMember
    : bindingProfileDecl
    | priorityDecl
    | selectSubjectWhereDecl
    | useElementDecl
    | variantDecl
    ;

bindingProfileDecl
    : PROFILE ident
    ;

priorityDecl
    : PRIORITY number
    ;

selectSubjectWhereDecl
    : SELECT bindingSubjectKind WHERE LBRACE propertyAssignment* RBRACE
    ;

bindingSubjectKind
    : DEVICE
    | FUNCTION
    ;

useElementDecl
    : USE ELEMENT STRING VERSION STRING
    ;

variantDecl
    : VARIANT STRING
    ;

elementDecl
    : ELEMENT ident LBRACE elementMember* RBRACE
    ;

elementMember
    : identityDecl
    | versionDecl
    | boundsDecl
    | resourceDecl
    | graphicDecl
    | elementChildDecl
    | exportAnchorDecl
    | exportLabelDecl
    ;

elementChildDecl
    : CHILD ident LBRACE elementChildMember* RBRACE
    ;

elementChildMember
    : symbolRefDecl
    | translateDecl
    | rotateDecl
    | scaleDecl
    | zOrderDecl
    ;

symbolRefDecl
    : SYMBOL STRING
    ;

translateDecl
    : TRANSLATE pointTuple
    ;

rotateDecl
    : ROTATE number
    ;

scaleDecl
    : SCALE pointTuple
    ;

zOrderDecl
    : Z_ORDER number
    ;

exportAnchorDecl
    : EXPORT ANCHOR ident FROM ident DOT ident
    ;

exportLabelDecl
    : EXPORT LABEL ident FROM ident DOT ident
    ;

symbolMember
    : identityDecl
    | versionDecl
    | resourceDecl
    | graphicDecl
    | anchorDecl
    ;

identityDecl
    : IDENTITY STRING
    ;

versionDecl
    : VERSION STRING
    ;

graphicDecl
    : GRAPHIC LBRACE graphicStatement* RBRACE
    | GRAPHIC SVG RESOURCE ident
    ;

resourceDecl
    : RESOURCE ident LBRACE resourceMember* RBRACE
    ;

resourceMember
    : kindDecl
    | pathDecl
    ;

kindDecl
    : KIND SVG
    ;

pathDecl
    : PATH STRING
    ;

graphicStatement
    : boundsDecl
    | linePrimitiveDecl
    | polylinePrimitiveDecl
    | arcPrimitiveDecl
    | circlePrimitiveDecl
    | rectanglePrimitiveDecl
    | labelSlotDecl
    ;

boundsDecl
    : BOUNDS numberTuple4
    ;

linePrimitiveDecl
    : LINE ident FROM pointTuple TO pointTuple STYLE styleValueName
    ;

polylinePrimitiveDecl
    : POLYLINE ident POINTS pointList STYLE styleValueName
    ;

pointList
    : LPAREN pointTuple (COMMA pointTuple)* RPAREN
    ;

arcPrimitiveDecl
    : ARC ident CENTER pointTuple RADIUS number FROM number SWEEP number STYLE styleValueName
    ;

circlePrimitiveDecl
    : CIRCLE ident CENTER pointTuple RADIUS number STYLE styleValueName
    ;

rectanglePrimitiveDecl
    : RECTANGLE ident AT pointTuple SIZE sizeTuple STYLE styleValueName
    ;

labelSlotDecl
    : LABEL ident AT pointTuple SIZE sizeTuple ROLE profileValueName STYLE styleValueName
    ;

styleValueName
    : profileValueName
    ;

anchorDecl
    : ANCHOR ident LBRACE anchorMember* RBRACE
    ;

anchorMember
    : primitiveRefDecl
    | pointDecl
    | roleDecl
    | acceptsDirectionDecl
    | acceptsSignalDecl
    ;

primitiveRefDecl
    : PRIMITIVE_REF ident
    ;

pointDecl
    : POINT pointTuple
    ;

roleDecl
    : ROLE ident
    ;

acceptsDirectionDecl
    : ACCEPTS DIRECTION directionPredicate
    ;

acceptsSignalDecl
    : ACCEPTS SIGNAL ident
    ;

directionPredicate
    : IN
    | OUT
    | BIDIRECTIONAL
    ;

pointTuple
    : LPAREN number COMMA number RPAREN
    ;

sizeTuple
    : LPAREN number COMMA number RPAREN
    ;

numberTuple4
    : LPAREN number COMMA number COMMA number COMMA number RPAREN
    ;

number
    : NUMBER
    | POSITIVE_INTEGER
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
    | FUNCTION
    | PORTS
    | CONNECT
    | PACKAGE
    | IMPORT
    | LAYOUT
    | PLACE
    | AT
    | ORIENTATION
    | NEAR
    | BELOW
    | ALIGN
    | AXIS
    | HORIZONTAL
    | VERTICAL
    | GROUP
    | INSTALLATION
    | CABINET
    | ENCLOSURE
    | SURFACE
    | RAIL
    | DUCT
    | CHANNEL
    | TERMINAL_GROUP
    | MOUNT
    | ROUTE
    | ON
    | THROUGH
    | LENGTH
    | LANES
    | MARGIN
    | WALL
    | MOUNTING
    | SYMBOL
    | ELEMENT
    | PROFILE
    | BINDING
    | PROJECTION
    | STANDARD
    | FALLBACK
    | FAIL_CLOSED
    | PRIORITY
    | SELECT
    | WHERE
    | USE
    | VARIANT
    | CHILD
    | TRANSLATE
    | ROTATE
    | SCALE
    | Z_ORDER
    | EXPORT
    | IDENTITY
    | VERSION
    | GRAPHIC
    | SVG
    | RESOURCE
    | KIND
    | PATH
    | BOUNDS
    | LINE
    | POLYLINE
    | ARC
    | CIRCLE
    | RECTANGLE
    | POINTS
    | CENTER
    | RADIUS
    | SWEEP
    | SIZE
    | LABEL
    | FROM
    | TO
    | STYLE
    | ANCHOR
    | PRIMITIVE_REF
    | POINT
    | ROLE
    | ACCEPTS
    | DIRECTION
    | SIGNAL
    | IN
    | OUT
    | BIDIRECTIONAL
    ;

SYSTEM : 'system' ;
DEVICE : 'device' ;
PORT : 'port' ;
FUNCTION : 'function' ;
PORTS : 'ports' ;
CONNECT : 'connect' ;
PACKAGE : 'package' ;
IMPORT : 'import' ;
LAYOUT : 'layout' ;
PLACE : 'place' ;
AT : 'at' ;
ORIENTATION : 'orientation' ;
NEAR : 'near' ;
BELOW : 'below' ;
ALIGN : 'align' ;
ALIGNED_WITH : 'aligned-with' ;
AXIS : 'axis' ;
HORIZONTAL : 'horizontal' ;
VERTICAL : 'vertical' ;
GROUP : 'group' ;
GROUPED_WITH : 'grouped-with' ;
INSTALLATION : 'installation' ;
CABINET : 'cabinet' ;
ENCLOSURE : 'enclosure' ;
SURFACE : 'surface' ;
RAIL : 'rail' ;
DUCT : 'duct' ;
CHANNEL : 'channel' ;
TERMINAL_GROUP : 'terminal-group' ;
MOUNT : 'mount' ;
ROUTE : 'route' ;
ON : 'on' ;
THROUGH : 'through' ;
LENGTH : 'length' ;
LANES : 'lanes' ;
MARGIN : 'margin' ;
WALL : 'wall' ;
MOUNTING : 'mounting' ;
SYMBOL : 'symbol' ;
ELEMENT : 'element' ;
PROFILE : 'profile' ;
BINDING : 'binding' ;
PROJECTION : 'projection' ;
STANDARD : 'standard' ;
FALLBACK : 'fallback' ;
FAIL_CLOSED : 'fail-closed' ;
PRIORITY : 'priority' ;
SELECT : 'select' ;
WHERE : 'where' ;
USE : 'use' ;
VARIANT : 'variant' ;
CHILD : 'child' ;
TRANSLATE : 'translate' ;
ROTATE : 'rotate' ;
SCALE : 'scale' ;
Z_ORDER : 'zOrder' ;
EXPORT : 'export' ;
IDENTITY : 'identity' ;
VERSION : 'version' ;
GRAPHIC : 'graphic' ;
SVG : 'svg' ;
RESOURCE : 'resource' ;
KIND : 'kind' ;
PATH : 'path' ;
BOUNDS : 'bounds' ;
LINE : 'line' ;
POLYLINE : 'polyline' ;
ARC : 'arc' ;
CIRCLE : 'circle' ;
RECTANGLE : 'rectangle' ;
POINTS : 'points' ;
CENTER : 'center' ;
RADIUS : 'radius' ;
SWEEP : 'sweep' ;
SIZE : 'size' ;
LABEL : 'label' ;
FROM : 'from' ;
TO : 'to' ;
STYLE : 'style' ;
ANCHOR : 'anchor' ;
PRIMITIVE_REF : 'primitiveRef' ;
POINT : 'point' ;
ROLE : 'role' ;
ACCEPTS : 'accepts' ;
DIRECTION : 'direction' ;
SIGNAL : 'signal' ;
IN : 'in' ;
OUT : 'out' ;
BIDIRECTIONAL : 'bidirectional' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA : ',' ;
DOT : '.' ;
ARROW : '->' ;
MINUS : '-' ;
MM : 'mm' ;

STRING
    : '"' (~["\r\n])* '"'
    ;

IDENT
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

POSITIVE_INTEGER
    : [1-9] [0-9]*
    ;

NUMBER
    : '-'? [0-9]+ ('.' [0-9]+)?
    ;

BOM
    : '\uFEFF' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
