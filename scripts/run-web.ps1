Param(
  [int]$Port = 5173
)

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$logsDir = Join-Path $root "logs"
if (-not (Test-Path $logsDir)) {
  New-Item -ItemType Directory -Path $logsDir | Out-Null
}

$logFile = Join-Path $logsDir ("web-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".log")
python -m http.server $Port 2>&1 | Tee-Object -FilePath $logFile
