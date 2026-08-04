# M42 Comparable Contracts Research

Date: 2026-08-04

## Scope And Evidence Policy

This note grounds M42 product requirements in primary sources only. EPLAN is evidence for mature
electrical-engineering behavior, not a kernel architecture to copy. AutomationML is evidence for
implementation-neutral cross-domain exchange semantics, not a proposed Athena serialization.
JSON Schema and JSON Canonicalization Scheme sources apply only to M42's open JSON contracts.

## Observed Source Facts

### EPLAN: Functions, Devices, Parts, And Connection Points

1. EPLAN models a Function as the smallest managed logical unit. A Function belongs to one item,
   has a Function Definition, and contains its Connection Points. A device can aggregate multiple
   Functions, including one Main Function and auxiliary Functions. [Functions: Principle](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/xfctdefbrowsergui_k_prinzip.htm)
   [Structure of Devices in Eplan](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/xfctdefbrowsergui_k_betriebsmittel.htm)

2. Function identity is distinct from graphical representation. Assigning a Function Definition
   creates a Function; representing it with a schematic symbol creates a Component. EPLAN also
   manages unplaced Functions with no symbol. [Functions: Principle](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/xfctdefbrowsergui_k_prinzip.htm)

3. A Function Definition supplies standard behavior, report treatment, connection-point count,
   and logical defaults. EPLAN permits an individual Function to override some defaults, including
   Connection Point logic. [Function Definitions: Principle](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/xfctdefbrowsergui_k_funktionsdefinitionen.htm)

4. EPLAN Part records can contain Function Templates. Templates contain Function data and
   identifying properties used for device comparison, while placed or unplaced project Functions
   remain separate project objects. EPLAN assigns Parts through a device's Main Function.
   [Devices: Principle](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/devicelistgui_k_prinzip.htm)
   [Tab Function templates](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/articlesgui_r_funktionsschablone.htm)
   [Assigning a Part to a Device](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/devicetaggui_h_artikeldatenzuweisen.htm)

5. Connection Point logic includes engineering properties such as Connection Point type,
   potential type, and target count. When Function and symbol Connection Point counts differ,
   their mapping must be assigned explicitly. [Editing Connection Point Logic](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/xfctdefbrowsergui_h_anschlussdatenbearbeiten.htm)

6. EPLAN defines a Connection between source and target Connection Points and gives it transmission
   meaning, including information, electrical power, liquids, or air. Connection generation and
   updating are distinct from drawing placement. [Connections](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/connectionbrowsergui_k_start.htm)

### EPLAN: Structure And Validation

7. EPLAN applies hierarchical project structure to pages, devices, and Functions. Page and device
   structures use separately configurable identifier schemes and identifier blocks. Structure
   changes can produce inconsistent data that the product reports to the user.
   [Examples of the Design of Structure Identifiers](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/projectstructure_k_aufbaukennzeichnungsbl.htm)
   [Defining the Project Structure](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/projectstructure_h_prjstrukturdefinieren.htm)

8. EPLAN project checks produce durable messages with short descriptions, detailed causes, and
   suggested solutions. Check criteria are grouped into reusable schemes. Checks cover both device
   completeness and Function-level conditions. [Check Runs](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/msgmanagementgui_k_prueflaufprinzip.htm)

9. EPLAN gives check messages stable numbers and classes, plus Error, Warning, and Note categories.
   Category selection can be project-specific. [Check Runs: Message Numbers, Classes, and Categories](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/msgmanagementgui_k_prinzip.htm)

