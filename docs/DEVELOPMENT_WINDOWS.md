# Windows Development Setup

This project is a single JetBrains/IntelliJ Platform plugin. The Gradle project
lives at the repository root, so developer commands should be run from the root
directory.

## Requirements

- Windows 10 or 11
- Git
- PowerShell 7 or Windows PowerShell
- JDK 17
- IntelliJ IDEA for plugin development and manual testing

## Install JDK 17

Recommended with Scoop:

```powershell
scoop bucket add java
scoop install temurin17-jdk
```

This repository should be built with JDK 17. The plugin build uses the modern
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

To run a command with JDK 17 activated only for that command:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat test
.\scripts\dev-env.ps1 .\gradlew.bat build
.\scripts\dev-env.ps1 .\gradlew.bat runIde
```

The script sets `JAVA_HOME` and prepends JDK 17 to `PATH` for the current
PowerShell process only. It does not permanently change the machine-wide Java
configuration.

If JDK 17 is installed somewhere custom, set:

```powershell
$env:CERT_VIEWER_JDK17 = "C:\path\to\jdk-17"
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
with JDK 17 through `scripts/dev-env.ps1`.
