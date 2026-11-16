# Demo ensaiável do ProjektTellus

**Aluno:** Matheus Luiz Bueno · **Professor:** EDER MAGALHAES MACHADO · **Turma:** 4P

**Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS

**Trabalho:** ProjektTellus — Simulador Planetário · **Período:** 2º Bimestre de 2026

Esta revisão prepara a conversa individual: escolha uma trilha abaixo quando o professor pedir um padrão e abra diretamente suas classes pelo [guia de atalhos](ATALHOS_DEFESA.md). A demo integrada criar → capturar manualmente → asteroide 25% → observar → restaurar permanece no roteiro seguinte. Os quatro padrões colaboram em etapas diferentes do fluxo; não executam todos simultaneamente em uma única operação.

**Proveniência:** as capturas, os recibos gráficos e os 69 testes são evidências anteriores, preservadas na entrega `8f7e58d`. Não houve novo teste do aplicativo nesta revisão documental. A conferência atual é leitura das fontes e validação de caminhos/âncoras do lançador em `-ListOnly`, sem abrir UI. Execução gráfica canônica anterior: **15/11/2026, 12:47**, [capturas/run-20261115-124710](capturas/run-20261115-124710). São 33 PNGs reais e 249 verificações do harness, com saída zero. [SUCCESS.txt](capturas/run-20261115-124710/SUCCESS.txt), [checks.tsv](capturas/run-20261115-124710/checks.tsv) e [execution.log](capturas/run-20261115-124710/execution.log) registram aquela execução. Essas verificações não são uma contagem de testes JUnit.

## Preparação única para escolher qualquer padrão

No ensaio manual do estudante, abra o lançador temporário descrito abaixo. Prepare quatro mundos independentes com os defaults do Builder: `Defesa · Builder`, `Defesa · Factory`, `Defesa · Observer` e `Defesa · Memento`. Em cada um dos três últimos, capture um retrato manual com rótulo `Base · Factory`, `Base · Observer` ou `Base · Memento`. Essa preparação acontece uma vez; não repita a demonstração completa a cada pergunta.

Antes de executar uma trilha, selecione o mundo correspondente em **Meus mundos → Carregar selecionado → Confirmar** e confira seu nome no observatório. A seleção simples na biblioteca não muda o mundo carregado. Não deixe edições pendentes no painel; se necessário, carregar novamente descarta-as mediante a confirmação exibida. Para repetir uma trilha, restaure sua base pelo rótulo; histórico e estatísticas continuarão acumulados. Para zerar também a sessão, use outro mundo/processo de ensaio, preservando os anteriores.

Os percursos abaixo foram conferidos contra o código atual, **não executados novamente**. Resultados numéricos já medidos são identificados como evidência anterior; efeitos esperados de novas entradas são descritos sem inventar novas medições.

## Trilha independente — Builder

**Entrada:** biblioteca temporária pronta; não precisa de evento nem retrato anterior. **Ação:** **＋ Criar mundo**, nomeie `Defesa · Builder 2`, confira os campos e use **Construir e salvar**. Na aba **Apresentação**, o atalho **Builder** abre o mesmo diálogo; não é uma criação fictícia. Se o pedido for apenas mostrar a montagem, abra o diálogo e cancele, sem produzir outro mundo.

**Saída esperada:** ao confirmar, novo UUID, mundo independente, ano 0, registro “Mundo criado”, retrato “Origem do mundo” e notificação da criação. Nos defaults, a evidência anterior registra humanos 100%, biológico 100% e civil 85,87782018658834%. **Cancelar** não devolve um builder para criar/persistir. Gases precisam somar 100%; o domínio valida os valores, e o formulário desabilita a confirmação inválida. Não misture o teste de entrada inválida com a demo ensaiada.

