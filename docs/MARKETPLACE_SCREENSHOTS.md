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
  `X.509 Certificate Viewer`.
- JetBrains Starter + Driver remains the preferred next step for finer UI
  interaction and deterministic editor selection.
- `integrationTest` provides the first sandbox smoke-test entry point for
  launching IntelliJ with the plugin installed.

## Generate Screenshots

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
target test should mirror the VS Code extension workflow: open generated
certificate and keystore fixtures as a user would, then assert that the custom
viewer/editor appears instead of a plain text editor or generic IDE fallback.

That same Driver foundation should later select the expected IDE window/editor
and capture more deterministic screenshots. JetBrains documents the Driver
approach for IntelliJ UI automation because IntelliJ-based IDEs use Swing/AWT
rather than browser UI.
