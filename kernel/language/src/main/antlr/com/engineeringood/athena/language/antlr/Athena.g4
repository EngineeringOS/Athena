/*
 * INTERNAL IMPLEMENTATION DETAIL — not part of Athena's public syntax contract.
 *
 * Generated lexer/parser types in this package must not be imported by
 * :kernel:compiler, :kernel:runtime, :ide:*, or any other downstream module.
 * Downstream code must use only com.engineeringood.athena.language contracts.
 *
 * Scope includes file-level package and import declarations, system-scoped layout block grammar
 * admission, nested device-owned ports, compact grouped connect authoring, standalone typed
 * Symbol/Element declarations, typed installation cabinet declarations, and grouped connectivity
 * Interface declarations:
 * system, package, import, device, port, connect, grouped connect, qualified names,
 * string literals, property assignments, layout place/align/group statements,
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
    | relationDecl
    | evidenceDecl
    | projectionPolicyDecl
    | viewDecl
    | layoutDecl
    | installationDecl
    ;

deviceDecl
    : DEVICE ident LBRACE deviceMember* RBRACE
    ;

deviceMember
    : propertyAssignment
    | nestedPortDecl
    | interfaceDecl
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

interfaceDecl
    : INTERFACE ident LBRACE interfaceMember* RBRACE
    ;

interfaceMember
    : propertyAssignment
    | interfacePortsDecl
    ;

interfacePortsDecl
    : PORTS LBRACE interfacePortMember* RBRACE
    ;

interfacePortMember
    : ident (LBRACE propertyAssignment* RBRACE)?
    ;

portDecl
    : PORT twoPartName LBRACE propertyAssignment* RBRACE
    ;

connectDecl
    : CONNECT ident twoPartName connectionSeparator twoPartName
    ;

connectGroupDecl
    : CONNECT ident LBRACE connectGroupEdge* RBRACE
    ;

connectGroupEdge
    : ident twoPartName connectionSeparator twoPartName
    ;

relationDecl
    : relationWord twoPartName connectionSeparator relationTarget
    ;

connectionSeparator
    : TO
    | ARROW
    ;

relationWord
    : IDENT
    ;

relationTarget
    : twoPartName
    | LBRACK twoPartName (COMMA twoPartName)* RBRACK
    ;

evidenceDecl
    : EVIDENCE ident LBRACE evidenceMember* RBRACE
    ;

evidenceMember
    : evidenceNamespaceDecl
    | evidenceReferenceDecl
    | evidenceSubjectDecl
    | evidenceProvenanceDecl
    ;

evidenceNamespaceDecl
    : NAMESPACE ident
    ;

evidenceReferenceDecl
    : REFERENCE STRING
    ;

evidenceSubjectDecl
    : SUBJECT evidenceSubjectKind qualifiedReference
    ;

evidenceSubjectKind
    : CONTRACT
    | INTERFACE
    | PORT
    | RELATION MINUS CONTRACT
    | ROUTE MINUS POLICY
    ;

evidenceProvenanceDecl
    : PROVENANCE STRING
    ;

projectionPolicyDecl
    : PROJECTION ident LBRACE projectionPolicyMember* RBRACE
    ;

projectionPolicyMember
    : projectionPolicyTargetDecl
    | projectionPolicyLayoutDecl
    | projectionPolicyDrawingProfileDecl
    | projectionPolicyRouteQualityDecl
    | projectionPolicyProofDecl
    | projectionPolicyEngineeringTruthDecl
    ;

projectionPolicyTargetDecl
    : TARGET profileValueName
    ;

projectionPolicyLayoutDecl
    : LAYOUT profileValueName
    ;

projectionPolicyDrawingProfileDecl
    : DRAWING_PROFILE ident
    ;

projectionPolicyRouteQualityDecl
    : ROUTE_QUALITY ident
    ;

projectionPolicyProofDecl
    : PROOF profileValueName
    ;

projectionPolicyEngineeringTruthDecl
    : PORT qualifiedReference ident?
    | CONNECT ident qualifiedReference TO qualifiedReference
    | EVIDENCE ident?
    | ANCHOR ident?
    ;

viewDecl
    : VIEW ident LBRACE viewMember* RBRACE
    ;

viewMember
    : sheetDecl
    | gridDecl
    | regionDecl
    | readingOrderDecl
    | constructDecl
    ;

sheetDecl
    : SHEET ident
    ;

gridDecl
    : GRID ident LBRACE gridSizeDecl RBRACE
    ;

gridSizeDecl
    : ROWS positiveInteger COLUMNS positiveInteger
    ;

regionDecl
    : REGION STRING LBRACE regionMember* RBRACE
    ;

regionMember
    : OCCURRENCES LBRACK regionOccurrenceList RBRACK
    ;

regionOccurrenceList
    : (ident (COMMA ident)*)?
    ;

readingOrderDecl
    : READING_ORDER LBRACK ident (COMMA ident)* RBRACK
    ;

constructDecl
    : constructKind (ident)? LBRACK constructMemberList RBRACK
    ;

constructKind
    : POWER_RAIL
    | RUNG
    | BRANCH
    | WIRE_BUNDLE
    | TERMINAL_STRIP
    | CONTACT_GROUP
    | COIL_GROUP
    ;

constructMemberList
    : (qualifiedReference (COMMA qualifiedReference)*)?
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
    : MOUNT ident AS ident ON ident AT lengthPoint LBRACE installationMountMember* RBRACE
    ;

installationMountMember
    : installationFootprintDecl
    | installationMountingDecl
    | installationMountOrientationDecl
    | installationAllowedOrientationsDecl
    | installationClearanceDecl
    | installationCompatibleContainersDecl
    ;

installationFootprintDecl
    : FOOTPRINT lengthTuple3
    ;

installationMountingDecl
    : MOUNTING ident
    ;

installationMountOrientationDecl
    : ORIENTATION installationMountOrientation
    ;

installationAllowedOrientationsDecl
    : ALLOWED_ORIENTATIONS installationMountOrientationList
    ;

installationClearanceDecl
    : CLEARANCE lengthTuple4
    ;

installationCompatibleContainersDecl
    : COMPATIBLE_CONTAINERS identList
    ;

installationMountOrientationList
    : LBRACK installationMountOrientation (COMMA installationMountOrientation)* RBRACK
    ;

installationMountOrientation
    : DEG0
    | DEG90
    | DEG180
    | DEG270
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

lengthTuple4
    : LPAREN lengthLiteral COMMA lengthLiteral COMMA lengthLiteral COMMA lengthLiteral RPAREN
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
    : refDecl
    | anchorPortDecl
    | directionDecl
    | signalDecl
    | pointDecl
    | roleDecl
    ;

refDecl
    : REF STRING
    ;

anchorPortDecl
    : PORT twoPartName
    ;

directionDecl
    : DIRECTION directionPredicate
    ;

signalDecl
    : SIGNAL twoPartName
    ;

directionPredicate
    : IN
    | OUT
    | BIDIRECTIONAL
    ;

pointDecl
    : POINT pointTuple
    ;

roleDecl
    : ROLE ident
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

qualifiedReference
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
    | INTERFACE
    | EVIDENCE
    | NAMESPACE
    | REFERENCE
    | SUBJECT
    | CONTRACT
    | RELATION
    | PROVENANCE
    | TARGET
    | DRAWING_PROFILE
    | ROUTE_QUALITY
    | PROOF
    | DEFAULT
    | POLICY
    | CONNECT
    | PACKAGE
    | IMPORT
    | LAYOUT
    | VIEW
    | SHEET
    | GRID
    | ROWS
    | COLUMNS
    | REGION
    | OCCURRENCES
    | READING_ORDER
    | POWER_RAIL
    | RUNG
    | BRANCH
    | WIRE_BUNDLE
    | TERMINAL_STRIP
    | CONTACT_GROUP
    | COIL_GROUP
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
    | AS
    | ROUTE
    | ON
    | THROUGH
    | LENGTH
    | LANES
    | MARGIN
    | WALL
    | MOUNTING
    | FOOTPRINT
    | ALLOWED_ORIENTATIONS
    | CLEARANCE
    | COMPATIBLE_CONTAINERS
    | DEG0
    | DEG90
    | DEG180
    | DEG270
    | SYMBOL
    | ELEMENT
    | PROFILE
    | ROLE
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
    | POINT
    | ACCEPTS
    | DIRECTION
    | SIGNAL
    | REF
    | IN
    | OUT
    | BIDIRECTIONAL
    ;

SYSTEM : 'system' ;
DEVICE : 'device' ;
PORT : 'port' ;
FUNCTION : 'function' ;
PORTS : 'ports' ;
INTERFACE : 'interface' ;
EVIDENCE : 'evidence' ;
NAMESPACE : 'namespace' ;
REFERENCE : 'reference' ;
SUBJECT : 'subject' ;
CONTRACT : 'contract' ;
RELATION : 'relation' ;
PROVENANCE : 'provenance' ;
TARGET : 'target' ;
DRAWING_PROFILE : 'drawingProfile' ;
ROUTE_QUALITY : 'routeQuality' ;
PROOF : 'proof' ;
DEFAULT : 'default' ;
POLICY : 'policy' ;
CONNECT : 'connect' ;
PACKAGE : 'package' ;
IMPORT : 'import' ;
LAYOUT : 'layout' ;
VIEW : 'view' ;
SHEET : 'sheet' ;
GRID : 'grid' ;
ROWS : 'rows' ;
COLUMNS : 'columns' ;
REGION : 'region' ;
OCCURRENCES : 'occurrences' ;
READING_ORDER : 'reading-order' ;
POWER_RAIL : 'power-rail' ;
RUNG : 'rung' ;
BRANCH : 'branch' ;
WIRE_BUNDLE : 'wire-bundle' ;
TERMINAL_STRIP : 'terminal-strip' ;
CONTACT_GROUP : 'contact-group' ;
COIL_GROUP : 'coil-group' ;
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
AS : 'as' ;
ROUTE : 'route' ;
ON : 'on' ;
THROUGH : 'through' ;
LENGTH : 'length' ;
LANES : 'lanes' ;
MARGIN : 'margin' ;
WALL : 'wall' ;
MOUNTING : 'mounting' ;
FOOTPRINT : 'footprint' ;
ALLOWED_ORIENTATIONS : 'allowed-orientations' ;
CLEARANCE : 'clearance' ;
COMPATIBLE_CONTAINERS : 'compatible-containers' ;
DEG0 : 'deg0' ;
DEG90 : 'deg90' ;
DEG180 : 'deg180' ;
DEG270 : 'deg270' ;
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
ARROW : '->' ;
STYLE : 'style' ;
ANCHOR : 'anchor' ;
POINT : 'point' ;
ROLE : 'role' ;
ACCEPTS : 'accepts' ;
DIRECTION : 'direction' ;
SIGNAL : 'signal' ;
REF : 'ref' ;
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
