# Roteiro — apresentação completa e defesa individual

- **Aluno:** Matheus Luiz Bueno
- **Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS
- **Professor:** EDER MAGALHAES MACHADO
- **Turma:** 4P
- **Trabalho:** ProjektTellus — Simulador Planetário
- **Período:** 2º Bimestre de 2026

### Uso do roteiro

As falas A/B podem ser adaptadas às próprias palavras. Os campos de apoio técnico e de demonstração ficam como consulta, fora da fala principal. Em uma interrupção, responder ao ponto e retomar o próximo padrão.

**Modo A — completo:** 15 slides, faixa de 10–12 minutos, alvo de 10min40s. **Modo B — individual:** faixa de 5–7 minutos, alvo de 6min20s; sequência **1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15**. Slides 3, 13 e 14 ficam como contexto opcional. Os pares 4/5, 6/7, 8/9 e 10/11 preservam a explicação de cada padrão, seu problema real, UML e Java. Perguntas extensas ficam fora do orçamento.

Aprofundamento em [DEFESA_CODIGO_JAVA.md](DEFESA_CODIGO_JAVA.md); consulta rápida em [GUIA_DEFESA_INDIVIDUAL.md](GUIA_DEFESA_INDIVIDUAL.md); arguição em [PERGUNTAS_APRESENTACAO.md](PERGUNTAS_APRESENTACAO.md). A [transcrição histórica](ENUNCIADO_ORIGINAL_TRANSCRITO.md) mantém a referência à Prof. Célia pelo projeto conjunto; esta apresentação é para EDER MAGALHAES MACHADO.

### Preparação e contingência

Deixar as classes localizáveis no editor e seguir [DEMO_APRESENTACAO.md](DEMO_APRESENTACAO.md) para o banco temporário. O aplicativo usa a propriedade `projekttellus.db`; a reabertura precisa usar o mesmo arquivo. O fluxo básico é construir → capturar manualmente → asteroide 25% → observar → restaurar.

Se o aplicativo não abrir, identificar a falha e continuar com as capturas anteriores, UML e fonte local. A evidência anterior deve ser apresentada como registro. Esta revisão é documental, sem nova execução de Maven ou GUI; abertura no PowerPoint desktop e computador da faculdade não foram validados por esta revisão.

### Orçamento de tempo

| Slide | Modo A (s) | Modo B (s) | Função |
|---|---:|---:|---|
| 1 | 20 | 10 | abertura |
| 2 | 30 | 15 | abertura |
| 3 | 35 | opcional | contexto |
| 4 | 35 | 25 | UML/Java essencial |
| 5 | 45 | 30 | UML/Java essencial |
| 6 | 35 | 25 | UML/Java essencial |
| 7 | 45 | 35 | UML/Java essencial |
| 8 | 35 | 25 | UML/Java essencial |
| 9 | 45 | 35 | UML/Java essencial |
| 10 | 35 | 25 | UML/Java essencial |
| 11 | 45 | 35 | UML/Java essencial |
| 12 | 45 | 30 | integração/demo |
| 13 | 50 | opcional | contexto |
| 14 | 45 | opcional | contexto |
| 15 | 95 | 90 | integração/demo |

Total planejado: A=640 s (10min40s), B=380 s (6min20s). Perguntas e exploração adicional do código não entram nessa soma.

## Slide 1 — Título: ProjektTellus — Simulador Planetário

**Modo A:** 0:00–0:20 (20 s). **Modo B:** 0:00–0:10 (10 s).

**Fala A:** “Professor, sou Matheus Luiz Bueno, da turma 4P. Este é o ProjektTellus, um simulador planetário em Java. Nele, eu monto um mundo, aplico eventos, acompanho as mudanças e recupero estados anteriores. Essas quatro partes usam Builder, Factory Method, Observer e Memento.”

**Fala B:** “Sou Matheus Luiz Bueno, da 4P. O ProjektTellus permite construir mundos, aplicar eventos, acompanhar mudanças e restaurar estados.”

**Conceito e técnica:** Identificação: ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS; professor EDER MAGALHAES MACHADO; trabalho ProjektTellus — Simulador Planetário; 2º Bimestre de 2026.

**Demo / abrir código:** Aplicativo preparado no banco temporário; apresentação inicial junto à imagem do planeta.

**Transição:** “O fluxo começa nas condições que eu escolho para o mundo.”

