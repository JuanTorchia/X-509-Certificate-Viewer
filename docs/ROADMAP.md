# Roadmap

This roadmap tracks the work needed to make CertView X.509 for
IntelliJ useful for real certificate-heavy projects, trustworthy as a
security-adjacent tool, and easy for outside contributors to improve.

## Public Project Polish

- Add Marketplace-ready screenshots and keep them in sync with the plugin UI.
- Keep reusable Marketplace copy in `docs/MARKETPLACE_COPY.md`.
- Keep screenshot generation reproducible with generated demo fixtures.
- Keep GitHub repository metadata aligned with the JetBrains plugin positioning:
  IntelliJ, JetBrains, PKI, X.509, Java keystores, digital trust.
- Maintain clear README sections for installation, supported formats, security
  limits, and contribution entry points.
- Keep public claims tied to implemented behavior. No marketing copy should
  promise validation, revocation checks, or policy decisions the plugin does not
  perform.

## Parser And Fixture Quality

- Add generated PEM and DER fixtures that are safe to publish.
- Add PKCS#12 and JKS fixture coverage with generated passwords documented in
  test code.
- Cover malformed, empty, encrypted, unsupported, and oversized inputs.
- Support multi-certificate PEM chains.
- Decode and display common extensions such as SAN, Key Usage, Extended Key
  Usage, Basic Constraints, and Subject Key Identifier.

## Security Hardening

- Maintain explicit maximum input sizes before parsing certificates and keystores.
- Keep oversized file rejection before expensive parser work starts.
- Show clear IDE errors for unsupported, malformed, encrypted, and oversized
  inputs.
- Avoid holding keystore passwords longer than needed.
- Keep dependency security automation on by default: Dependabot, Dependency
  Review, CodeQL, Gradle build checks, and repository-level secret scanning.
- Stabilize the experimental UI Integration workflow before making
  `validateFunctional` a required pull request check.

## Contributor Experience

- Keep issues small enough for first-time contributors.
- Label starter work with `good first issue` and `help wanted`.
- Prefer generated fixtures over real certificates or private material.
- Ask UI contributors for screenshots or short before/after notes.
- Keep Windows development setup documented because this project is maintained
  from a Windows workstation.

## Release And Marketplace Readiness

- Publish only from the protected GitHub Release path documented in
  `docs/RELEASE_POLICY.md`; merging to `main` must stay validation-only.
- Document JetBrains Marketplace secrets and signing expectations.
- Add release notes once the plugin starts receiving regular Marketplace
  updates.
- Verify compatibility against supported IntelliJ Platform versions before
  widening the supported build range.
