param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Run
)

$ErrorActionPreference = "Stop"

$candidatePaths = @()

if ($env:CERT_VIEWER_JDK17) {
    $candidatePaths += $env:CERT_VIEWER_JDK17
}

$candidatePaths += @(
    "$env:USERPROFILE\scoop\apps\temurin17-jdk\current",
    "$env:ProgramFiles\Eclipse Adoptium\jdk-17",
    "$env:ProgramFiles\Java\jdk-17"
)

$jdkHome = $candidatePaths |
    Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) } |
    Select-Object -First 1

if (-not $jdkHome) {
    Write-Error "JDK 17 not found. Install it with: scoop install temurin17-jdk"
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
