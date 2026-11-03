# Verify the published plugin on IntelliJ2026.2

Issue #53 originally named1.0.35. Marketplace now lists1.0.36, which includes
PR #55's password-dialog fix;1.0.35 rejects correct protected-keystore passwords.
The current release is therefore the useful verification target. Do not record
1.0.35 keystore acceptance as successful.

The **Published Marketplace verification** workflow installs exact public1.0.36
through JetBrains Starter's PluginManager downloader, not `path.to.build.plugin`.
It boots actual IntelliJ IDEA2026.2.2 and asserts the running build is262.x,
the plugin is loaded/enabled and its runtime version is1.0.36. No new version
is published by this workflow.

Generated disposable PEM, DER, PEM chain, PKCS12, JKS and JCEKS inputs exercise
the custom editor, modal password entry, subject/SAN, full fingerprint tooltip,
validity dates/status and issuer certificate rendering. The fixtures use only
reserved `.example` names and password `changeit`; they are not production PKI.
Keys remain under ignored build output and are not uploaded. UI checks resolve
the queries with `waitFound`; creating a lazy query alone proves nothing.

Reproduce on a Linux CI/verification machine with JDK21, OpenSSL, Xvfb and
sufficient memory (do not run heavy IDE evaluation in the local VM):

```bash
python3 scripts/verification/generate-marketplace-fixtures.py
CERTVIEW_PUBLISHED_SMOKE=1 xvfb-run --auto-servernum ./gradlew integrationTest --tests '*PublishedMarketplaceSmokeTest' verifyIdePluginLogs --no-daemon
python3 scripts/verification/verify-marketplace-evidence.py
```

The test is opt-in and excluded from ordinary local-ZIP integration runs. The
evidence gate rejects skipped/missing reports, missing flows and absent/empty
IDE logs. Keep test reports, IDE logs and the projected receipt in the14-day
CI artifact. Record the exact CI head/run and live Marketplace metadata in the
issue/Discussion before marking acceptance complete. No personal IDE or project
is used. Changes here affect only verification; rollback is reverting the PR.
