# Roteiro de apresentação — 15 slides

Notas completas para inserção automática no deck. Ordem fixa solicitada pelo usuário. Meta de ensaio: **10min40s**, com margem até 12 minutos; não é exigência docente. Os tempos incluem pausas curtas para apontar o código. Falar o texto entre aspas; “Conceito”, “Técnica”, “Demo” e “Transição” são orientação ao apresentador, sem necessidade de ler seus rótulos.

Fonte primária: [enunciado textual integral](ENUNCIADO_ORIGINAL_TRANSCRITO.md), duas páginas fornecidas pelo usuário. A [matriz de conformidade](CONFORMIDADE_TRABALHO.md) foi preparada antes do deck. Capas sem nome do aluno e sem curso inferido: identificação nominal permanece pendente. O enunciado pede no mínimo três padrões; são defendidos quatro. Trabalho individual e vínculo com a disciplina da Prof. Célia são requisitos declarados; a documentação não comprova autoria individual.

As referências originais são o [catálogo Refactoring Guru](https://refactoring.guru/pt-br/design-patterns/catalog) e o [guia DevMedia de padrões em Java](https://www.devmedia.com.br/guia/padroes-de-projeto-em-java/34456). Links específicos por padrão aparecem nas notas abaixo; não abrir a web durante a demo. [ESTUDO_PADROES.md](ESTUDO_PADROES.md) contém o código literal completo dos recortes e suas explicações; [snippets-deck.json](uml-apresentacao/snippets-deck.json) contém linhas e hashes.

### Preparação da demo e contingência

Ensaiar com banco temporário iniciado pelo procedimento da frente principal; não usar a biblioteca pessoal. Antes de apresentar, criar um mundo de controle e anotar seu nome/ano/condições. Deixar a aplicação aberta, com outro mundo selecionado, ou abrir na tela inicial se o ensaio demonstrar que cabe no tempo. O lançamento precisa usar a mesma propriedade real `projekttellus.db` ao reabrir. Modo de animação curto ou pular animação pode reduzir espera sem alterar o motor.

No slide 15, construir “Demo”, capturar um retrato, aplicar asteroide 25%, observar notificações, restaurar e conferir o controle. Reabertura só deve integrar a demo se o procedimento tiver sido ensaiado com o mesmo banco temporário; caso contrário, mostrar a captura/recibo de reabertura da frente gráfica e dizer que é evidência registrada. Nenhum passo abaixo constitui afirmação de que esta frente executou a GUI.

Se a GUI falhar, seguir com capturas reais verificadas, Java e UML locais do deck/PDF, dizendo explicitamente que a execução ao vivo não concluiu. Não simular cliques ou usar animação como prova da persistência. O computador da faculdade permanece sem validação por esta frente.

## Slide 1 — Título: ProjektTellus

**Tempo:** 0:00–0:20, 20 s. **Tela:** título, simulador planetário e quatro padrões. Sem nome de aluno inventado.

**Fala:** “Este é o ProjektTellus, um simulador planetário em Java. Vou explicar quatro padrões usados no funcionamento real: Builder para construir cenários, Factory Method para criar eventos, Observer para comunicar mudanças e Memento para recuperar estados. A ideia é mostrar o problema de cada escolha, as classes e o código que executa essa responsabilidade.”

**Conceito:** padrão é uma solução de projeto aplicada a um problema recorrente; um nome de pasta sozinho não demonstra sua implementação.

**Técnica:** título do trabalho e janela de 16 a 23/11/2026 vêm do enunciado; 70% trabalho e 30% prova podem ficar nas notas, sem consumir a fala. A utilização de IA é permitida com compreensão do código.

**Demo:** apenas apontar a imagem real já incorporada; reservar a operação ao vivo para o final.

**Transição:** “Primeiro, o problema de software que esses padrões ajudam a resolver.”

## Slide 2 — Problema e proposta

**Tempo:** 0:20–0:50, 30 s. **Tela:** condições iniciais, eventos, comunicação e recuperação; quatro itens exigidos por padrão.

**Fala:** “O usuário precisa montar uma Terra, mudar condições, aplicar eventos e comparar consequências. Isso exige construir um estado coerente, variar o comportamento dos eventos e atualizar subsistemas interessados. Também é preciso voltar a um estado anterior sem tentar calcular o inverso de uma catástrofe. A proposta reúne essas responsabilidades em uma aplicação desktop com mundos independentes e histórico local. O modelo é didático: seus resultados não são previsões ambientais reais.”

**Conceito:** motivação significa ligar problema real à solução, como pede o enunciado. Não apresentar padrões apenas como requisito numérico.

**Técnica:** JavaFX na interface; SQLite para estado, histórico e retratos; Java no domínio e casos de uso. Os quatro escolhidos pertencem à lista dos dez autorizados.

**Demo:** apontar onde criar, aplicar e restaurar aparecem na captura; não mudar parâmetros agora.

**Transição:** “Para entender onde cada responsabilidade ficou, esta é a arquitetura.”

## Slide 3 — Arquitetura geral

**Tempo:** 0:50–1:25, 35 s. **Tela:** camadas e dependências reais. Fonte: `uml-apresentacao/03-arquitetura.mmd`.

**Fala:** “MainController coordena a tela e envia os casos de uso para SimulacaoService. Terra mantém identidade, nome e uma referência ao estado atual. Os componentes do estado são valores imutáveis. ModeloPlanetario calcula os indicadores e a evolução. O serviço depende do contrato MundoRepository, implementado com SQLite, e publica mudanças no ObserverBus. As operações e acessos ao banco são serializados num worker; a atualização visual é agendada na thread JavaFX.”

**Conceito:** separação de responsabilidades e dependência de contratos. O domínio não conhece telas ou SQL; Terra conhece TerraMemento para capturar/restaurar.

**Técnica:** não desenhar dependência inversa de `EstadoPlanetario` para `MainController`. O serviço cria monitor e estatísticas; o Controller registra o assinante gráfico. Diagrama de arquitetura não precisa mostrar todas as classes da UI.

**Demo:** indicar o mesmo aplicativo para Sandbox e Apresentação: a trilha visual usa resultados já executados, sem motor paralelo.

**Transição:** “O primeiro caso de uso é reunir as condições iniciais: aqui entra Builder.”

## Slide 4 — Builder: propósito, motivação e UML

**Tempo:** 1:25–2:00, 35 s. **Tela:** problema/solução e UML `04-builder.mmd`.

**Fala:** “Uma Terra reúne atmosfera, clima, água e gelo, vegetação, população e tempo. Configurar tudo por parâmetros posicionais na tela seria difícil de ler. TerraBuilder oferece etapas nomeadas e valores padrão; no fim, build reúne o estado e cria a Terra. Esta é uma implementação concreta e fluente. Não há Director nem interface abstrata de Builder. O diálogo coleta os dados, mas não substitui o Builder e não é o responsável por gravar o mundo.”

**Conceito:** construção em etapas e configuração separada do produto. A fluência vem de retornar o mesmo Builder.

**Técnica:** Terra referencia EstadoPlanetario. Evitar diamante de composição exclusiva: estados imutáveis também podem ser referenciados por snapshots. `build()` cria tanto EstadoPlanetario quanto Terra; os records validam seus limites.

**Demo:** apontar os campos do diálogo na captura. Não afirmar que toda validação acontece apenas ao clicar no botão: os records já validam quando são instanciados.

**Transição:** “No Java, essa construção aparece em duas partes pequenas.”

**Referência:** [Builder](https://refactoring.guru/design-patterns/builder).

## Slide 5 — Builder: código funcional e construção

**Tempo:** 2:00–2:45, 45 s. **Tela:** `TerraBuilder.atmosfera` e `build`; manifesto B-fluente-deck e B-build-deck.

**Fala:** “Neste método, atmosfera recebe o componente e return this devolve o próprio Builder, permitindo encadear as próximas escolhas. O build cria um UUID novo, reúne os componentes num EstadoPlanetario e constrói Terra com nome e estado. As regras de gases, água e demais limites ficam nos componentes; o estado rejeita nulos e tempo inválido, e Terra valida o nome. Dois builds geram identidades diferentes. Eles podem compartilhar referências de valores imutáveis, porque uma mudança substitui o estado em vez de editar o valor anterior.”

**Conceito:** identidade do produto versus configuração de valores; imutabilidade dos componentes não torna o Builder imutável.

**Técnica:** arquivos `domain/TerraBuilder.java`, `ui/BuilderDialog.java` e `application/SimulacaoService.java`. O método `criar` chama build, captura origem e persiste. UUID aleatório não torna a evolução numérica aleatória. O Builder não possui reset automático nem promessa de segurança concorrente.

**Demo:** no final, usar “Construir e salvar” para criar uma Terra com nome Demo. Se questionado, mostrar no estudo o conversor real do diálogo e o teste escrito de independência; não alegar execução de teste por esta frente.

**Transição:** “Depois de construir o mundo, precisamos escolher qual evento será criado.”

## Slide 6 — Factory Method: propósito, motivação e UML

**Tempo:** 2:45–3:20, 35 s. **Tela:** UML `06-factory-method.mmd` com os cinco pares.

**Fala:** “O serviço aplica eventos diferentes usando um contrato comum. EventoCreator declara criar, e cada subclasse decide qual produto concreto instanciar. Os cinco pares são Asteroide, Supervulcao, GuerraNuclear, InvasaoAlienigena e RestauracaoEcologica, cada um com seu Creator. Os produtos herdam a base comum e cumprem EventoPlanetario. A escolha do tipo fica nos criadores; o serviço recebe o contrato. Adicionar um sexto evento também exigiria registrá-lo no catálogo da interface.”

**Conceito:** criação polimórfica por sobrescrita. Não confundir com um método estático que decide todos os produtos por switch.

**Técnica:** triângulo contínuo: Creator concreto herda EventoCreator; produto herda EventoBase. Triângulo tracejado: EventoBase implementa EventoPlanetario. Seta de criação: cada Creator aponta para seu produto correspondente. Nome/intensidade podem ser omitidos no desenho, sem inventar classes genéricas.

**Demo:** indicar os cinco itens reais do seletor de eventos na captura. Dizer que a invasão é ficcional.

**Transição:** “O trecho mostra onde a escolha concreta acontece e como o serviço usa o resultado.”

**Referência:** [Factory Method](https://refactoring.guru/design-patterns/factory-method).

## Slide 7 — Factory Method: criar e aplicar

**Tempo:** 3:20–4:05, 45 s. **Tela:** `AsteroideCreator.criar`, `creator.criar` e `evento.aplicar`, todos extraídos literalmente.

**Fala:** “AsteroideCreator sobrescreve criar e retorna new Asteroide com a intensidade recebida. O new é esperado: está no criador responsável pela decisão. No serviço, creator tem o tipo do contrato e criar executa a sobrescrita escolhida. Em seguida, evento.aplicar usa o produto pelo contrato e produz um novo estado. A Terra candidata mantém a identidade do mundo atual. Na interface, 25% vira 0,25 no domínio. Intensidade zero mantém as condições; uma intensidade fora de zero a um é rejeitada.”

**Conceito:** polimorfismo tanto na criação quanto na aplicação. Builder configura o objeto composto; Factory Method seleciona o produto da família de eventos.

**Técnica:** `EventoBase.aplicar` é final; valida entrada e chama transformar quando a intensidade não é zero. No método aplicar do serviço, criar evento vem **antes** de capturar o estado anterior. O produto apenas calcula os efeitos; o serviço confirma a operação.

**Demo:** reservar Asteroide 25% para o slide 15. Não aplicar outro evento aqui e consumir o estado de referência antes da captura planejada.

**Transição:** “O mundo mudou. Como os interessados descobrem isso sem entrar no código de Terra?”

## Slide 8 — Observer: propósito, motivação e UML

**Tempo:** 4:05–4:40, 35 s. **Tela:** UML `08-observer.mmd`.

**Fala:** “Alertas, estatísticas e interface reagem à mesma mudança, com responsabilidades diferentes. ObserverBus mantém assinantes do contrato SimulacaoObserver. Depois de persistir e atualizar a Terra, o serviço publica MudancaPlanetaria. O monitor gera alertas e as estatísticas registram contagem e extremos por mundo. MainController cria e inscreve um terceiro observador anônimo para a UI; o Controller não implementa diretamente a interface. Assim, o planeta não precisa conhecer a implementação de cada interessado.”

**Conceito:** assinatura e notificação; o Subject é o bus separado da entidade Terra.

**Técnica:** dois observadores no serviço sem GUI; três com assinante gráfico. Inscrever devolve AutoCloseable para remover a inscrição. Registro e entrega não são persistência de mensagens.

**Demo:** apontar mensagem Observer, alertas e estatísticas na captura. Não igualar qualquer atualização visual a uma notificação: carregar mundo também renderiza, sem publicar a mesma mudança no serviço.

**Transição:** “A comunicação ocorre nesta chamada do laço de publicação.”

**Referência:** [Observer](https://refactoring.guru/design-patterns/observer).

## Slide 9 — Observer: entrega e reação

**Tempo:** 4:40–5:25, 45 s. **Tela:** recorte de `ObserverBus.publicar`; explicar que o slide mostra parte do método.

**Fala:** “O bus percorre os assinantes e chama aoMudar em cada implementação. Depois do retorno normal, o nome é registrado como notificado. O método completo captura RuntimeException por assinante, registra falhas e continua; uma falha auxiliar não desfaz o mundo já gravado. A entrega do bus é síncrona. O assinante da interface apenas agenda o trabalho com Platform.runLater. Portanto, o recibo confirma o callback, mas não prova que os pixels já apareceram. Um assinante lento ainda pode retardar a operação.”

**Conceito:** desacoplamento por interface, sem pressupor paralelismo nem entrega garantida.

**Técnica:** monitor e estatísticas guardam projeções de sessão por UUID; não são recarregadas como histórico de estatísticas ao reiniciar. CopyOnWriteArrayList facilita iteração/inscrição. Não prometer captura de qualquer Error. O recorte do deck termina antes do catch; o bloco completo está no estudo.

**Demo:** após aplicar no final, observar os nomes efetivamente notificados e as mensagens. `salvar` e captura manual não geram notificação ambiental no serviço.

**Transição:** “Comunicar a mudança é uma parte. Recuperar exatamente o estado anterior é outra.”

## Slide 10 — Memento: propósito, motivação e UML

**Tempo:** 5:25–6:00, 35 s. **Tela:** UML `10-memento.mmd`; nota de variante legível.

**Fala:** “Uma catástrofe não tem uma operação inversa simples capaz de recuperar todos os valores perdidos. Terra atua como originador, TerraMemento guarda o retrato e SimulacaoService coordena seu uso como caretaker. O retrato conserva nome, estado completo, identidade do mundo e metadados. Aqui ele é um record público: o estado pode ser lido. A proteção vem do grafo imutável, que impede alterações compartilhadas. Esta variante não oferece a opacidade estrita do Memento clássico.”

**Conceito:** capturar e restaurar valores anteriores; distinguir imutabilidade, leitura e ocultação de representação.

**Técnica:** UUID do retrato não é UUID do mundo. EstadoPlanetario e componentes são records de valores sem coleções mutáveis. A associação permite reter o mesmo estado antigo com segurança; não se exige cópia profunda. O repositório acrescenta armazenamento durável.

**Demo:** apontar retrato selecionável; não chamar o botão Salvar de Memento. SQLite por si só não é o padrão.

**Transição:** “Estas são as linhas que capturam e validam a restauração.”

**Referência:** [Memento](https://refactoring.guru/design-patterns/memento).

## Slide 11 — Memento: captura e restauração

**Tempo:** 6:00–6:45, 45 s. **Tela:** `Terra.capturar` e trecho interno de `restaurar`; IDs M-capturar/M-restaurar no manifesto.

**Fala:** “Capturar cria um id de retrato e guarda o id do mundo, rótulo, nome, instante e estado atual. Isso já funciona em memória. Restaurar rejeita um retrato cujo mundoId não corresponde à Terra. Depois reassocia nome e estado, incluindo o tempo e todos os componentes. O id da Terra permanece o mesmo. No serviço, essa validação ocorre numa candidata antes de gravar. O estado que será substituído também é capturado, permitindo voltar a ele depois.”

**Conceito:** identidade do originador e restauração exata de um valor, sem recalcular inversamente efeitos.

**Técnica:** o método completo começa com `Objects.requireNonNull(memento)` antes do if mostrado. Restaurar não apaga histórico, não volta câmera/animação e não restaura estatísticas de sessão. Uma cópia tem UUID novo; rejeita retrato do mundo original mesmo se começar com valores iguais.

**Demo:** no slide final, comparar nome, ano e condições com o retrato inicial; mostrar o mundo de controle sem alterações. Para comprovar persistência é preciso recarregar/reabrir o banco, além de ver a tela restaurada.

**Transição:** “Os quatro padrões se encontram nos casos de uso coordenados pelo serviço.”

## Slide 12 — Integração e ordem da confirmação

**Tempo:** 6:45–7:30, 45 s. **Tela:** sequência `12-integracao.mmd` ou fluxo editável equivalente.

**Fala:** “O Builder já produziu a Terra em um caso de uso anterior. Ao aplicar um evento, o serviço primeiro cria o produto, captura o estado anterior e calcula uma candidata. Calcula também os indicadores. O repositório grava candidato, histórico e retrato numa transação SQLite. Após retorno normal, o serviço atualiza a Terra e publica a mudança. A interface apresenta os passos e animações depois. Restaurar é outro caso de uso, que também persiste antes de notificar.”

**Conceito:** orquestração e fronteira transacional; integração não significa que todos os padrões executem a cada clique.

**Técnica:** a ordem cronológica em aplicar é **Factory Method → Memento → aplicar/calcular → SQLite → atualizar Terra → Observer**. Se a gravação lança exceção antes do retorno, o serviço não chega à atualização/publicação dessa mudança. Não prometer atomicidade distribuída entre banco, memória e FX.

**Demo:** percorrer os passos reais de uma operação concluída no modo Apresentação. Não confundir reprodução visual com nova aplicação do evento.

**Distinção para a defesa:** o retrato manual da demo é capturado antes do clique de aplicar. Dentro de aplicar, o serviço cria o evento antes de capturar outro retrato automático. O slide 12 mostra essa ordem interna; restauração é uma operação posterior.

**Transição:** “Os padrões organizam o software. Os efeitos numéricos vêm deste modelo.”

## Slide 13 — Algoritmo e indicadores

**Tempo:** 7:30–8:20, 50 s. **Tela:** H, B, C/K, pressão parcial e passos temporais.

**Fala:** “O modelo separa três perguntas: adequação à sobrevivência humana, potencial biológico e sustentabilidade da civilização. Os indicadores são produtos de fatores, não probabilidades. Pressão parcial é pressão total vezes fração do gás. O suporte estimado considera continente, água, vegetação e condições ambientais; um oceano pode ter ambiente favorável e suporte civilizacional zero. O avanço divide o intervalo em passos de até um ano, recalculando clima, gelo, vegetação e população. A mesma entrada e o mesmo intervalo produzem a mesma saída.”

**Conceito:** determinismo e separação entre indicadores; estabilidade numérica não demonstra validade científica.

**Técnica:** `ModeloPlanetario.calcular`, `avancar` e `evoluir`; n=ceil(anos), dt=anos/n. Água+gelo conserva uma reserva de área superficial, não massa em kg. Coeficientes e escalas são didáticos. Fracionar uma chamada em várias pode produzir diferenças por integração/arredondamento. População inteira pode reter resíduos pequenos.

**Demo:** indicar os fatores explicativos e o ano; avanço é opcional fora dos 95 segundos finais, se o ensaio permitir. Evento isolado não avança o tempo.

**Transição:** “Os experimentos ajudam a enxergar essas escolhas e também seus limites.”

## Slide 14 — Experimentos e limites observados

**Tempo:** 8:20–9:05, 45 s. **Tela:** três achados do arquivo `docs/auditoria/slide-findings.json`, conferidos pela integração contra os CSVs da execução final.

**Fala:** “Os experimentos da frente de auditoria ilustram três pontos. Substituir parte do nitrogênio por hélio preservou os indicadores quando as pressões relevantes foram mantidas. Um cenário de oceano total teve adequação ambiental alta, mas suporte e sustentabilidade civilizacional zero, pela falta de continente. No cenário sem oxigênio, o arredondamento populacional deixou um pequeno resíduo ao longo da evolução. Esse último é um limite numérico observado, não uma previsão de sobrevivência humana. Os recibos distinguem checagens do programa de validação científica.”

**Conceito:** comparar entradas controladas, ler saídas e reconhecer limitações em vez de chamar todos os resultados de realismo.

**Técnica:** execução externa à frente acadêmica: `docs/auditoria/experimental-20261115-final/execution.json` registra ExitCode 0; `summary.json` registra 2.026 experimentos e 252.628 checagens, com zero falhas. Esses totais são categorias de auditoria, não número de testes JUnit. `snapshots.csv` contém entradas/saídas por experimento. Os números do slide devem corresponder às linhas selecionadas pela integração, inclusive horizonte e demais condições.

**Demo:** mostrar gráficos/capturas já produzidos; não repetir a varredura ao vivo. Esta frente leu os recibos; não executou a auditoria. Caso os arquivos mudem, conferir novamente antes de gerar o deck.

**Ajuste para integração:** o gerador lido cita `docs/auditoria/experimentos.csv`, que não estava presente. A fonte disponível é `docs/auditoria/experimental-20261115-final/snapshots.csv`, além de summary/execution. “O arredondamento impede a extinção completa” deve ser qualificado como achado **deste cenário**; não generalizar para qualquer entrada.

**Transição:** “Agora vou reunir as responsabilidades no fluxo do aplicativo.”

## Slide 15 — Demonstração e conclusão

**Tempo:** 9:05–10:40, 95 s. **Tela:** aplicação real e captura de referência no deck. Tempo extra até 12 minutos é margem de ensaio, não regra.

**Fala de condução:** “Vou construir a Terra Demo. O Builder reúne as condições e o mundo recebe sua identidade. Agora preservo o retrato e aplico asteroide a 25%. O Creator produz o evento, o estado muda e os assinantes recebem a mudança confirmada. Seleciono o retrato e restauro: nome, ano e condições voltam aos valores guardados. O mundo de controle continua independente. A recuperação após reabertura usa o mesmo banco local temporário.”

**Conceito:** prova observável de um fluxo com quatro responsabilidades; o estudante precisa explicar quem cria, quem aplica, quem notifica e quem restaura.

**Técnica:** usar o worker, os casos de uso e as listas reais, com o mesmo banco temporário antes/depois.

**Demo — cliques planejados:**

1. 0–12 s: abrir criação, nome Demo, “Construir e salvar”; mostrar Terra criada. Condições padrão evitam gastar tempo digitando muitos números.
2. 12–22 s: preservar retrato “Antes do impacto”; conferir que ele aparece na lista.
3. 22–37 s: selecionar Asteroide e 25%, aplicar; pular animação se necessário. A UI converte 25 para 0,25.
4. 37–45 s: apontar indicadores, alertas e nomes de assinantes na trilha; mostrar o resultado da operação concluída.
5. 45–58 s: selecionar “Antes do impacto”, restaurar e confirmar. Conferir nome/ano/condições; não se basear apenas na cor do globo.
6. 58–70 s: carregar o mundo de controle preparado antes; conferir seus valores e voltar a Demo se couber.
7. 70–88 s: se ensaiado, fechar e reabrir pelo mesmo lançamento de banco temporário; carregar Demo e mostrar retratos/histórico. Se a abertura consumir mais tempo, apresentar a captura/recibo real de reabertura e identificá-lo como registro, sem alegar execução ao vivo.
8. 88–95 s: concluir com as responsabilidades e abrir para perguntas.

**Fala de conclusão:** “Os quatro padrões têm responsabilidades demonstráveis no Java: construir, criar eventos, comunicar e restaurar. Os limites do modelo e da evidência estão documentados.”

**Limites da demonstração:** restauração visual não basta para provar durabilidade; comparação com segundo mundo não prova todas as formas de concorrência; os registros de GUI precisam identificar o banco usado. A fala sobre a recuperação deve acompanhar o que efetivamente foi executado ou o registro mostrado.

**Transição para perguntas:** “Posso detalhar as classes, os contratos ou o motivo de cada escolha.” Usar [PERGUNTAS_APRESENTACAO.md](PERGUNTAS_APRESENTACAO.md) e os recortes completos do estudo para a defesa. Não inventar nome do aluno, curso, autoria prévia do código ou uso histórico de ferramentas.
