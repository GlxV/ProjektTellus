# Perguntas e respostas para a apresentação

Banco de **62 perguntas de apoio** (36 aprofundamentos e 26 perguntas explicitamente solicitadas pelo usuário), preparado com IA nesta tarefa a partir do Java real e dos requisitos/complementos do usuário. Origem dos temas: exigências do enunciado, ordem de slides e correções conceituais fornecidas pelo usuário; formulação abaixo e respostas: material de apoio elaborado nesta frente. Não atribuir estas frases a uma lista histórica do usuário ou a perguntas efetivamente feitas pela docente. O objetivo é o estudante conseguir explicar o código, como exige o enunciado.

Caminhos nesta página são relativos a `src/main/java/com/projekttellus/`, salvo indicação. “Evidência” indica fonte inspecionada e ação demonstrável, não uma alegação de teste executado. A frente acadêmica não executou Maven, testes nem GUI. Ver [ESTUDO_PADROES.md](ESTUDO_PADROES.md) para recortes literais e [CONFORMIDADE_TRABALHO.md](CONFORMIDADE_TRABALHO.md) para exigências.

## 1. Por que escolher esses quatro padrões?

**Resposta:** o software tem quatro problemas distintos: reunir condições iniciais, criar diferentes eventos, comunicar mudanças e recuperar estados anteriores. Builder, Factory Method, Observer e Memento têm responsabilidades correspondentes no Java. O enunciado exige ao menos três; utilizar quatro não obriga a reivindicar padrões adicionais.

**Evidência:** TerraBuilder; EventoCreator/concretos; ObserverBus; Terra/TerraMemento/SimulacaoService. Apontar os pares de slides 4–11.

**Cuidado:** nomear classes com nomes de padrões não prova sua aplicação; explicar as chamadas e o problema real.

## 2. Qual é o problema real que Builder resolve?

**Resposta:** uma Terra tem vários componentes e condições opcionais. TerraBuilder centraliza defaults e permite configurar componentes por nomes antes de reunir EstadoPlanetario e construir Terra. Evita espalhar construtores posicionais longos na coleta de condições.

**Evidência:** `domain/TerraBuilder.java`; `ui/BuilderDialog.java`, conversor do resultado do diálogo.

**Cuidado:** Builder constrói; o serviço persiste. O botão reúne essas etapas para o usuário, mas as classes continuam separadas.

## 3. Onde estão Director e interface abstrata de Builder?

**Resposta:** não existem nesta implementação. É um Builder concreto e fluente. A seleção das etapas é feita pelo cliente, como o diálogo; não há razão para inventar uma classe Director no UML.

**Evidência:** declaração `public final class TerraBuilder`, sem implements/extends; métodos devolvem TerraBuilder.

**Cuidado:** não dizer que qualquer encadeamento de métodos, por si só, comprova Builder. O argumento é a configuração separada seguida da construção do produto composto.

## 4. Quando o cenário é validado?

**Resposta:** componentes como Atmosfera e Hidrosfera validam seus limites nos construtores. EstadoPlanetario verifica componentes não nulos e tempo; Terra valida o nome e referências. Os setters de TerraBuilder atribuem valores, sem repetir toda a validação. Assim, uma Atmosfera inválida pode ser rejeitada antes de chegar ao Builder.

**Evidência:** `domain/Atmosfera.java`, `Hidrosfera.java`, `EstadoPlanetario.java`, `Terra.java` e `Validacao.java`.

**Cuidado:** `atmosfera(null)` pode guardar null no Builder; a falha acontece ao montar EstadoPlanetario. A UI tem validações para facilitar o uso, mas não substitui o domínio.

## 5. Por que retornar this? Builder e produto são o mesmo objeto?

**Resposta:** return this retorna o mesmo objeto Builder para configurar a próxima etapa. O produto é outra instância, criada por build. Cada build cria uma nova Terra e um novo UUID; o Builder continua disponível e não é resetado automaticamente.

**Evidência:** `TerraBuilder.atmosfera` e `TerraBuilder.build`.

**Cuidado:** o Builder é mutável e não tem garantia de uso concorrente; os valores de domínio fornecidos a ele são imutáveis.

## 6. Duas Terras podem compartilhar objetos? Isso viola independência?

**Resposta:** podem compartilhar referências a componentes ou EstadoPlanetario imutáveis. A independência é de identidade e mudanças: cada Terra tem seu UUID, e atualizar uma substitui sua referência, sem editar o valor compartilhado. Compartilhar valor imutável não é compartilhar estado mutável.