## Slide 2 — Mundos, eventos e histórico

**Modo A:** 0:20–0:50 (30 s). **Modo B:** 0:10–0:25 (15 s).

**Fala A:** “Eu escolho as condições do ambiente e a população para construir um mundo. Depois posso aplicar um dos cinco eventos e acompanhar o que mudou no globo, nos indicadores, nos alertas e nas estatísticas. Os retratos guardam estados que posso recuperar depois. A interface usa JavaFX, e o SQLite mantém os mundos, o histórico e os retratos entre as sessões.”

**Fala B:** “Eu construo o mundo com suas condições e população, aplico eventos, acompanho as alterações e recupero retratos. JavaFX reúne os controles; SQLite guarda os dados.”

**Conceito e técnica:** Quatro responsabilidades: TerraBuilder monta o mundo; cinco famílias de eventos produzem transformações; ObserverBus distribui mudanças à interface, alertas e estatísticas; Terra/TerraMemento permitem recuperação. Detalhes dos critérios acadêmicos permanecem na matriz, fora desta fala.

**Demo / abrir código:** Modo B: seguir ao slide 4 depois desta introdução. Os controles da aplicação mostram as mesmas quatro responsabilidades.

**Transição:** “Essas ações ficam divididas entre a interface e as classes que executam a simulação.”

## Slide 3 — Arquitetura geral

