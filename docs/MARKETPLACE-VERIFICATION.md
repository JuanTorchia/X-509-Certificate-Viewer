# Verify CertView certificate flows in IntelliJ2026.2

Issue #53 originally named1.0.35. Public1.0.36 includes PR #55's password-dialog
fix;1.0.35 rejects correct protected-keystore passwords. Marketplace approval
is already complete. The current public1.0.36 still has the PEM-chain dispatch
bug tracked in #64: Java's single-certificate factory accepts PEM, so the DER
probe consumed the first block before the chain parser could run.

The candidate fix makes the DER entry point require the binary ASN.1 SEQUENCE
prefix. PEM then reaches the existing multi-block parser. The regression checks
that actual dispatch boundary and preserves binary DER support. The viewer header also has its own NORTH layout slot, with cards in CENTER
(#65); the previous duplicate NORTH assignment hid the status/support header.
No certificate trust, path or revocation validation is implied.

The **Certificate IDE flows** workflow runs the same assertions on actual
IntelliJ IDEA2026.2.2 with two explicit sources:

- PR runs install the locally built **candidate**, version1.0-SNAPSHOT.
- Manual `published` runs download the exact requested version via JetBrains
  Starter's official PluginManager downloader. They must not install a local ZIP.

Both modes assert the actual262.x IDE build, loaded plugin/version and source.
The projected receipt records the source; a candidate pass is not a public-release
pass. #53 remains incomplete until the corrected version is publicly approved,
installed and passes the published-mode test. The strict public1.0.36 chain
assertion must not be weakened to make its known behavior look successful.

Generated disposable PEM, DER, PEM chain, PKCS12, JKS and JCEKS inputs exercise
the custom editor, subject/SAN, full fingerprint tooltip, validity dates/status
and issuer card. PKCS12 must prompt; JKS/JCEKS may expose public certificates
with a null password, without verifying store integrity or decrypting private
keys. The test records that distinction. Fixtures use only reserved `.example`
names and password `changeit`. Keys remain ignored build output, not artifacts.

Reproduce candidate verification on a Linux CI/verification machine with JDK21,
OpenSSL, Xvfb and sufficient memory (not the local VM):

```bash
python3 scripts/verification/generate-marketplace-fixtures.py
CERTVIEW_PUBLISHED_SMOKE=1 CERTVIEW_VERIFY_SOURCE=candidate xvfb-run --auto-servernum ./gradlew integrationTest --tests '*PublishedMarketplaceSmokeTest' verifyIdePluginLogs --no-daemon
CERTVIEW_VERIFY_SOURCE=candidate python3 scripts/verification/verify-marketplace-evidence.py
```

For a public version, use source `published` and set
`CERTVIEW_MARKETPLACE_VERSION` to its exact version in both commands, or select
those inputs on the manual workflow. Never substitute a candidate when public
acceptance is intended.

The test is opt-in. UI queries are resolved with waitFound; lazy queries alone
prove nothing. The evidence gate rejects skipped/missing reports, missing flows
and absent/empty IDE logs. Keep reports, IDE logs and the source-labelled receipt
in the14-day artifact. Record exact head/run and Marketplace metadata in the
issue/Discussion. No personal IDE/project or production PKI is used.

Prepared patch-release notes are in RELEASE-1.0.37.md. Publishing still follows
RELEASE_POLICY.md, including explicit maintainer release intent and the protected
release workflow. Merging this candidate does not publish. Code rollback is a
revert; public release rollback normally uses a fix-forward version.

## Modal-dialog ordering

Opening a password-protected store can show a modal dialog between remote
editor calls. The controller must remain free to enter the generated password.
The openFile phase runs on a worker with the pinned Driver's per-thread
context; the main test handles the dialog, then waits for the bounded opening
result. The Driver serializes RPC on each connection, so this is not a generic
guarantee for APIs that synchronously open nested modals. CertView constructs
its editor asynchronously; that operation returns before showing the dialog. Editor selection/current-file/text-editor assertions run only after
that modal has closed. Source/version/field/log assertions remain mandatory.
This fixes a harness race (#66), not plugin runtime behavior; no new release.
