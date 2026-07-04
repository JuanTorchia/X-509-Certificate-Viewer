# CertView X.509

[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/30727?style=flat-square&logo=jetbrains&label=JetBrains%20Marketplace)](https://plugins.jetbrains.com/plugin/30727-x-509-certificate-viewer)
[![JetBrains Downloads](https://img.shields.io/jetbrains/plugin/d/30727?style=flat-square&logo=jetbrains&label=downloads)](https://plugins.jetbrains.com/plugin/30727-x-509-certificate-viewer)
[![Build](https://img.shields.io/github/actions/workflow/status/JuanTorchia/X-509-Certificate-Viewer/intellij-publish.yml?branch=main&style=flat-square&label=build)](https://github.com/JuanTorchia/X-509-Certificate-Viewer/actions/workflows/intellij-publish.yml)
[![Security & Quality](https://img.shields.io/github/actions/workflow/status/JuanTorchia/X-509-Certificate-Viewer/security-quality.yml?branch=main&style=flat-square&label=security)](https://github.com/JuanTorchia/X-509-Certificate-Viewer/actions/workflows/security-quality.yml)
[![UI Integration](https://img.shields.io/github/actions/workflow/status/JuanTorchia/X-509-Certificate-Viewer/ui-integration.yml?branch=main&style=flat-square&label=ui%20integration)](https://github.com/JuanTorchia/X-509-Certificate-Viewer/actions/workflows/ui-integration.yml)
[![License](https://img.shields.io/github/license/JuanTorchia/X-509-Certificate-Viewer?style=flat-square)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/JuanTorchia/X-509-Certificate-Viewer?style=social)](https://github.com/JuanTorchia/X-509-Certificate-Viewer/stargazers)

Inspect X.509 certificates and Java keystores inside IntelliJ without leaving
the IDE. This plugin is part of Juan Torchia's public engineering lab around
secure systems, digital trust, PKI workflows, and developer tooling that stays
close to real production inputs.

[Install from JetBrains Marketplace](https://plugins.jetbrains.com/plugin/30727-x-509-certificate-viewer)
· [Star the repo](https://github.com/JuanTorchia/X-509-Certificate-Viewer)
· [Report an issue](https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues)
· [Contribute](CONTRIBUTING.md)
· [Roadmap discussion](https://github.com/JuanTorchia/X-509-Certificate-Viewer/discussions/24)
· [juanchi.dev](https://juanchi.dev/en)

## Why This Exists

Certificate-heavy Java projects often force developers to leave the IDE for
terminal glue: `openssl`, `keytool`, temporary dumps, copied passwords, and
half-remembered commands. CertView X.509 keeps the inspection loop
inside JetBrains IDEs while staying explicit about what it does and does not do.

Use it when you need to quickly inspect:

- local PEM, DER, CRT, and CER certificate files
- PKCS#12 keystores used by Java services and integrations
- JKS and JCEKS keystores in legacy or enterprise Java systems
- subject, issuer, serial number, validity dates, and common certificate data
- certificate files during secure backend, PKI, identity, and signing work

This is an inspection tool. It is not a certificate authority validator, WebPKI
policy engine, revocation checker, or replacement for organizational PKI review.

## Marketplace

The plugin is published on JetBrains Marketplace:

| Product | What it does | Live signal |
| --- | --- | --- |
| [CertView X.509](https://plugins.jetbrains.com/plugin/30727-x-509-certificate-viewer) | Inspect X.509 certificates and Java keystores inside IntelliJ-based IDEs. | ![version](https://img.shields.io/jetbrains/plugin/v/30727?style=flat-square&logo=jetbrains&label=version) ![downloads](https://img.shields.io/jetbrains/plugin/d/30727?style=flat-square&logo=jetbrains&label=downloads) |

## Screenshots

Marketplace and README screenshots are generated from safe demo certificates
and keystores, not from a maintainer's personal IDE or real customer material.

Screenshot automation is tracked in
[#17](https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues/17), and
the first README/Marketplace image batch is tracked in
[#14](https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues/14).

## Supported Formats

Current IntelliJ file associations:

| Extension | Format |
| --- | --- |
| `.pem` | PEM certificate |
| `.crt`, `.cer` | PEM or DER certificate |
| `.der` | DER certificate |
| `.p12`, `.pfx` | PKCS#12 keystore |
| `.jks`, `.jceks` | Java keystore |

Input size limits:

| Input type | Maximum size |
| --- | --- |
| PEM, DER, CRT, CER certificate files | 1 MiB |
| PKCS#12, JKS, JCEKS keystores | 10 MiB |

Oversized files are refused before parsing to keep the IDE responsive.

## IntelliJ Usage

1. Install the plugin from
   [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/30727-x-509-certificate-viewer).
2. Open a supported certificate or keystore file from the Project view.
3. The custom certificate viewer opens instead of the default editor.
4. Password-protected keystores prompt for a password in the IDE.
5. Certificate metadata and validity information are shown in the viewer.

## Project Status

This repository is the IntelliJ Platform implementation of the viewer. The
current focus is hardening parser behavior, improving real fixtures and tests,
polishing Marketplace metadata, and keeping the contributor workflow simple.

Active work is tracked in:

- [Roadmap](docs/ROADMAP.md)
- [Public contributor readiness milestone](https://github.com/JuanTorchia/X-509-Certificate-Viewer/milestone/1)
- [Roadmap discussion](https://github.com/JuanTorchia/X-509-Certificate-Viewer/discussions/24)
- [Open issues](https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues)
- [Dependency health policy](docs/DEPENDENCY_HEALTH.md)
- [Marketplace screenshot workflow](docs/MARKETPLACE_SCREENSHOTS.md)

## Build From Source

Requirements:

- JDK 21
- IntelliJ Platform Gradle Plugin 2.x
- Gradle 9.x through the included wrapper
- IntelliJ Platform 2026.1.x

Build:

```bash
./gradlew build
```

On Windows, prefer the repo-local environment script so Gradle uses JDK 21:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat build --no-daemon
```

Full functional validation, including parser tests, IntelliJ UI integration,
and plugin build:

```powershell
.\scripts\dev-env.ps1 .\gradlew.bat validateFunctional --no-daemon
```

The plugin ZIP is generated under:

```text
build/distributions/
```

Full Windows setup is documented in
[docs/DEVELOPMENT_WINDOWS.md](docs/DEVELOPMENT_WINDOWS.md).

## Repository Layout

| Path | Purpose |
| --- | --- |
| `src/main/kotlin/` | IntelliJ Platform plugin source code. |
| `src/test/kotlin/` | Parser and plugin tests. |
| `src/main/resources/META-INF/plugin.xml` | JetBrains plugin metadata and extension registrations. |
| `build.gradle.kts` | Gradle build for the plugin. |
| `.github/workflows/` | CI, security, quality, and publishing workflows. |
| `.github/dependabot.yml` | Daily dependency update automation for Gradle and GitHub Actions. |
| `docs/` | Development, roadmap, and dependency health notes. |

## Security And Quality

This project handles security-adjacent files, so maintenance is intentionally
conservative:

- Dependabot checks Gradle and GitHub Actions dependencies daily.
- Dependency Review blocks high-severity dependency findings in PRs.
- CodeQL analyzes Java/Kotlin code.
- Gradle build and tests run in CI.
- UI Integration runs the full `validateFunctional` gate as an experimental,
  non-blocking signal while the IntelliJ sandbox test is stabilized.
- Certificate and keystore inputs have explicit size limits and oversized
  files are rejected before parser work starts.
- Secret scanning and push protection are expected at the repository level;
  GitGuardian may also appear as an external app check when enabled by the
  repository owner.
- Real private keys, customer certificates, keystores, signing credentials, and
  Marketplace tokens must never be committed.

Security-sensitive findings should be reported privately. See
[SECURITY.md](SECURITY.md).

## Contributing

Contributions are welcome when they are scoped, testable, and clear about the
certificate formats affected. Good first contributions include:

- generated certificate fixtures for parser tests
- malformed input test cases
- clearer parser error states
- UI polish with before/after screenshots
- Marketplace and documentation improvements
- compatibility checks for newer JetBrains IDE builds

Current contributor entry points:

- [Good first issues](https://github.com/JuanTorchia/X-509-Certificate-Viewer/labels/good%20first%20issue)
- [Help wanted issues](https://github.com/JuanTorchia/X-509-Certificate-Viewer/labels/help%20wanted)
- [Public contributor readiness milestone](https://github.com/JuanTorchia/X-509-Certificate-Viewer/milestone/1)
- [Roadmap discussion](https://github.com/JuanTorchia/X-509-Certificate-Viewer/discussions/24)

Start with [CONTRIBUTING.md](CONTRIBUTING.md) and look for
[`good first issue`](https://github.com/JuanTorchia/X-509-Certificate-Viewer/labels/good%20first%20issue)
or [`help wanted`](https://github.com/JuanTorchia/X-509-Certificate-Viewer/labels/help%20wanted).

## Author

Built by [Juan Torchia](https://github.com/JuanTorchia), a Software Architect
focused on secure enterprise systems, digital trust, PKI/X.509 tooling, digital
signature workflows, and AI-assisted engineering.

- Website: [juanchi.dev](https://juanchi.dev/en)
- LinkedIn: [linkedin.com/in/jtorchia-dev](https://www.linkedin.com/in/jtorchia-dev/)
- Public lab: [juanchi.dev/lab](https://juanchi.dev/en#lab)

## License

This project is licensed under the [Apache License 2.0](LICENSE).
