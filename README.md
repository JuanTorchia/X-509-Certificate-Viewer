# X.509 Certificate Viewer

Inspect X.509 certificates and keystores directly inside IntelliJ-based IDEs,
without switching to OpenSSL for every check.

## Repository layout

| Path | Purpose |
| --- | --- |
| `src/` | IntelliJ Platform plugin source code. |
| `build.gradle.kts` | Gradle build for the plugin. |
| `.github/workflows/` | CI and publishing workflows. |
| `.github/dependabot.yml` | Dependency update automation for Gradle and GitHub Actions. |

## Current status

This repository is the IntelliJ Platform plugin implementation. It can register
certificate and keystore file types, open them in a custom editor, parse X.509
certificates, and prompt for keystore passwords when required.

## Supported formats

Current IntelliJ file associations:

| Extension | Format |
| --- | --- |
| `.pem` | PEM certificate |
| `.crt`, `.cer` | PEM or DER certificate |
| `.der` | DER certificate |
| `.p12`, `.pfx` | PKCS#12 keystore |
| `.jks`, `.jceks` | Java keystore |

## IntelliJ usage

1. Open a supported certificate or keystore file from the Project view.
2. The custom certificate viewer opens instead of the default editor.
3. Password-protected keystores prompt for a password in the IDE.
4. Certificate metadata and validity information are shown in the viewer.

## Build from source

```bash
./gradlew build
```

The plugin ZIP is generated under:

```text
build/distributions/
```

## Development notes

- Java 17 is required for the IntelliJ plugin build.
- The build uses IntelliJ Platform Gradle Plugin 2.x, Gradle 9.x, and targets
  IntelliJ Platform 2023.3+.
- Dependency updates are handled by Dependabot.
- Dependency health policy and known compatibility exceptions are tracked in
  [docs/DEPENDENCY_HEALTH.md](docs/DEPENDENCY_HEALTH.md).
- Security and quality checks run through GitHub Actions: Gradle build/test,
  dependency review, Gradle wrapper validation via `gradle/actions/setup-gradle`,
  and CodeQL for Java/Kotlin.
- Do not commit real private keys, certificates, keystores, or marketplace
  signing secrets.
- Windows setup is documented in [`docs/DEVELOPMENT_WINDOWS.md`](docs/DEVELOPMENT_WINDOWS.md).

## Security notes

This tool is intended for local certificate inspection. It does not establish
trust, perform full certificate path validation, check revocation status, or
replace organizational PKI policy review.

Security-sensitive findings should be reported privately. See
[`SECURITY.md`](SECURITY.md).

## Roadmap

The active improvement backlog is tracked in [`docs/ROADMAP.md`](docs/ROADMAP.md).
Use GitHub Issues for specific bugs, hardening tasks, and feature proposals.

## Contributing

Contributions are welcome when they are scoped, testable, and clear about the
certificate formats affected. Start with [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

This project is licensed under the Apache License 2.0.
