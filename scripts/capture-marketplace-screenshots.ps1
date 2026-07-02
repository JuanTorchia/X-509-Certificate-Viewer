param(
    [ValidateSet("Pem", "Pkcs12", "Jks")]
    [string] $Target = "Pem",

    [int] $StartupDelaySeconds = 35,

    [switch] $SkipLaunch,

    [switch] $GenerateOnly
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$demoRoot = Join-Path $repoRoot "build\marketplace-demo"
$fixtureDir = Join-Path $demoRoot "fixtures"
$outputDir = Join-Path $repoRoot "docs\assets\marketplace"

New-Item -ItemType Directory -Force -Path $fixtureDir, $outputDir | Out-Null

function Resolve-Jdk21 {
    $candidatePaths = @()

    if ($env:CERT_VIEWER_JDK21) {
        $candidatePaths += $env:CERT_VIEWER_JDK21
    }

    $candidatePaths += @(
        "$env:USERPROFILE\scoop\apps\temurin21-jdk\current",
        "$env:ProgramFiles\Eclipse Adoptium\jdk-21",
        "$env:ProgramFiles\Java\jdk-21"
    )

    $jdkHome = $candidatePaths |
        Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) } |
        Select-Object -First 1

    if (-not $jdkHome) {
        throw "JDK 21 not found. Install it with: scoop install temurin21-jdk"
    }

    return $jdkHome
}

function Invoke-Keytool {
    param([string[]] $Arguments)

    $keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    & $keytool @Arguments

    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed with exit code $LASTEXITCODE"
    }
}

function New-DemoFixtures {
    $password = "changeit"
    $pkcs12 = Join-Path $fixtureDir "demo-keystore.p12"
    $jks = Join-Path $fixtureDir "demo-keystore.jks"
    $pem = Join-Path $fixtureDir "demo-certificate.pem"
    $der = Join-Path $fixtureDir "demo-certificate.der"
    $readme = Join-Path $demoRoot "README.txt"

    Remove-Item -LiteralPath $pkcs12, $jks, $pem, $der -Force -ErrorAction SilentlyContinue

    Invoke-Keytool @(
        "-genkeypair",
        "-alias", "marketplace-demo",
        "-keyalg", "RSA",
        "-keysize", "2048",
        "-validity", "365",
        "-dname", "CN=Marketplace Demo,O=X.509 Certificate Viewer,L=Buenos Aires,C=AR",
        "-storetype", "PKCS12",
        "-keystore", $pkcs12,
        "-storepass", $password,
        "-keypass", $password,
        "-noprompt"
    )

    Invoke-Keytool @(
        "-importkeystore",
        "-srckeystore", $pkcs12,
        "-srcstoretype", "PKCS12",
        "-srcstorepass", $password,
        "-destkeystore", $jks,
        "-deststoretype", "JKS",
        "-deststorepass", $password,
        "-destkeypass", $password,
        "-noprompt"
    )

    Invoke-Keytool @(
        "-exportcert",
        "-rfc",
        "-alias", "marketplace-demo",
        "-keystore", $pkcs12,
        "-storetype", "PKCS12",
        "-storepass", $password,
        "-file", $pem
    )

    Invoke-Keytool @(
        "-exportcert",
        "-alias", "marketplace-demo",
        "-keystore", $pkcs12,
        "-storetype", "PKCS12",
        "-storepass", $password,
        "-file", $der
    )

    @"
X.509 Certificate Viewer marketplace demo project.

All files under fixtures/ are generated demo inputs.
Password for demo keystores: $password

Do not replace these with real private keys, customer certificates, or production keystores.
"@ | Set-Content -LiteralPath $readme -Encoding UTF8

    return @{
        Pem = $pem
        Pkcs12 = $pkcs12
        Jks = $jks
    }
}

