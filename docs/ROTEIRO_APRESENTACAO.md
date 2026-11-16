# Roteiro — apresentação completa e defesa individual

- **Aluno:** Matheus Luiz Bueno
- **Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS
- **Professor:** EDER MAGALHAES MACHADO
- **Turma:** 4P
- **Trabalho:** ProjektTellus — Simulador Planetário
- **Período:** 2º Bimestre de 2026

### Como conduzir a conversa

Não é um discurso obrigatório para decorar. As falas são propostas em primeira pessoa para explicar o funcionamento atual; não inventam autoria pessoal, alternativas historicamente avaliadas, dificuldades subjetivas nem testes realizados pelo aluno. Se houver interrupção, respondo primeiro, abro a classe ou UML correspondente e retomo no próximo padrão sem recomeçar tudo. Para respostas de 20 segundos, um minuto e aprofundamento dos quatro padrões, usar [DEFESA_CODIGO_JAVA.md](DEFESA_CODIGO_JAVA.md). Para revisão rápida, [GUIA_DEFESA_INDIVIDUAL.md](GUIA_DEFESA_INDIVIDUAL.md); arguição em [PERGUNTAS_APRESENTACAO.md](PERGUNTAS_APRESENTACAO.md).

**Modo A — completo:** todos os 15 slides; faixa de 10–12 minutos, alvo 10min40s, sem perguntas extensas. **Modo B — individual:** faixa de 5–7 minutos, alvo 6min20s de exposição inicial; sequência **1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15**. Abertura 1/2 curta; 4–11 essenciais, sem esconder nenhum padrão. Pular 3, 13 e 14 apenas como contexto opcional; continuam no arquivo e podem ser abertos sob pergunta. Os tempos não são exigência do enunciado.

Cada par 4/5, 6/7, 8/9 e 10/11 mantém **nome/propósito + problema/solução + UML + Java** acessíveis nos dois modos. Se precisar reduzir ainda mais, reduzir fala/contexto; não eliminar um padrão inteiro ou ocultar seu UML/código. Código aberto para responder pergunta substitui explicação de slide naquele momento, sem obrigar a repetir o trecho.

A [transcrição do enunciado](ENUNCIADO_ORIGINAL_TRANSCRITO.md) e a [matriz](CONFORMIDADE_TRABALHO.md) são referências. A Prof. Célia aparece no texto histórico pelo projeto conjunto; esta apresentação é para EDER MAGALHAES MACHADO. A revisão atual é dos materiais; não reexecutou Maven ou GUI. Recibos antigos são identificados como anteriores. Não presumir PowerPoint desktop ou computador da faculdade validados pelas renderizações.

### Preparação e contingência

Deixar as classes da tabela do guia localizáveis no editor e o aplicativo no procedimento de banco temporário descrito em [DEMO_APRESENTACAO.md](DEMO_APRESENTACAO.md). A fonte real usa a propriedade `projekttellus.db`; não inventar opções do lançador. Não abrir a biblioteca pessoal para ensaio. Usar o mesmo banco se houver pedido de reabertura.

Se JavaFX/aplicativo falhar, dizer o que falhou, apresentar capturas/registro anterior como tal e seguir pelo Java/UML local. Não repetir o fluxo inteiro por insistência se uma classe já responde à pergunta. A demonstração básica é construir → capturar manualmente → asteroide 25% → observar → restaurar. Caminhos independentes ficam no guia e no documento da demo; o documento da demo mantém os detalhes operacionais.

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

**Fala A:** “Professor, sou Matheus Luiz Bueno, da turma 4P. Vou mostrar o ProjektTellus por quatro responsabilidades: quem constrói a Terra, quem cria os eventos, quem recebe mudanças e quem restaura os estados. Posso seguir pelos slides ou abrir diretamente uma classe conforme a pergunta.”

**Fala B:** “Professor, sou Matheus Luiz Bueno, da 4P. Vou mostrar os quatro padrões no fluxo real e posso abrir as classes durante a conversa.”

**Conceito e técnica:** Identidade acadêmica completa na capa e foco no código. A disciplina é ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS; professor EDER MAGALHAES MACHADO; período 2º Bimestre de 2026. Não inferir instituição ou matrícula.

**Demo / abrir código:** Apontar o planeta e os quatro nomes; deixar o aplicativo preparado, sem executar o fluxo ainda.

