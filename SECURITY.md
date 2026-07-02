# Security Policy

## Reporting a vulnerability

Please do not open public issues for vulnerabilities involving parser crashes,
unsafe file handling, secret exposure, signing credentials, or repository
publishing tokens.

Report security-sensitive findings privately through GitHub Security Advisories
for this repository, or contact the maintainer through the GitHub profile if
advisories are not available.

## Scope

Security reports are useful when they affect:

- certificate, keystore, CSR, CRL, or key parsing
- denial-of-service risks from large or malformed inputs
- accidental exposure of private keys, passwords, tokens, or signing material
- marketplace publishing workflows
- dependency vulnerabilities with a realistic exploit path

## Non-goals

This viewer is not a certificate authority validator. Findings about trust
decisions, revocation status, WebPKI compliance, FIPS compliance, or enterprise
PKI policy should be framed as feature requests unless they demonstrate an
implementation vulnerability.

## Handling expectations

The maintainer will triage reports based on impact, reproducibility, and whether
the affected behavior exists in the current implementation.
