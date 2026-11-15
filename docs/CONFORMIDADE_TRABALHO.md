# Matriz de conformidade acadêmica — ProjektTellus

Base: leitura direta do Java em 15/11/2026 e transcrição textual integral das duas páginas do enunciado fornecida pelo usuário (fonte primária, espaçamento e pontuação normalizados). A matriz foi criada antes dos slides e atualizada após a validação local da entrega. Não equivale a aceite da docente ou cotejo com o PDF binário. Os recibos de testes e da demonstração estão em `docs/evidencias/` e `docs/capturas/`.

## Quatro padrões × quatro itens exigidos

Matriz preparada antes do deck e atualizada após integração. Caminhos Java relativos a `src/main/java/com/projekttellus/`. Há 16 itens acadêmicos abaixo, individualmente cobertos. PNGs UML em 2732×1536 e diagramas nativos editáveis nos slides; Mermaid completo permanece disponível como fonte complementar. Trechos reais, explicação e fontes completas: [ESTUDO_PADROES.md](ESTUDO_PADROES.md) e [CODIGO_JAVA_APRESENTACAO.md](CODIGO_JAVA_APRESENTACAO.md).

| Padrão | Exigência | Implementação/evidência | Slides | Java utilizado | Diagrama | Demonstração | Pendência |
|---|---|---|---|---|---|---|---|
| Builder | Nome e propósito | Construção em etapas de uma Terra com componentes opcionais/defaults. | 4–5 | `domain/TerraBuilder.java; ui/BuilderDialog.java` | [UML](uml-apresentacao/Builder.png) | Criar mundo → Construir e salvar | Atendido no material; execução local comprovada. |
| Builder | Problema e solução real | Editor reúne atmosfera/clima/água/vida/população; builder evita montagem repetida e parâmetros anônimos no cliente. | 4–5 | `domain/TerraBuilder.java; ui/BuilderDialog.java` | [UML](uml-apresentacao/Builder.png) | Criar mundo → Construir e salvar | Atendido no material; execução local comprovada. |
| Builder | Estrutura UML | BuilderDialog depende de TerraBuilder; build cria Terra/EstadoPlanetario; estado referencia cinco componentes imutáveis. Sem Director inventado. | 4–5 | `domain/TerraBuilder.java; ui/BuilderDialog.java` | [UML](uml-apresentacao/Builder.png) | Criar mundo → Construir e salvar | Atendido no material; execução local comprovada. |
| Builder | Código Java funcional | Setters fluentes retornam this; build cria EstadoPlanetario e Terra com UUID novo. Validação pertence ao domínio. | 4–5 | `domain/TerraBuilder.java; ui/BuilderDialog.java` | [UML](uml-apresentacao/Builder.png) | Criar mundo → Construir e salvar | Atendido no material; execução local comprovada. |
| Factory Method | Nome e propósito | Subclasses de EventoCreator determinam o produto concreto retornado por criar(double). | 6–7 | `events/EventoCreator.java; events/AsteroideCreator.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Factory-Method.png) | Selecionar Asteroide 25% → Aplicar evento | Atendido no material; execução local comprovada. |
| Factory Method | Problema e solução real | Cinco produtos distintos precisam de criação variável e aplicação por contrato comum, sem a fórmula de cada um no serviço. | 6–7 | `events/EventoCreator.java; events/AsteroideCreator.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Factory-Method.png) | Selecionar Asteroide 25% → Aplicar evento | Atendido no material; execução local comprovada. |
| Factory Method | Estrutura UML | Creator abstrato, AsteroideCreator, interface EventoPlanetario, EventoBase e Asteroide. Quatro pares adicionais no estudo e Mermaid completo. | 6–7 | `events/EventoCreator.java; events/AsteroideCreator.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Factory-Method.png) | Selecionar Asteroide 25% → Aplicar evento | Atendido no material; execução local comprovada. |
| Factory Method | Código Java funcional | AsteroideCreator.criar retorna new Asteroide; serviço chama criar e evento.aplicar pelo contrato; produtos especializam transformar. | 6–7 | `events/EventoCreator.java; events/AsteroideCreator.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Factory-Method.png) | Selecionar Asteroide 25% → Aplicar evento | Atendido no material; execução local comprovada. |
| Observer | Nome e propósito | Distribuir MudancaPlanetaria aos assinantes por SimulacaoObserver. | 8–9 | `patterns/observer/ObserverBus.java; SimulacaoObserver.java; ui/MainController.java` | [UML](uml-apresentacao/Observer.png) | Mostrar passos dos 3 assinantes, indicadores e caixa Observer | Atendido no material; execução local comprovada. |
| Observer | Problema e solução real | Alertas, estatísticas e globo/indicadores recebem a mesma alteração confirmada sem o domínio conhecer suas implementações. | 8–9 | `patterns/observer/ObserverBus.java; SimulacaoObserver.java; ui/MainController.java` | [UML](uml-apresentacao/Observer.png) | Mostrar passos dos 3 assinantes, indicadores e caixa Observer | Atendido no material; execução local comprovada. |
| Observer | Estrutura UML | SimulacaoService usa ObserverBus; bus guarda assinantes; AlertaObserver/EstatisticasObserver realizam a interface. MainController registra objeto anônimo. | 8–9 | `patterns/observer/ObserverBus.java; SimulacaoObserver.java; ui/MainController.java` | [UML](uml-apresentacao/Observer.png) | Mostrar passos dos 3 assinantes, indicadores e caixa Observer | Atendido no material; execução local comprovada. |
| Observer | Código Java funcional | inscrever usa addIfAbsent e devolve AutoCloseable; publicar percorre assinantes e chama aoMudar. Bus síncrono; UI agenda Platform.runLater. | 8–9 | `patterns/observer/ObserverBus.java; SimulacaoObserver.java; ui/MainController.java` | [UML](uml-apresentacao/Observer.png) | Mostrar passos dos 3 assinantes, indicadores e caixa Observer | Atendido no material; execução local comprovada. |
| Memento | Nome e propósito | Guardar e restaurar um estado anterior do mesmo mundo. | 10–11 | `domain/Terra.java; patterns/memento/TerraMemento.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Memento.png) | Capturar → Asteroide → Restaurar selecionado → comparar estado | Atendido no material; execução local comprovada. |
| Memento | Problema e solução real | Recuperar condições anteriores sem tentar inverter cada catástrofe; snapshot completo imutável vinculado a mundoId. | 10–11 | `domain/Terra.java; patterns/memento/TerraMemento.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Memento.png) | Capturar → Asteroide → Restaurar selecionado → comparar estado | Atendido no material; execução local comprovada. |
| Memento | Estrutura UML | Terra = Originator; TerraMemento = Memento; SimulacaoService = Caretaker; MundoRepository = contrato de armazenamento. Valores imutáveis podem ser compartilhados. | 10–11 | `domain/Terra.java; patterns/memento/TerraMemento.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Memento.png) | Capturar → Asteroide → Restaurar selecionado → comparar estado | Atendido no material; execução local comprovada. |
| Memento | Código Java funcional | capturar inclui nome/estado/identidade/metadados; restaurar valida UUID antes de reassociar nome/estado. Record legível, sem opacidade estrita. | 10–11 | `domain/Terra.java; patterns/memento/TerraMemento.java; application/SimulacaoService.java` | [UML](uml-apresentacao/Memento.png) | Capturar → Asteroide → Restaurar selecionado → comparar estado | Atendido no material; execução local comprovada. |

## Demais exigências e contexto

| Exigência/contexto informado | Evidência disponível | Situação/ação para integração |
|---|---|---|
| Trabalho individual | Enunciado textual integral | **Não verificável pelo código**; autoria e participação precisam de declaração real do estudante |
| Pelo menos três padrões da lista autorizada | Builder, Factory Method, Observer, Memento pertencem à lista | Quatro defendidos; não é preciso reivindicar outros padrões |
| Lista: Singleton, Factory Method, Adapter, Bridge, Decorator, Builder, Proxy, Iterator, Memento, Observer | Mensagem do usuário, preservada em [ENUNCIADO_ORIGINAL_TRANSCRITO.md](ENUNCIADO_ORIGINAL_TRANSCRITO.md) | Fonte primária textual integral registrada; PDF binário não disponível para cotejo visual |
| Nome/propósito, motivação, UML e Java para cada padrão | 16 linhas da matriz acima | Deck final incorpora os 16 itens, UML e Java real |
| Java funcional | Fontes reais e testes existentes inspecionados | Compilado no verify final; 69 testes, zero falhas/erros/skips. Fragmentos correspondem às fontes atuais |
| Datas de 16 a 23/11/2026 | Enunciado textual integral, página 1 | Janela registrada; data/hora exatas não confirmadas |
| 70% trabalho e 30% prova | Enunciado textual integral, página 1 | Peso registrado; nota e rubrica detalhada não inferidas |
| Célia; disciplina conjunta | Enunciado textual integral, página 1 | **Não verificável pelo código**; não inferir nome da outra disciplina nem regras adicionais |
| Nome do aluno e curso | Não fornecidos neste contexto | **Identificação pendente**; não inventar. Título do deck pode mostrar apenas ProjektTellus até receber os dados |
| Identificação institucional/disciplinar de documentos antigos | `REQUISITOS.md` contém identificação anterior | Não usar como confirmação do PDF, nome do curso ou nome do aluno |
| Referências refactoring.guru e DevMedia | URLs originais da página 2 e URLs específicas em ESTUDO e no roteiro | Referências para estudo; demonstração local independe de navegação |
| Transcrição integral, PDF e anexos “Sem título” | Texto integral das duas páginas recebido; ausentes apenas PDF binário e dois anexos | **Texto registrado como fonte primária**; leiaute e conteúdo dos anexos não verificáveis |
| Perguntas para defesa | Banco de apoio elaborado com IA a partir dos requisitos e do Java, com proveniência explícita | 62 perguntas: 36 aprofundamentos e as 26 solicitadas explicitamente; respostas com apoio de IA |
| 15 slides na ordem solicitada | [ROTEIRO_APRESENTACAO.md](ROTEIRO_APRESENTACAO.md) | Ordem preservada: 1 título; 2 problema; 3 arquitetura; 4–11 pares dos padrões; 12 integração; 13 algoritmo; 14 experimentos; 15 demo/conclusão |
| Duração 10–12 min | Planejamento do usuário | Meta de ensaio, **não exigência docente**; roteiro-base de 10min40s |
| Experimentos e evidências visuais | Recibos externos lidos em `auditoria/experimental-20261115-final/summary.json` e `execution.json`; entradas/saídas em `snapshots.csv` | Auditoria externa registra 2.026 experimentos, 252.628 checagens, zero falhas e ExitCode 0; não confundir com JUnit, ciência ou validação da GUI por esta frente |
| Demonstração no computador da faculdade | Nenhuma execução nesta frente | Não validada; ensaiar abertura, banco temporário, telas e restauração no ambiente real |
| Uso de ChatGPT / Gemini / Perplexity / outros com compreensão do código | Enunciado textual integral, página 1 | Roteiro, estudo linha a linha e perguntas apoiam a defesa; compreensão individual depende do estudante |

## Ressalvas conceituais obrigatórias no deck

- Builder: implementação concreta e fluente. Não existe Director ou Builder abstrato nesta implementação. Fluent API sozinha não prova o padrão; o objeto de configuração separado e a construção final sustentam a classificação.
- Factory Method: método abstrato sobrescrito por subclasses reais. O `new` existe nos criadores; o cliente trabalha com o contrato. Adicionar evento também exige registrá-lo na seleção da UI.
- Observer: `publicar` entrega callbacks de modo síncrono; a UI agenda `Platform.runLater`. Callback concluído não prova que os pixels já foram desenhados. Falhas `RuntimeException` são isoladas; não prometer isolamento de qualquer `Error`.
- Memento: record público permite consultar `estado()`. O grafo de valores imutáveis impede mutação compartilhada; não oculta a representação. SQLite torna retratos duráveis, mas não define o padrão. Não é Event Sourcing nem reversão da história.
- Persistência: a ordem observada é calcular → gravar candidato/registro/retrato → atualizar Terra → publicar. Não prometer atomicidade distribuída entre SQLite, memória e renderização JavaFX.

## Aceite desta frente

Critérios locais: trechos Java extraídos literalmente com caminho/linhas/hash; quatro diagramas ligados a classes reais; sequência de integração; roteiro com fala/conceito/técnica/tempo/transição/demo para 15 slides; banco de 62 perguntas com autoria corretamente qualificada. Renderização, compilação, Maven, testes de GUI, PDF e deck são entregas da integração e demais frentes.

## Revisão final da integração

- Conteúdo do enunciado: **16/16 itens dos quatro padrões cobertos**; quatro padrões pertencem à lista autorizada e excedem o mínimo de três.
- Deck final: `apresentacao/ProjektTellus-Apresentacao-r2.pptx`, 15 slides editáveis; `apresentacao/ProjektTellus-Apresentacao.pdf`, 15 páginas de contingência. O PDF incorpora as renderizações, sem texto Java pesquisável; o PPTX mantém texto/diagramas editáveis.
- Java atual: `mvn -B -ntp -Pgui-tests verify` executado duas vezes nesta tarefa; ver `evidencias/verify-final.txt`: 69 testes, nenhuma falha/erro/skip. A integridade/geométrica do PPTX tem recibo separado; não substitui inspeção visual.
- Demonstração: 249 verificações do harness gráfico, 33 capturas canônicas, cinco eventos e restauração/reabertura SQLite. Vídeo de 24 s é uma sequência legendada de capturas reais, não gravação contínua.
- Permanecem externos ao material: nome/curso/turma ainda não informados, declaração de autoria individual e vínculo com a disciplina conjunta, ensaio pessoal e computador físico da faculdade. Os anexos indisponíveis não tiveram conteúdo inventado. Aprovação acadêmica e compreensão do estudante só podem ser demonstradas por ele.