**Modo A:** 0:50–1:25 (35 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “Na interface, MainController recebe as ações do usuário e chama SimulacaoService. O serviço coordena a operação, usa o domínio para representar o mundo e chama ModeloPlanetario para os cálculos. A gravação passa por MundoRepository, com implementação SQLite. Quando a mudança está confirmada, ObserverBus a distribui aos interessados. As operações usam um worker, enquanto os componentes visuais são atualizados na thread JavaFX.”

**Fala B:** “MainController recebe a ação, SimulacaoService coordena, o domínio representa o mundo e o repositório grava. ObserverBus distribui a mudança confirmada.”

**Conceito e técnica:** Consulta: application/SimulacaoService.java; ui/MainController.java. O domínio não depende de JavaFX/SQL; Terra conhece TerraMemento. Na UI, operações e acessos ao repositório são serializados pelo executor de uma thread.

**Demo / abrir código:** Contexto opcional no modo B. Localizar o serviço no diagrama e seguir suas dependências; sem nova operação na aplicação.

**Transição:** “A primeira dessas operações é construir uma Terra.”

## Slide 4 — Builder: propósito, problema e UML

**Modo A:** 1:25–2:00 (35 s). **Modo B:** 0:25–0:50 (25 s).

**Fala A:** “Montar uma Terra envolve nome, atmosfera, clima, água, vegetação e população. Eu uso TerraBuilder para escolher esses componentes em etapas, partindo de valores iniciais já definidos. O diálogo reúne as escolhas e configura o Builder. Quando chamo build, ele cria o estado planetário e a Terra. Assim, a tela não precisa repetir toda a montagem do cenário a cada criação.”

**Fala B:** “TerraBuilder reúne as condições iniciais em etapas. O diálogo configura os componentes, e build cria o estado e a Terra. Isso concentra a montagem do cenário em um lugar.”

**Conceito e técnica:** Builder concreto e fluente; sem Director ou interface abstrata de Builder. Esse detalhe pertence ao estudo complementar. UML: BuilderDialog depende de TerraBuilder; build cria EstadoPlanetario e Terra. Os valores imutáveis podem ser compartilhados por referência, sem posse exclusiva do estado.

**Demo / abrir código:** Entrada independente: abrir criação de mundo. Java principal: domain/TerraBuilder.java; cliente: ui/BuilderDialog.java, setResultConverter.

**Transição:** “O encadeamento das escolhas aparece nestes métodos.”

## Slide 5 — Builder: Java e construção

**Modo A:** 2:00–2:45 (45 s). **Modo B:** 0:50–1:20 (30 s).

**Fala A:** “O método atmosfera recebe o componente e o guarda no Builder. return this devolve o mesmo Builder para eu continuar configurando o mundo. Já build reúne os componentes num EstadoPlanetario e cria uma Terra com UUID novo. Os componentes conferem seus limites, o estado verifica as referências e o tempo, e Terra valida o nome. Ao final, o serviço recebe o mundo construído para gravá-lo.”

**Fala B:** “atmosfera guarda o componente e retorna o próprio Builder. Isso permite encadear as escolhas. build cria EstadoPlanetario e uma Terra com identidade nova; a validação dos valores fica no domínio.”

**Conceito e técnica:** Consulta: domain/TerraBuilder.java, atmosfera/build; domain/EstadoPlanetario.java; ui/BuilderDialog.java, setResultConverter; application/SimulacaoService.java, criar. Os setters não repetem toda a validação. Builder é mutável, pode ser reutilizado e não tem reset automático. UUID identifica o mundo; não participa da evolução numérica.

**Demo / abrir código:** Mostrar a criação no aplicativo ao final ou usar o caminho independente de Builder. build devolve Terra; o diálogo devolve TerraBuilder configurado.

**Transição:** “Com o mundo pronto, o próximo passo é escolher um evento.”

## Slide 6 — Factory Method: propósito, problema e UML

**Modo A:** 2:45–3:20 (35 s). **Modo B:** 1:20–1:45 (25 s).

**Fala A:** “Um asteroide, um supervulcão e uma guerra nuclear afetam o planeta de maneiras diferentes. O simulador também tem uma invasão alienígena fictícia e uma restauração ecológica. Cada evento tem um criador que sabe qual produto instanciar. Todos devolvem EventoPlanetario, então o serviço aplica o evento escolhido pelo mesmo contrato. Essa criação por subclasses é o papel do Factory Method.”

**Fala B:** “Os cinco eventos têm criadores próprios. Cada subclasse de EventoCreator decide qual produto criar e o devolve como EventoPlanetario. O serviço aplica o evento pelo contrato comum.”

**Conceito e técnica:** Pares: AsteroideCreator→Asteroide; SupervulcaoCreator→Supervulcao; GuerraNuclearCreator→GuerraNuclear; InvasaoAlienigenaCreator→InvasaoAlienigena; RestauracaoEcologicaCreator→RestauracaoEcologica. Produtos herdam EventoBase, que implementa EventoPlanetario. No UML, distinguir herança, realização de interface e dependência de criação. Catálogo da UI registra os criadores disponíveis.

**Demo / abrir código:** Consulta: events/EventoCreator.java e events/AsteroideCreator.java. Num mundo aberto, selecionar um evento e sua intensidade.

**Transição:** “A criação do asteroide mostra como o contrato é usado.”

## Slide 7 — Factory Method: criar e aplicar

**Modo A:** 3:20–4:05 (45 s). **Modo B:** 1:45–2:20 (35 s).

**Fala A:** “AsteroideCreator sobrescreve criar e retorna uma nova instância de Asteroide com a intensidade escolhida. O retorno declarado é EventoPlanetario; por isso, no serviço, esse é o tipo inferido de var evento. A chamada evento.aplicar recebe o estado atual e devolve o estado transformado. Esse resultado entra numa Terra candidata com o mesmo id do mundo. Para uma intensidade de 25% na interface, o evento recebe 0,25.”

**Fala B:** “AsteroideCreator.criar retorna new Asteroide pelo contrato EventoPlanetario. Esse é o tipo de var evento no serviço. Depois, evento.aplicar transforma o estado e o resultado forma a Terra candidata.”

**Conceito e técnica:** Consulta: events/EventoCreator.java, criar; AsteroideCreator.java, criar; EventoPlanetario.java, aplicar; EventoBase.java, aplicar/transformar; application/SimulacaoService.java, aplicar. @Override permite conferir a sobrescrita em compilação. Entre a criação do evento e a candidata, o serviço captura automaticamente o estado anterior; as duas chamadas não são adjacentes. Intensidade fora de [0,1]/não finita é rejeitada; zero mantém as condições, mas o serviço ainda pode registrar a ação.

**Demo / abrir código:** Para aprofundar o polimorfismo, seguir EventoBase.aplicar até transformar no produto concreto. A candidata só substitui o estado público depois da confirmação.

**Transição:** “Depois que a mudança é salva, os outros componentes precisam recebê-la.”

## Slide 8 — Observer: propósito, problema e UML

**Modo A:** 4:05–4:40 (35 s). **Modo B:** 2:20–2:45 (25 s).

**Fala A:** “Uma alteração no planeta interessa a mais de uma parte do aplicativo. O monitor avalia as condições de vida, as estatísticas contam mudanças e acompanham extremos, e a interface apresenta o resultado. Eu uso ObserverBus para entregar a mesma mudança a esses assinantes por SimulacaoObserver. Cada um executa sua responsabilidade sem colocar as regras de alertas ou de desenho dentro de Terra.”

**Fala B:** “ObserverBus entrega a mudança aos assinantes: alertas, estatísticas e interface. Todos recebem pelo contrato SimulacaoObserver e cada um faz uma atualização diferente.”

**Conceito e técnica:** Consulta: patterns/observer/SimulacaoObserver.java; ObserverBus.java; AlertaObserver.java; EstatisticasObserver.java. O serviço inscreve os dois monitores. MainController inscreve um objeto anônimo cujo callback agenda a UI; não implementa diretamente SimulacaoObserver. Este é o papel do assinante gráfico no UML.

**Demo / abrir código:** Entrada independente: alterar condições ou aplicar um evento num mundo já aberto e consultar os assinantes notificados.

**Transição:** “A inscrição define quem participa; a publicação chama cada assinante.”

## Slide 9 — Observer: interface, inscrição e callback

**Modo A:** 4:40–5:25 (45 s). **Modo B:** 2:45–3:20 (35 s).

**Fala A:** “SimulacaoObserver define aoMudar, o método que recebe a mudança. inscrever adiciona o observador à lista e devolve uma forma de remover a inscrição depois. Quando o serviço publica, ObserverBus percorre a lista e chama observer.aoMudar(mudanca). Essa entrega é síncrona. O assinante da interface agenda a atualização com Platform.runLater, na thread JavaFX. Se um callback lança RuntimeException, o bus registra a falha e continua com os demais.”

**Fala B:** “O serviço publica e o bus chama aoMudar em cada observador inscrito, de forma síncrona. O assinante gráfico usa Platform.runLater para agendar a atualização na thread JavaFX. RuntimeException é registrada por assinante.”

**Conceito e técnica:** Consulta: ObserverBus.inscrever/publicar; SimulacaoService.confirmar; MainController.initialize/close. O serviço publica após salvar e atualizar Terra. Notificado significa retorno normal de aoMudar; no assinante gráfico, isso confirma o agendamento, não a conclusão da renderização. Callback lento retarda o laço; catch captura RuntimeException, não todo Error. AutoCloseable remove a inscrição. Alertas e estatísticas são projeções de sessão por UUID.

**Demo / abrir código:** Consultar nomes dos assinantes na trilha e contagem da sessão. Asteroide 25% pode manter a mensagem de condições estáveis do monitor mesmo tendo sido entregue; notificação e alerta crítico são fatos diferentes.

**Transição:** “Além de acompanhar uma mudança, eu posso recuperar o estado anterior.”

## Slide 10 — Memento: propósito, problema e UML

**Modo A:** 5:25–6:00 (35 s). **Modo B:** 3:20–3:45 (25 s).

**Fala A:** “Antes de alterar o mundo, eu posso guardar um retrato. Ele preserva o nome e o estado completo daquele momento. Terra cria e restaura o retrato, TerraMemento carrega esses dados e SimulacaoService coordena quando usar cada um. Depois de uma catástrofe, recuperar esse valor guardado devolve as condições anteriores sem precisar calcular um evento que desfaça todos os efeitos.”

**Fala B:** “Memento guarda um retrato do mundo para recuperá-lo depois. Terra captura e restaura, TerraMemento conserva os dados e o serviço coordena a operação. A recuperação usa os valores guardados antes da mudança.”

**Conceito e técnica:** Consulta: domain/Terra.java, capturar/restaurar; patterns/memento/TerraMemento.java. Papéis: Terra originador, TerraMemento memento e SimulacaoService caretaker. O record é público e estado() é legível; a proteção é a imutabilidade do grafo, sem opacidade estrita GoF. Terra e snapshot podem referenciar o mesmo EstadoPlanetario imutável. SQLite acrescenta durabilidade. Detalhes em DEFESA_CODIGO_JAVA.md.

**Demo / abrir código:** Entrada independente: num mundo existente, capturar, alterar e restaurar. A lista de retratos oferece o valor preservado para seleção.

**Transição:** “Na restauração, o retrato precisa pertencer à mesma Terra.”

## Slide 11 — Memento: captura e restauração

**Modo A:** 6:00–6:45 (45 s). **Modo B:** 3:45–4:20 (35 s).

**Fala A:** “capturar guarda o id do mundo, o nome, o instante e o estado num retrato com identificação própria. restaurar compara o mundoId desse retrato com o id da Terra. Se forem diferentes, rejeita a operação. Quando correspondem, recupera o nome e o estado sem trocar a identidade do mundo. O serviço valida isso numa candidata e também preserva o estado que está sendo substituído.”

**Fala B:** “A Terra confere o mundoId do retrato antes de restaurar. Se ele pertence ao mesmo mundo, recupera nome e estado mantendo seu id. O serviço também captura um retrato do estado que será substituído.”

**Conceito e técnica:** Consulta: Terra.capturar/restaurar e SimulacaoService.restaurar. Objects.requireNonNull antecede a comparação dos UUIDs. Diferenciar id do snapshot de mundoId. Cópia tem UUID novo e rejeita retrato do original. Restaurar recupera componentes e anos; não apaga histórico nem devolve estatísticas de sessão/câmera ao passado. RestauracaoEcologica transforma o estado atual, sem ser restauração exata por Memento.

**Demo / abrir código:** Comparar nome, ano e componentes. Recuperação de valores e persistência após reabertura têm evidências próprias. Legenda técnica: “A Terra recupera o nome e o estado sem trocar sua identidade.”

**Transição:** “Essas responsabilidades se encontram no fluxo de uso do simulador.”

## Slide 12 — Os quatro padrões no fluxo do simulador

**Modo A:** 6:45–7:30 (45 s). **Modo B:** 4:20–4:50 (30 s).

**Fala A:** “Eu construo o mundo, preservo retratos, aplico eventos e recupero estados. São ações que posso repetir durante uma simulação. Dentro de aplicar, o serviço cria o evento, captura automaticamente o estado anterior, calcula os efeitos e os indicadores e grava no SQLite. Depois atualiza a Terra e publica a mudança. Restaurar usa outro fluxo: valida o retrato, preserva o estado atual e confirma a recuperação.”

**Fala B:** “O usuário constrói, guarda retratos, aplica eventos e restaura. Dentro de aplicar, a ordem é criar evento, capturar automaticamente, aplicar e calcular, gravar, atualizar Terra e publicar a mudança.”

**Conceito e técnica:** Consulta: SimulacaoService.criar/capturar/aplicar/confirmar/restaurar; persistence/SqliteMundoRepository.salvar e TransacaoSqlite.executar. MundoRepository recebe candidata, RegistroEvento e snapshot opcional. Captura manual é uma operação separada, com notificar=false; pode ocorrer antes do clique de aplicar, que faz sua própria captura após criar o produto. Em falha SQL anterior ao commit, tenta-se rollback. Se salvar não retorna normalmente, o serviço não avança para atualizar/publicar, mas isso não prova ausência de commit: o finally de TransacaoSqlite pode falhar ao restaurar autocommit depois de um commit bem-sucedido. Nesse caso, o banco pode estar gravado enquanto memória/notificação não avançaram. Não há transação distribuída entre SQLite, memória e GUI. RuntimeException do observador é posterior à gravação.

**Demo / abrir código:** Distinguir o retrato manual do automático na trilha. No modo B, seguir ao slide 15; no modo A, continuar por indicadores e resultados.

**Transição:** “A simulação calcula indicadores para acompanhar as condições desse mundo.”

## Slide 13 — Indicadores e evolução do planeta

**Modo A:** 7:30–8:20 (50 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “O simulador calcula três indicadores. A adequação humana considera as condições para humanos sem proteção. O potencial biológico usa condições mais amplas para vida associada à água. A sustentabilidade da civilização combina o ambiente com recursos, área continental e população. No avanço do tempo, clima, gelo, vegetação e população evoluem em passos de até um ano. Os indicadores são recalculados a partir dessas novas condições.”

**Fala B:** “Adequação humana, potencial biológico e sustentabilidade respondem a aspectos diferentes do mesmo mundo. Quando o tempo avança, o motor atualiza o ambiente e a população e recalcula esses indicadores.”

**Conceito e técnica:** Contexto opcional no modo B. Consulta: simulation/ModeloPlanetario.java, calcular/avancar/evoluir; docs/MODELO.md. Os índices são produtos de adequações, não probabilidades. Pressão parcial = pressão total × fração do gás. O modelo é global e didático, sem calibração científica. Água+gelo conserva reserva de cobertura de superfície; o evento isolado não avança o relógio. Mesma entrada/intervalo produzem a mesma saída; particionar chamadas pode mudar arredondamentos.

**Demo / abrir código:** Localizar fatores nos indicadores. Evolução longa ou varredura experimental fica fora da demonstração inicial.

**Transição:** “Os experimentos mostram como essas regras aparecem nos resultados.”

## Slide 14 — Resultados dos experimentos

**Modo A:** 8:20–9:05 (45 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “Nos cenários medidos, substituir parte do nitrogênio por hélio, mantendo as pressões relevantes, preservou os indicadores. Um planeta com oceano total manteve adequação humana de 100%, mas teve capacidade de suporte civilizacional zero. No asteroide a 25%, H passou de 100 para cerca de 64,73, a temperatura de 15 para 8 e a população de 8 para 6,5 bilhões. A restauração recuperou os valores iniciais.”

**Fala B:** “Três resultados: a troca controlada de nitrogênio por hélio preservou os indicadores; oceano total teve suporte civilizacional zero; asteroide a 25% alterou o mundo e o retrato recuperou os valores anteriores.”

**Conceito e técnica:** Contexto opcional no modo B. Primeiro/segundo resultados: docs/auditoria/experimental-20261115-final/snapshots.csv. Evento/restauração: docs/capturas/run-20261115-124710/measurements.csv, registros antes/evento_0/restaurado; visual-values.csv e sqlite-reopen.txt. H=100→64,72968169602449→100; T=15→8→15 °C; população=8.000.000.000→6.500.000.000→8.000.000.000. A UI pode arredondar H. Esses registros são anteriores e não foram reexecutados nesta revisão. Na evolução sem O₂, o arredondamento inteiro de Math.round pode reter duas pessoas; esse resíduo numérico não prevê sobrevivência. Os documentos de estudo detalham os limites do modelo.

**Demo / abrir código:** Apresentar os três resultados e a captura restaurada. Três números ilustram recuperação; a comparação de estado completo/checagens do ensaio sustenta a recuperação de todos os valores. Usar os registros como evidência anterior.

**Transição:** “O mundo restaurado reúne esse ciclo de construção, evento e recuperação.”

## Slide 15 — Construção, eventos, comunicação e recuperação

**Modo A:** 9:05–10:40 (95 s). **Modo B:** 4:50–6:20 (90 s).

**Fala A:** “Nesta captura, a Terra voltou aos valores do retrato depois do impacto. O aplicativo reúne quatro responsabilidades: TerraBuilder monta o mundo, os criadores produzem os eventos, ObserverBus comunica as mudanças e Terra recupera o estado guardado em TerraMemento. Os mundos, o histórico e os retratos ficam no SQLite. A entrega anterior registrou 69 testes do aplicativo e 2.026 experimentos. O resultado é um simulador em que posso construir cenários, acompanhar transformações e recuperar estados.”

**Fala B:** “O mundo voltou ao estado preservado. Builder constrói, Factory Method cria os eventos, Observer comunica as mudanças e Memento permite recuperar o retrato. Essas partes formam o fluxo do simulador, com mundos e histórico guardados no SQLite.”

**Conceito e técnica:** Captura restaurada e quatro papéis concretos acompanham a conclusão. Os 69 testes são do recibo histórico docs/evidencias/verify-final.txt; os 2.026 experimentos, de docs/auditoria/experimental-20261115-final/summary.json. Não são novas execuções desta revisão, nem 2.026 testes JUnit. Na demo, o retrato manual antecede aplicar, que captura outro automaticamente após criar o produto. Reabertura ao vivo só conta se efetivamente realizada; recibo anterior permanece registro.

**Demo / abrir código:** 0–15s criar Terra com valores padrão; 15–25s capturar; 25–40s selecionar Asteroide 25% e aplicar; 40–50s acompanhar assinantes/valores; 50–70s selecionar retrato, restaurar e conferir; 70–90s concluir junto ao mundo restaurado. No modo A, 5s adicionais para pausa. Comparar H/T/população e nome/ano. Mundo de controle, reabertura e caminhos independentes permanecem no guia e em DEMO_APRESENTACAO.md, fora do orçamento básico. Se usar apenas a captura, apresentar como registro, sem narrar cliques que não ocorreram.

**Transição:** Encerramento da apresentação.
