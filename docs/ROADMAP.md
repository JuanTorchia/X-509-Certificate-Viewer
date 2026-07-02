# Roadmap

This roadmap tracks practical improvements for making the IntelliJ plugin
easier to use, safer to maintain, and easier to contribute to.

## Near term

- Fix CI workflow correctness and split build from publish steps.
- Add real parser tests with valid PEM and DER fixtures.
- Add input size limits before parsing certificates and keystores.
- Improve IntelliJ plugin metadata, screenshots, and marketplace documentation.

## Parser and security hardening

- Support multi-certificate PEM chains.
- Show advisory warnings for expired and soon-to-expire certificates.
- Decode and display common extensions such as SAN, Key Usage, Extended Key
  Usage, Basic Constraints, and Subject Key Identifier.
- Add clear error states for encrypted, unsupported, malformed, and oversized
  inputs.
- Avoid holding keystore passwords longer than needed.

## Contributor experience

- Keep issue templates focused on reproducible bugs and specific feature
  proposals.
- Track larger improvements as GitHub Issues instead of hidden TODOs.
- Add screenshots or generated fixtures for UI and parser changes.
- Keep release notes in the README or a dedicated changelog once releases begin.
- Treat security and quality automation as required PR checks once the workflows
  are stable.

## Release readiness

- Verify Gradle wrapper and Java version compatibility in CI.
- Ensure publish workflows only run on protected release paths.
- Document required repository secrets for marketplace publishing.
- Add dry-run or build-only jobs for pull requests.
