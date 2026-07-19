---
status: ready-for-dev
baseline_commit: 265e26109ebf75352fdf9db3f814edcc77bbf546
epic: 1
story: 1.2
title: Define presentation policy profile contracts
---

# Story 1.2: Define presentation policy profile contracts

## Story

As a presentation-policy maintainer,
I want `athena-industrial-control-v0` represented as a governed profile,
So that component appearance is selected by policy rather than renderer hardcoding.

## Acceptance Criteria

- `athena-industrial-control-v0` can select supported representations for PLC/controller,
  HMI/operator, terminal block, power supply, protection device, and load/actuator.
- The profile is vendor-neutral and does not claim IEC completeness.
- Unsupported families produce diagnosable fallback metadata.
- The accepted proof path can be checked for zero fallback symbols.

## Tasks/Subtasks

- [x] Add presentation policy profile contracts.
- [x] Add `athena-industrial-control-v0` profile seed.
- [x] Add fallback metadata contract.
- [x] Add tests for profile coverage and unsupported-family behavior.

## Dev Notes

- Governed by architecture AD-3, AD-7, AD-8.
- Do not implement QElectroTech import or symbol marketplace behavior.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`; compile failed because presentation policy contracts did not exist.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`.

### Completion Notes

- Added `:kernel:presentation-policy-model` as a Kotlin JVM kernel module depending only on
  `:kernel:representation-model`.
- Added `PresentationPolicyProfile`, `RepresentationSelection`, fallback diagnostics, and
  `RepresentationPolicyCoverageProof`.
- Added the vendor-neutral `athena-industrial-control-v0` profile seed with six M25 families and no
  IEC-completeness claim.
- Added tests for supported family coverage, unsupported fallback metadata, and zero-fallback proof.

### File List

- `settings.gradle.kts`
- `kernel/presentation-policy-model/build.gradle.kts`
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/PresentationPolicyProfile.kt`
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/AthenaIndustrialControlV0Profile.kt`
- `kernel/presentation-policy-model/src/test/kotlin/com/engineeringood/athena/policy/PresentationPolicyProfileContractTest.kt`

## Change Log

- 2026-07-19: Implemented presentation policy profile contracts and `athena-industrial-control-v0`.

## Status

done
