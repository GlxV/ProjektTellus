# ProjektTellus — apresentação acadêmica r4

**Aluno:** Matheus Luiz Bueno · **Turma:** 4P

**Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS

**Professor:** EDER MAGALHAES MACHADO · **Período:** 2º Bimestre de 2026

**Trabalho:** ProjektTellus — Simulador Planetário

## Arquivos para abrir

- **[PowerPoint r4](ProjektTellus-Defesa-Individual-r4.pptx)** — 15 slides editáveis, com conteúdo sobre o software e notas nos dois modos.
- **[PDF atualizado](ProjektTellus-Defesa-Individual-r4.pdf)** — 15 páginas para contingência; incorpora as renderizações e não oferece Java pesquisável.
- **[Guia compacto de defesa](../GUIA_DEFESA_INDIVIDUAL.md)** — o que dominar e qual classe abrir.
- **[Roteiro A e B](../ROTEIRO_APRESENTACAO.md)** — exposição completa e conversa individual.
- **[Código em três níveis](../DEFESA_CODIGO_JAVA.md)** — 20 segundos, um minuto e aprofundamento para cada padrão.
- **[Perguntas e arguição](../PERGUNTAS_APRESENTACAO.md)** — 62 perguntas anteriores e 21 itens de arguição, com respostas curta/aprofundada; vários temas se repetem.
- **[Demo e trilhas independentes](../DEMO_APRESENTACAO.md)** e **[atalhos para classes](../ATALHOS_DEFESA.md)**.
- **[Conformidade](../CONFORMIDADE_TRABALHO.md)** e **[mudanças dos 15 slides](../ALTERACOES_SLIDES_DEFESA.md)**.
- **[Revisão editorial r4](../REVISAO_EDITORIAL_R4.md)** — textos retirados, substituições e alcance da validação atual.

Abra `Apresentar-ProjektTellus.cmd` na raiz para ensaiar com Java 25 e banco temporário novo. Use `Abrir-Codigo-Defesa.cmd -Pattern FactoryMethod -Item 2` para abrir apenas o método solicitado no editor; sem parâmetros, o lançador abre o primeiro item de Builder. A opção `-ListOnly` de `scripts/Open-DefenseSources.ps1` apenas lista e valida caminhos/âncoras: não executa Java ou abre janelas.

## Como conversar com o professor

Comece: “Professor, posso fazer um panorama curto dos quatro padrões e abrir o código ou a demonstração conforme o senhor preferir.” Em cada padrão, explique o problema real, a solução e as classes, aponte o UML e siga uma chamada Java. Se houver pergunta, responda primeiro e retome no próximo padrão; não reinicie o discurso.

**Modo A:** todos os 15 slides, aproximadamente 10min40s, com margem até 12 minutos sem perguntas extensas. **Modo B:** aproximadamente 6min20s; sequência `1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15`. Os pares 4/5, 6/7, 8/9 e 10/11 continuam essenciais: nenhum padrão perde propósito, motivação, UML ou código. Os slides 3, 13 e 14 são opcionais na abertura curta e podem ser consultados depois. Os tempos são sugestões; o enunciado não fixa duração.

A referência à Prof. Célia foi preservada na [transcrição histórica](../ENUNCIADO_ORIGINAL_TRANSCRITO.md), pois o trabalho envolve projeto conjunto com outra disciplina. O professor desta defesa é EDER MAGALHAES MACHADO. Não foram inventados instituição ou matrícula. As notas editoriais antigas da transcrição registram o contexto da primeira entrega; os dados atuais estão neste índice e nos materiais r3.

## Classes e UML

| Padrão | Primeiro arquivo, sob src/main/java/com/projekttellus/ | Métodos | UML e Java |
|---|---|---|---|
| Builder | domain/TerraBuilder.java | atmosfera, build | slides 4–5; [UML](editorial-r4/UML-Builder.png) |
| Factory Method | events/EventoCreator.java → AsteroideCreator.java | criar; depois EventoBase.aplicar/transformar | slides 6–7; [UML](editorial-r4/UML-Factory-Method.png) |
| Observer | patterns/observer/ObserverBus.java | inscrever, publicar; contrato aoMudar | slides 8–9; [UML](editorial-r4/UML-Observer.png) |
| Memento | domain/Terra.java → TerraMemento.java | capturar, restaurar | slides 10–11; [UML](editorial-r4/UML-Memento.png) |