**Evidência:** `Terra.atualizar`; `SimulacaoService.duplicar` cria UUID novo a partir do mesmo estado; teste escrito `DomainTest.builderProducesIndependentCoherentEarths`.

**Cuidado:** não afirmar que cada componente é clonado nem desenhar composição exclusiva do estado por Terra.

## 7. Por que há UUID aleatório num simulador determinístico?

**Resposta:** UUID identifica mundo, retrato e registro; não participa do cálculo dos efeitos ambientais. O determinismo se refere a ModeloPlanetario e às transformações: mesma entrada e mesmo intervalo/evento produzem os mesmos valores. Instantes e ids de registros naturalmente podem mudar.

**Evidência:** `TerraBuilder.build`, `Terra.capturar` versus `simulation/ModeloPlanetario.java`.

**Cuidado:** não comparar o objeto Terra inteiro, incluindo UUID/timestamp, como se fosse uma saída numérica determinística.

## 8. Builder e Factory Method fazem a mesma coisa?

**Resposta:** ambos são de criação, mas resolvem problemas diferentes. Builder configura por etapas uma Terra composta; Factory Method deixa subclasses escolherem o tipo de EventoPlanetario criado. O Builder não seleciona por herança entre cinco produtos, e o Creator não reúne os componentes de Terra.

**Evidência:** `TerraBuilder.build` e `EventoCreator.criar` com suas sobrescritas.

**Cuidado:** métodos chamados criar ou build sozinhos não determinam a classificação; observar as responsabilidades e relações.

## 9. Por que é Factory Method? Quais são os cinco pares?

**Resposta:** EventoCreator declara criar abstrato, sobrescrito em AsteroideCreator→Asteroide, SupervulcaoCreator→Supervulcao, GuerraNuclearCreator→GuerraNuclear, InvasaoAlienigenaCreator→InvasaoAlienigena e RestauracaoEcologicaCreator→RestauracaoEcologica. Cada produto é utilizável como EventoPlanetario por meio de EventoBase.

**Evidência:** os dez arquivos correspondentes sob `events/`; UML `docs/uml-apresentacao/06-factory-method.mmd`.

**Cuidado:** há subclasses reais escolhendo o produto; não representar isso como uma única fábrica estática com switch.

## 10. Usar new dentro do criador não anula a fábrica?

**Resposta:** não. A instanciação precisa ocorrer em algum lugar; o Creator concreto concentra a escolha do produto. O cliente SimulacaoService recebe EventoCreator e chama criar sem instanciar ou decidir diretamente cada produto. O new é a implementação da decisão da subclasse.

**Evidência:** `AsteroideCreator.criar` e `SimulacaoService.aplicar`.

**Cuidado:** MainController conhece o catálogo de Creators para montar a seleção. O desacoplamento do serviço não significa que o sistema inteiro desconheça todas as classes concretas.

## 11. O que mudaria para incluir um sexto evento?

**Resposta:** implementar produto com contrato EventoPlanetario, normalmente estendendo EventoBase, implementar seu Creator e registrá-lo no ComboBox da UI; acrescentar cobertura de comportamento e eventual mapeamento visual. A lógica genérica de aplicação do serviço não precisa selecionar o novo tipo.

**Evidência:** lista explícita em `MainController.initialize`; `EventoBase`; famílias existentes.

**Cuidado:** não prometer “extensão sem alterar arquivo algum”. O catálogo e a apresentação são pontos reais de composição.

## 12. O que acontece com intensidade zero ou inválida?

**Resposta:** EventoBase aceita somente intensidade finita em [0,1]. Zero retorna a mesma referência de EstadoPlanetario; não produz efeito ambiental. Mesmo assim, o serviço pode registrar a ação, capturar retrato e notificar o fluxo da operação. Na UI, 25% é convertido em 0,25.

**Evidência:** `EventoBase` e `MainController.applyEvent`; teste escrito `SimulationTest.realFactoriesCreateIndependentPolymorphicEvents`.

**Cuidado:** “zero não muda condições” não significa “nenhum efeito no histórico ou nos contadores de sessão”.

## 13. Quem realmente aplica os efeitos e quem grava?

**Resposta:** o produto aplica sua transformação por EventoBase.aplicar/transformar e retorna um EstadoPlanetario. O serviço cria uma Terra candidata com esse estado, calcula indicadores e pede ao repositório que grave. Terra atual é atualizada somente depois do retorno normal da gravação.

