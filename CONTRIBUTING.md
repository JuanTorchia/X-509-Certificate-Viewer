# Contributing

Thanks for helping improve CertView X.509 for IntelliJ. This is a
security-adjacent developer tool from Juan Torchia's public engineering lab, so
contributions should be practical, testable, and careful about real-world PKI
inputs.

## Good first workflow

1. Open or pick an issue before starting larger work.
2. Keep each PR focused on one bug, format, parser behavior, UI improvement, or
   workflow change.
3. Add or update tests for parser behavior and supported formats.
4. Run the relevant local checks before opening a PR.
5. Describe what changed, how it was tested, and any remaining risk.

Good first contribution areas:

- generated PEM or DER fixtures for parser tests
- malformed input cases that should fail cleanly
- documentation and Marketplace wording
- UI copy and small layout improvements
- compatibility notes for newer JetBrains IDE builds

## Local setup

On Windows, use the repo-local environment script so Gradle runs with JDK 21:

```powershell
./scripts/dev-env.ps1 ./gradlew.bat test
./scripts/dev-env.ps1 ./gradlew.bat build
```

Use the narrowest validation that proves the change:

```powershell
# Parser-only changes
./scripts/dev-env.ps1 ./gradlew.bat test --no-daemon

# Plugin metadata, build, or dependency changes
./scripts/dev-env.ps1 ./gradlew.bat build --no-daemon

# UI, editor provider, supported format, or screenshot changes
./scripts/dev-env.ps1 ./gradlew.bat validateFunctional --no-daemon

# plugin.xml, IntelliJ extension, or Marketplace compatibility changes
./scripts/dev-env.ps1 ./gradlew.bat verifyPlugin --no-daemon
```

The full functional validation gate runs parser tests, the IntelliJ
Starter/Driver UI integration test, checks IntelliJ sandbox logs for errors
blamed on this plugin, and builds the plugin:

```powershell
./scripts/dev-env.ps1 ./gradlew.bat validateFunctional --no-daemon
```

CI also runs an experimental UI Integration workflow for `validateFunctional`.
It is intentionally non-blocking until the sandbox test is stable enough to make
required.

If IntelliJ shows a bottom-right IDE error notification while testing this
plugin, treat it as a bug until the sandbox logs prove otherwise. The
`validateFunctional` gate is expected to catch plugin-blamed IDE log errors such
as deprecated `FileEditor` contract violations.

See [`docs/DEVELOPMENT_WINDOWS.md`](docs/DEVELOPMENT_WINDOWS.md) for the full
Windows setup.

## Contribution standards

- Prefer small PRs that reviewers can validate quickly.
- Keep claims concrete. Do not describe behavior that is not implemented.
- Include sample certificates only when they are generated test fixtures.
- Never add real private keys, customer certificates, signing credentials, or
  marketplace tokens.
- Document limitations when a parser feature is advisory rather than a full
  trust decision.
- For UI changes, include screenshots or a short before/after description.
- For publishing or CI changes, include the exact command or workflow path that
  was validated.
- Normal pull requests must not publish to JetBrains Marketplace. Releases are
  maintainer-owned and follow [`docs/RELEASE_POLICY.md`](docs/RELEASE_POLICY.md).

## Issue quality

Good issues include:

- affected IDE or workflow area
- file format: PEM, DER, PKCS#12, JKS, JCEKS, etc.
- expected behavior
- actual behavior
- reproduction steps
- sanitized sample input or a generated fixture when possible

## Pull request checklist

- [ ] The PR has one clear scope.
- [ ] `validateFunctional` or the relevant narrower validation is documented.
- [ ] Security-sensitive behavior is described.
- [ ] Docs were updated when user-facing behavior changed.
- [ ] No secrets, private certificates, or generated build outputs are included.
