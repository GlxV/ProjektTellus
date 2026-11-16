param(
    [Parameter(Mandatory=$true)][string]$RuntimeRoot,
    [Parameter(Mandatory=$true)][string]$PresentationsSkill,
    [string]$PdfSkill = '',
    [string]$PythonExecutable = 'python',
    [string]$DeckName = 'ProjektTellus-Defesa-Individual-r4.pptx',
    [string]$PreviewDirectory = 'docs/apresentacao/editorial-r4',
    [string]$BuildDirectory = 'target/editorial-r4-build'
)
$ErrorActionPreference = 'Stop'
$materialsRoot = Split-Path -Parent $PSScriptRoot
if ($DeckName -notmatch '^[a-zA-Z0-9_-]+\.pptx$') { throw 'Use um nome simples terminado em .pptx.' }
$materialsOutput = Join-Path $materialsRoot ('docs/apresentacao/' + $DeckName)
if (Test-Path -LiteralPath $materialsOutput) { throw 'Esse PowerPoint já existe. Escolha outro DeckName para preservar a edição anterior.' }
$materialsPdf = [IO.Path]::ChangeExtension($materialsOutput,'.pdf')
if (Test-Path -LiteralPath $materialsPdf) { throw 'Esse PDF já existe. Escolha outra edição para preservar a anterior.' }
if (Test-Path -LiteralPath (Join-Path $materialsRoot $PreviewDirectory)) { throw 'A pasta de previews já existe. Escolha outra PreviewDirectory para preservar seus arquivos.' }
$materialsNode = Join-Path $RuntimeRoot 'dependencies/node/bin/node.exe'
$env:RUNTIME_NODE_MODULES = Join-Path $RuntimeRoot 'dependencies/node/node_modules'
$env:TELLUS_ARTIFACT_MODULE = Join-Path $env:RUNTIME_NODE_MODULES '@oai/artifact-tool/dist/artifact_tool.mjs'
$env:TELLUS_RUNTIME_PYTHON = Join-Path $RuntimeRoot 'dependencies/python/python.exe'
$env:TELLUS_PRESENTATIONS_SKILL = $PresentationsSkill
$env:TELLUS_DECK_NAME = $DeckName
$env:TELLUS_PDF_NAME = [IO.Path]::GetFileName($materialsPdf)
$env:TELLUS_PREVIEW_DIR = $PreviewDirectory
$env:TELLUS_BUILD_DIR = $BuildDirectory
foreach ($required in @($materialsNode,$env:TELLUS_ARTIFACT_MODULE,$env:TELLUS_RUNTIME_PYTHON)) {
    if (-not (Test-Path -LiteralPath $required)) { throw "Dependência do runtime ausente: $required" }
}
Push-Location -LiteralPath $materialsRoot
try {
    & $PythonExecutable -c "import sys; sys.path.insert(0,'.tools/audit-python'); import reportlab, fitz, PIL"
    if ($LASTEXITCODE -ne 0) { throw 'Prepare ReportLab, PyMuPDF e Pillow conforme docs/apresentacao/README.md.' }
    & $materialsNode (Join-Path $PresentationsSkill 'container_tools/mark_artifact_operation_started.mjs') --operation-kind edit --expected-output-count 1 --output-format pptx
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar a operação de geração.' }
    & $PythonExecutable scripts/Prepare-DefenseFindings.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao selecionar os resultados medidos.' }
    & $materialsNode scripts/Build-AcademicDeck.mjs
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar slides.' }
    & $PythonExecutable scripts/Personalize-DefenseMetadata.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao definir metadados acadêmicos.' }
    $materialsPdfSkill = $PdfSkill
    if (-not $materialsPdfSkill) {
        $materialsSkillVersion = Split-Path -Leaf (Split-Path -Parent (Split-Path -Parent $PresentationsSkill))
        $materialsSkillsRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PresentationsSkill)))
        $materialsPdfSkill = Join-Path $materialsSkillsRoot ("pdf/$materialsSkillVersion/skills/pdf")
    }
    & $materialsNode (Join-Path $materialsPdfSkill 'container_tools/mark_artifact_operation_started.mjs') --operation-kind edit --expected-output-count 1 --output-format pdf
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar a exportação PDF.' }
    & $PythonExecutable scripts/Export-AcademicPdf.py
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao montar o PDF.' }
    & $materialsNode scripts/Finalize-AcademicDeck.mjs
    if ($LASTEXITCODE -ne 0) { throw 'A validação do PowerPoint falhou; consulte o recibo.' }
    & $materialsNode scripts/Render-AcademicPptx.mjs $materialsOutput (Join-Path $BuildDirectory 'pptx-check')
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao renderizar o PowerPoint final.' }
    Write-Host "Apresentação gerada: $materialsOutput"
    Write-Host 'Inspecione os PNGs antes de usar a nova edição. O vídeo é gerado separadamente por Build-DemoVideo.py.'
} finally { Pop-Location }