**Evidência:** `events/Asteroide.java`, `Efeitos.java`, `application/SimulacaoService.aplicar/confirmar`.

**Cuidado:** o evento não altera a GUI, não acessa SQLite e não modifica internamente os records de entrada.

## 14. Esses efeitos têm validade científica? A invasão é realista?

**Resposta:** o modelo é didático, global e não calibrado cientificamente. Coeficientes e capacidade de suporte são escolhas da simulação. A invasão alienígena é explicitamente ficcional. Indicadores e gráficos mostram comportamento do programa, sem certificar sobrevivência, clima ou biologia do mundo real.

**Evidência:** comentário de `InvasaoAlienigena`; `ModeloPlanetario` e `docs/MODELO.md`.

**Cuidado:** radiação e partículas são índices abstratos; água+gelo representa cobertura de área, não conservação de massa em kg.

## 15. Quem é o Subject no Observer?

**Resposta:** ObserverBus é o publicador que guarda assinantes e entrega MudancaPlanetaria pelo contrato SimulacaoObserver. Terra não mantém uma lista de observadores. SimulacaoService coordena a mudança e chama o bus após persistir e atualizar o objeto vivo.

**Evidência:** `patterns/observer/ObserverBus.java`; `SimulacaoService.confirmar`.

**Cuidado:** usar um bus separado é uma escolha desta implementação; não desenhar métodos inscrever/publicar dentro de Terra no UML.

## 16. Quem são os observadores? MainController implementa a interface?

**Resposta:** AlertaObserver calcula recomendações; EstatisticasObserver conta mudanças e extremos por mundo. MainController cria e inscreve uma implementação anônima de SimulacaoObserver para atualizar a UI. MainController em si implementa AutoCloseable, não SimulacaoObserver.

**Evidência:** construtor de `SimulacaoService` e registro da subscription em `MainController.initialize`.

**Cuidado:** no serviço sem UI são dois observadores padrão; na aplicação com assinatura gráfica, três. Não fixar a quantidade como parte obrigatória do padrão.

## 17. Observer significa que os callbacks são assíncronos?

**Resposta:** não. ObserverBus chama aoMudar de forma síncrona. O callback gráfico agenda um Runnable com Platform.runLater; isso faz a atualização da UI acontecer depois, na thread FX. A assinatura do contrato não exige paralelismo.

**Evidência:** `ObserverBus.publicar` e implementação anônima em `MainController`.

**Cuidado:** o recibo notificados mostra retorno do callback. Para a UI, confirma agendamento, sem provar que o render visual já terminou.

## 18. E se um observador lançar exceção?

**Resposta:** RuntimeException em um callback é registrada como falha, e o bus prossegue para os demais. O serviço já gravou a mudança, então não desfaz o mundo por essa falha auxiliar. Metadados de nome defeituosos também recebem tratamento. A captura não é universal para todo Error.

**Evidência:** catch em `ObserverBus.publicar`, `nomeSeguro` e tratamento de Entrega em `SimulacaoService.confirmar`.

**Cuidado:** não prometer entrega garantida, retry persistente ou atomicidade entre banco e assinantes. A lista de falhas precisa ser diferenciada da de notificados.

## 19. E se o observador for lento? CopyOnWrite torna tudo concorrente?

**Resposta:** um callback lento retarda a publicação síncrona e a conclusão da operação. CopyOnWriteArrayList protege a iteração frente a mudanças de inscrição; não torna automaticamente cada objeto thread-safe. No app, o worker único serializa os casos de uso e acessos ao repositório.

**Evidência:** campo observers no bus e executor de `MainController`.

**Cuidado:** listas copy-on-write têm custo nas alterações de assinaturas; são adequadas ao padrão atual de poucas inscrições e várias publicações, sem ser uma garantia universal de desempenho.

## 20. Como evitar assinante esquecido depois de fechar a tela?

**Resposta:** inscrever devolve AutoCloseable que remove a inscrição. MainController guarda subscription e enfileira subscription.close e fechamento do repository no mesmo worker durante close, atrás das operações em andamento. A flag closed impede atualizações da tela após o fechamento.

**Evidência:** `ObserverBus.inscrever/remover`; `MainController.close`.

**Cuidado:** não fechar o repositório enquanto uma gravação está em andamento e não bloquear a thread FX esperando esse trabalho.

