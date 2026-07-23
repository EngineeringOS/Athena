# Athena M32 Sample Project

This sample demonstrates the M32 Engineering Package Platform boundary with Athena-owned synthetic
package data.

It contains:

- semantic `.athena` source in `src/01-package-platform-demo.athena`
- Engineering Package descriptors under `packages/engineering`
- Presentation Profile descriptors under `packages/profiles`
- Binding Manifests under `packages/manifests`
- Representation Package descriptors under `packages/representation`
- Athena-owned vector Graphic Resources under `packages/resources`

The names are synthetic demo names owned by Athena. The sample does not depend on QElectroTech,
vendor package feeds, proprietary symbols, internet registries, or runtime tool mirrors.

Profile switching is a package/binding runtime fact. It must not modify `.athena` source. The
`ShutterMotorM32` subject resolves through both `m32-iec` and `m32-compact` profiles with different
representation package and descriptor ids.
