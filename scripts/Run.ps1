. "$PSScriptRoot/Tools.ps1"
$java = Initialize-TellusTools
$jar = Join-Path $script:ProjectRoot 'target/ProjektTellus.jar'
if (-not (Test-Path -LiteralPath $jar)) { & "$PSScriptRoot/Build.ps1" }
Push-Location -LiteralPath $script:ProjectRoot
try {
    $javaw = Join-Path (Split-Path -Parent $java) 'javaw.exe'
    $stderr = Join-Path $script:ProjectRoot 'target/startup.err.log'
    $stdout = Join-Path $script:ProjectRoot 'target/startup.out.log'
    $quotedJar = '"' + $jar + '"'
    # A janela interativa precisa ser visível mesmo se o lançador foi iniciado oculto.
    $application = Start-Process -FilePath $javaw -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',$quotedJar -WorkingDirectory $script:ProjectRoot -WindowStyle Normal -RedirectStandardError $stderr -RedirectStandardOutput $stdout -Wait -PassThru
    if ($application.ExitCode -ne 0) {
        Get-Content -LiteralPath $stderr -ErrorAction SilentlyContinue
        throw 'A aplicação não pôde ser iniciada. Consulte target/startup.err.log.'
    }
} finally { Pop-Location }