**Transição:** “O problema do sistema organiza a explicação.”

## Slide 2 — Problema e proposta

**Modo A:** 0:20–0:50 (30 s). **Modo B:** 0:10–0:25 (15 s).

**Fala A:** “Eu preciso reunir condições iniciais coerentes, aplicar eventos diferentes, avisar interessados e recuperar estados. Uso Builder, Factory Method, Observer e Memento nessas responsabilidades. Para cada um vou mostrar o propósito, o problema e a solução, o UML e o Java. O simulador é didático; os indicadores não pretendem prever a vida ou o clima real.”

**Fala B:** “O problema é construir, aplicar eventos, comunicar e restaurar. Vou passar por UML e Java dos quatro padrões. O modelo planetário é didático.”

**Conceito e técnica:** O enunciado exige pelo menos três padrões; os quatro escolhidos pertencem à lista. A transcrição mantém a Prof. Célia pelo projeto conjunto; o professor desta apresentação é EDER MAGALHAES MACHADO. Datas 16–23/11/2026; 70% trabalho/30% prova.

**Demo / abrir código:** No modo B, esta é a última contextualização: ir ao slide 4. Não gastar o tempo dos padrões com experimentos.

**Transição:** “Vou começar pela construção de uma Terra.”

## Slide 3 — Arquitetura geral

