# Athena M3 Proof Corpus

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m3/` publishes the minimum hosted-domain proof corpus for the M3 extensibility milestone.

## Purpose

- Prove `domain-electrical` through the stable hosted SPI with an authored electrical-only fixture.
- Prove `domain-dummy` through the same hosted SPI with an authored dummy-only fixture.
- Prove both hosted domains can coexist without kernel special cases.
- Keep the corpus small enough to review while still being reusable by the Epic 3 hosted verification matrix.

## Contents

- `electrical-proof.athena` - authored electrical-only hosted proof
- `electrical-proof.expectation.txt` - approved-plugin, view, and render expectation contract
- `dummy-proof.athena` - authored dummy-only hosted proof
- `dummy-proof.expectation.txt` - hosted proof contract for the no-global-view dummy path
- `dual-domain-proof.athena` - authored mixed-domain hosted proof
- `dual-domain-proof.expectation.txt` - approved-plugin and mixed-host expectation contract

## Boundary

This folder is not a sample gallery. It is a milestone proof corpus. The authored `.athena` sources remain the only semantic truth, while the sidecar expectation files describe the minimum hosted outcomes needed for deterministic regression checks.
