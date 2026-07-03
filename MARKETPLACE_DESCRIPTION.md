# JetBrains Marketplace Listing Information

Use this content to fill out the plugin listing page. Keep it aligned with
`README.md`, `docs/MARKETPLACE_COPY.md`, and
`src/main/resources/META-INF/plugin.xml`.

## Plugin Name

X.509 Certificate Viewer for IntelliJ

## Short Description

Inspect X.509 certificates and Java keystores inside IntelliJ-based IDEs.

## Full Description

X.509 Certificate Viewer brings local certificate and Java keystore inspection
into IntelliJ-based IDEs. It is built for certificate-heavy Java projects, PKI
workflows, digital identity integrations, signing systems, and secure backend
development where developers need to inspect certificate material without
leaving the IDE.

### Key Features

- Open PEM, DER, CRT, and CER certificate files from the Project view.
- Inspect PKCS#12, JKS, and JCEKS keystores used by Java services and
  integrations.
- Review certificate metadata such as subject, issuer, serial number, validity
  dates, and common certificate fields in a native editor tab.
- Prompt for keystore passwords when protected stores require them.
- Keep local inspection inside the IDE without terminal glue or temporary
  certificate dumps.

### Scope And Limitations

This is an inspection tool. It does not replace certificate authority
validation, certificate path validation, revocation checks, WebPKI policy
validation, FIPS review, or organizational PKI approval.

## Tags

Security, Cryptography, Certificate, X509, Keystore, JKS, PKCS12, SSL/TLS

## Screenshot Notes

Reuse generated screenshots from `docs/assets/marketplace/` once demo assets
are available. Do not upload screenshots that expose private keys, customer
certificates, production keystores, local usernames, or private paths.

## Links

- Plugin repository: https://github.com/JuanTorchia/X-509-Certificate-Viewer
- Author site: https://juanchi.dev/en
- Security policy: https://github.com/JuanTorchia/X-509-Certificate-Viewer/blob/main/SECURITY.md

## Version Notes

- Initial IntelliJ Platform plugin release with certificate and keystore file
  associations.
- Native certificate detail view for supported certificate files.
- Password prompt support for encrypted keystores.
