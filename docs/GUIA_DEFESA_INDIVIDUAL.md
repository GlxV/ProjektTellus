# Guia compacto para a defesa individual

- **Aluno:** Matheus Luiz Bueno
- **Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS
- **Professor:** EDER MAGALHAES MACHADO
- **Turma:** 4P
- **Trabalho:** ProjektTellus — Simulador Planetário
- **Período:** 2º Bimestre de 2026

## 1. O que preciso dominar primeiro

A minha resposta central é: **TerraBuilder constrói; o Creator concreto cria o evento; os SimulacaoObserver inscritos recebem a mudança; Terra restaura um TerraMemento, coordenada por SimulacaoService.**

Para cada padrão, preciso explicar quatro itens: nome/propósito; problema e solução reais; UML; Java. São quatro padrões permitidos, acima do mínimo de três do enunciado. Não trocar essa explicação por experimentos. A Prof. Célia continua no [enunciado histórico](ENUNCIADO_ORIGINAL_TRANSCRITO.md) pelo projeto conjunto; o professor desta defesa é EDER MAGALHAES MACHADO. Não inventar instituição ou matrícula.

## 2. Quais classes abrir primeiro

Caminhos abaixo sob `src/main/java/com/projekttellus/`. Deixar essas classes localizáveis no editor antes de apresentar; procurar pelo nome do método é suficiente, sem compilar ou depender de números de linha.

| Prioridade | Padrão / slides | Primeira classe e métodos | Depois, se o professor aprofundar |
|---|---|---|---|
| 1 | Builder / 4–5 | domain/TerraBuilder.java: atmosfera, build | ui/BuilderDialog.java: setResultConverter; application/SimulacaoService.java: criar |
| 2 | Factory Method / 6–7 | events/EventoCreator.java: criar; AsteroideCreator.java: criar | EventoPlanetario.java: aplicar; EventoBase.java: aplicar/transformar; SimulacaoService.aplicar |
| 3 | Observer / 8–9 | patterns/observer/SimulacaoObserver.java: aoMudar; ObserverBus.java: inscrever/publicar | AlertaObserver/EstatisticasObserver.aoMudar; MainController.initialize, assinante anônimo |
| 4 | Memento / 10–11 | domain/Terra.java: capturar/restaurar; patterns/memento/TerraMemento.java | EstadoPlanetario.java; SimulacaoService.restaurar |
| 5 | Integração / 12 | application/SimulacaoService.java: confirmar | persistence/SqliteMundoRepository.java: salvar; TransacaoSqlite.java: executar |

## 3. Explicar o UML sem decorar um desenho

| Padrão | O que aponto | O que não devo afirmar |
|---|---|---|
| Builder | Diálogo configura Builder; build cria estado e Terra | Não há Director ou Builder abstrato; setter não valida tudo |
| Factory Method | Creator concreto herda EventoCreator e cria seu produto; produto herda EventoBase, que realiza EventoPlanetario | Uma interface não é a mesma relação que herança de implementação; os cinco pares não são um switch oculto |
| Observer | Serviço publica no bus; bus guarda contrato; observadores realizam interface | MainController cria assinante anônimo; não implementa diretamente SimulacaoObserver |
| Memento | Terra originador, TerraMemento retrato, serviço caretaker; repositório armazena | Record público é legível; não é opacidade estrita. Estado imutável pode ter referências compartilhadas |

No desenho, triângulo vazio contínuo aponta para classe base; tracejado indica realização de interface; dependência de criação liga Creator ao produto. Para associação ao estado imutável, não alegar posse exclusiva que impeça compartilhamento com retratos.

## 4. Escolher o percurso

**Modo B, recomendado para abrir a conversa: 5–7 minutos, alvo de 6min20s.** Seguir **1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15**. Abertura 1/2 leva 25 s. Os pares 4–11 continuam essenciais: todos os padrões, com propósito, problema/solução, UML e Java. Slides 3, 13 e 14 são contexto opcional, acessível se solicitado. Integração 12 separa operações; demo 15 fecha o fluxo. Não reduzir tempo escondendo um padrão inteiro.

**Modo A, completo: 10–12 minutos, alvo de 10min40s.** Percorrer os 15 slides e explicar arquitetura, algoritmo e evidências brevemente. Os tempos são sugestões, não exigência do enunciado; perguntas extensas ficam fora da previsão. O [roteiro](ROTEIRO_APRESENTACAO.md) traz fala A/B e tempo por slide.

Se o professor pedir uma classe, abro o arquivo no ponto da tabela e faço entrada → chamada → retorno → falha. A fala de 20 segundos abre o assunto; a de um minuto explica a colaboração; a aprofundada segue os métodos. Esses três níveis estão para **cada um dos quatro padrões** em [DEFESA_CODIGO_JAVA.md](DEFESA_CODIGO_JAVA.md).

## 5. Fluxo real que não posso inverter

