# Marketplace Screenshots

This project uses generated demo inputs for Marketplace and README screenshots.
Do not capture screenshots from a personal IntelliJ project or from real
certificate material.

## Decision Log

- Screenshots must come from generated fixtures, not customer files or a
  maintainer's personal workspace.
- The first implementation is a local Windows workflow because desktop IDE UI
  automation in CI is more fragile than normal unit or integration tests.
- `runIde --args` is used to open a generated fixture in an IntelliJ sandbox.
- The script captures the visible IntelliJ window after startup and writes PNG
  files under `docs/assets/marketplace/`.
- First-run setup windows such as the IntelliJ User Agreement are treated as
  invalid screenshot targets.
- The capture script intentionally ignores unrelated IntelliJ windows. A valid
  target window title must reference `demo-certificate`, `demo-keystore`, or
  `CertView X.509`.
- JetBrains Starter + Driver remains the preferred next step for finer UI
  interaction and deterministic editor selection.
- `integrationTest` provides the first sandbox smoke-test entry point for
  launching IntelliJ with the plugin installed.

## Generate Screenshots on Linux

`scripts/capture-marketplace-screenshots.sh` generates the fixtures under
`/tmp/certview-demo/fixtures/` (a neutral path, since the project path is
visible in the Project view) and launches `runIde` on an isolated Xvfb display
(`:99` by default, never the live desktop). A Gradle init script adds sandbox-only
JVM flags that skip the consent, trust-project and tips dialogs.

```bash
scripts/capture-marketplace-screenshots.sh                 # fixtures + sandbox IDE on :99
scripts/capture-marketplace-screenshots.sh --generate-only # fixtures only
```

Requires JDK 21, `openssl`, `Xvfb`, `xdotool` and ImageMagick. Once the IDE is
up, dismiss the "Meet the Islands Theme" tour if shown, then size and capture
the IDE window:

```bash
export DISPLAY=:99
win=$(xdotool search --name '^certview-demo' | head -1)
xdotool windowmove "$win" 0 0 windowsize "$win" 1280 800
import -window "$win" docs/assets/marketplace/certificate-viewer.png
```

Open `demo-keystore.p12` from the Project view, enter `changeit`, and capture
`pkcs12-keystore-viewer.png` the same way. `certview-demo.gif` is assembled
from those frames with `convert -delay ... -resize 960x -layers Optimize`.

The leaf certificate is backdated 120 days and signed by a demo root CA, so the
viewer shows a realistic issuer, SANs, key usage and a partially elapsed
validity bar.

## Generate Screenshots on Windows

From PowerShell:

```powershell
.\scripts\capture-marketplace-screenshots.ps1 -Target Pem
```

Other supported targets:

```powershell
.\scripts\capture-marketplace-screenshots.ps1 -Target Pkcs12
.\scripts\capture-marketplace-screenshots.ps1 -Target Jks
```

If IntelliJ starts slowly on the machine, increase the startup wait:

```powershell
.\scripts\capture-marketplace-screenshots.ps1 -Target Pem -StartupDelaySeconds 60
```

To generate only the safe demo inputs without launching IntelliJ:

```powershell
.\scripts\capture-marketplace-screenshots.ps1 -GenerateOnly
```

If the sandbox IDE is already open, capture the current IntelliJ window without
launching another IDE:

```powershell
.\scripts\capture-marketplace-screenshots.ps1 -Target Pem -SkipLaunch
```

## Generated Inputs

The script creates demo files under `build/marketplace-demo/fixtures/`:

- `demo-certificate.pem`
- `demo-certificate.der`
- `demo-keystore.p12`
- `demo-keystore.jks`

The demo keystore password is `changeit`. These files are generated build
artifacts and should not be committed.

## Current Assets

| File | Content |
| --- | --- |
| `certificate-viewer.png` | `demo-certificate.pem` in the custom editor. |
| `pkcs12-keystore-viewer.png` | `demo-keystore.p12` after entering the demo password. |
| `certview-demo.gif` | Certificate, password prompt, keystore. README only. |

Upload the two PNGs to the JetBrains Marketplace listing (Marketplace
screenshots are managed in the vendor web UI, not from `plugin.xml`).

## Output

Screenshots are written to:

```text
docs/assets/marketplace/
```

Review every image before committing it. Screenshots must not expose private
paths, unrelated projects, unrelated plugins, secrets, private keys, customer
certificates, or production keystores.

## Future Improvement

The next version should add JetBrains Starter + Driver integration tests. The
target test should open generated certificate and keystore fixtures as a user
would, then assert that the custom viewer/editor appears instead of a plain text
editor or generic IDE fallback.

That same Driver foundation should later select the expected IDE window/editor
and capture more deterministic screenshots. JetBrains documents the Driver
approach for IntelliJ UI automation because IntelliJ-based IDEs use Swing/AWT
rather than browser UI.