## 21. Salvar, capturar retrato ou carregar mundo notificam Observer?

**Resposta:** salvar e captura manual passam notificar=false no serviço; não representam nova alteração ambiental. Carregar consulta o repositório e a UI renderiza o estado sem usar a publicação de uma nova mudança pelo serviço. Criar, aplicar, alterar, avançar, renomear, duplicar e restaurar usam notificação no fluxo atual.

**Evidência:** `SimulacaoService.salvar/capturar` e `MainController.loadWorld/renderCurrent`.

**Cuidado:** não fabricar passos “Sistema notificado” para uma operação que não os produziu. Renderizar um mundo carregado não prova publicação no bus.

## 22. Como impedir que estatísticas e alertas misturem mundos? Eles persistem?

**Resposta:** as projeções usam mapas indexados por UUID do mundo. Estatísticas registra contagem e extremos durante a sessão; o monitor guarda a lista mais recente de alertas. Eles não são tabelas de histórico persistidas nem são reconstituídos automaticamente ao abrir o banco.

**Evidência:** ConcurrentHashMap em `AlertaObserver` e `EstatisticasObserver`; `MainController.renderCurrent`.

**Cuidado:** histórico de eventos no SQLite e estatística de sessão são dados de finalidades diferentes. Reabrir o app não preserva os contadores em memória.

## 23. Quem é Originator, Memento e Caretaker?

**Resposta:** Terra é originador porque captura e restaura seu nome/estado. TerraMemento é o retrato. SimulacaoService é caretaker porque decide os checkpoints, coordena a restauração e pede persistência. MundoRepository guarda e consulta retratos, como infraestrutura de armazenamento.

**Evidência:** `domain/Terra.java`; `patterns/memento/TerraMemento.java`; `SimulacaoService.restaurar`.

**Cuidado:** não substituir o caretaker pelo banco nem confundir o padrão com o formato de armazenamento.

## 24. O Memento é opaco? O caretaker consegue ler o estado?

**Resposta:** o record TerraMemento é público e oferece estado(), logo permite leitura por clientes e caretaker. É uma variante legível: a proteção é contra mutação dos valores, não contra sua consulta. Não corresponde à opacidade estrita de certas descrições clássicas GoF.

**Evidência:** declaração pública do record e seu componente EstadoPlanetario.

**Cuidado:** dizer “ninguém fora de Terra consegue ler” seria falso. Restrição de UUID é consistência de domínio, não controle de acesso a informação sensível.

## 25. Por que não clonar profundamente o estado ao capturar?

**Resposta:** o grafo contém records com valores primitivos e componentes imutáveis, além de UUID, String e Instant imutáveis. Terra troca a referência de estado, e um retrato continua apontando para o valor antigo que ninguém pode modificar internamente. Assim, compartilhamento é seguro sem deep copy.

**Evidência:** `EstadoPlanetario` e os cinco componentes; `Terra.capturar/atualizar`.

**Cuidado:** um record não é sempre profundamente imutável: se contivesse lista mutável exposta, precisaria de cópia defensiva ou outro contrato. A garantia vem deste grafo concreto.

## 26. O que exatamente o retrato restaura? O que não volta?

**Resposta:** recupera nome do mundo e EstadoPlanetario completo: atmosfera, clima, biosfera, hidrosfera, civilização e anos. Não troca o id da Terra. Histórico, estatísticas da sessão, câmera, globo animado e preferências de interface não são parte do estado capturado.

**Evidência:** componentes do record TerraMemento e atribuições finais de `Terra.restaurar`.

**Cuidado:** recuperar valores do mundo não é apagar todas as ações posteriores nem restaurar a sessão inteira do aplicativo.

## 27. Posso usar o snapshot de outro mundo ou de uma cópia?

**Resposta:** não no fluxo atual. Terra.restaurar compara seu id ao mundoId do retrato e rejeita diferença. O serviço faz essa checagem na candidata antes de gravar. Duplicar cria outro UUID e retrato próprio, mesmo se os valores iniciais forem iguais.

**Evidência:** `Terra.restaurar`, `SimulacaoService.restaurar/duplicar`; teste escrito `ApplicationTest.foreignMementoRejectedBeforeAnyHistoryOrStateChanges`.

**Cuidado:** igualdade de EstadoPlanetario não implica identidade de mundo. A rejeição não é mecanismo de autenticação de usuário.