- **Construir:** diálogo configura Builder → service.criar chama build → retrato de origem → confirma.
- **Capturar manualmente:** service.capturar preserva retrato e grava, sem publicar nova mudança ambiental.
- **Aplicar evento:** criar produto → capturar automaticamente → aplicar/calcular → SQLite → atualizar Terra → publicar.
- **Restaurar depois:** validar snapshot numa candidata → capturar retorno → confirmar estado restaurado → notificar.

A demo captura manualmente antes de clicar em aplicar. Isso não inverte a ordem interna de aplicar: o Creator cria o produto antes da captura automática. O título do slide 12 é **Os quatro padrões no fluxo do simulador**.

## 6. Perguntas de Java que preciso responder sem hesitar

- `return this` retorna o mesmo Builder; build retorna outra instância de Terra.
- `@Override` ajuda o compilador a conferir sobrescrita; polimorfismo vem dos contratos/objetos e despacho de métodos.
- `var evento` tem tipo estático inferido **EventoPlanetario**, porque esse é o retorno de criar; o objeto pode ser Asteroide.
- O `new` do produto está no Creator concreto; o serviço coordena aplicação e gravação.
- `observer.aoMudar(mudanca)` é síncrono. O callback da UI agenda `Platform.runLater`; callback recebido não significa renderização concluída.
- `memento.estado()` é público e legível; a imutabilidade impede alterar o valor compartilhado.
- `mundoId` impede usar retrato de outra Terra; id do retrato e id do mundo são coisas diferentes.
- SQLite recebe candidato, histórico e retrato opcional na transação; persistência não é sinônimo de Memento.

## 7. Interrupções e perguntas difíceis

Respondo a pergunta primeiro, sem terminar um parágrafo decorado. Uso “Posso mostrar isso neste método” e abro a classe. Se a pergunta for só Observer, faço uma alteração em mundo já aberto e mostro a publicação; não preciso reconstruir o mundo nem refazer toda a demo. Depois retomo no próximo padrão ainda não coberto.

O banco original de 62 perguntas permanece em [PERGUNTAS_APRESENTACAO.md](PERGUNTAS_APRESENTACAO.md). A seção nova tem **21 itens de arguição**: 4 básicos, 4 intermediários e as 13 perguntas difíceis solicitadas, com resposta curta e aprofundada. Muitos retomam o banco: não são 21 temas inéditos. Dificuldade pessoal e contribuição histórica de IA só devem ser relatadas conforme a experiência real do aluno.

Se não souber uma resposta, localizo o método, digo o que consigo provar pelo código e distingo isso da hipótese. Não invento que escrevi/testei algo. “O recibo anterior registra” é diferente de “eu executei agora”.

## 8. Demo curta e caminhos independentes

**Ciclo básico:** criar Terra → capturar retrato → asteroide 25% → observar nomes/indicadores → restaurar. Usar o banco temporário definido pelo procedimento da demo, sem saves pessoais. Detalhes operacionais em [DEMO_APRESENTACAO.md](DEMO_APRESENTACAO.md), com procedimentos independentes por padrão.

| Pedido isolado | Ação suficiente para começar | Fonte a abrir |
|---|---|---|
| Builder | Abrir criação e montar mundo | TerraBuilder.build |
| Factory Method | Selecionar evento e intensidade num mundo aberto | AsteroideCreator.criar e EventoCreator.criar |
| Observer | Aplicar uma alteração/evento; conferir assinantes | ObserverBus.publicar e inscrição em MainController |
| Memento | Capturar, alterar e restaurar um mundo existente | Terra.capturar/restaurar e TerraMemento |

Comparação do ensaio anterior: H 100→64,7297→100; temperatura 15→8→15°C; população 8→6,5→8 bilhões. Fonte: [measurements.csv](capturas/run-20261115-124710/measurements.csv). Os controles podem arredondar o display. Recuperar estado completo e persistência exige conferir mais que a cor do planeta; reabrir só se o professor pedir e houver tempo, com o mesmo banco.

Se a GUI falhar, explicar a falha, mostrar capturas/registro anterior identificado e continuar por Java/UML. Não simular execução bem-sucedida nem prometer compatibilidade com o computador da faculdade. Esta revisão não reexecutou GUI ou Maven.

## 9. Limites que reconheço

Builder é concreto/fluente, não uma hierarquia artificial; novo evento exige registro no catálogo; Observer é síncrono, com falhas RuntimeException isoladas após persistir; Memento é uma variante legível, sem opacidade estrita; não existe transação distribuída entre banco, memória e tela. Modelo científico é simplificado. O resíduo de duas pessoas sem O₂ é arredondamento numérico, não sobrevivência prevista: fica nas notas, fora da chamada principal do slide 14.

Antes de encerrar, verifico se consegui mostrar **quem constrói a Terra, quem cria os eventos, quem recebe as mudanças e quem restaura o estado**, com código e UML de todos os quatro.
