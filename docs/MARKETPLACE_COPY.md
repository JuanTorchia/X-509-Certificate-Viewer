# Marketplace Copy

Use this copy as the source of truth for JetBrains Marketplace wording. Keep it
aligned with `README.md` and `src/main/resources/META-INF/plugin.xml`.

## Plugin Name

X.509 Certificate Viewer for IntelliJ

## Short Description

Inspect X.509 certificates and Java keystores inside IntelliJ-based IDEs.

## Long Description

X.509 Certificate Viewer brings local certificate and Java keystore inspection
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

## Links

- Plugin repository: https://github.com/JuanTorchia/X-509-Certificate-Viewer
- Author site: https://juanchi.dev/en
- Security policy: https://github.com/JuanTorchia/X-509-Certificate-Viewer/blob/main/SECURITY.md
