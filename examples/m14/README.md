# M14 Examples

This folder contains the first repository-backed M14 proof corpus.

Current corpus:

- `siemens-proof-corpus`

Purpose:

- prove component knowledge resolution on a real governed repository root
- exercise the narrow electrical proof families currently shipped by the hosted electrical extension
- keep the proof Siemens-first without claiming broad catalog coverage

The current narrow proof slice covers:

- PLC CPU
- power contactor
- overload relay
- AC motor
- 24V DC power supply

The current narrow resolved semantic-port and physical-trait proof remains attached only to `PLC1`.

Verification lives in runtime tests and uses this repository directly instead of temporary inline-only fixtures.