function Add-NativeWindowApi {
    if ("NativeWindow" -as [type]) {
        return
    }

    Add-Type @"
using System;
using System.Runtime.InteropServices;
using System.Text;

public static class NativeWindow {
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool IsWindowVisible(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

    [DllImport("user32.dll")]
    public static extern bool GetWindowRect(IntPtr hWnd, out Rect lpRect);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [StructLayout(LayoutKind.Sequential)]
    public struct Rect {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }
}
"@
}

function Get-WindowTitle {
    param([IntPtr] $Handle)

    $builder = New-Object System.Text.StringBuilder 512
    [NativeWindow]::GetWindowText($Handle, $builder, $builder.Capacity) | Out-Null
    return $builder.ToString()
}

function Find-IntelliJWindow {
    Add-NativeWindowApi

    $foundWindows = New-Object System.Collections.Generic.List[object]
    $callback = [NativeWindow+EnumWindowsProc] {
        param([IntPtr] $hWnd, [IntPtr] $lParam)

        if ([NativeWindow]::IsWindowVisible($hWnd)) {
            $title = Get-WindowTitle -Handle $hWnd
            if ($title -match "X\.509 Certificate Viewer|demo-certificate|demo-keystore") {
                $foundWindows.Add([pscustomobject]@{ Handle = $hWnd; Title = $title }) | Out-Null
            }
        }

        return $true
    }

    [NativeWindow]::EnumWindows($callback, [IntPtr]::Zero) | Out-Null
    return $foundWindows | Select-Object -First 1
}

function Find-FirstRunWindow {
    Add-NativeWindowApi

    $foundWindows = New-Object System.Collections.Generic.List[object]
    $callback = [NativeWindow+EnumWindowsProc] {
        param([IntPtr] $hWnd, [IntPtr] $lParam)

        if ([NativeWindow]::IsWindowVisible($hWnd)) {
            $title = Get-WindowTitle -Handle $hWnd
            if ($title -match "User Agreement|Privacy Policy|Welcome to IntelliJ IDEA") {
                $foundWindows.Add([pscustomobject]@{ Handle = $hWnd; Title = $title }) | Out-Null
            }
        }

        return $true
    }

    [NativeWindow]::EnumWindows($callback, [IntPtr]::Zero) | Out-Null
    return $foundWindows | Select-Object -First 1
}

function Save-WindowScreenshot {
    param(
        [IntPtr] $Handle,
        [string] $Path
    )

    Add-Type -AssemblyName System.Drawing
    Add-Type -AssemblyName System.Windows.Forms

    [NativeWindow]::SetForegroundWindow($Handle) | Out-Null
    Start-Sleep -Milliseconds 750

    $rect = New-Object NativeWindow+Rect
    [NativeWindow]::GetWindowRect($Handle, [ref] $rect) | Out-Null

    $width = $rect.Right - $rect.Left
    $height = $rect.Bottom - $rect.Top

    if ($width -le 0 -or $height -le 0) {
        throw "Cannot capture IntelliJ window because its bounds are invalid."
    }

    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)

    try {
        $graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0, $bitmap.Size)
        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

$env:JAVA_HOME = Resolve-Jdk21
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version

$fixtures = New-DemoFixtures
$targetFile = $fixtures[$Target]
$screenshotPath = Join-Path $outputDir ("{0}.png" -f $Target.ToLowerInvariant())

if ($GenerateOnly) {
    Write-Host "Demo fixtures generated under $fixtureDir"
    Write-Host "Skipping IntelliJ launch and screenshot capture because -GenerateOnly was provided."
    exit 0
}

if (-not $SkipLaunch) {
    Write-Host "Launching IntelliJ sandbox for target: $targetFile"
    $gradle = Join-Path $repoRoot "gradlew.bat"

    Start-Process `
        -FilePath $gradle `
        -ArgumentList @("runIde", "--args", "`"$targetFile`"", "--no-daemon") `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden | Out-Null

    Write-Host "Waiting $StartupDelaySeconds seconds for IntelliJ startup..."
    Start-Sleep -Seconds $StartupDelaySeconds
}

$window = Find-IntelliJWindow

if (-not $window) {
    $firstRunWindow = Find-FirstRunWindow

    if ($firstRunWindow) {
        throw "IntelliJ first-run window detected ('$($firstRunWindow.Title)'). Complete the sandbox first-run setup, then retry with -SkipLaunch. This setup screen is not a valid Marketplace screenshot."
    }

    throw "No demo IntelliJ window found. Expected a window title containing demo-certificate, demo-keystore, or X.509 Certificate Viewer. Increase -StartupDelaySeconds or open the sandbox demo file before retrying with -SkipLaunch."
}

Write-Host "Capturing window: $($window.Title)"
Save-WindowScreenshot -Handle $window.Handle -Path $screenshotPath
Write-Host "Screenshot written to $screenshotPath"
