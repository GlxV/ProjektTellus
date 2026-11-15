param([string]$RunName = ('experimental-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')))
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
if ($RunName -notmatch '^[a-zA-Z0-9_-]+$') { throw 'RunName must contain only letters, digits, _ and -.' }
. (Join-Path $PSScriptRoot 'Tools.ps1')
$java = Find-TellusJava
if (-not $java) { throw 'Java 25 is required; prepare the local .tools JDK first.' }
$javac = Join-Path (Split-Path -Parent $java) 'javac.exe'
if (-not (Test-Path -LiteralPath $javac)) { throw 'The selected Java 25 must be a JDK with javac.' }
$production = Join-Path $project 'target/classes'
if (-not (Test-Path -LiteralPath (Join-Path $production 'com/projekttellus/simulation/ModeloPlanetario.class'))) {
    throw 'target/classes must first be compiled by the main agent. This script never starts Maven.'
}
$evidence = Join-Path $project ('docs/auditoria/' + $RunName)
if (Test-Path -LiteralPath $evidence) { throw "Evidence directory already exists: $evidence" }
New-Item -ItemType Directory -Path $evidence | Out-Null
$binary = Join-Path $evidence 'classes'
New-Item -ItemType Directory -Path $binary | Out-Null
$source = Join-Path $project 'src/test/java/com/projekttellus/ExperimentalAudit.java'
$driver = Join-Path $env:USERPROFILE '.m2/repository/org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar'
if (-not (Test-Path -LiteralPath $driver)) {
    $driver = Join-Path $project 'target/ProjektTellus.jar'
    if (-not (Test-Path -LiteralPath $driver)) { throw 'SQLite runtime jar or target/ProjektTellus.jar is required.' }
}
$classpath = $binary + [IO.Path]::PathSeparator + $production + [IO.Path]::PathSeparator + $driver
$files = @(Get-ChildItem -LiteralPath (Join-Path $project 'src/main/java') -Recurse -File -Filter '*.java')
$files += @(Get-ChildItem -LiteralPath $production -Recurse -File -Filter '*.class')
$files += @(Get-Item -LiteralPath $source)
$files += @(Get-Item -LiteralPath (Join-Path $project 'scripts/Run-ExperimentalAudit.ps1'))
$files += @(Get-Item -LiteralPath $driver)
$files | ForEach-Object { $hash = Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256; $label = $_.FullName; if ($label.StartsWith($project + '\')) { $label = $label.Substring($project.Length+1) }; [pscustomobject]@{Path=$label;SHA256=$hash.Hash} } |
    Export-Csv -LiteralPath (Join-Path $evidence 'input-hashes.csv') -NoTypeInformation -Encoding UTF8
Push-Location $project
try {
    $personal = @(Get-ChildItem -LiteralPath (Join-Path $project 'data') -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Extension -in '.db','.sqlite','.sqlite3' })
    $saveHashes = @($personal | ForEach-Object { Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256 })
    $saveHashes | Export-Csv -LiteralPath (Join-Path $evidence 'personal-db-before.csv') -NoTypeInformation -Encoding UTF8
    & $javac -encoding UTF-8 -cp $production -d $binary $source 2>&1 | Tee-Object -FilePath (Join-Path $evidence 'javac.log')
    if ($LASTEXITCODE -ne 0) { throw "javac failed: $LASTEXITCODE" }
    & $java --enable-native-access=ALL-UNNAMED -cp $classpath com.projekttellus.ExperimentalAudit $evidence 2>&1 |
        Tee-Object -FilePath (Join-Path $evidence 'execution.log')
    $result = $LASTEXITCODE
    $afterHashes = @($personal | ForEach-Object { Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256 })
    $afterHashes | Export-Csv -LiteralPath (Join-Path $evidence 'personal-db-after.csv') -NoTypeInformation -Encoding UTF8
    $savedUnchanged = (($saveHashes | ForEach-Object { $_.Path + ':' + $_.Hash }) -join '|') -ceq (($afterHashes | ForEach-Object { $_.Path + ':' + $_.Hash }) -join '|')
    $outputs = @(Get-ChildItem -LiteralPath $evidence -File | Where-Object { $_.Name -ne 'output-hashes.csv' })
    $outputs | ForEach-Object { $hash = Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256; [pscustomobject]@{File=$_.Name;Bytes=$_.Length;SHA256=$hash.Hash} } |
        Export-Csv -LiteralPath (Join-Path $evidence 'output-hashes.csv') -NoTypeInformation -Encoding UTF8
    [pscustomobject]@{RunName=$RunName;ExitCode=$result;Classpath=$classpath;Java=$java;Javac=$javac;Command='scripts/Run-ExperimentalAudit.ps1';MavenStarted=$false;PersonalDatabases=$saveHashes.Count;PersonalDatabasesUnchanged=$savedUnchanged;Commit=(& git rev-parse HEAD);CompletedUtc=[DateTime]::UtcNow.ToString('o')} |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $evidence 'execution.json') -Encoding UTF8
    if ($result -ne 0) { throw "Experimental audit returned $result. Inspect $evidence" }
    Write-Host "Evidence: $evidence"
} finally { Pop-Location }
