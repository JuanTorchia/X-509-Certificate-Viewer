# Windows Development Setup

This project is a single JetBrains/IntelliJ Platform plugin. The Gradle project
lives at the repository root, so developer commands should be run from the root
directory.

## Requirements

- Windows 10 or 11
- Git
- PowerShell 7 or Windows PowerShell
- JDK 21
- IntelliJ IDEA for plugin development and manual testing

## Install JDK 21

Recommended with Scoop:

```powershell
scoop bucket add java
scoop install temurin21-jdk
```

This repository should be built with JDK 21. The plugin build uses the modern
IntelliJ Platform Gradle Plugin 2.x toolchain and Gradle 9.x. Newer JDKs can
still break Gradle/Kotlin tooling before compilation. In particular, Java 25 can
fail before compilation with an error like:

```text
java.lang.IllegalArgumentException: 25.0.2
```

## Use the repo-local environment script

From the repository root:

```powershell
.\scripts\dev-env.ps1
```

To run a command with JDK 21 activated only for that command:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat test
.\scripts\dev-env.ps1 .\gradlew.bat build
.\scripts\dev-env.ps1 .\gradlew.bat runIde
```

The script sets `JAVA_HOME` and prepends JDK 21 to `PATH` for the current
PowerShell process only. It does not permanently change the machine-wide Java
configuration.

If JDK 21 is installed somewhere custom, set:

```powershell
$env:CERT_VIEWER_JDK21 = "C:\path\to\jdk-21"
.\scripts\dev-env.ps1 .\gradlew.bat build
```

## Common commands

Run tests:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat test
```

Build the plugin:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat build
```

Run the full functional gate before PRs that touch the editor UI, supported
formats, screenshots, or IntelliJ integration:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat validateFunctional --no-daemon
```

Run JetBrains Plugin Verifier before PRs that touch `plugin.xml`, IntelliJ
extension registrations, dependency compatibility, or Marketplace compatibility
warnings:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat verifyPlugin --no-daemon
```

The verifier is configured against IntelliJ IDEA Ultimate 2026.1.4 because that
is the Marketplace compatibility report version currently being tracked. This
command may download IDE artifacts and can be slower than normal build checks.

Run Gradle commands one at a time on Windows. Parallel Gradle invocations can
lock Kotlin incremental compilation caches and produce `AccessDeniedException`
errors under `build\kotlin\...`.

Run a sandbox IDE:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat runIde
```

The plugin ZIP is created under:

```text
build/distributions/
```

## Troubleshooting

Check which Java is active:

```powershell
java -version
$env:JAVA_HOME
where.exe java
```

Stop Gradle daemons after switching Java versions:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat --stop
```

If the build fails before compiling Kotlin, verify that the command is running
with JDK 21 through `scripts/dev-env.ps1`.