10. Official checks include missing Main Function, missing Connection Point designation, missing
    Part on a Main Function, and disagreement between actual Connection Point designations and a
    Part Function Template. [P007005](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/messages_p_007005.htm)
    [P007006](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/messages_p_007006.htm)
    [P007017](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/messages_p_007017.htm)
    [P007034](https://www.eplan.help/en-us/Infoportal/Content/Plattform/2026/Content/htm/messages_p_007034.htm)

### AutomationML: Implementation-Neutral Exchange Semantics

11. AutomationML describes itself as an XML-based, object-oriented language for storing and
    exchanging engineering models across multiple engineering aspects and domains. Its CAEX base
    carries object hierarchies, properties, and relations, while geometry, kinematics, and logic
    can remain referenced domain artifacts. [What is AutomationML?](https://www.automationml.org/about-automationml/automationml/)

12. AutomationML Role Classes model domain concepts and their properties; Interface Classes model
    possible abstract relations; Instance Hierarchies apply those classes as semantic indicators.
    This separates reusable domain meaning from project instances. [What is AutomationML?](https://www.automationml.org/about-automationml/automationml/)

13. AutomationML publishes separate specification parts and libraries for architecture, semantics,
    geometry and kinematics, logic, communication, and automation components. Its component work
    explicitly recognizes interlinked functional, mechanical, and electrical information.
    [AutomationML Specifications](https://www.automationml.org/about-automationml/specifications/)

14. AutomationML's ECLASS integration guidance treats external catalog classifications as a way to
    identify object types and property semantics in exchange data. It does not make one catalog the
    universal modeling language. [AutomationML Specifications](https://www.automationml.org/about-automationml/specifications/)

### JSON Contracts

15. JSON Schema Draft 2020-12 treats a schema version as a dialect. The root `$schema` declaration
    communicates the intended dialect to readers and tooling. [JSON Schema dialect and vocabulary declaration](https://json-schema.org/understanding-json-schema/reference/schema)

16. JSON Schema object properties are optional unless listed in `required`, and unknown properties
    are allowed unless constrained with `additionalProperties`. Closed M42 records therefore need
    both decisions stated explicitly. [JSON Schema object reference](https://json-schema.org/understanding-json-schema/reference/object)

17. JSON Schema validation defines structural assertions and annotations. It does not define
    Athena's domain satisfaction, dimensional analysis, relationship admissibility, or provenance
    rules. [JSON Schema Validation Draft 2020-12](https://json-schema.org/draft/2020-12/json-schema-validation)

18. RFC 8785 defines canonical JSON through strict primitive serialization, the I-JSON subset, and
    deterministic property sorting. JSON Schema alone does not define canonical bytes.
    [RFC 8785: JSON Canonicalization Scheme](https://www.rfc-editor.org/rfc/rfc8785.html)

## Recommendations For M42

1. Keep `EngineeringEntity`, `EngineeringFunction`, and `EngineeringPort` distinct. Let an Entity
   own Functions and a Function own Ports, while preserving stable package-qualified identities for
   every subject.

2. Keep semantic subjects independent from Projection Occurrences and paint. A Function must remain
   valid, queryable, and traceable without any symbol or diagram occurrence.

3. Make Concept knowledge define expected functional anatomy and Port contracts. Make a selected
   Part declare supported anatomy and implementation facts. Validate their agreement explicitly;
   never generate, overwrite, or repair authored Functions and Ports silently.

4. Bind a Part to the Entity realization, not to EPLAN's vendor-specific Main Function storage
   convention. Electrical packages may expose Main/Auxiliary Function roles where useful, but the
   kernel must not require that anatomy across domains.

5. Model concrete connectivity as typed Relationships between exact Ports. Keep Flow meaning,
   participant roles, direction, cardinality, and medium or signal identity explicit. Never infer
   semantic connectivity from a painted line.

6. Model Structure Assignment as validated context and human display address. Do not derive
   canonical semantic identity from mutable EPLAN-style designations such as `=`, `+`, or `-`.

7. Publish deterministic validation records containing stable rule identity, state or severity,
   exact subject, plain cause, expected and actual facts, correction options, and project and
   knowledge provenance. Preserve authored Engineering Reality for `INCOMPLETE`; fail closed for
   corrupt or contradictory knowledge as `INVALID`.

8. Keep rule activation and severity governed by versioned Athena knowledge or project policy.
   Do not copy EPLAN's mutable check modes or numeric message classes as kernel truth.

9. Use AutomationML's separation lesson, not its XML architecture: domain packages own reusable
   concepts and relationship semantics; project source owns instances; presentation and solver
   artifacts remain separate. Any future AutomationML support should be an import/export adapter.

10. Give each open JSON contract an explicit Draft 2020-12 `$schema`, stable `$id`, schema version,
    required fields, and deliberate extension policy. Use JSON Schema for transport shape only;
    execute engineering validation in Athena's deterministic compiler.

11. Define canonicalization independently from schema validation. Adopt RFC 8785 directly or state
    an equally precise Athena byte-canonicalization contract before using document hashes, fixture
    equality, signatures, or reproducibility claims.

## Non-Adoption Boundaries

- No EPLAN object model, message number, Part database, Function Definition library, or device-tag
  syntax becomes Athena kernel authority.
- No AutomationML Role Class hierarchy, CAEX document, XML payload, ECLASS catalog, or external
  exchange schema becomes Athena source of truth.
- No JSON Schema keyword substitutes for typed units, formula evaluation, capability satisfaction,
  relationship participation, correction generation, or provenance checks.
- Source facts above support product requirements only. They do not establish EPLAN, IEC,
  AutomationML, or professional-engineering compliance.
