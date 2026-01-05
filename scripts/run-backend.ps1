Param(
  [string]$Profile = ""
)

$root = Split-Path -Parent $PSScriptRoot
Set-Location (Join-Path $root "backend")

if ($Profile -ne "") {
  $env:SPRING_PROFILES_ACTIVE = $Profile
}

$logsDir = Join-Path $root "logs"
if (-not (Test-Path $logsDir)) {
  New-Item -ItemType Directory -Path $logsDir | Out-Null
}
$logFile = Join-Path $logsDir ("backend-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".log")
mvn spring-boot:run 2>&1 | Tee-Object -FilePath $logFile
