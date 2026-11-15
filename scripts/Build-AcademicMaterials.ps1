param(
    [Parameter(Mandatory=$true)][string]$RuntimeRoot,
    [Parameter(Mandatory=$true)][string]$PresentationsSkill,
    [string]$PythonExecutable = 'python',
    [string]$DeckName = 'ProjektTellus-Apresentacao-regenerada.pptx'
)
$ErrorActionPreference = 'Stop'
$materialsRoot = Split-Path -Parent $PSScriptRoot
if ($DeckName -notmatch '^[a-zA-Z0-9_-]+\.pptx$') { throw 'Use um nome simples terminado em .pptx.' }
$materialsOutput = Join-Path $materialsRoot ('docs/apresentacao/' + $DeckName)
if (Test-Path -LiteralPath $materialsOutput) { throw 'Esse PowerPoint já existe. Escolha outro DeckName para preservar a edição anterior.' }
$materialsNode = Join-Path $RuntimeRoot 'dependencies/node/bin/node.exe'
$env:RUNTIME_NODE_MODULES = Join-Path $RuntimeRoot 'dependencies/node/node_modules'
$env:TELLUS_ARTIFACT_MODULE = Join-Path $env:RUNTIME_NODE_MODULES '@oai/artifact-tool/dist/artifact_tool.mjs'
$env:TELLUS_RUNTIME_PYTHON = Join-Path $RuntimeRoot 'dependencies/python/python.exe'
$env:TELLUS_PRESENTATIONS_SKILL = $PresentationsSkill
$env:TELLUS_DECK_NAME = $DeckName
foreach ($required in @($materialsNode,$env:TELLUS_ARTIFACT_MODULE,$env:TELLUS_RUNTIME_PYTHON)) {
    if (-not (Test-Path -LiteralPath $required)) { throw "Dependência do runtime ausente: $required" }
}
Push-Location -LiteralPath $materialsRoot
try {
    & $PythonExecutable -c "import sys; sys.path.insert(0,'.tools/audit-python'); import matplotlib, reportlab, fitz, PIL"
    if ($LASTEXITCODE -ne 0) { throw 'Prepare matplotlib, ReportLab, PyMuPDF e Pillow conforme docs/apresentacao/README.md.' }
    & $materialsNode (Join-Path $PresentationsSkill 'container_tools/mark_artifact_operation_started.mjs') --operation-kind create --expected-output-count 1 --output-format pptx
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar a operação de geração.' }
    & $PythonExecutable scripts/Export-AcademicSources.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao exportar fontes Java.' }
    & $PythonExecutable scripts/Plot-ExperimentalAudit.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar gráficos.' }
    & $materialsNode scripts/Build-AcademicDeck.mjs
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar slides.' }
    & $PythonExecutable scripts/Export-AcademicPdf.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao montar o PDF.' }
    & $materialsNode scripts/Finalize-AcademicDeck.mjs
    if ($LASTEXITCODE -ne 0) { throw 'A validação do PowerPoint falhou; consulte o recibo.' }
    Write-Host "Apresentação gerada: $materialsOutput"
    Write-Host 'Inspecione os PNGs antes de usar a nova edição. O vídeo é gerado separadamente por Build-DemoVideo.py.'
} finally { Pop-Location }
