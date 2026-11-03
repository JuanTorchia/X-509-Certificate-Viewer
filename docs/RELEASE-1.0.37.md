# CertView X.5091.0.37 — release candidate

Status: prepared for maintainer review; not published.

Fix: PEM chains now retain all certificate blocks in the editor. The DER probe
requires binary ASN.1 input, so it no longer consumes the first PEM certificate
before the chain parser runs. Subject, SAN, fingerprints and validity rendering
retain their existing inspection-only meaning.

Compatibility: IntelliJ2026.1 minimum, verifier targets2026.1.4/2026.2.2. No new
runtime libraries, trust validation, telemetry or network certificate lookups.

Before publication, require candidate unit/UI/build/verifier checks and the
six generated-format flows in actual2026.2.2. Record exact CI evidence in #64.
Publish only after explicit maintainer release intent, using a GitHub Release
tagv1.0.37 and the existing protected workflow. After Marketplace approval,
run Certificate IDE flows with sourcepublished/version1.0.37, inspect retained
logs/reports and close #53 only after that public validation succeeds.

Public1.0.36 remains immutable; its single-certificate PEM behavior is a known
limitation. A source merge or candidate pass does not fix downloaded copies.
