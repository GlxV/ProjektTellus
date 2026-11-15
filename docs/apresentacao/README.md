# Abrir, estudar e apresentar

Arquivo principal: **[ProjektTellus-Apresentacao-r2.pptx](ProjektTellus-Apresentacao-r2.pptx)**, 15 slides editáveis. Texto Java e diagramas são objetos nativos; as notas contêm o roteiro completo por slide. Abra em modo Apresentação, sem depender de internet.

Alternativas: **[PDF](ProjektTellus-Apresentacao.pdf)**, 15 páginas; **[vídeo de capturas reais](ProjektTellus-Demo-Capturas.mp4)**, 24 segundos; PNGs `slide-01.png` a `slide-15.png`. O PDF incorpora imagens dos slides, com a aparência preservada, sem texto Java pesquisável. O vídeo tem legendas e não tem áudio; não é gravação contínua nem medição de FPS. As fontes e diagramas permanecem editáveis no PowerPoint.

## Ordem para preparar a defesa

1. Leia a [transcrição primária](../ENUNCIADO_ORIGINAL_TRANSCRITO.md) e a [matriz de conformidade](../CONFORMIDADE_TRABALHO.md).
2. Estude [ESTUDO_PADROES.md](../ESTUDO_PADROES.md), seguindo as chamadas e os papéis. Consulte o [Java completo](../CODIGO_JAVA_APRESENTACAO.md) se precisar de contexto.
3. Ensaie o [roteiro de fala](../ROTEIRO_APRESENTACAO.md), alvo ajustável de 10min40s. O enunciado não fixa duração.
4. Abra `Apresentar-ProjektTellus.cmd` na raiz e siga a [demo ensaiada](../DEMO_APRESENTACAO.md): construir → retrato manual → asteroide 25% → observar → restaurar.
5. Responda sem consultar as primeiras perguntas de [PERGUNTAS_APRESENTACAO.md](../PERGUNTAS_APRESENTACAO.md), depois as 26 perguntas originais nas questões 37–62.

Os quatro diagramas em [uml-apresentacao](../uml-apresentacao/) estão nos slides 4, 6, 8 e 10, em PNG 2732×1536 e fontes Mermaid complementares. O Factory do slide mostra uma família concreta e nomeia as demais; o Mermaid/estudo detalha os cinco pares. O Observer da UI é um objeto anônimo registrado pelo controlador; o controlador não implementa diretamente a interface. O Memento é legível e profundamente imutável.

Para apresentar, mantenha o PPTX/PDF abertos e o aplicativo com banco temporário. O lançador cria uma biblioteca nova a cada abertura; anote o caminho mostrado para uma reabertura no mesmo banco. **Java 25 é necessário para o aplicativo, não para o PDF/vídeo/PPTX.** Consulte o [ambiente](../AMBIENTE_APRESENTACAO.md). A validação local não substitui o computador físico da faculdade. Nome, curso/turma e declaração de autoria pessoal devem ser completados pelo estudante.

## Reproduzir a auditoria

Na raiz, com o JDK 25 local e Maven já preparados:

```powershell
. ./scripts/Tools.ps1
$tellusJava = Initialize-TellusTools
$tellusMaven = Find-TellusMaven
& $tellusMaven -B -ntp -Pgui-tests verify
if ($LASTEXITCODE -ne 0) { throw 'verify falhou' }
./scripts/Run-ExperimentalAudit.ps1 -RunName experimental-nova-rodada
```

Use nome novo; o runner recusa sobrescrever evidências. O diretório canônico é `docs/auditoria/experimental-20261115-final`. [Relatório](../AUDITORIA_EXPERIMENTAL.md) e CSV detalham 2.026 experimentos, 4.125 estados e o que os checks efetivamente verificam. IDs de retrato/instantes são variáveis; dados numéricos comparáveis repetiram hashes.

## Regenerar os materiais

A geração de PowerPoint usa **@oai/artifact-tool do runtime local do Codex**; não depende de uma conta em Canva/Google nem de slides publicados. No Codex, consulte `load_workspace_dependencies` para obter os caminhos atuais. O script recebe o diretório do runtime que contém `dependencies/` e a pasta da skill Presentations que contém `container_tools/`. Não escreva dentro dessas dependências.

O Python usado nesta máquina tem ReportLab, PyMuPDF e Pillow. Matplotlib 3.11.2 foi instalado de forma isolada em `.tools/audit-python`. Se for necessário preparar outra máquina de geração, instale as bibliotecas em um ambiente virtual local; não é necessário para apresentar os arquivos prontos.

```powershell
# Preparação opcional somente na máquina que vai GERAR materiais:
python -m pip install --target .tools/audit-python matplotlib==3.11.2 reportlab PyMuPDF Pillow
# Informe os caminhos reais retornados pelo Codex; não use literalmente os exemplos entre < >.
./scripts/Build-AcademicMaterials.ps1 -RuntimeRoot '<runtime>' -PresentationsSkill '<skill-presentations>' -DeckName 'ProjektTellus-Apresentacao-nova.pptx'
```

`Build-AcademicDeck.mjs` extrai Java diretamente das fontes, lê os resultados do CSV e insere notas do roteiro. `Finalize-AcademicDeck.mjs` verifica estrutura, fontes/geometria e reimportação do PPTX. O nome final precisa ser novo para preservar edições. `Export-AcademicPdf.py` monta o PDF a partir das renderizações: o exportador PDF direto do runtime não conseguiu resolver `#standard-fonts/Helvetica`, portanto essa dependência não foi modificada. Foi usada a skill de PDF com ReportLab e renderização por PyMuPDF, pois Poppler não estava disponível.

Para regenerar o vídeo, use `python scripts/Build-DemoVideo.py` após escolher um nome/arquivo novo ou arquivar a versão anterior. Requer FFmpeg/FFprobe no PATH; o script não sobrescreve o MP4 existente. Para nova validação completa, `python scripts/Validate-AcademicMaterials.py` verifica a entrega r2; ajuste a seleção do nome se estiver avaliando outra edição. Revise visualmente os slides após qualquer alteração.

## Evidências e limites

- [Maven final](../evidencias/verify-final.txt): 69 testes, zero falhas/erros/skips.
- [Validação do PowerPoint](../evidencias/ProjektTellus-Apresentacao-r2.validacao.json): estrutura/geometria/fontes/reimportação. Nenhuma alegação de abertura no Microsoft PowerPoint.
- [Validação dos materiais](../evidencias/materiais-validacao.json): fontes, pedidos experimentais, contagens, arquivos e recibos.
- [PDF](../evidencias/pdf-validacao.json) e [vídeo](../evidencias/video-validacao.json): metadados e renderização/decodificação.
- [Capturas](../capturas/latest.json): 33 PNGs, 249 verificações do harness, banco temporário, sem input global. A animação intermediária usa relógio controlado; o computador físico da faculdade e FPS não foram certificados.