Em seguida abra `application/SimulacaoService.java` para ligar as responsabilidades. [ESTUDO_PADROES](../ESTUDO_PADROES.md) e [Java completo](../CODIGO_JAVA_APRESENTACAO.md) permanecem disponíveis. Builder é concreto/fluente, sem Director. Factory Method usa sobrescrita em subclasses reais. O bus é síncrono; o assinante da UI agenda Platform.runLater. O record Memento é público/legível e profundamente imutável; restaurar exige o mesmo UUID.

## Demonstração e contingência

O ciclo preservado é criar Terra → capturar retrato → Asteroide 25% → observar notificações → restaurar. Valores do ensaio anterior: H 100→64,73→100; temperatura 15→8→15 °C; população 8→6,5→8 bilhões. As trilhas do documento permitem demonstrar apenas um padrão após uma preparação comum.

Se JavaFX falhar, use PDF/PPTX, [capturas reais](../capturas/latest.json), Java/UML e o **[vídeo anterior preservado](ProjektTellus-Demo-Capturas.mp4)**. O vídeo tem 24 segundos, legendas e nenhum áudio: é uma sequência de capturas reais, não gravação contínua nem medição de FPS. Java 25 é necessário para executar o aplicativo; PDF, vídeo e slides não dependem dele. [Ambiente e lançamento](../AMBIENTE_APRESENTACAO.md).

## Versões anteriores preservadas

- [PPTX r2](ProjektTellus-Apresentacao-r2.pptx), [PDF anterior](ProjektTellus-Apresentacao.pdf) e previews `slide-01.png` a `slide-15.png` nesta pasta.
- [PPTX r3](ProjektTellus-Defesa-Individual-r3.pptx), [PDF r3](ProjektTellus-Defesa-Individual-r3.pdf) e seus previews em `defesa-r3/` permanecem idênticos.
- A edição atual usa previews separados em `editorial-r4/`; os diagramas anteriores e as fontes Mermaid em `../uml-apresentacao/` não foram apagados.
- A auditoria original, seus dados e recibos permanecem em [AUDITORIA_EXPERIMENTAL](../AUDITORIA_EXPERIMENTAL.md). Esta revisão não executou novos experimentos nem testes do aplicativo: as 69 aprovações e os 2.026 experimentos são evidências identificadas da base anterior.

## Regenerar a edição corrigida

A geração usa o runtime local do Codex com @oai/artifact-tool, a skill Presentations e Python com ReportLab, PyMuPDF e Pillow. Consulte `load_workspace_dependencies` no Codex para os caminhos reais; não altere as bibliotecas instaladas. A revisão atual não exige reinstalar Matplotlib ou repetir a auditoria.

```powershell
# Os textos entre < > representam caminhos reais retornados pelo Codex.
./scripts/Build-AcademicMaterials.ps1 -RuntimeRoot '<runtime>' -PresentationsSkill '<skill-presentations>' -PdfSkill '<skill-pdf>' -DeckName 'ProjektTellus-Defesa-Individual-r5.pptx' -PreviewDirectory 'docs/apresentacao/editorial-r5' -BuildDirectory 'target/editorial-r5-build'
```

O script recusa PowerPoint/PDF ou pasta de previews existentes. Seleciona medidas anteriores, extrai Java atual, gera previews/diagramas, define metadados acadêmicos e finaliza o PPTX. `Export-AcademicPdf.py` monta o PDF raster a partir dos mesmos PNGs, preservando a aparência. Para nova edição, escolha nomes/pastas novos e revise os 15 slides visualmente. O validador da edição atual é `scripts/Validate-EditorialRevision.py`; o validador anterior continua disponível para a r3.

O gerador também reimporta o PPTX final e renderiza seus 15 slides em `BuildDirectory/pptx-check`. Para repetir somente a validação da r4 entregue, configure `TELLUS_ARTIFACT_MODULE` com o caminho retornado pelo runtime, execute `scripts/Render-AcademicPptx.mjs` com o Node do runtime e depois `python scripts/Validate-EditorialRevision.py`. O segundo passo compara as imagens do arquivo final com os previews revisados e confere a preservação dos anteriores.

## Alcance da validação

O [relatório editorial r4](../REVISAO_EDITORIAL_R4.md) reúne as mudanças e as evidências atuais. O [registro r3](../VALIDACAO_DEFESA_INDIVIDUAL.md) preserva o resultado das 650 verificações da revisão anterior.

Renderização e inspeção em 1366×768, fidelidade de trechos/diagramas e integridade do pacote não equivalem a abrir o arquivo em Microsoft PowerPoint. O computador físico/projetor da faculdade e a fluidez continuam dependentes de ensaio local. A autoria histórica de cada linha do projeto não foi inferida; a compreensão precisa ser demonstrada pelo aluno ao seguir o código e reconhecer seus limites.
