. "$PSScriptRoot/Tools.ps1"
$java = Initialize-TellusTools
$maven = Find-TellusMaven
Push-Location -LiteralPath $script:ProjectRoot
try {
    & $maven -B -ntp clean verify
    if ($LASTEXITCODE -ne 0) { throw 'Compilação ou testes falharam. Veja a mensagem acima.' }
    Write-Host 'Pronto: target/ProjektTellus.jar'
} finally { Pop-Location }