## 28. Se já há SQLite, por que preciso de Memento? Isso é Event Sourcing?

**Resposta:** Memento define como capturar e recuperar um valor anterior, inclusive sem disco. SQLite permite manter esse valor entre sessões e listar retratos. Não é Event Sourcing: o mundo é carregado de seu estado persistido, sem reexecutar toda a história de eventos para reconstruí-lo.

**Evidência:** `Terra.capturar` não acessa banco; `SqliteMundoRepository.carregar/snapshots` reconstituem os componentes de colunas.

**Cuidado:** persistir qualquer dado não implementa automaticamente Memento; é preciso originador, retrato e recuperação coerentes.

## 29. Restauração ecológica e restaurar Memento são equivalentes?

**Resposta:** não. RestauracaoEcologica transforma o estado atual com intensidade e aproxima algumas condições de alvos, sem ressuscitar população. Memento recupera exatamente os valores do retrato. Uma transformação “boa” não é a inversa matemática de todas as catástrofes.

**Evidência:** `events/RestauracaoEcologica.java` versus `Terra.restaurar`.

**Cuidado:** o botão de evento e a ação de restaurar retrato podem melhorar a aparência, mas têm semânticas e fontes de dados diferentes.

## 30. Há desfazer/refazer automático? O histórico some quando restauro?

**Resposta:** há seleção explícita de retratos, não uma pilha genérica de undo/redo por atalho. Ao restaurar, o serviço captura o estado que vai ser substituído com “Antes da restauração” e acrescenta o registro da restauração. O histórico não é apagado; pode-se selecionar o retrato de retorno depois.

**Evidência:** `SimulacaoService.restaurar` e chamada confirmar com o snapshot de retorno.

**Cuidado:** não prometer retenção ilimitada eficiente nem uma política automática de limpeza que o código não implementa.

## 31. Qual é a ordem correta da operação completa?

**Resposta:** Terra já foi criada pelo Builder. Ao aplicar: criar evento pelo Creator → capturar estado → aplicar ao estado e construir candidata → calcular indicadores → gravar candidata, registro e snapshot → atualizar Terra atual → publicar no bus → apresentar o resultado na UI. Restaurar é outra operação coordenada pelo serviço.

**Evidência:** `SimulacaoService.aplicar/confirmar`; `MainController.operate`.

**Cuidado:** o slide de integração não deve inverter Factory Method e captura se pretende representar chamadas de aplicar. Não confundir sequência de botões escolhida para a demo com ordem interna dos métodos.

## 32. Se o banco falhar, a Terra muda ou os observadores recebem a mudança?

**Resposta:** no fluxo observado, salvar ocorre antes de atualizar o destino e publicar. Uma exceção de gravação antes do retorno impede chegar a essas etapas. A implementação SQLite usa transação e rollback. Há teste escrito com trigger de falha para comparar memória, banco, histórico, retratos e zero notificações.

**Evidência:** `SqliteMundoRepository.salvar`; `TransacaoSqlite.executar`; `ApplicationTest.failedTransactionLeavesLiveEarthAndObserversUntouched`.

**Cuidado:** não prometer atomicidade distribuída entre SQLite, objeto em memória e pixels; não extrapolar um teste escrito para toda falha de hardware. Esta frente não executou esse teste.

## 33. Como provar persistência e independência na demo?

**Resposta:** usar banco temporário, construir dois mundos, registrar valores do controle, capturar o primeiro, aplicar evento e restaurar. Conferir valores e carregar o controle. Depois fechar/reabrir com o mesmo banco, carregar o mundo e consultar histórico/retratos. Captura visual da restauração sozinha não comprova durabilidade.

**Evidência:** propriedade `projekttellus.db` em MainController; chamadas reais carregar/historico/snapshots; teste escrito `ApplicationTest.fullDemonstrationSurvivesRestartAndKeepsWorldsIndependent`.

**Cuidado:** ensaiar antes; o computador da faculdade e sua configuração não foram validados por esta frente. Se usar registro de demo, identificá-lo como evidência registrada, sem fingir execução ao vivo.

## 34. Por que H, B e C diferem? Como o tempo avança?

**Resposta:** H mede adequação ambiental para humanos sem proteção; B admite formas hipotéticas associadas à água e não exige O₂; C combina ambiente, recursos continentais e ocupação do suporte K. São produtos de fatores, não probabilidades. O avanço usa n=ceil(anos), dt=anos/n≤1, decaimentos e aproximações graduais, recalculando condições e população.

