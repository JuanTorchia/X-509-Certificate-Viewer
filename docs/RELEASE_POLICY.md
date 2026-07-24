# Release Policy

This project separates normal development validation from JetBrains Marketplace
publishing. Merging to `main` must never publish a plugin update.

Design discussion and implementation tracking live in
[#34](https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues/34).

## Principles

- `main` is a validation branch, not a deployment trigger.
- Pull requests must be reviewable without carrying Marketplace release risk.
- Marketplace publishing requires explicit maintainer release intent.
- Signing and Marketplace credentials are release-only secrets.
- Every public plugin version must be traceable to a GitHub Release and tag.
- Release notes must describe user-visible changes and relevant compatibility
  or security hardening.

## Release Trigger

Publishing to JetBrains Marketplace is triggered only by publishing a GitHub
Release whose tag matches:

```text
vX.Y.Z
vX.Y.Z-suffix
```

Examples:

```text
v1.0.28
v1.1.0
v1.1.0-beta.1
```

The workflow strips the leading `v` and passes the remaining value to Gradle as
`-PpluginVersion`. A release tag `v1.0.28` therefore publishes plugin version
`1.0.28`.

## GitHub Actions Layout

| Workflow | Purpose | Publishes to Marketplace |
| --- | --- | --- |
| `IntelliJ Plugin Build` | Builds and tests PRs and relevant `main` changes. | No |
| `Security & Quality` | Runs dependency review, CodeQL, and Gradle quality checks. | No |
| `UI Integration` | Runs experimental `validateFunctional` sandbox coverage. | No |
| `JetBrains Marketplace Release` | Revalidates and publishes a GitHub Release tag. | Yes |

The release workflow is intentionally separate from build and quality workflows
so contributors can reason about CI without needing Marketplace credentials.

## Protected Environment

The `JetBrains Marketplace Release` workflow uses the GitHub environment:

```text
jetbrains-marketplace
```

Configure that environment in GitHub repository settings with:

- required reviewers
- deployment branch/tag rule allowing release tags such as `v*`
- environment-scoped secrets:
  - `PUBLISH_TOKEN`
  - `CERTIFICATE_CHAIN`
  - `PRIVATE_KEY`
  - `PRIVATE_KEY_PASSWORD`

Do not store Marketplace publishing credentials as broadly available repository
secrets once the environment is configured.

## Release Checklist

Before publishing a GitHub Release:

- Confirm the target commit is already merged to `main`.
- Confirm PR checks passed for the release content.
- Run or review `build`.
- Run or review `verifyPlugin`.
- Run or review `validateFunctional`; if the experimental UI job is flaky,
  document the reason and any manual IDE validation.
- Confirm JetBrains Plugin Verifier reports compatibility for the supported IDE
  range.
- Review release notes for user-visible changes, compatibility notes, and
  security-sensitive behavior.
- Review `docs/MARKETPLACE_COPY.md` when Marketplace-facing behavior changes.
- Review Marketplace screenshots when UI behavior changes.
- Confirm no real private keys, customer certificates, signing credentials, or
  generated build artifacts are committed.

## Contributor Expectations

External contributors do not need Marketplace credentials and should not run
release publishing. Contributor PRs should focus on source, tests, docs, parser
fixtures, UI validation, and release notes when their changes are user-visible.

Maintainers are responsible for converting merged work into a release.

## Rollback Model

Normal code changes can be reverted with a PR. Marketplace releases are external
artifacts, so the safer rollback path is usually a new patch release with a
clear fix and release note.

Avoid deleting tags or rewriting release history after a Marketplace publish
unless the release was created incorrectly and no users could reasonably have
consumed it.

## References

- JetBrains publishing guide:
  https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
- JetBrains plugin signing:
  https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
- JetBrains IntelliJ Platform Plugin Template release flow:
  https://github.com/JetBrains/intellij-platform-plugin-template#release-flow
- GitHub Actions release events:
  https://docs.github.com/actions/reference/workflows-and-actions/events-that-trigger-workflows
- GitHub protected deployment environments:
  https://docs.github.com/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments
