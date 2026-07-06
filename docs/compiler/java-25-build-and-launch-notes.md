# Athena Java 25 Build And Launch Notes

## Purpose

This note records the verified Java `25` build and desktop-launch posture for Athena after the Compose viewer and root build experiments.

The important boundary is that Athena currently has three distinct JVM selection points:

- Gradle launcher JVM
- Gradle daemon JVM used by root verification tasks such as `verifyJava25`
- Compose Desktop application JVM used by `:apps:desktop-viewer:run`

Treating those as one thing caused the original failures.

## What Failed

The workstation shell could report Java `19` even after invoking `java25`.

That created two different failure modes:

- root `build` and `test` failed at `:verifyJava25` because that task checks the JVM actually running Gradle build logic
- `:apps:desktop-viewer:run` could start with Java `19` and fail with `UnsupportedClassVersionError` because the app classes were compiled for Java `25`

The Kotlin toolchain alone was not enough. It controls compilation, not every runtime that Athena uses.

## Final Fix

### Root Build And Test

Root verification is now pinned through Gradle's daemon JVM criteria:

- `settings.gradle.kts`
  - applies `org.gradle.toolchains.foojay-resolver-convention`
- `gradle/gradle-daemon-jvm.properties`
  - records the required daemon JVM as Java `25` from `ADOPTIUM`

That means normal wrapper commands such as `.\gradlew.bat build` and `.\gradlew.bat test` can succeed even when the shell launcher JVM is still Java `19`, because Gradle forks a compatible Java `25` daemon for the actual build logic.

`verifyJava25` in the root build remains valuable. It now proves the daemon selection is correct instead of forcing developers to hand-edit `JAVA_HOME` for ordinary verification.

### Compose Desktop Run

The desktop app uses its own runtime selection path.

`apps/desktop-viewer/build.gradle.kts` now pins:

- `compose.desktop.application.javaHome`
  - resolved from the Java `25` toolchain
- runtime JVM arguments
  - `--enable-native-access=ALL-UNNAMED`
  - `-Dskiko.renderApi=SOFTWARE`

This fixes two real problems:

- the desktop app no longer launches on Java `19`
- the first Windows desktop proof avoids the native crash observed without the stable Skiko/runtime flags

## Verified Behavior

The following checks were rerun successfully after the final fix:

```powershell
.\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:run
```

The live desktop proof also verified that:

- the `Athena` window opened and responded
- the actual application process ran on the Java `25` toolchain under `D:\GRADLE\jdks\...`
- the process command line included `--enable-native-access=ALL-UNNAMED`
- the process command line included `-Dskiko.renderApi=SOFTWARE`

## Operational Guidance

- For normal Athena development, use the wrapper commands directly from the repo root.
- Do not assume the shell's `java -version` alone tells you what JVM Athena will build or run on.
- If daemon criteria need to be regenerated, use:

```powershell
.\gradlew.bat updateDaemonJvm --jvm-version=25 --jvm-vendor=ADOPTIUM
```

- If the desktop launcher path changes, re-verify both:
  - root `build` or `test`
  - `:apps:desktop-viewer:run`

Both checks matter because the root build JVM and the Compose app JVM are configured through different mechanisms.