**Evidência:** `ModeloPlanetario.calcular/avancar/evoluir` e `docs/MODELO.md`.

**Cuidado:** mesmo percentual de O₂ com pressão diferente muda a pressão parcial. Sem terra, K/C zeram, mesmo com ambiente favorável. Particionar o intervalo em chamadas diferentes pode alterar arredondamentos. População inteira pode deixar resíduos pequenos; no experimento sem O₂, esse achado deve ser qualificado por cenário/horizonte.

## 35. O que os testes e experimentos realmente demonstram?

**Resposta:** os testes escritos expressam contratos de construção, polimorfismo, entrega, persistência e restauração. A auditoria externa registra verificações numéricas de casos controlados; seu summary final informa 2.026 experimentos, 252.628 checagens e zero falhas, com ExitCode 0 no recibo. Isso não é contagem de testes JUnit nem validação científica. Esta frente só leu fontes e recibos; não executou Maven ou auditoria.

**Evidência:** testes em `src/test/java/com/projekttellus/`; `docs/auditoria/experimental-20261115-final/summary.json`, `execution.json` e `snapshots.csv`.

**Cuidado:** o slide de achados precisa corresponder aos CSVs. Não citar `docs/auditoria/experimentos.csv` se esse arquivo não existe. GUI, PDF/deck e ambiente da faculdade exigem evidências próprias.

## 36. E a autoria, uso de IA, identificação, datas e pesos?

**Resposta:** o texto integral do enunciado permite aproveitar ChatGPT, Gemini, Perplexity e outros e exige entender o código. Estes materiais tiveram apoio de IA nesta tarefa; não se infere quem escreveu anteriormente cada classe. Trabalho individual e uso no projeto conjunto com a disciplina da Prof. Célia são exigências, não fatos provados pelo repositório. A janela é 16–23/11/2026 e os pesos são 70% trabalho/30% prova. Nome do aluno e curso não foram informados; a capa deve preservar essa pendência.

**Evidência:** `docs/ENUNCIADO_ORIGINAL_TRANSCRITO.md`, fonte primária textual integral de duas páginas. Apenas PDF binário e dois anexos Sem título estão indisponíveis.

**Cuidado:** não inventar histórico de colaboração, identificação pessoal ou exigência de duração. 10–12 minutos é meta de ensaio do usuário; a compreensão individual deve ser demonstrada nas respostas.


## Perguntas originais solicitadas pelo usuário — respostas diretas

As 26 perguntas abaixo preservam os temas e a formulação da solicitação atual. As respostas complementam os aprofundamentos anteriores.

### 37. O que é um padrão de projeto?

É uma solução de organização de classes e responsabilidades para um problema recorrente em determinado contexto. Não é uma biblioteca nem código pronto: eu adapto seus participantes ao problema real. No projeto, EventoCreator e suas subclasses concretizam Factory Method; só dar esse nome à pasta não bastaria.

### 38. Por que escolher esses quatro padrões?

Eles correspondem às responsabilidades que consigo apontar no código: TerraBuilder reúne condições; EventoCreator varia a criação dos eventos; ObserverBus comunica mudanças; Terra captura e restaura TerraMemento. O enunciado permite os quatro e exige pelo menos três. Isso explica a adequação atual, sem inventar uma história de decisões anteriores.

### 39. Como Builder funciona?

BuilderDialog lê os componentes pelo EnvironmentEditor e encadeia chamadas em TerraBuilder. Os métodos guardam os valores e retornam this. SimulacaoService.criar chama build(), que reúne EstadoPlanetario e cria Terra com novo UUID. Defaults cobrem condições não substituídas; os records e Terra validam os dados.

### 40. Por que não usar apenas um construtor?

O construtor de Terra continua existindo. Builder separa a montagem dos vários componentes e oferece nomes nas chamadas, defaults e personalização opcional. Não é obrigatório para três argumentos simples; aqui reduz o trabalho do cliente ao montar atmosfera, clima, biosfera, hidrosfera, civilização e tempo. Não existe Director separado nesta variante.

### 41. Qual a diferença entre Factory Method e uma fábrica simples?

Uma fábrica simples costuma selecionar produtos num método com condicionais. Aqui EventoCreator declara criar(double) abstrato e AsteroideCreator, SupervulcaoCreator e os demais sobrescrevem esse método para instanciar seus produtos. A variação ocorre por subclasses e despacho polimórfico. MainController ainda mantém uma lista explícita dos criadores disponíveis.

