# Marketplace Copy

Use this copy as the source of truth for JetBrains Marketplace wording. Keep it
aligned with `README.md` and `src/main/resources/META-INF/plugin.xml`.

## Short Description

Inspect X.509 certificates and Java keystores inside IntelliJ-based IDEs.

## Long Description

CertView X.509 brings local certificate and Java keystore inspection
into JetBrains IDEs. It is built for certificate-heavy Java projects, PKI
workflows, digital identity integrations, signing systems, and secure backend
development where developers often need to inspect certificate material without
leaving the IDE.

Supported inputs include PEM, DER, CRT, CER, PKCS#12, JKS, and JCEKS files.

The plugin can open supported files from the Project view, show certificate
metadata such as subject, issuer, serial number, and validity dates, and prompt
for keystore passwords when required.

This is an inspection tool. It does not replace certificate path validation,
revocation checks, WebPKI policy validation, FIPS review, or organizational PKI
policy decisions.

## Getting Started

1. Install CertView X.509 from JetBrains Marketplace.
2. Open a supported certificate or keystore file from the Project view.
3. Review subject, issuer, serial number, validity dates, and related metadata
   in the dedicated viewer.
4. Enter the keystore password when opening PKCS#12, JKS, or JCEKS files that
   require one.

## What Is Specific About This Plugin

- Focused on local X.509 certificate and Java keystore inspection.
- Uses IDE file associations for PEM, DER, CRT, CER, PKCS#12, JKS, and JCEKS.
- Avoids terminal dump workflows when a developer only needs quick metadata
  inspection.
- Explicitly scoped as an inspection aid, not a certificate trust or revocation
  validation engine.

## Marketplace Feature Bullets

- Open PEM, DER, CRT, and CER certificate files from the Project view.
- Inspect PKCS#12, JKS, and JCEKS keystores used by Java services and
  integrations.
- Review subject, issuer, serial number, validity dates, and common certificate
  fields in a native IntelliJ editor tab.
- Prompt for keystore passwords when protected stores require them.
- Keep local certificate inspection inside the IDE without terminal glue or
  temporary certificate dumps.

## Tags

- X.509
- certificates
- PKI
- keystore
- Java
- IntelliJ
- JetBrains
- security tooling
- digital trust

Recommended Marketplace tag priority:

1. certificates
2. security tooling
3. Java
4. IntelliJ
5. PKI

## Links

- Plugin repository: https://github.com/JuanTorchia/X-509-Certificate-Viewer
- Author site: https://juanchi.dev/en
- Security policy: https://github.com/JuanTorchia/X-509-Certificate-Viewer/blob/main/SECURITY.md
- Issue tracker: https://github.com/JuanTorchia/X-509-Certificate-Viewer/issues
- Documentation: https://github.com/JuanTorchia/X-509-Certificate-Viewer#readme

## Marketplace Admin Checklist

- Upload at least one reviewed screenshot from `docs/assets/marketplace/` after
  running the screenshot workflow.
- Set source code URL to the public GitHub repository.
- Set issue tracker URL to GitHub Issues.
- Set documentation URL to the README.
- Set license to Apache License 2.0.
- Keep donation/support links out of the plugin description; use Marketplace's
  dedicated field if needed.
