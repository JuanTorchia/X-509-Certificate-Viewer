# Dependency Health

This project treats vulnerable dependencies as urgent maintenance work. The
normal target is to stay on the latest stable compatible release, with explicit
exceptions only when a security tool or JetBrains compatibility blocks an
upgrade.

## Current Policy

- Dependabot checks Gradle and GitHub Actions dependencies every day.
- Dependabot security alerts are enabled for the repository.
- Secret scanning and push protection are enabled for the repository.
- Pull requests run dependency review, CodeQL, a Gradle build, and the Plugin
  Verifier compatibility matrix.
- The UI Integration workflow runs `validateFunctional` as an experimental,
  non-blocking signal for parser, editor, build, and sandbox coverage.
- JetBrains Plugin Verifier checks IntelliJ IDEA Ultimate 2026.1.4 and 2026.2.2
  and should be run for plugin metadata, IntelliJ extension, or Marketplace
  compatibility changes.
- The build targets the lowest supported platform (2026.1 on Java 21) while
  verifying the same artifact on 2026.2's Java 25 runtime. The generated
  Marketplace descriptor is checked to prevent an accidental `until-build`
  cap.
- GitGuardian is treated as an external repository/app check when enabled; it is
  not implemented by a workflow file in this repository.
- High-severity dependency review findings fail pull requests.
- Dependency or build-tool merges to `main` do not publish a Marketplace update.
  Publishing is restricted to the release process documented in
  `docs/RELEASE_POLICY.md`.

## Version Notes

| Component | Current version | Status |
| --- | --- | --- |
| Gradle wrapper | 9.6.1 | The 9.7.1 update is tracked separately in Dependabot PR #50. |
| IntelliJ Platform Gradle Plugin | 2.19.0 | Latest stable at the 2026-09-29 review. |
| Kotlin Gradle Plugin | 2.3.21 | Held below 2.4.x because CodeQL currently rejects Kotlin 2.4.0. |
| JUnit 4 | 4.13.2 | Latest JUnit 4 release; JUnit 4 is maintenance-only. |

## Kotlin Exception

Kotlin should move forward as soon as CodeQL supports the target version. The
current blocker is CodeQL analysis, not IntelliJ Platform compatibility.

Observed failure with Kotlin 2.4.0:

```text
Kotlin version 2.4.0 is too recent. CodeQL currently supports versions below 2.3.30
```

Before upgrading Kotlin beyond 2.3.x, verify that the CodeQL job passes on a
pull request.

## Manual Audit Commands

```powershell
gh api repos/JuanTorchia/X-509-Certificate-Viewer/dependabot/alerts
gh pr list --state open --author app/dependabot
.\scripts\dev-env.ps1 .\gradlew.bat build --no-daemon
.\scripts\dev-env.ps1 .\gradlew.bat verifyPublishedCompatibility --no-daemon
.\scripts\dev-env.ps1 .\gradlew.bat verifyPlugin --no-daemon
```