### 42. Onde existe polimorfismo nos eventos?

SimulacaoService.aplicar recebe EventoCreator e chama creator.criar(intensidade). O objeto concreto decide a implementação. O resultado é EventoPlanetario, pelo qual o serviço chama aplicar(estado); EventoBase delega a transformar(), implementado por Asteroide ou pelo outro produto concreto. A mesma chamada usa comportamentos diferentes conforme o objeto.

### 43. Como funciona o Observer?

Depois de gravar e atualizar uma mudança confirmada, SimulacaoService publica MudancaPlanetaria. ObserverBus percorre os SimulacaoObserver inscritos e chama aoMudar. Monitor e estatísticas atualizam suas projeções por UUID; o assinante anônimo de MainController agenda a atualização gráfica no JavaFX.

### 44. O que é acoplamento?

É o grau de dependência entre componentes. O serviço depender de EventoPlanetario para aplicar um evento reduz seu conhecimento de cada produto concreto. ObserverBus conhece SimulacaoObserver, não as regras de alertas, estatísticas ou desenho da tela. As dependências continuam existindo, mas ficam concentradas em contratos menores.

### 45. Como os observadores são inscritos e notificados?

O construtor de SimulacaoService chama observerBus.inscrever(monitor) e inscrever(estatisticas). MainController inscreve outro objeto anônimo. inscrever usa addIfAbsent e devolve um AutoCloseable para remover o assinante. publicar percorre a lista, chama aoMudar e registra sucesso ou RuntimeException isoladamente. O laço é síncrono; Platform.runLater da UI é que agenda trabalho posterior.

### 46. Qual o propósito do Memento?

Guardar um estado anterior para recuperá-lo sem calcular uma operação inversa de cada catástrofe. Terra é o originador, TerraMemento carrega nome, estado, identidade e metadados; SimulacaoService coordena o uso como caretaker. O estado é profundamente imutável. Esta variante permite consultar os campos do snapshot; não promete opacidade estrita.

### 47. Qual a diferença entre Memento e persistência SQLite?

Memento é a representação do retrato e o comportamento de captura/restauração. SQLite guarda mundos, retratos e histórico entre processos. Um Memento pode existir apenas em memória; o repositório torna-o durável. Restaurar um retrato não apaga os registros de eventos nem volta as estatísticas da sessão para o passado.

### 48. Como a restauração protege a identidade dos mundos?

Terra.id é final. Terra.restaurar compara esse id com memento.mundoId e rejeita retrato estrangeiro antes de reassociar nome/estado. SimulacaoService restaura primeiro numa candidata do mesmo UUID e só grava após essa validação. Uma cópia tem novo id e não pode importar o retrato do original por essa API.

### 49. Onde existe encapsulamento?

Terra mantém id, nome e estado em campos privados e concentra alteração e restauração em métodos. EventoBase mantém intensidade privada e validada. Os records expõem valores por acessores, mas não setters; seu grafo imutável evita modificar o estado através de uma referência compartilhada. Encapsulamento aqui não significa que todo dado é secreto: TerraMemento é legível.

### 50. Onde existe herança?

AsteroideCreator extends EventoCreator, e os outros criadores seguem a mesma hierarquia. Asteroide extends EventoBase; os outros produtos também. EventoBase implements EventoPlanetario e os observadores implements SimulacaoObserver são realizações de interfaces, uma relação que deve ser distinguida da herança de implementação entre classes.

### 51. Onde existe abstração?

EventoPlanetario define nome, intensidade e aplicar sem exigir que o cliente conheça a fórmula de cada evento. EventoCreator define a operação de criação sem fixar o produto. SimulacaoObserver define a notificação; MundoRepository define operações de armazenamento sem impor JDBC ao serviço. Esses contratos tornam explícito o que o cliente pode solicitar.

### 52. Como o projeto segue os princípios da POO?

Objetos reúnem estado e responsabilidade: Terra possui identidade e estado, o Builder monta o produto, eventos transformam estados, observadores reagem. Campos privados e validação dão encapsulamento; interfaces e classes abstratas dão abstração; as hierarquias dos eventos dão herança; criar/aplicar/aoMudar oferecem polimorfismo. Os componentes ambientais são compostos por referência, com valores imutáveis compartilháveis.

### 53. Por que utilizar interfaces?