**Modo A:** 0:50–1:25 (35 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “Eu começo na tela, mas a operação é coordenada por SimulacaoService. Terra mantém identidade e referência ao estado atual; os componentes são valores imutáveis. ModeloPlanetario calcula indicadores e evolução. O serviço usa MundoRepository para gravar e ObserverBus para comunicar. MainController envia trabalho ao executor de uma thread e agenda a atualização visual na thread JavaFX. Isso permite explicar a lógica sem depender do desenho do planeta.”

**Fala B:** Pular na exposição B; se o professor perguntar pela arquitetura, voltar aqui e seguir MainController → SimulacaoService → domínio/repositório/bus.

**Conceito e técnica:** Arquivo de orientação: src/main/java/com/projekttellus/application/SimulacaoService.java. Domínio não depende de JavaFX/SQL; Terra conhece TerraMemento. As operações e acessos ao repositório da UI são serializados pelo worker.

**Demo / abrir código:** Apontar o diagrama de arquitetura. Não iniciar uma demonstração extra.

**Transição:** “O Builder cuida da montagem inicial, antes da aplicação de eventos.”

## Slide 4 — Builder: propósito, problema e UML

**Modo A:** 1:25–2:00 (35 s). **Modo B:** 0:25–0:50 (25 s).

**Fala A:** “Eu uso TerraBuilder para reunir nome, atmosfera, clima, água, vegetação, população e tempo. Sem ele, cada cliente teria de repetir essa montagem. No UML, o diálogo configura o Builder, e build cria EstadoPlanetario e Terra. Este é um Builder concreto e fluente, sem Director nem interface abstrata. Ele organiza a construção; salvar é outra responsabilidade, coordenada pelo serviço.”

**Fala B:** “Eu uso Builder para reunir componentes e defaults antes de criar Terra. O diálogo configura TerraBuilder; build cria o estado e o produto. No UML não há Director: a implementação é concreta e fluente.”

**Conceito e técnica:** Propósito: construção em etapas. Problema: montagem extensa no cliente. Solução: configuração nomeada e construção final. UML: BuilderDialog depende de TerraBuilder; Builder cria Terra/EstadoPlanetario. Valores imutáveis podem ser compartilhados, sem posse exclusiva por composição.

**Demo / abrir código:** Para pedido isolado: abrir criação de mundo. Se pedir Java, seguir ao slide 5 ou TerraBuilder.java.

**Transição:** “No código, eu aponto o retorno do configurador e a criação do produto.”

## Slide 5 — Builder: Java e construção

**Modo A:** 2:00–2:45 (45 s). **Modo B:** 0:50–1:20 (30 s).

**Fala A:** “Aqui atmosfera recebe um componente e return this devolve o mesmo Builder, permitindo a próxima escolha. build devolve outra coisa: uma Terra com UUID novo e EstadoPlanetario montado. Os limites são validados nos componentes; o estado rejeita nulos e tempo inválido, e Terra valida o nome. O Builder continua mutável e pode ser reutilizado, sem reset automático. Os mundos podem compartilhar valores imutáveis porque cada mudança troca a referência do estado.”

**Fala B:** “atmosfera recebe Atmosfera e retorna o próprio Builder. build cria EstadoPlanetario e Terra com UUID novo. A validação fica no domínio, não em todos os setters. A Terra é o produto; o Builder é o configurador.”

**Conceito e técnica:** No slide 5, atmosfera está em quatro linhas legíveis, com os mesmos tokens da fonte e apenas whitespace reorganizado. Abrir src/main/java/com/projekttellus/domain/TerraBuilder.java: atmosfera e build. Depois ui/BuilderDialog.java: setResultConverter; application/SimulacaoService.java: criar. Recebe TerraBuilder, chama build, captura origem e confirma. UUID identifica; não torna o cálculo aleatório.

**Demo / abrir código:** No modo B, mostrar o Java no slide e reservar a criação ao final. Uma interrupção pode substituir essa fala pela trilha Builder do DEFESA_CODIGO_JAVA.

**Transição:** “Com a Terra construída, eu passo à criação dos tipos de evento.”

## Slide 6 — Factory Method: propósito, problema e UML

**Modo A:** 2:45–3:20 (35 s). **Modo B:** 1:20–1:45 (25 s).

**Fala A:** “Eu tenho cinco eventos diferentes, mas quero que o serviço use um contrato comum. EventoCreator declara criar; cada Creator concreto decide o produto. No UML, os cinco pares são Asteroide, Supervulcao, GuerraNuclear, InvasaoAlienigena e RestauracaoEcologica com seus Creators. Os produtos herdam EventoBase, que implementa EventoPlanetario. O serviço aplica pelo contrato; o catálogo da interface ainda conhece os criadores disponíveis.”

**Fala B:** “Factory Method deixa cada subclasse de EventoCreator escolher o produto. Tenho cinco pares reais; todos podem ser usados como EventoPlanetario. No UML distingo herança entre classes, realização da interface e dependência de criação.”

**Conceito e técnica:** Slides/UML devem dar acesso aos cinco pares, mesmo quando um par é ampliado para leitura. Propósito: variar criação. Problema: serviço aplicando cinco tipos. Solução: criar abstrato sobrescrito, sem switch de produtos no serviço. Produto base→interface é realização, não extends entre classes.

**Demo / abrir código:** Abrir events/EventoCreator.java e events/AsteroideCreator.java. Posso selecionar um evento num mundo já aberto sem reconstruí-lo.

**Transição:** “Vou mostrar a sobrescrita e o tipo que o serviço recebe.”

## Slide 7 — Factory Method: criar e aplicar

**Modo A:** 3:20–4:05 (45 s). **Modo B:** 1:45–2:20 (35 s).

**Fala A:** “@Override confirma a sobrescrita de criar em AsteroideCreator. O retorno declarado é EventoPlanetario, e a instância criada é new Asteroide. No serviço, var evento tem tipo estático inferido EventoPlanetario; Java continua tipado. Eu chamo evento.aplicar pelo contrato. A base encaminha para a transformação concreta, e o resultado entra numa Terra candidata com o mesmo id. Na tela, 25% vira 0,25. A captura automática fica entre as duas linhas do serviço mostradas em recortes separados.”

**Fala B:** “Este @Override sobrescreve criar e retorna new Asteroide. O retorno é EventoPlanetario; esse também é o tipo inferido de var evento no serviço. evento.aplicar usa o contrato e devolve o novo estado para a Terra candidata. As duas linhas são recortes separados: há captura entre elas.”

**Conceito e técnica:** Display final: AsteroideCreator real, @Override/EventoPlanetario destacados e método em quatro linhas; a candidata quebra após terra.nome(). São apenas quebras/espaços reorganizados, com os mesmos tokens do Java. Não substituir var por declaração fictícia no recorte. Usar as linhas literais `var evento = Objects.requireNonNull(creator).criar(intensidade);` e `var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));`. Fonte completa/explicação em DEFESA_CODIGO_JAVA.md.

**Demo / abrir código:** Abrir events/EventoPlanetario.java e EventoBase.aplicar se perguntar pelo polimorfismo. Intensidade inválida é rejeitada; zero mantém condições, mas pode gerar registro/contagem no serviço.

**Transição:** “Depois da confirmação, os interessados recebem a mudança.”

## Slide 8 — Observer: propósito, problema e UML

**Modo A:** 4:05–4:40 (35 s). **Modo B:** 2:20–2:45 (25 s).

**Fala A:** “Eu não quero que Terra chame diretamente telas, alertas e estatísticas. ObserverBus guarda assinantes do contrato SimulacaoObserver. O serviço publica uma mudança confirmada; AlertaObserver gera recomendações e EstatisticasObserver atualiza contagem e extremos por mundo. MainController cria um terceiro assinante anônimo para a UI. Ele não implementa a interface diretamente. O UML mostra exatamente essas dependências e realizações.”

**Fala B:** “Observer distribui a mesma mudança a responsabilidades diferentes. O bus é o publicador; alertas e estatísticas implementam SimulacaoObserver. MainController inscreve um objeto anônimo da UI. A Terra não conhece essas implementações.”

**Conceito e técnica:** Propósito: comunicar aos interessados inscritos. Problema: múltiplas reações a um mesmo fato. Solução: bus/contrato comum. Não desenhar MainController realizando diretamente SimulacaoObserver. Há dois assinantes padrão do serviço e um gráfico quando a UI está inscrita.

**Demo / abrir código:** Para pedido isolado: alterar condições ou aplicar evento em mundo existente e mostrar assinantes. Não reiniciar a demo inteira.

**Transição:** “Eu explico interface, inscrição e callback nesta ordem.”

## Slide 9 — Observer: interface, inscrição e callback

**Modo A:** 4:40–5:25 (45 s). **Modo B:** 2:45–3:20 (35 s).

**Fala A:** “Primeiro, a interface define aoMudar. Depois, inscrever guarda o assinante e devolve AutoCloseable para removê-lo. Na publicação, o laço contém observer.aoMudar(mudanca); essa é a chamada concreta pelo contrato. O nome entra em notificados só após retorno normal. RuntimeException é registrada por assinante e o laço continua. O bus é síncrono. O callback gráfico agenda Platform.runLater; receber a notificação não prova que os pixels já foram atualizados.”

**Fala B:** “Eu mostro quatro blocos: interface, corpo de inscrever, publicação pelo serviço e callback. Na fonte, observer.aoMudar(mudanca) está dentro do laço. O callback é síncrono e o bus isola RuntimeException. A UI agenda runLater; o recibo confirma o callback, não o fim da renderização.”

**Conceito e técnica:** Abrir patterns/observer/SimulacaoObserver.java; ObserverBus.inscrever/publicar; application/SimulacaoService.confirmar; ui/MainController.initialize. Display final em quatro blocos: interface completa; corpo de inscrever em três linhas; serviço chama publicar/new MudancaPlanetaria em quatro linhas; callback observer.aoMudar(mudanca); em linha única azul. A publicação também reorganiza whitespace mantendo tokens. Quem publica é o serviço depois de salvar/atualizar; quem recebe são os assinantes. O método completo está em DEFESA_CODIGO_JAVA.md. Callback lento retarda a publicação; não prometer paralelismo ou captura de qualquer Error.

**Demo / abrir código:** Ver nomes na trilha e estatística da sessão. Não prometer alerta crítico no Asteroide 25%: o ensaio registra condições estáveis no monitor, embora os assinantes tenham recebido a mudança.

**Transição:** “Para recuperar exatamente um estado anterior, eu uso Memento.”

## Slide 10 — Memento: propósito, problema e UML

**Modo A:** 5:25–6:00 (35 s). **Modo B:** 3:20–3:45 (25 s).

**Fala A:** “Eu não consigo desfazer uma catástrofe apenas aplicando um evento bom. Memento preserva os valores anteriores para restaurá-los. Terra é o originador, TerraMemento é o retrato e SimulacaoService coordena como caretaker. O record é público e o estado pode ser lido. A proteção vem do grafo imutável, não de opacidade estrita. O banco acrescenta durabilidade; o retrato já existe em memória.”

**Fala B:** “Memento permite recuperar valores anteriores sem calcular o inverso de uma catástrofe. Terra captura/restaura, TerraMemento guarda e o serviço coordena. O record é público e legível: é imutabilidade, sem opacidade estrita GoF.”

**Conceito e técnica:** Propósito: preservar/restaurar. Problema: perda de valores em múltiplos componentes. Solução: retrato completo vinculado ao mundo. UML: referências ao mesmo EstadoPlanetario imutável podem coexistir em Terra e Memento; armazenamento não substitui caretaker.

**Demo / abrir código:** Abrir domain/Terra.java e patterns/memento/TerraMemento.java. Usar mundo existente para captura/restauração independente.

**Transição:** “A proteção de identidade aparece nestas linhas.”

## Slide 11 — Memento: captura e restauração

**Modo A:** 6:00–6:45 (45 s). **Modo B:** 3:45–4:20 (35 s).

**Fala A:** “Capturar recebe rótulo e devolve um record com id do retrato, id do mundo, nome, instante e estado. Restaurar verifica null e compara mundoId com o id final da Terra. Só então reassocia nome e estado, incluindo tempo e componentes. No serviço, a candidata valida isso antes de gravar e um retrato de retorno preserva o que será substituído. Não preciso clonar os valores imutáveis. O histórico e as estatísticas de sessão não voltam ao passado.”

**Fala B:** “capturar guarda identidade, nome e estado. restaurar rejeita retrato de outro mundo e reassocia os valores; não troca o id da Terra. O serviço valida numa candidata antes de gravar e captura um retorno. O estado é legível e imutável, sem deep copy obrigatório.”

**Conceito e técnica:** Abrir Terra.capturar/restaurar e SimulacaoService.restaurar. Null-check antecede if se o slide recorta só a parte interna. Diferenciar id do retrato e mundoId. Snapshot de mundo original não serve na cópia de UUID novo; RestauracaoEcologica não é restauração exata por Memento.

**Demo / abrir código:** No final, comparar nome/ano/componentes, não só imagem. Para durabilidade, usar recibo de reabertura ou pedido específico do professor.

**Transição:** “Agora eu separo os casos de uso para não confundir a ordem interna.”

## Slide 12 — Os quatro padrões no fluxo do simulador

**Modo A:** 6:45–7:30 (45 s). **Modo B:** 4:20–4:50 (30 s).

**Fala A:** “Eu separo quatro ações. Construir usa Builder e captura a origem. Capturar manualmente preserva um retrato sem anunciar nova mudança ambiental. Ao aplicar evento, a ordem interna é criar produto, capturar automaticamente, aplicar e calcular, gravar no SQLite, atualizar a Terra viva e publicar. Restaurar é uma ação posterior: valida o retrato numa candidata, preserva o retorno e confirma novamente. Não são quatro padrões rodando simultaneamente em qualquer clique.”

**Fala B:** “Construir, capturar manualmente, aplicar e restaurar são ações distintas. Dentro de aplicar: criar evento → captura automática → aplicar/calcular → SQLite → atualizar Terra → publicar. A captura manual da demo aconteceu antes do clique; ela não inverte essa ordem interna.”

**Conceito e técnica:** Abrir SimulacaoService.criar/capturar/aplicar/confirmar/restaurar. Grava candidata + RegistroEvento + snapshot opcional por MundoRepository. Erro de gravação antes de retorno normal impede avançar para atualizar/publicar; falha de observador é posterior. Não prometer transação distribuída banco/memória/GUI.

**Demo / abrir código:** Apontar na trilha o retrato manual e o automático como capturas distintas. Modo B: após este slide ir direto ao 15; 13/14 ficam acessíveis para contexto.

**Transição:** “Vou mostrar o fluxo no aplicativo; o modelo e os experimentos ficam de apoio se quiser detalhar.”

## Slide 13 — Algoritmo e indicadores

**Modo A:** 7:30–8:20 (50 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “Eu separo adequação humana, potencial biológico e sustentabilidade da civilização. Os índices multiplicam adequações; não são probabilidades. Pressão parcial é pressão total vezes fração do gás. O suporte depende também de continente e recursos. O tempo avança em passos de até um ano, recalculando clima, gelo, vegetação e população. O determinismo se refere à mesma entrada e ao mesmo intervalo; fatiar chamadas pode mudar arredondamentos.”

**Fala B:** Pular na exposição B; se solicitado, explicar H/B/C e abrir ModeloPlanetario.calcular/avancar. Não substituir os quatro padrões por discussão científica.

**Conceito e técnica:** Fonte: simulation/ModeloPlanetario.java e docs/MODELO.md. O motor é global didático, sem calibração científica. Água+gelo conserva reserva de cobertura de superfície; eventos não avançam relógio por conta própria.

**Demo / abrir código:** Apontar indicadores/fatores se perguntado. Não executar varredura ou evolução longa durante a abertura curta.

**Transição:** “Os registros ajudam a conferir comportamento e recuperação do estado.”

## Slide 14 — Experimentos: evento e restauração do estado

**Modo A:** 8:20–9:05 (45 s). **Modo B:** opcional; pular na exposição inicial.

**Fala A:** “Eu mantenho os experimentos como evidência de apoio. A troca controlada de nitrogênio por hélio preserva indicadores quando mantenho as pressões relevantes; oceano total pode ter ambiente favorável e suporte civilizacional zero. No ensaio registrado da GUI, asteroide a 25% levou H de 100 para aproximadamente 64,73, temperatura de 15 para 8 e população de 8 para 6,5 bilhões. Restaurar recuperou os valores iniciais. Isso mostra transformação e recuperação pelo fluxo real, sem certificar ciência.”

**Fala B:** Pular na exposição B; usar como apoio se o professor pedir evidência. O terceiro exemplo é Asteroide 25% + restauração exata do estado, não população residual.

**Conceito e técnica:** Fonte do ensaio anterior: docs/capturas/run-20261115-124710/measurements.csv, registros antes/evento_0/restaurado; visual-values.csv e sqlite-reopen.txt. H=100 → 64,72968169602449 →100; T=15→8→15; população=8.000.000.000→6.500.000.000→8.000.000.000. O display pode arredondar H para 64,7297 ou 64,73. As evidências são anteriores, não reexecutadas nesta revisão.

**Demo / abrir código:** Mostrar captura/recibo real; não repetir a auditoria. Três números ilustram recuperação; comparação completa do estado/checagens sustenta a restauração exata, não somente a aparência do planeta.

**Transição:** “Posso demonstrar o ciclo completo ou só o padrão que quiser verificar.”

**Nota técnica, fora da chamada principal:** o resíduo de duas pessoas no cenário sem O₂ é limitação numérica de arredondamento inteiro; não representa sobrevivência real. Se perguntado, explicar sem usar esse achado como título/headline. A nova evidência de recuperação não elimina essa limitação do motor.

## Slide 15 — Demonstração e conclusão

**Modo A:** 9:05–10:40 (95 s). **Modo B:** 4:50–6:20 (90 s).

**Fala A:** “Vou construir a Terra, preservar um retrato e aplicar asteroide a 25%. O Creator produz o evento; o serviço salva e os assinantes recebem a mudança. Agora seleciono o retrato e restauro. Eu confiro os valores e a identidade do mundo, não só o globo. Builder constrói, Factory Method cria o evento, Observer comunica e Memento recupera. Posso abrir qualquer uma dessas classes para detalhar.”

**Fala B:** “Vou mostrar construir → capturar → asteroide 25% → observar → restaurar. Depois posso abrir a classe do padrão que quiser. O foco é explicar quem constrói a Terra, cria o evento, recebe a mudança e recupera o estado.”

**Conceito e técnica:** Usar banco temporário do procedimento de demo. Ordem UI e ordem interna são diferentes: o retrato manual antecede aplicar, que cria outro automático após criar o produto. Checar H/T/população e nome/ano. Só afirmar reabertura ao vivo se ocorrer; recibo anterior não é execução nova.

**Demo / abrir código:** 0–15s criar Terra com defaults; 15–25s capturar; 25–40s selecionar Asteroide 25% e aplicar; 40–50s apontar nomes dos assinantes/valores; 50–70s selecionar retrato/restaurar e conferir; 70–90s concluir e aceitar pedido de classe. No modo A, os 5s restantes servem para controle/pausa. Mundo de controle e reabertura são extensões sob pergunta, fora do orçamento básico. Se o professor pedir só um padrão, usar o caminho independente do guia/DEMO_APRESENTACAO.

**Transição:** “Posso seguir do contrato até a chamada concreta, ou mostrar outra operação.”
