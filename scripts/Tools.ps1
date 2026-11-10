$ErrorActionPreference = 'Stop'
$script:ProjectRoot = Split-Path -Parent $PSScriptRoot
$script:ToolsRoot = Join-Path $script:ProjectRoot '.tools'

function Find-TellusJava {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += Join-Path $env:JAVA_HOME 'bin/java.exe' }
    $candidates += Get-ChildItem -LiteralPath $script:ToolsRoot -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'bin/java.exe' }
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) { $candidates += $javaCommand.Source }
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            $start = New-Object System.Diagnostics.ProcessStartInfo
            $start.FileName = $candidate
            $start.Arguments = '-version'
            $start.UseShellExecute = $false
            $start.CreateNoWindow = $true
            $start.RedirectStandardError = $true
            $start.RedirectStandardOutput = $true
            $process = [System.Diagnostics.Process]::Start($start)
            $versionText = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
            $process.WaitForExit()
            $process.Dispose()
            if ($versionText -match 'version "(\d+)\.') {
                if ([int]$Matches[1] -eq 25) { return $candidate }
            }
        }
    }
    return $null
}

function Install-TellusJava {
    New-Item -ItemType Directory -Force -Path $script:ToolsRoot | Out-Null
    Write-Host 'Preparando Java 25 local (Eclipse Temurin)...'
    $assets = Invoke-RestMethod 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
    $package = $assets[0].binary.package
    $archive = Join-Path $script:ToolsRoot 'jdk25.zip'
    Invoke-WebRequest -Uri $package.link -OutFile $archive -UseBasicParsing
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $package.checksum) {
        throw 'A verificação do download do Java falhou. Execute novamente.'
    }
    Expand-Archive -LiteralPath $archive -DestinationPath $script:ToolsRoot -Force
    $java = Find-TellusJava
    if (-not $java) { throw 'Não foi possível preparar Java 25.' }
    return $java
}

function Find-TellusMaven {
    $local = Join-Path $script:ToolsRoot 'apache-maven-3.9.11/bin/mvn.cmd'
    if (Test-Path -LiteralPath $local) { return $local }
    $command = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    New-Item -ItemType Directory -Force -Path $script:ToolsRoot | Out-Null
    Write-Host 'Preparando Maven local...'
    $url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip'
    $archive = Join-Path $script:ToolsRoot 'maven.zip'
    Invoke-WebRequest -Uri $url -OutFile $archive -UseBasicParsing
    $checksum = (Invoke-WebRequest -Uri "$url.sha512" -UseBasicParsing).Content
    if ($checksum -is [byte[]]) { $checksum = [Text.Encoding]::UTF8.GetString($checksum) }
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash.ToLowerInvariant() -ne $checksum.Trim().ToLowerInvariant()) {
        throw 'A verificação do download do Maven falhou. Execute novamente.'
    }
    Expand-Archive -LiteralPath $archive -DestinationPath $script:ToolsRoot -Force
    return $local
}

function Initialize-TellusTools {
    $java = Find-TellusJava
    if (-not $java) { $java = Install-TellusJava }
    $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $java)
    return $java
}
