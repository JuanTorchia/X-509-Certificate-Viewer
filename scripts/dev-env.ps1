param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Run
)

$ErrorActionPreference = "Stop"

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
    Write-Error "JDK 21 not found. Install it with: scoop install temurin21-jdk"
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version

if ($Run.Count -gt 0) {
    $command = $Run[0]
    $arguments = @()
    if ($Run.Count -gt 1) {
        $arguments = $Run[1..($Run.Count - 1)]
    }

    & $command @arguments
    exit $LASTEXITCODE
}
