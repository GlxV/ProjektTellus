[CmdletBinding()]
param(
    [ValidateSet('Builder','FactoryMethod','Observer','Memento')]
    [string]$Pattern = 'Builder',
    [ValidateRange(1,30)][int]$Item = 1,
    [ValidateSet('Auto','VSCode','Notepad')][string]$Editor = 'Auto',
    [switch]$ListOnly
)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
# Each Find is a declaration or a unique call site. Lines are resolved from current sources.
$catalog = @{
    Builder = @(
        @('domain/TerraBuilder.java','public TerraBuilder nome(','nome / return this'),
        @('domain/TerraBuilder.java','public Terra build()','build / new Terra'),
        @('ui/BuilderDialog.java','setResultConverter(button','conversor do diálogo'),
        @('application/SimulacaoService.java','public ResultadoOperacao criar(','criar / persistir origem'),
        @('ui/MainController.java','private void createWorld()','createWorld / botão real')
    )
    FactoryMethod = @(
        @('events/EventoCreator.java','public abstract EventoPlanetario criar(','contrato do criador'),
        @('events/AsteroideCreator.java','@Override public EventoPlanetario criar(','sobrescrita / new Asteroide'),
        @('events/EventoPlanetario.java','EstadoPlanetario aplicar(','contrato do produto'),
        @('events/EventoBase.java','public final EstadoPlanetario aplicar(','aplicar / transformar'),
        @('events/Asteroide.java','protected EstadoPlanetario transformar(','transformação concreta'),
        @('application/SimulacaoService.java','public ResultadoOperacao aplicar(','cliente polimórfico / ordem real'),
        @('ui/MainController.java','private void applyEvent()','applyEvent / slider dividido por 100')
    )
    Observer = @(
        @('patterns/observer/SimulacaoObserver.java','void aoMudar(','callback void'),
        @('patterns/observer/ObserverBus.java','public AutoCloseable inscrever(','inscrever / desinscrição'),
        @('patterns/observer/ObserverBus.java','public Entrega publicar(','publicar / callbacks síncronos'),
        @('application/SimulacaoService.java','var entrega = observerBus.publicar(','publicação após persistência'),
        @('ui/MainController.java','subscription = service.observerBus().inscrever(','assinante JavaFX / runLater'),
        @('patterns/observer/AlertaObserver.java','public void aoMudar(','alertas por limiares'),
        @('patterns/observer/EstatisticasObserver.java','public void aoMudar(','estatísticas da sessão'),
        @('ui/MainController.java','private void renderObserved(','projeção na UI'),
        @('ui/MainController.java','private <T> void run(','sucesso / erro assíncrono')
    )
    Memento = @(
        @('domain/Terra.java','public TerraMemento capturar(','originador / capturar'),
        @('patterns/memento/TerraMemento.java','public record TerraMemento(','record / conteúdo legível'),
        @('domain/Terra.java','public void restaurar(','restaurar / validar mundoId'),
        @('application/SimulacaoService.java','public ResultadoOperacao capturar(','captura manual sem notificar'),
        @('application/SimulacaoService.java','public ResultadoOperacao restaurar(','candidato / retorno / confirmação'),
        @('persistence/SqliteMundoRepository.java','public synchronized void salvar(','transação de mundo / evento / retrato'),
        @('persistence/TransacaoSqlite.java','static void executar(','commit / rollback'),
        @('ui/MainController.java','private void restoreSnapshot()','restaurar selecionado / confirmação'),
        @('ui/MainController.java','private void operate(String message, Callable<ResultadoOperacao> action, AnimationKind','resultado confirmado / falha na releitura')
    )
}
$order = 0
$entries = foreach ($source in $catalog[$Pattern]) {
    $relative = 'src/main/java/com/projekttellus/' + $source[0]
    $path = Join-Path $project $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Fonte ausente: $relative" }
    $lines = @(Get-Content -LiteralPath $path -Encoding utf8)
    $hits = @(for ($n = 0; $n -lt $lines.Count; $n++) {
        if ($lines[$n].IndexOf($source[1],[StringComparison]::Ordinal) -ge 0) { $n + 1 }
    })
    if ($hits.Count -ne 1) { throw "Âncora deve ocorrer exatamente uma vez: $relative / $($source[1]); ocorrências: $($hits.Count)" }
    [pscustomobject]@{
        Pattern=$Pattern; Item=(++$order); Method=$source[2]; RelativePath=$relative
        Line=$hits[0]; Path=$path; Navigation=('{0}:{1}' -f $path,$hits[0])
    }
}
if ($ListOnly) { $entries; return } # Read-only: no editor discovery, process, GUI, files or DB writes.
$selected = $entries | Where-Object Item -eq $Item
if (-not $selected) { throw "Item inválido para ${Pattern}; use -ListOnly (1 a $($entries.Count))." }
$selected | Format-List Pattern,Item,Method,Navigation
$code = $null
if ($Editor -ne 'Notepad') { $code = Get-Command code -ErrorAction SilentlyContinue | Select-Object -First 1 }
if ($Editor -eq 'VSCode' -and -not $code) { throw 'VS Code CLI ausente. Use -Editor Notepad.' }
if ($code) {
    # Explicit user invocation opens one source tab at the requested line. It never runs Java/Maven.
    & $code.Source --reuse-window --goto $selected.Navigation
    if ($LASTEXITCODE -ne 0) { throw "VS Code retornou $LASTEXITCODE" }
} else {
    Write-Host "Notepad: use Ctrl+G, informe $($selected.Line) e pressione Enter. O arquivo pode ser editado pelo usuário; este script somente o abre."
    Start-Process -FilePath 'notepad.exe' -ArgumentList ('"' + $selected.Path + '"') -WindowStyle Normal
}
