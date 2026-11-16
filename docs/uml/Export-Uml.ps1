param([ValidateRange(2, 6)][int]$Scale = 3)
$ErrorActionPreference = 'Stop'
$umlRoot = $PSScriptRoot
$projectRoot = Split-Path -Parent (Split-Path -Parent $umlRoot)
$rendererRoot = Join-Path $projectRoot '.tools/uml-renderer'
$renderer = Join-Path $rendererRoot 'node_modules/.bin/mmdc.cmd'
if (-not (Test-Path -LiteralPath $renderer)) {
    & npm.cmd install --prefix $rendererRoot --no-audit --no-fund '@mermaid-js/mermaid-cli@11.12.0' 'mermaid@11.17.2' 'puppeteer@23.11.1'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar Mermaid CLI.' }
}
$config = Join-Path $umlRoot 'mermaid-config.json'
foreach ($source in Get-ChildItem -LiteralPath $umlRoot -Filter '*.mmd' | Sort-Object Name) {
    foreach ($format in @('svg', 'png')) {
        $output = [IO.Path]::ChangeExtension($source.FullName, $format)
        & $renderer -i $source.FullName -o $output -c $config -b white -w 1366 -H 768 -s $Scale
        if ($LASTEXITCODE -ne 0) { throw "Falha ao exportar $($source.Name) para $format." }
    }
}
