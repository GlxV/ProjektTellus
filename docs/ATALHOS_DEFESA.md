# Atalhos para a defesa individual

**Aluno:** Matheus Luiz Bueno · **Professor:** EDER MAGALHAES MACHADO · **Turma:** 4P

**Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS

**Trabalho:** ProjektTellus — Simulador Planetário · **Período:** 2º Bimestre de 2026

Use este índice quando o professor pedir uma classe ou interromper a demo. Abra primeiro o contrato/objeto central, depois o colaborador e o ponto que coordena a ação. As [quatro trilhas práticas](DEMO_APRESENTACAO.md#preparação-única-para-escolher-qualquer-padrão) funcionam independentemente após a preparação. Não é preciso repetir criar → capturar → asteroide → restaurar para explicar cada padrão.

As linhas abaixo foram conferidas nas fontes da base `8f7e58d` nesta revisão. O lançador procura uma âncora única em cada arquivo e calcula a linha atual, evitando depender apenas de números fixos. Sua listagem valida caminhos e âncoras; **não compila nem testa o aplicativo**. Os 69 testes e os recibos gráficos pertencem à entrega anterior `8f7e58d`; não são um novo teste desta revisão.

## Abrir somente a fonte solicitada

Na raiz do projeto, liste uma trilha sem abrir interface:

```powershell
.\scripts\Open-DefenseSources.ps1 -Pattern Builder -ListOnly
.\scripts\Open-DefenseSources.ps1 -Pattern FactoryMethod -ListOnly
.\scripts\Open-DefenseSources.ps1 -Pattern Observer -ListOnly
.\scripts\Open-DefenseSources.ps1 -Pattern Memento -ListOnly
```

`-ListOnly` apenas lê os arquivos e retorna objetos com `Pattern`, `Item`, `Method`, `RelativePath`, `Line`, `Path` e `Navigation`. Não cria arquivos, processos, bancos ou janelas. Uma âncora ausente ou duplicada faz a listagem falhar, para não apontar silenciosamente à linha errada.

**Somente quando o estudante quiser abrir um editor**, escolha um item:

```powershell
# Sobrescrita criar() e criação do Asteroide:
.\scripts\Open-DefenseSources.ps1 -Pattern FactoryMethod -Item 2 -Editor VSCode
# Callback JavaFX: o item 5 começa na inscrição, logo acima de aoMudar/runLater:
.\scripts\Open-DefenseSources.ps1 -Pattern Observer -Item 5 -Editor VSCode
# Notepad: o script informa a linha; use Ctrl+G e digite esse número manualmente:
.\scripts\Open-DefenseSources.ps1 -Pattern Memento -Item 3 -Editor Notepad
```

`-Editor Auto` (padrão) usa `code` se disponível, senão Notepad. VS Code recebe `--reuse-window --goto arquivo:linha`; Notepad abre só o arquivo, sem simular teclas. O script abre um item por chamada, não um conjunto de janelas. O editor permite edição humana normal; o script não grava as fontes. Duplo clique em **Abrir-Codigo-Defesa.cmd** abre o primeiro item de Builder; no terminal, o wrapper aceita os mesmos argumentos, por exemplo `./Abrir-Codigo-Defesa.cmd -Pattern Observer -ListOnly`.

A abertura de editores foi preparada para invocação explícita do usuário, **não executada nesta revisão**. Foram testadas as quatro listagens, sem abrir UI global, Java, Maven ou simulador.

## Builder — quem constrói a Terra

| Item do script | Fonte / linha de entrada | Método e explicação a mostrar |
|---|---|---|
| 1 | [TerraBuilder.java:13](../src/main/java/com/projekttellus/domain/TerraBuilder.java#L13) | `nome(String)` guarda a configuração e devolve `this`, a mesma instância do builder; os demais setters seguem essa forma. |
| 2 | [TerraBuilder.java:20](../src/main/java/com/projekttellus/domain/TerraBuilder.java#L20) | `build()` devolve uma nova `Terra`, com UUID novo e `EstadoPlanetario` composto pelos valores reunidos. |
| 3 | [BuilderDialog.java:30](../src/main/java/com/projekttellus/ui/BuilderDialog.java#L30) | `setResultConverter`: ao confirmar, lê/valida o formulário e devolve um `TerraBuilder`; cancelar devolve `null` e não cria o mundo. |
| 4 | [SimulacaoService.java:32](../src/main/java/com/projekttellus/application/SimulacaoService.java#L32) | `criar(TerraBuilder)` chama `build()`, captura “Origem do mundo” e retorna `ResultadoOperacao` após a confirmação do serviço. |
| 5 | [MainController.java:154](../src/main/java/com/projekttellus/ui/MainController.java#L154) | `createWorld()` abre o diálogo; `showAndWait().ifPresent` encaminha o builder ao serviço. |

**Entrada/retorno:** setters recebem nome ou componentes tipados e retornam o builder; `build()` retorna Terra. Os construtores do domínio validam gases, intervalos e componentes; o builder não duplica todas as regras. A tela não é um Director formal e não há Builder abstrato. A solução reduz montagem repetida; sem ela seria possível chamar diretamente os construtores, com mais coordenação no cliente.

**Fala curta:** “Eu separo reunir condições de construir a Terra. `return this` permite encadear a configuração; o objeto final nasce em `build()` e só depois é persistido pelo serviço.” Demo isolada: abrir **＋ Criar mundo**; não precisa mostrar catástrofe.

## Factory Method — criação e polimorfismo

| Item | Fonte / linha | Método e explicação a mostrar |
|---|---|---|
| 1 | [EventoCreator.java:6](../src/main/java/com/projekttellus/events/EventoCreator.java#L6) | `criar(double)` é abstrato e retorna `EventoPlanetario`. |
| 2 | [AsteroideCreator.java:5](../src/main/java/com/projekttellus/events/AsteroideCreator.java#L5) | `@Override criar(double)` faz literalmente `return new Asteroide(intensidade);`. |
| 3 | [EventoPlanetario.java:6](../src/main/java/com/projekttellus/events/EventoPlanetario.java#L6) | O contrato `aplicar(EstadoPlanetario)` retorna outro estado planetário. |
| 4 | [EventoBase.java:11](../src/main/java/com/projekttellus/events/EventoBase.java#L11) | `aplicar` é `final`; intensidade zero devolve o estado recebido, caso contrário delega a `transformar`. |
| 5 | [Asteroide.java:6](../src/main/java/com/projekttellus/events/Asteroide.java#L6) | `transformar` implementa os efeitos específicos do asteroide. |
| 6 | [SimulacaoService.java:50](../src/main/java/com/projekttellus/application/SimulacaoService.java#L50) | `aplicar(Terra, EventoCreator, double)` chama `creator.criar` na linha 52 e `evento.aplicar` na linha 55. `var evento` infere `EventoPlanetario`, sem depender de Asteroide. |
| 7 | [MainController.java:214](../src/main/java/com/projekttellus/ui/MainController.java#L214) | `applyEvent()` lê a seleção real e divide o slider por 100. |

**Duas escolhas dinâmicas:** a subclasse do criador resolve qual produto construir; no produto, a transformação concreta dá o comportamento específico. Asteroide herda `aplicar` de EventoBase; não diga que cada evento sobrescreve esse método `final`. O `@Override` do criador permite ao compilador verificar a sobrescrita. O retorno por interface preserva um contrato comum, embora o objeto real seja concreto. Não é uma fábrica estática com um `switch` dentro do serviço.

**Ordem real do evento:** criar produto → capturar estado anterior → aplicar efeitos no candidato → calcular indicadores → salvar candidato/registro/retrato juntos no SQLite → atualizar Terra em memória → publicar mudança → construir/devolver `ResultadoOperacao` → callback da UI apresenta o resultado. O novo registro e o snapshot são objetos transitórios antes do commit; sua criação em memória não prova persistência. Para um sexto evento, criar os tipos e registrá-lo na seleção da UI ([MainController.java:110](../src/main/java/com/projekttellus/ui/MainController.java#L110)); o mapeamento visual em `AnimationKind.forCreator` também deve ser considerado, sem prometer que só uma classe nova basta.

**Fala curta:** “Eu tenho cinco eventos e deixo cada subclasse criar seu produto. O serviço pede um `EventoPlanetario` e o aplica sem carregar as fórmulas concretas.” Demo isolada: mundo-base carregado → selecionar evento → **Aplicar evento**.

## Observer — recebimento e renderização

| Item | Fonte / linha | Contrato ou etapa |
|---|---|---|
| 1 | [SimulacaoObserver.java:4](../src/main/java/com/projekttellus/patterns/observer/SimulacaoObserver.java#L4) | `void aoMudar(MudancaPlanetaria)` recebe mundo/estado/indicadores/causa; não devolve resultado nem confirmação visual. |
| 2 | [ObserverBus.java:10](../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java#L10) | `inscrever` guarda o assinante e devolve `AutoCloseable` para removê-lo; não inicia thread. |
| 3 | [ObserverBus.java:16](../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java#L16) | `publicar` percorre assinantes; `observer.aoMudar(mudanca);` está na linha 22. Retorna `Entrega(notificados, falhas)`. |
| 4 | [SimulacaoService.java:109](../src/main/java/com/projekttellus/application/SimulacaoService.java#L109) | A publicação vem depois de `repository.salvar` e da atualização da Terra. Os passos relatam a entrega realizada. |
| 5 | [MainController.java:128](../src/main/java/com/projekttellus/ui/MainController.java#L128) | Inscrição do objeto anônimo. `aoMudar`, linha 130, agenda `Platform.runLater`, linha 131. |
| 6 | [AlertaObserver.java:11](../src/main/java/com/projekttellus/patterns/observer/AlertaObserver.java#L11) | Callback calcula mensagens por limiares; pode produzir “Condições estáveis”. |
| 7 | [EstatisticasObserver.java:9](../src/main/java/com/projekttellus/patterns/observer/EstatisticasObserver.java#L9) | Callback incrementa contagem/min/max por UUID no mapa da sessão. |
| 8 | [MainController.java:296](../src/main/java/com/projekttellus/ui/MainController.java#L296) | `renderObserved` preenche os indicadores e a caixa da sessão na FX thread. |
| 9 | [MainController.java:309](../src/main/java/com/projekttellus/ui/MainController.java#L309) | `run` executa o caso de uso no worker e agenda seu próprio callback de sucesso/erro na FX thread. |

**Síncrono não significa pixels imediatos:** o bus chama os três callbacks em sequência, na thread publicadora; nas ações do controller, essa é a thread do worker. Monitor e estatísticas executam ali. O terceiro callback agenda a tarefa JavaFX e retorna `void`. Quando `Entrega.notificados` contém o nome desse assinante, o callback já retornou sem `RuntimeException`; isso não prova que a tarefa FX executou ou que o frame foi desenhado. A animação também é apresentação posterior, não a operação do domínio sendo repetida.

**Se um assinante falhar:** o bus captura `RuntimeException`, registra o nome em `falhas` e continua com os próximos. O serviço mantém o resultado persistido e acrescenta uma etapa de atualização auxiliar incompleta. Não promete capturar qualquer `Error`; uma falha posterior dentro da tarefa enfileirada por `Platform.runLater` não volta ao `try/catch` síncrono do bus. As estatísticas podem refletir esse estado já confirmado independentemente de problema visual.

**Fala curta:** “O serviço publica uma mudança depois de salvar. O monitor, as estatísticas e o assinante da interface a recebem pelo mesmo contrato. A entrega do bus é síncrona; o desenho da interface é agendado separadamente.” Demo isolada: temperatura 15 → 30 → **Aplicar condições**, sem asteroide.

## Memento — condições anteriores e identidade

| Item | Fonte / linha | Método e explicação a mostrar |
|---|---|---|
| 1 | [Terra.java:23](../src/main/java/com/projekttellus/domain/Terra.java#L23) | `capturar(String)` cria/devolve TerraMemento com UUID próprio, mundoId, rótulo, nome, instante e estado. |
| 2 | [TerraMemento.java:10](../src/main/java/com/projekttellus/patterns/memento/TerraMemento.java#L10) | Record público. `estado()` é consultável; imutabilidade não implica representação oculta. |
| 3 | [Terra.java:26](../src/main/java/com/projekttellus/domain/Terra.java#L26) | `restaurar(TerraMemento)` retorna `void`; rejeita mundoId diferente antes de atribuir nome/estado. |
| 4 | [SimulacaoService.java:74](../src/main/java/com/projekttellus/application/SimulacaoService.java#L74) | `capturar` persiste registro/retrato com `notificar=false`; não cria mudança de condições. |
| 5 | [SimulacaoService.java:66](../src/main/java/com/projekttellus/application/SimulacaoService.java#L66) | `restaurar` valida num candidato, preserva “Antes da restauração” e confirma com notificação. |
| 6 | [SqliteMundoRepository.java:84](../src/main/java/com/projekttellus/persistence/SqliteMundoRepository.java#L84) | `salvar` coordena gravação do mundo, registro e retrato opcional na mesma transação. |
| 7 | [TransacaoSqlite.java:13](../src/main/java/com/projekttellus/persistence/TransacaoSqlite.java#L13) | `executar`: commit na linha 19, tentativa de rollback na linha 23; fecha conexão se não puder recuperar o rollback. |
| 8 | [MainController.java:228](../src/main/java/com/projekttellus/ui/MainController.java#L228) | `restoreSnapshot` usa o retrato selecionado e pede confirmação, sem assumir último índice. |
| 9 | [MainController.java:256](../src/main/java/com/projekttellus/ui/MainController.java#L256) | `operate` trata a releitura das listas separadamente do resultado confirmado. |

Terra é o originador, TerraMemento é o retrato, SimulacaoService é o caretaker e MundoRepository armazena os retratos. O grafo de [EstadoPlanetario, linha 5](../src/main/java/com/projekttellus/domain/EstadoPlanetario.java#L5) é composto por valores imutáveis; compartilhar essa referência no retrato não cria um estado mutável compartilhado. SQLite dá durabilidade ao Memento, mas o padrão não é definido por usar banco.

**O que volta:** nome, atmosfera, clima, água/gelo, vegetação, população e ano. A identidade do mundo permanece. **O que não volta:** lista de eventos, conjunto de retratos, contagem/min/max do EstatisticasObserver, estado dos controles/câmera ou outros mundos. Indicadores e alertas são recalculados a partir das condições restauradas; estatísticas da sessão recebem outra mudança. A história cresce em vez de ser apagada. Na demo anterior, humanos voltaram a 100%, mas a sessão continuou com mínimo 64,7% e três mudanças. Isso é o comportamento esperado.

**Fala curta:** “Eu restauro condições, não desfaço a história do sistema. O mundo conserva sua identidade, e o retrato completo evita tentar calcular um evento inverso.” Demo isolada: base manual → mudar temperatura → restaurar a base.

## Persistência e callbacks — resposta para falhas

Abra [SimulacaoService.confirmar, linha 99](../src/main/java/com/projekttellus/application/SimulacaoService.java#L99) e depois [MainController.operate, linha 256](../src/main/java/com/projekttellus/ui/MainController.java#L256). A distinção abaixo evita prometer rollback de algo já salvo.

| Situação | O que ocorre no código atual |
|---|---|
| Falha em `repository.salvar` durante evento | A exceção interrompe `confirmar` antes de atualizar a Terra e publicar no bus; não há ResultadoOperacao bem-sucedido nem `Completed` retornado por essa ação. Se a ação SQL ou o commit lançar exceção, a transação tenta rollback; se o rollback também falhar, fecha a conexão quando necessário. Isso não garante recuperação de uma falha do próprio armazenamento. |
| Callback de erro do controller | `run`, linhas 318–328, captura `Exception` no worker e agenda a mensagem de erro por `Platform.runLater`. Limpa `busy`, informa que a ação não foi concluída e abre o diálogo. Não chama `success.accept`; a animação do novo resultado não é iniciada. Não promete tratar todo `Error`. |
| Gravação concluída, mas `library(...)` falha ao reler listas | `operate`, linhas 259–264, captura a `RuntimeException` da releitura e **retorna `new Completed(result, null)`**, mantendo o resultado salvo. O callback de sucesso recebe esse Completed, atualiza o mundo/apresentação e inicia o animador; limpa histórico/retratos na interface e informa “Confirmado e salvo” mais o aviso de que não pôde atualizar as listas. Não chama isso de falha da gravação. |
| Falha de um observador após salvar | O tratamento de `RuntimeException` no bus isola aquele assinante; não desfaz o commit. A entrega e os passos registram a falha auxiliar. |
| Falha dentro de callback FX posterior | A chamada assíncrona já foi agendada; a exceção não retorna automaticamente ao worker ou ao bus e não produz rollback da gravação. Não há transação distribuída entre SQLite, memória e desenho. |

Há ainda um limite preciso no `finally` de `TransacaoSqlite`: se o commit concluiu e só a restauração de autocommit falhou, o método fecha a conexão e lança exceção apesar da gravação já confirmada. Portanto, a mensagem genérica de erro da UI não prova, sozinha, que nada foi gravado; esse caso exige reabrir e conferir o banco. Não atribua ao código uma garantia de atomicidade entre banco, objeto e renderização que ele não implementa.

**Quem retorna o quê:** o serviço devolve `ResultadoOperacao` ([record, linha 7](../src/main/java/com/projekttellus/application/ResultadoOperacao.java#L7)); a `Callable` interna de `operate` devolve `Completed(result, libraryOuNull)` ao `run`. O callback `Consumer<T>.accept` de sucesso retorna `void`, assim como `SimulacaoObserver.aoMudar` e a tarefa `Runnable` passada a `Platform.runLater`. `ObserverBus.publicar`, por outro lado, devolve o recibo `Entrega`. Não use a palavra “callback retornou o planeta” para essas chamadas `void`.

Para a arguição, explique a falha nas linhas acima. Não corrompa banco ou remova permissão durante a demonstração. O banco do ensaio é temporário; os saves pessoais permanecem fora deste percurso.

## Slides, UML e contingência

Edição atual: [PPTX r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pptx) e [PDF r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pdf). Permanecem disponíveis o [PPTX r2 preservado](apresentacao/ProjektTellus-Apresentacao-r2.pptx) e o [PDF anterior](apresentacao/ProjektTellus-Apresentacao.pdf). Não altere a transcrição histórica que cita a Prof. Célia; a identificação desta defesa usa EDER MAGALHAES MACHADO.

| Pedido do professor | Par de slides / UML acessível | Código e demonstração de apoio sem JavaFX |
|---|---|---|
| Builder | 4–5 / [Builder.png](uml-apresentacao/Builder.png) | Item Builder 2; [diálogo real anterior](capturas/run-20261115-124710/01-builder-dialogo.png). |
| Factory Method | 6–7 / [Factory-Method.png](uml-apresentacao/Factory-Method.png) | Itens FactoryMethod 2 e 6; [impacto anterior](capturas/run-20261115-124710/evento-1-asteroid-meio.png). |
| Observer | 8–9 / [Observer.png](uml-apresentacao/Observer.png) | Itens Observer 3 e 5; [etapa notificada anterior](capturas/run-20261115-124710/09-apresentacao-observer.png). |
| Memento | 10–11 / [Memento.png](uml-apresentacao/Memento.png) | Itens Memento 1 e 3; [restauração anterior](capturas/demo-restaurado.png). |

Se o aplicativo não abrir, mantenha a conversa no padrão solicitado: propósito → problema/solução do projeto → UML → método real → evidência anterior. Os pares mantêm acesso aos quatro requisitos de cada padrão; a versão curta não os elimina. Para o fluxo completo use o slide 12, “Os quatro padrões no fluxo do simulador”, quando atualizado na revisão, e a [demo integrada preservada](DEMO_APRESENTACAO.md#roteiro-integrado-preservado).

O [vídeo preservado](apresentacao/ProjektTellus-Demo-Capturas.mp4) é uma sequência de capturas reais, não gravação contínua ou prova de FPS. [CSV dos estados](capturas/run-20261115-124710/measurements.csv) e [valores por imagem](capturas/run-20261115-124710/visual-values.csv) permitem verificar os números sem JavaFX. Não anuncie a simples consulta a esses arquivos como nova execução de testes. Faculdade física, projetor e abertura dos slides no Microsoft PowerPoint continuam sem validação nesta frente.

## Verificação desta revisão

`-ListOnly` passou nas quatro trilhas também no Windows PowerShell usado pelo wrapper: Builder 5 entradas, FactoryMethod 7, Observer 9 e Memento 9; 30 âncoras únicas ao todo. O wrapper `.cmd` foi exercitado com `-Pattern Memento -ListOnly`. Foram conferidos 84 links locais dos dois documentos, incluindo 47 links Java com linha; os hashes de 72 arquivos de `src/main` permaneceram iguais antes/depois da validação de listagem. Nenhum editor, simulador, JavaFX ou Maven foi aberto/executado. A abertura por VS Code/Notepad permanece uma opção para invocação explícita do estudante, sem teste visual nesta revisão.
