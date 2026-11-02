# Resolved dependency advisory coverage

The Security & Quality Gradle gate exports `build/security/inventory.json`
using `scripts/security/inventory.init.gradle`. It records exact Maven
coordinates separately for runtime candidates, compile/IDE classpath, tests,
integration tests (when present), and build plugins. Unresolved dependencies
or an empty inventory fail the gate instead of becoming a clean result.

`scripts/security/scan_osv.py` queries the maintained OSV Maven database with
those exact coordinates. Only public coordinates and versions are sent, not
source files, paths, certificates or credentials. Partial responses, HTTP errors
and malformed results fail. Any matched advisory fails pending investigation;
there is no blanket allowlist. JSON inventory and findings are retained for
30 days, including failed runs. Four local contract tests cover missing results
and invalid inventories. No Gradle resolution or build runs on the local VM.

This is not complete platform coverage. Runtime candidates can include
IDE-provided dependencies; membership alone does not prove inclusion in the
plugin ZIP. Native IDE/JBR components, Gradle itself, local/project artifacts
and JavaScript bundles are outside this Maven advisory lookup. Review those
through their own inventory and release evidence. No match in OSV means no
match in that source, not a general assurance of no vulnerabilities.

Reproduce in CI/JDK21:

```sh
./gradlew --init-script scripts/security/inventory.init.gradle securityInventory --no-daemon
python3 -m unittest discover -s scripts/security -p 'test_*.py'
python3 scripts/security/scan_osv.py
```

This change adds verification only. It does not modify product dependencies,
IDE compatibility or publish a Marketplace version. Reverting removes the scan.


Actual plugin ZIPs and a per-JAR SHA-256/Maven-metadata inventory are retained
alongside the Gradle coordinates. This distinguishes the built payload from
IDE/test/build classpaths. JARs without Maven metadata are explicitly unmapped
and require review (including identifying this project's own output); they
are never interpreted as third-party libraries with a clean advisory result.
Shaded dependencies and non-JAR payloads remain explicit coverage limits.
