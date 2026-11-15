param([switch]$PrepareOnly)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/Tools.ps1"
$demoJava = Find-TellusJava
if (-not $demoJava) { throw 'Java 25 não localizado. Prepare o ambiente com Abrir-ProjektTellus.cmd antes do dia da apresentação.' }
$demoJar = Join-Path $script:ProjectRoot 'target/ProjektTellus.jar'
if (-not (Test-Path -LiteralPath $demoJar)) { throw 'JAR ausente. Compile antes com scripts/Build.ps1.' }
# Every rehearsal starts in its own empty, temporary SQLite library.
$demoDirectory = Join-Path ([IO.Path]::GetTempPath()) ('ProjektTellus-apresentacao-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $demoDirectory | Out-Null
$demoDatabase = Join-Path $demoDirectory 'demo.db'
[pscustomobject]@{ Java=$demoJava; Jar=$demoJar; Database=$demoDatabase; CreatedUtc=[DateTime]::UtcNow.ToString('o') } |
    ConvertTo-Json | Set-Content -LiteralPath (Join-Path $demoDirectory 'launch.json') -Encoding UTF8
Write-Host "Banco temporário do ensaio: $demoDatabase"
if ($PrepareOnly) { Write-Host 'Preparação conferida; aplicativo não iniciado.'; return }
Push-Location -LiteralPath $script:ProjectRoot
try {
    & $demoJava --enable-native-access=ALL-UNNAMED "-Dprojekttellus.db=$demoDatabase" -jar $demoJar
    if ($LASTEXITCODE -ne 0) { throw "O aplicativo terminou com código $LASTEXITCODE" }
} finally { Pop-Location }