**Código a abrir:** [TerraBuilder.nome, linha 13; build, linha 20](../src/main/java/com/projekttellus/domain/TerraBuilder.java#L13), [BuilderDialog, conversor, linha 30](../src/main/java/com/projekttellus/ui/BuilderDialog.java#L30) e [SimulacaoService.criar, linha 32](../src/main/java/com/projekttellus/application/SimulacaoService.java#L32). Diga: “Eu reúno as condições neste builder. `return this` mantém a mesma configuração na cadeia; `build()` cria outra Terra. Não há Director ou builder abstrato nesta implementação.” Pare aqui se o professor pediu somente Builder.

## Trilha independente — Factory Method

**Entrada:** `Defesa · Factory` carregado, nas condições do retrato `Base · Factory`. Se já houve eventos, restaure essa base. **Ação:** role o painel direito até **Eventos planetários**, selecione **Impacto de asteroide**, ajuste a intensidade visível para **25%** e clique em **Aplicar evento**. Espere ou use **Pular animação**. Apenas selecionar o criador não aplica o evento. Não precisa repetir criação ou captura manual durante a pergunta.

**Saída esperada desde a referência:** humanos 64,7%, biológico 75,4%, civil 44,8% nos rótulos arredondados; população 6.500.000.000, temperatura 8 °C e ano 0 na evidência anterior. O serviço cria o produto, captura automaticamente “Antes: Impacto de asteroide”, aplica os efeitos num candidato, calcula indicadores, grava mundo/registro/retrato, atualiza a Terra e publica a mudança. A animação apresenta um resultado já confirmado. Para outro evento, restaure primeiro a mesma base; a intensidade no serviço é `0.25`, pois a UI divide 25 por 100.

**Código a abrir:** [EventoCreator.criar, linha 6](../src/main/java/com/projekttellus/events/EventoCreator.java#L6), [AsteroideCreator.criar, linha 5](../src/main/java/com/projekttellus/events/AsteroideCreator.java#L5) e [SimulacaoService.aplicar, linha 50](../src/main/java/com/projekttellus/application/SimulacaoService.java#L50). Diga: “A subclasse sobrescreve `criar` e faz `new Asteroide`. O produto chega ao serviço pelo tipo `EventoPlanetario`; `var` infere esse contrato. `evento.aplicar` usa a implementação herdada de `EventoBase`, que delega à transformação concreta.” Veja o produto no [atalho técnico](ATALHOS_DEFESA.md#factory-method--criação-e-polimorfismo).

## Trilha independente — Observer

**Entrada:** `Defesa · Observer` carregado; anote a contagem da sessão antes da ação. Carregar dados não publica uma mudança nova. **Ação:** no painel **Condições ambientais**, altere somente **Temperatura** de 15 para **30 °C** e use **Aplicar condições**. Role até a caixa **Observer**; abra **Modo apresentação** e avance até as etapas “Sistema notificado”. Não precisa aplicar asteroide, criar outro mundo ou capturar outro retrato durante essa pergunta.

**Saída esperada por leitura do código:** temperatura confirmada em 30 °C, indicadores recalculados, contagem daquele mundo incrementada em uma mudança e etapas dos três assinantes: monitor, estatísticas e globo/painel. Não há um novo número medido aqui para essa temperatura. Se repetir sem alterar os valores, a operação ainda pode notificar; não use isso como prova de que os indicadores mudaram. O monitor pode informar estabilidade: receber uma notificação não exige um alerta crítico. Para repetir a mudança 15 → 30, restaure `Base · Observer`; essa própria restauração também notifica e incrementa a sessão.

**Código a abrir:** [ObserverBus.publicar, linha 16](../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java#L16), [assinante JavaFX, MainController, linha 128](../src/main/java/com/projekttellus/ui/MainController.java#L128) e [renderObserved, linha 296](../src/main/java/com/projekttellus/ui/MainController.java#L296). Diga: “O bus chama `aoMudar` de cada assinante na mesma thread que publica; na operação da UI, é o worker. O assinante JavaFX recebe ali e agenda `Platform.runLater`. Seu callback retorna `void`; o bus registra a entrega, mas não espera que a thread FX desenhe os pixels.” Não há paralelismo criado pelo bus.

## Trilha independente — Memento

**Entrada:** `Defesa · Memento` carregado e retrato `Base · Memento` selecionável, com defaults e nome reconhecível. **Ação:** altere somente **Temperatura** para **30 °C → Aplicar condições**. Abra **Retratos salvos**, selecione **Base · Memento** pelo nome e use **Restaurar selecionado → Confirmar**. Não é necessário asteroide. Se a base ainda não existe, use **＋ Capturar retrato** antes da alteração; registre que isso é a preparação desta trilha.

**Saída esperada:** nome e condições da base voltam, incluindo 15 °C, população e ano preservados; surge “Antes da restauração” para permitir retorno ao estado que estava ativo. Restaurar valida o `mundoId`; não troca a identidade da Terra. Não selecione automaticamente o último retrato: ele pode ser um backup de outra ação. Na demo integrada específica abaixo, o manual é a segunda posição; fora dela, não prometa índices fixos.

**Código a abrir:** [Terra.capturar/restaurar, linhas 23 e 26](../src/main/java/com/projekttellus/domain/Terra.java#L23), [TerraMemento, linha 10](../src/main/java/com/projekttellus/patterns/memento/TerraMemento.java#L10) e [SimulacaoService.restaurar, linha 66](../src/main/java/com/projekttellus/application/SimulacaoService.java#L66). Diga: “O retrato guarda nome e um grafo imutável de condições. A restauração recupera esses valores, não apaga a história nem volta as estatísticas da sessão. Ela é mais uma operação registrada e notificada.” O record é público e legível; não reivindique opacidade estrita. Capturar manualmente cria registro/retrato sem notificar; **Salvar** grava mundo/histórico sem criar um novo retrato.

## Se houver interrupção ou falha

Antes de cada ação, diga o padrão, mostre a entrada e anuncie a saída esperada. Se o professor pedir código, pare a sequência e abra o item correspondente em [ATALHOS_DEFESA.md](ATALHOS_DEFESA.md). Retome do mundo/base daquela trilha; não é preciso reiniciar o ensaio completo. Evite executar falhas de banco ao vivo: explique o caminho real do erro pelas fontes em [Persistência e callbacks](ATALHOS_DEFESA.md#persistência-e-callbacks--resposta-para-falhas).

**Sem JavaFX:** use o [PPTX r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pptx) e o [PDF r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pdf). A edição anterior continua preservada em [PPTX r2](apresentacao/ProjektTellus-Apresentacao-r2.pptx) e [PDF anterior](apresentacao/ProjektTellus-Apresentacao.pdf). Vá diretamente aos pares dos padrões (4–5, 6–7, 8–9 ou 10–11); abra a fonte pelo atalho. Complete com os [quatro UML PNG](ATALHOS_DEFESA.md#slides-uml-e-contingência), capturas anteriores e o [vídeo preservado](apresentacao/ProjektTellus-Demo-Capturas.mp4), uma sequência de capturas reais. Explique o que foi registrado anteriormente; não finja uma execução ao vivo. Teste físico da faculdade continua pendente.

## Roteiro integrado preservado

Para ensaiar, abra **`Apresentar-ProjektTellus.cmd`**, na raiz do projeto. Ele chama `scripts/Start-AcademicDemo.ps1`, usa Java 25 e `target/ProjektTellus.jar` e cria um banco novo em `%TEMP%/ProjektTellus-apresentacao-<UUID>/demo.db` a cada abertura. O caminho aparece no console e em `launch.json`. O lançador foi inspecionado na entrega anterior; sua execução interativa física não foi feita aqui. A opção `-PrepareOnly` prepara os caminhos sem abrir a aplicação.

1. Clique em **＋ Criar mundo**. Dê o nome `Terra · demo acadêmica`. Confira os padrões do Builder: O₂ 21%, N₂ 78,96%, CO₂ 0,04%, He 0%, pressão **1 atm**, temperatura 15 °C, aerossóis/radiação 0, água 71%, gelo 3%, vegetação terrestre 65%, população 8.000.000.000; ano inicial 0. Clique em **Construir e salvar**. A vírgula em “0, 1 atm” separa dois campos; a pressão padrão é 1 atm.
2. Abra **Retratos salvos → ＋ Capturar retrato** e nomeie `Antes da catástrofe · demo`. Há agora dois retratos: origem e manual.
3. Volte à linha do tempo. Role o painel direito até **Eventos planetários**. Escolha **Impacto de asteroide**, intensidade **25%**, **Aplicar evento**. O resultado já está salvo quando a animação começa. Espere ou use **Pular animação**.
4. Mostre os indicadores e role até a caixa **Observer**. Diga que houve notificação real ao monitor, às estatísticas e ao globo/painel. Neste exemplo o alerta continua “Condições estáveis neste modelo didático.”; notificação e alerta crítico são fatos diferentes.
5. Clique em **Modo apresentação**. Use **Próximo →**, **← Anterior**, **▶ Reproduzir / Ⅱ Pausar**. As últimas três etapas identificam os assinantes que realmente foram notificados. Role essa aba para **Ver globo antes/depois**: a comparação muda a projeção visual, preservando o resultado salvo. Os atalhos Factory Method e Observer orientam a próxima ação; Memento abre os retratos; Builder abre a criação real.
6. Em **Retratos salvos**, selecione pelo nome **Antes da catástrofe · demo**, a segunda posição nesta sequência, e clique **Restaurar selecionado → Confirmar**. Nome, atmosfera, clima, água/gelo, vegetação, população e ano voltam exatamente. Um quarto retrato preserva o estado que existia antes da restauração.
7. Mostre **Indicadores**, incluindo a rolagem dos fatores e as três pontuações separadas. Para demonstrar SQLite no mesmo ensaio, use **Salvar** e **Carregar selecionado**. Para reabrir em outro processo, use explicitamente o mesmo caminho temporário informado pelo lançador; abrir o lançador de ensaio novamente cria outra biblioteca vazia. Consulte o comando no [ambiente](AMBIENTE_APRESENTACAO.md).

Valores efetivamente medidos, sem avanço de tempo entre as três etapas centrais:

| Medida | Antes | Asteroide 25% | Restaurado |
|---|---:|---:|---:|
| Habitabilidade humana (%) | 100 | 64.72968169602449 | 100 |
| Potencial biológico (%) | 100 | 75.4479635761296 | 100 |
| Sustentabilidade civil (%) | 85.87782018658834 | 44.765119622128324 | 85.87782018658834 |
| Suporte (pessoas) | 13740451230 | 7162419140 | 13740451230 |
| Temperatura (°C) | 15 | 8 | 15 |
| Aerossóis | 0 | 21.25 | 0 |
| Água / gelo (%) | 71 / 3 | 68 / 6 | 71 / 3 |
| Vegetação terrestre (%) | 65 | 51.1875 | 65 |
| População | 8000000000 | 6500000000 | 8000000000 |
| Ano | 0 | 0 | 0 |
| Histórico / retratos | 1 / 1 | 3 / 3 | 4 / 4 |
| Notificações da sessão | 1 | 2 | 3 |

O retrato manual é índice **1** (base zero), posição **2** na lista cronológica, UUID `fb18c274-a737-40d3-9519-f3215246d3c5` nesta execução. A origem é índice 0; o automático “Antes: Impacto de asteroide” é índice 2; o automático “Antes da restauração” é índice 3. UUIDs mudam em cada ensaio. O harness seleciona por rótulo e objeto, nunca pelo último item. Veja todos os índices reais em [snapshot-indices.csv](capturas/run-20261115-124710/snapshot-indices.csv).

Todos os valores do estado, inclusive gases, radiação, pressão e os doubles sem arredondamento editorial, estão em [measurements.csv](capturas/run-20261115-124710/measurements.csv). Cada PNG tem sua própria linha com valores completos e escopo em [visual-values.csv](capturas/run-20261115-124710/visual-values.csv); dimensões e SHA-256 estão em [images.tsv](capturas/run-20261115-124710/images.tsv). Em quadros de meio efeito, os números representam o resultado final já confirmado; o globo é uma interpolação visual, sem simulação de um estado físico intermediário.

Imagens finais para integração, todas relativas a `docs/capturas/`:

| Uso | Arquivo |
|---|---|
| Interface de referência, antes | `terra-referencia.png` |
| Interface restaurada, retrato manual selecionado | `demo-restaurado.png` |
| Globo ampliado para a capa, transparente | `planeta-referencia.png` |
| Globos de cenários adicionais | `planeta-oceano.png`, `planeta-co2.png`, `planeta-he.png`, `planeta-aerossol.png`, `planeta-congelado.png` |
| Builder real / captura manual / depois do impacto | `run-20261115-124710/01-builder-dialogo.png`, `03-snapshot-selecionavel.png`, `04-depois-asteroide.png` (os dois últimos na mesma pasta) |
| Apresentação: etapa Observer / controles | `run-20261115-124710/09-apresentacao-observer.png`, `10-apresentacao-controles.png` (mesma pasta) |
| Indicadores: início / fim / SQLite reaberto | `run-20261115-124710/06-indicadores-fatores.png`, `07-indicadores-fim.png`, `08-sqlite-reaberto-ui.png` (mesma pasta) |

As cópias para o deck têm valores em [deck-visual-values.csv](capturas/deck-visual-values.csv) e origem/hashes em [latest.json](capturas/latest.json). `planeta-*.png` é um snapshot real de outro **PlanetView** de 768×768, no desktop privado, alimentado com o mesmo estado confirmado do controller. É uma renderização isolada do componente, sem IA, sem redimensionamento posterior da imagem. Os PNGs preservam os contornos e eventuais serrilhados produzidos pelo renderer.

Os cinco eventos foram executados independentemente, sempre desde a referência, a 25%. Arquivos na pasta canônica:

| Evento / linha em measurements.csv | Quadro real intermediário | Resultado final (H / B / C em %, arredondados aqui) |
|---|---|---|
| Asteroide / `evento_0` | `evento-1-asteroid-meio.png` | 64.729682 / 75.447964 / 44.765120 |
| Supervulcão / `evento_1` | `evento-2-volcano-meio.png` | 55.969655 / 73.939118 / 33.577343 |
| Guerra nuclear / `evento_2` | `evento-3-nuclear-meio.png` | 54.371675 / 70.552847 / 34.448335 |
| Invasão alienígena / `evento_3` | `evento-4-alien-meio.png` | 86.975952 / 83.557777 / 64.803621 |
| Restauração ecológica / `evento_4` | `evento-5-ecology-meio.png` | 100 / 99.925056 / 87.547496 |

O harness pausa o `AnimationTimer` real e chama seu `handle` em 50% dos 3,8 s do plano, com a textura interpolada e a geometria real do efeito. [animation-frames.csv](capturas/run-20261115-124710/animation-frames.csv) registra 73 / 61 / 46 / 95 / 27 nós visíveis, incluindo grupos, respectivamente. Isso comprova quadros intermediários renderizados; **não mede cadência, FPS ou fluidez**. Um vídeo montado com essas imagens deve ser rotulado “sequência de capturas reais”, sem alegar gravação contínua.

O [vídeo final de contingência](apresentacao/ProjektTellus-Demo-Capturas.mp4) foi regenerado com as capturas canônicas `run-20261115-124710`: 24 segundos, 1366×768, H.264/24 fps, yuv420p, sem áudio. É uma sequência legendada de capturas reais; não é gravação contínua nem prova de FPS das animações. Metadados e decodificação completa verificados em [video-validacao.json](evidencias/video-validacao.json).

Depois da reabertura SQLite foram aplicados cinco cenários ambientais pelos controles reais. Para cada um há `cenario-<nome>-ui.png` e `planeta-<nome>.png`, associados às linhas `visual_<nome>` do CSV:

| Nome | Entradas que diferem da referência | Leitura visual honesta |
|---|---|---|
| oceano | água 100%, gelo 0% | superfície oceânica; suporte civil calculado zero, habitabilidade humana continua 100 neste modelo |
| co2 | O₂/N₂/CO₂/He = 21/69/10/0% | Terra de textura semelhante; CO₂ não pinta a atmosfera de vermelho; temperatura permanece 15 °C pois não houve avanço |
| he | gases = 21/28.96/.04/50% | troca de N₂ por He mantém O₂ parcial; o modelo conserva os indicadores de referência |
| aerossol | aerossóis 85 | véu/camada de dispersão visível; temperatura mantida em 15 °C |
| congelado | −40 °C, água 20%, gelo 70%, vegetação 5% | faixas glaciais amplas; habitabilidade humana e suporte civil zero |

A aba Apresentação foi capturada e os controles foram exercitados. O arquivo `12-uml-companheiro-harness.png` é um painel auxiliar JavaFX explicitamente rotulado, com relações resumidas dos quatro padrões; **não é uma funcionalidade UML dentro do aplicativo**. Os diagramas UML formais do deck são renderizados independentemente dessas capturas e correspondem às classes reais.

Limites: o modelo é educacional e ficcional, sem validação científica. O computador físico da faculdade, o projetor, DPI diferente de 100%, interação humana de mouse e fluidez contínua continuam **não testados nesta revisão**. Não houve nova execução de Maven, input global nem escrita nos saves pessoais.