Para definir contratos substituíveis. EventoPlanetario permite diferentes produtos; SimulacaoObserver permite novas reações sem alterar o bus; MundoRepository separa o serviço da implementação SQLite. A interface não elimina toda alteração futura: um sexto evento ainda precisa ser registrado no catálogo da interface gráfica.

### 54. Por que JavaFX?

O aplicativo é desktop Java e usa controles, FXML, CSS e um globo com recursos 3D. JavaFX oferece esses recursos no mesmo ambiente de linguagem do trabalho. MainController coordena ações da interface; operações gráficas respeitam a thread JavaFX. Isso demonstra a adequação técnica presente; não permite afirmar quais alternativas o autor avaliou historicamente.

### 55. Como o SQLite foi integrado?

MundoRepository é o contrato; SqliteMundoRepository implementa acesso JDBC. SchemaSqlite prepara o esquema e TransacaoSqlite coordena commit/rollback. SimulacaoService envia candidato, RegistroEvento e TerraMemento na mesma chamada salvar. A propriedade projekttellus.db escolhe o caminho. Os testes e ensaios usam bancos temporários, separados dos saves pessoais.

### 56. Como o algoritmo de habitabilidade funciona?

ModeloPlanetario.calcular transforma pressão parcial de O₂/CO₂, pressão total, temperatura, água, radiação e partículas em adequações e multiplica esses fatores para H. B usa uma janela mais ampla, sem exigir O₂, e penaliza distância ao alvo climático. K combina continente, produtividade, água, H e raiz de B; C inclui esses recursos e a ocupação K/população. São escores didáticos, não probabilidades.

### 57. Qual a principal limitação científica do modelo?

Ele não foi calibrado com dados científicos nem representa bioquímica, clima regional ou tecnologia. B alto numa atmosfera de hélio não prova existência de vida. A reserva água/gelo conserva cobertura superficial abstrata, não massa física; não há evaporação. O arredondamento demográfico também cria patamares de poucas pessoas, achado numérico distinto dessas simplificações científicas.

### 58. O que acontece se um evento falhar?

Se criar/aplicar lançar antes da confirmação, não há gravação nem atualização pública do mundo pelo serviço. Se salvar falhar, a transação faz rollback e o serviço não avança para atualizar Terra/publicar. Falhas RuntimeException de observadores ocorrem depois da gravação e são registradas individualmente; não desfazem o estado confirmado. A animação apresenta o resultado salvo e não controla a física do evento.

### 59. Como os testes verificam o projeto?

JUnit exercita domínio, eventos, indicadores, evolução, Memento, observadores, SQL/rollback e JavaFX no perfil gui-tests. O harness experimental registra entradas/saídas e verifica determinismo, limites, fórmulas e conservação declarada; o harness gráfico compara controles reais com o serviço. A quantidade e o resultado atuais devem ser lidos nos recibos da entrega. Testes aprovados não certificam realismo científico ou o computador da faculdade.

### 60. O que foi mais difícil de implementar?

A experiência pessoal precisa ser respondida pelo estudante; não se deduz uma dificuldade subjetiva lendo o Git. Um ponto técnico exigente que posso explicar é manter a ordem entre candidato, transação, atualização pública, observadores e animação, inclusive em falhas. Outro é preservar snapshots imutáveis e a identidade dos mundos. Escolha o ponto que corresponde à sua participação real e explique um caso concreto.

### 61. Como o código poderia evoluir futuramente?

A auditoria recomenda tratar o arredondamento em populações pequenas e documentar a sensibilidade ao passo temporal. Evoluções maiores incluem bioquímica/clima mais ricos e calibração, com novos testes e compatibilidade dos saves. Na arquitetura, novos eventos implementam produto/criador e entram no catálogo; novos observadores implementam o contrato. São propostas, não funcionalidades já entregues.

### 62. Quais partes do código foram produzidas ou auxiliadas por IA e como foram verificadas?

Nesta tarefa a IA auxiliou os harnesses de auditoria/captura, scripts de geração, relatórios, slides e estudo. As fontes preexistentes de produção não foram alteradas nesta entrega. O histórico disponível não prova a contribuição humana/IA de cada linha anterior: não vou inventar esse percentual. A verificação desta entrega está em Maven/JUnit, CSVs reproduzidos, comparação GUI/serviço, hashes e revisão do Java. Cabe ao estudante declarar honestamente seu processo e demonstrar que entende as chamadas e seus limites.
