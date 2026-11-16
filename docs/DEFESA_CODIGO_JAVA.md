# Defesa do código Java — abrir, apontar e explicar

- **Aluno:** Matheus Luiz Bueno
- **Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS
- **Professor:** EDER MAGALHAES MACHADO
- **Turma:** 4P
- **Trabalho:** ProjektTellus — Simulador Planetário
- **Período:** 2º Bimestre de 2026

Leitura do checkout atual para esta revisão documental. Não houve execução de Maven, testes ou GUI nesta revisão. As falas em primeira pessoa são sugestões para explicar o funcionamento presente; não atribuem ao estudante autoria de cada linha, dificuldades pessoais ou ações de verificação que ele não realizou. A referência à Prof. Célia no [enunciado histórico](ENUNCIADO_ORIGINAL_TRANSCRITO.md) permanece intacta: esta defesa é para o professor EDER MAGALHAES MACHADO.

## Como usar durante uma interrupção

Respondo primeiro em uma frase, abro a classe certa e aponto entrada → chamada → retorno → falha. Se a pergunta pedir mais, uso o nível de um minuto ou a trilha aprofundada. Os arquivos citados estão no repositório; não preciso compilar para abrir e explicar a fonte. Os snippets são recortes da implementação, não programas Java autônomos. No deck, TerraBuilder.atmosfera (slide 5, quatro linhas), AsteroideCreator, a linha de criação da candidata e a publicação do serviço foram diagramados com whitespace reorganizado; os tokens e o funcionamento são os da fonte. Este documento conserva os recortes canônicos do serviço e mostra a quebra de AsteroideCreator para leitura. Não exigir igualdade de bytes/linhas de display: conferir tokens e origem.

| Solicitação | Primeiro arquivo | Continuação | Slides |
|---|---|---|---|
| Construção | domain/TerraBuilder.java | ui/BuilderDialog.java → SimulacaoService.criar | 4–5 |
| Criação e produto | events/EventoCreator.java | AsteroideCreator.java → EventoPlanetario.java → EventoBase.java → SimulacaoService.aplicar | 6–7 |
| Inscrição e callback | patterns/observer/SimulacaoObserver.java | ObserverBus.java → SimulacaoService.confirmar → MainController.initialize | 8–9 |
| Captura e restauração | domain/Terra.java | patterns/memento/TerraMemento.java → SimulacaoService.restaurar | 10–11 |
| Ordem transacional | application/SimulacaoService.java | persistence/SqliteMundoRepository.java → TransacaoSqlite.java | 12 |

Todos os caminhos da tabela são relativos a `src/main/java/com/projekttellus/`.

## Builder — explicação em três níveis

### Em aproximadamente 20 segundos

“Eu uso TerraBuilder para reunir as condições iniciais sem espalhar uma montagem longa na tela. Os métodos nomeados guardam os componentes e devolvem o próprio Builder. Só build cria EstadoPlanetario e Terra com uma identidade nova. As regras de validade continuam no domínio.”

### Em aproximadamente um minuto

“Eu começo no diálogo de criação. Ele lê atmosfera, clima, água, vegetação e população e configura TerraBuilder. Os defaults cobrem valores que não substituo. Quando aponto return this, estou devolvendo o mesmo configurador, não uma Terra pronta. SimulacaoService.criar chama build; ali são instanciados EstadoPlanetario e Terra, com UUID novo. Os componentes validam seus próprios limites, o estado rejeita nulos/tempo inválido e Terra valida nome e referências. Este Builder é concreto e fluente, sem Director ou interface abstrata. A independência dos mundos vem da identidade e da troca de estado, não de clonar cada componente imutável.”

### Aprofundada — código e contrato

**Fonte:** [domain/TerraBuilder.java](../src/main/java/com/projekttellus/domain/TerraBuilder.java), linhas 14–14; literal.

```java
    public TerraBuilder atmosfera(Atmosfera valor) { atmosfera = valor; return this; }
```

**Fonte:** [domain/TerraBuilder.java](../src/main/java/com/projekttellus/domain/TerraBuilder.java), linhas 20–23; literal.

```java
    public Terra build() {
        return new Terra(UUID.randomUUID(), nome,
                new EstadoPlanetario(atmosfera, clima, biosfera, hidrosfera, civilizacao, anos));
    }
```

| Ponto | O que explico no Java |
|---|---|
| Recebe | atmosfera recebe Atmosfera; os demais métodos recebem componentes, nome ou anos |
| Devolve | setters devolvem TerraBuilder por this; build devolve Terra |
| Instanciação | build usa new EstadoPlanetario e new Terra; UUID.randomUUID identifica o mundo |
| Validação/falha | Atmosfera/Hidrosfera validam ao serem construídas; EstadoPlanetario rejeita null/tempo inválido; Terra rejeita nome inválido. O setter não valida tudo |
| Mutabilidade | Builder pode mudar durante configuração; o produto Terra troca referências; os valores ambientais são imutáveis |
| Coordenação | MainController.createWorld → BuilderDialog → SimulacaoService.criar → build → capturar origem → confirmar |

**Objeto a objeto:** o resultado do diálogo é um TerraBuilder. O serviço chama build e recebe uma Terra; a Terra recebe um EstadoPlanetario, que referencia cinco componentes. Um novo UUID não introduz aleatoriedade na evolução numérica. Reusar Builder não o reseta e não o torna seguro para threads concorrentes.

**Se retirar Builder:** o construtor público Terra(UUID, String, EstadoPlanetario) continua utilizável; o cliente precisaria reunir explicitamente os componentes e defaults. Builder reduz essa montagem no cliente, mas não é o único modo possível de construir uma Terra.

**UML que explico:** BuilderDialog depende de TerraBuilder; build cria Terra e EstadoPlanetario; Terra referencia o estado. Não invento Director e não uso posse exclusiva de um estado que pode ser compartilhado de modo imutável. Demo independente: criação de mundo, sem precisar aplicar catástrofe.

## Factory Method — explicação em três níveis

### Em aproximadamente 20 segundos

“Eu tenho cinco eventos diferentes, mas o serviço usa um contrato comum. EventoCreator declara criar; cada Creator concreto escolhe o produto. AsteroideCreator retorna Asteroide. O serviço recebe esse produto como EventoPlanetario e chama aplicar sem conhecer a fórmula concreta.”

### Em aproximadamente um minuto

“Eu abro EventoCreator para mostrar o criar abstrato e AsteroideCreator para mostrar a sobrescrita. @Override faz o compilador conferir que a assinatura realmente sobrescreve o contrato. O new Asteroide está no lugar onde a decisão concreta deve ocorrer. No serviço, var não elimina o tipo: o retorno declarado de criar é EventoPlanetario, então esse é o tipo estático inferido de evento. Chamo aplicar por esse contrato; EventoBase faz a parte comum e delega transformar ao produto concreto. O serviço prepara uma Terra candidata com o mesmo id, persiste e só então atualiza a Terra viva. Os outros quatro pares seguem essa relação de criação.”

### Aprofundada — slide 7 e tipos reais

**Fonte:** [events/EventoCreator.java](../src/main/java/com/projekttellus/events/EventoCreator.java), linhas 6–6; literal.

```java
    public abstract EventoPlanetario criar(double intensidade);
```

**Fonte:** [events/AsteroideCreator.java](../src/main/java/com/projekttellus/events/AsteroideCreator.java), linhas 3–6; mesmos tokens da fonte; somente espaços/quebras reorganizados.

```java
public final class AsteroideCreator extends EventoCreator {
    public String nome() { return new Asteroide(0).nome(); }
    @Override
    public EventoPlanetario criar(double intensidade) {
        return new Asteroide(intensidade);
    }
}
```

**Fonte:** [events/EventoPlanetario.java](../src/main/java/com/projekttellus/events/EventoPlanetario.java), linhas 3–7; literal.

```java
public interface EventoPlanetario {
    String nome();
    double intensidade();
    EstadoPlanetario aplicar(EstadoPlanetario estado);
}
```

**Fonte:** [application/SimulacaoService.java](../src/main/java/com/projekttellus/application/SimulacaoService.java), linhas 52–52; literal.

```java
        var evento = Objects.requireNonNull(creator).criar(intensidade);
```

**Fonte:** [application/SimulacaoService.java](../src/main/java/com/projekttellus/application/SimulacaoService.java), linhas 55–55; literal.

```java
        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));
```

As duas linhas do serviço são recortes separados de aplicar, não linhas adjacentes: entre elas há passos descritivos e captura automática. Não apagar essa etapa ao explicar a sequência.

| Expressão | Entrada / retorno / despacho |
|---|---|
| criar(double intensidade) | Recebe intensidade; declaração retorna EventoPlanetario, mesmo quando o objeto concreto é Asteroide |
| Objects.requireNonNull(creator) | Confere Creator não nulo; se faltar, lança NullPointerException antes de criar produto |
| var evento | Inferência estática de EventoPlanetario; não é Object genérico, dynamic ou troca de tipo em runtime |
| new Asteroide(intensidade) | Constrói produto; EventoBase valida intensidade finita em [0,1] |
| evento.aplicar(terra.estado()) | Recebe EstadoPlanetario e devolve EstadoPlanetario; a hierarquia leva à transformação concreta |
| var candidato = new Terra(...) | Tipo inferido Terra; preserva id/nome e recebe novo estado. Não é atualização ainda do destino |

**Cinco pares reais:** AsteroideCreator→Asteroide; SupervulcaoCreator→Supervulcao; GuerraNuclearCreator→GuerraNuclear; InvasaoAlienigenaCreator→InvasaoAlienigena; RestauracaoEcologicaCreator→RestauracaoEcologica. Todos os produtos estendem EventoBase, que implementa EventoPlanetario. Creator concreto herda EventoCreator. Herança entre classes e realização de interface são relações UML diferentes.

**Polimorfismo em duas chamadas:** criar escolhe a sobrescrita do Creator; aplicar usa o contrato do produto e o método final da base encaminha a transformar concreto. @Override é conferência de compilação, não o mecanismo runtime que cria polimorfismo.

**Falhas:** intensidade fora de [0,1]/NaN é rejeitada pelo domínio; produto recebe estado não nulo. Com zero, aplicar retorna a mesma referência de estado, mas o serviço ainda pode gravar a ação, preservar retrato e notificar. Falha antes de confirmar não chega à gravação deste fluxo.

**Extensão e alternativa:** um switch poderia atender um catálogo pequeno, mas centralizaria sua escolha numa fábrica simples. Esta implementação usa subclasses para criação; o serviço permanece genérico. Para sexto evento, preciso de produto/Creator, catálogo em MainController e testes/mapeamento visual adequado. Não prometo extensão sem alterar nada.

**UML e demo independente:** aponto as cinco relações Creator→produto e a interface comum; seleciono evento sem reconstruir o mundo. O seletor guarda Creators; a UI divide 25 por 100 antes de chamar o serviço. A invasão é ficção didática.

## Observer — explicação em três níveis

### Em aproximadamente 20 segundos

“Eu publico a mudança confirmada no ObserverBus. Ele chama os assinantes pelo contrato SimulacaoObserver: alertas, estatísticas e um objeto anônimo da tela. A entrega é síncrona; o callback da UI agenda Platform.runLater. Receber a notificação não significa que os pixels já foram desenhados.”

### Em aproximadamente um minuto

“Eu começo pela interface, depois mostro a inscrição e por fim publicar. O serviço inscreve monitor e estatísticas; MainController inscreve um objeto anônimo para a UI. Depois de salvar e atualizar Terra, o serviço entrega MudancaPlanetaria ao bus. O laço chama observer.aoMudar(mudanca); a implementação concreta faz sua responsabilidade. O bus anota sucesso após retorno normal e isola RuntimeException por assinante. O banco já está confirmado nesse ponto. A inscrição devolve AutoCloseable para remoção. O callback gráfico agenda o Runnable na thread FX: essa etapa posterior não transforma todo o bus em assíncrono.”

### Aprofundada — quatro blocos do slide 9 e fonte completa

O display final tem quatro blocos: interface completa; corpo de inscrever com três linhas; chamada do serviço a publicar/new MudancaPlanetaria em quatro linhas; callback `observer.aoMudar(mudanca);` em linha única azul. A fonte completa abaixo permite explicar também o laço e o tratamento de falhas. O corpo de inscrever e a linha do callback são recortes internos, não métodos completos isolados.

**1. Interface: quem pode receber.**

**Fonte:** [patterns/observer/SimulacaoObserver.java](../src/main/java/com/projekttellus/patterns/observer/SimulacaoObserver.java), linhas 2–5; literal.

```java
public interface SimulacaoObserver {
    String nome();
    void aoMudar(MudancaPlanetaria mudanca);
}
```

**2. Inscrição: quem entra na lista.**

**Fonte:** [patterns/observer/ObserverBus.java](../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java), linhas 10–14; literal.

```java
    public AutoCloseable inscrever(SimulacaoObserver observer) {
        Objects.requireNonNull(observer);
        observers.addIfAbsent(observer);
        return () -> remover(observer);
    }
```

**3. Publicação e callback: o laço contém a chamada, sem separá-la visualmente.**

**Fonte:** [patterns/observer/ObserverBus.java](../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java), linhas 16–31; literal.

```java
    public Entrega publicar(MudancaPlanetaria mudanca) {
        var notificados = new ArrayList<String>();
        var falhas = new ArrayList<String>();
        for (var observer : observers) {
            String nome = nomeSeguro(observer);
            try {
                observer.aoMudar(mudanca);
                notificados.add(nome);
            } catch (RuntimeException ex) {
                falhas.add(nome);
                System.getLogger(ObserverBus.class.getName()).log(System.Logger.Level.WARNING,
                        "Observador não recebeu a mudança: " + nome, ex);
            }
        }
        return new Entrega(notificados, falhas);
    }
```

**4. Quem publica: SimulacaoService.confirmar, depois de gravar e atualizar.**

**Fonte:** [application/SimulacaoService.java](../src/main/java/com/projekttellus/application/SimulacaoService.java), linhas 109–109; literal.

```java
            var entrega = observerBus.publicar(new MudancaPlanetaria(destino.id(), destino.estado(), indicadores, descricao));
```

**Recebimento gráfico real: MainController.initialize cria o objeto anônimo.**

**Fonte:** [ui/MainController.java](../src/main/java/com/projekttellus/ui/MainController.java), linhas 128–137; literal.

```java
            subscription = service.observerBus().inscrever(new SimulacaoObserver() {
                @Override public String nome() { return "Globo e painel de indicadores"; }
                @Override public void aoMudar(MudancaPlanetaria change) {
                    Platform.runLater(() -> {
                        if (closed) return;
                        observed.put(change.mundoId(), change);
                        if (current != null && current.id().equals(change.mundoId())) renderObserved(change);
                    });
                }
            });
```

| Método/objeto | Contrato e efeito |
|---|---|
| inscrever(SimulacaoObserver) | Recebe objeto, rejeita null, addIfAbsent; devolve AutoCloseable que remove a inscrição |
| publicar(MudancaPlanetaria) | Recebe fato com UUID/estado/indicadores/descrição; devolve Entrega com listas imutáveis de nomes de sucesso/falha |
| aoMudar(MudancaPlanetaria) | Retorno void; executa responsabilidade do assinante. O bus conhece a interface, não a regra concreta |
| AlertaObserver | Substitui lista de alertas por UUID conforme condições; nem todo impacto provoca alerta crítico |
| EstatisticasObserver | Incrementa contagem e atualiza mínimo/máximo humano por UUID; projeção de sessão, sem persistir contadores |
| Assinante anônimo da UI | Agenda FX; MainController não implementa diretamente SimulacaoObserver |

**O que sei afirmar sobre threads:** os callbacks são executados pelo chamador do bus; no caminho do aplicativo é o worker único. Os mapas de projeção são concorrentes e o mapa observed da UI só é usado na FX. CopyOnWriteArrayList facilita iteração/inscrição, sem tornar todo observador thread-safe. Callback lento bloqueia o andamento do laço. Um Error não é capturado pelo catch de RuntimeException.

**Sucesso/falha:** aoMudar retornou → nome entra em notificados; RuntimeException → nome entra em falhas e laço continua. Não há broker/retry persistente nem garantia de render completo. Salvar/captura manual usam notificar=false; carregar é renderizado pela UI sem a mesma publicação de mudança ambiental.

**UML e demo independente:** serviço→bus publica, bus referencia SimulacaoObserver; AlertaObserver/EstatisticasObserver realizam a interface. UI cria um terceiro objeto anônimo. Posso alterar condições ou aplicar evento num mundo já aberto e mostrar nomes de assinantes e estatística; não preciso reconstruir o cenário para explicar o padrão.

## Memento — explicação em três níveis

### Em aproximadamente 20 segundos

“Eu preservo um retrato completo antes de substituir o estado. Terra captura/restaura; TerraMemento guarda o valor e SimulacaoService coordena o uso. O record é público e legível. A proteção vem da imutabilidade, não de opacidade; restaurar exige o UUID do mesmo mundo.”

### Em aproximadamente um minuto

“Eu abro Terra.capturar e mostro que o retrato recebe id próprio, mundoId, rótulo, nome, instante e estado. Não preciso clonar profundamente: EstadoPlanetario e seus componentes são valores imutáveis, então a referência antiga continua válida. Terra.restaurar rejeita null e retrato de outro mundo e reassocia nome e estado; o id da Terra não muda. No serviço, isso é validado numa candidata antes de gravar, e outro retrato preserva o estado que será substituído. O record público permite consultar estado(), inclusive no caretaker. SQLite dá durabilidade; a representação/restauração do retrato já existe em memória.”

### Aprofundada — originador, retrato e caretaker

**Fonte:** [domain/Terra.java](../src/main/java/com/projekttellus/domain/Terra.java), linhas 23–25; literal.

```java
    public TerraMemento capturar(String rotulo) {
        return new TerraMemento(UUID.randomUUID(), id, rotulo, nome, Instant.now(), estado);
    }
```

**Fonte:** [domain/Terra.java](../src/main/java/com/projekttellus/domain/Terra.java), linhas 26–32; literal.

```java
    public void restaurar(TerraMemento memento) {
        Objects.requireNonNull(memento);
        if (!id.equals(memento.mundoId()))
            throw new IllegalArgumentException("Este retrato pertence a outro mundo.");
        nome = memento.nomeMundo();
        estado = memento.estado();
    }
```

**Fonte:** [patterns/memento/TerraMemento.java](../src/main/java/com/projekttellus/patterns/memento/TerraMemento.java), linhas 10–19; literal.

```java
public record TerraMemento(UUID id, UUID mundoId, String rotulo, String nomeMundo,
                          Instant criadoEm, EstadoPlanetario estado) {
    public TerraMemento {
        Objects.requireNonNull(id); Objects.requireNonNull(mundoId);
        rotulo = Validacao.texto("Nome do retrato", rotulo);
        nomeMundo = Validacao.texto("Nome do mundo", nomeMundo);
        Objects.requireNonNull(criadoEm); Objects.requireNonNull(estado);
    }
    @Override public String toString() { return rotulo; }
}
```

**Fonte:** [application/SimulacaoService.java](../src/main/java/com/projekttellus/application/SimulacaoService.java), linhas 66–73; literal.

```java
    public ResultadoOperacao restaurar(Terra terra, TerraMemento snapshot) {
        var candidato = new Terra(terra.id(), terra.nome(), terra.estado());
        candidato.restaurar(snapshot); // Verifica a identidade ANTES de qualquer gravação.
        var passos = new ArrayList<Passo>();
        var retorno = terra.capturar("Antes da restauração"); passos.add(preservado());
        passos.add(new Passo("Uma Terra anterior foi restaurada", "Todos os componentes, o tempo e o nome voltaram ao retrato selecionado. Outros mundos permanecem independentes.", "Memento"));
        return confirmar(terra, candidato, terra.estado(), "Restaurado: " + snapshot.rotulo(), 0, retorno, passos, true);
    }
```

| Ponto | O que recebe/devolve/preserva |
|---|---|
| capturar(String rotulo) | Recebe rótulo validado pelo record; devolve TerraMemento. Não grava sozinho no banco |
| UUID id do retrato | Identifica uma captura; não é o id do originador |
| mundoId | Vínculo com Terra.id, final. Restaurar não troca identidade |
| restaurar(TerraMemento) em Terra | Devolve void e modifica referências nome/estado após checagens; retrato permanece imutável |
| restaurar no serviço | Recebe Terra e retrato; devolve ResultadoOperacao depois de candidata validada, snapshot de retorno e confirmar |
| record público | Gera accessors públicos; estado é consultável. Não oferece encapsulamento opaco estrito GoF |

**Estado completo:** atmosfera, clima, biosfera, hidrosfera, civilização e anos estão em EstadoPlanetario. Nome está no Memento. Restaurar não desfaz histórico/estatísticas de sessão, não reverte câmera nem preferências de UI. Os registros de restauração são acrescentados; não é Event Sourcing nem Ctrl+Z genérico.

**Por que a referência é segura:** records finais podem guardar algo mutável em outros projetos; aqui o grafo concreto usa primitivos, records imutáveis, String, UUID e Instant. Terra pode apontar para um novo valor sem alterar o que o retrato guarda. Se o domínio ganhasse coleções mutáveis, teria de rever cópias defensivas e contrato.

**Por que record público:** representa explicitamente um valor legível e facilita validação/persistência com pouco boilerplate. É a propriedade verificável da escolha presente, não um relato de por que o autor a escolheu historicamente. A exposição não altera os valores, mas deixa de oferecer ocultação estrita.

**UML e demo independente:** Terra é originador, TerraMemento retrato, SimulacaoService caretaker; repositório armazena/reconstitui. Aponto associações compartilháveis ao estado, não composição exclusiva. Abro mundo existente, capturo, altero e restauro; não confundo evento RestauracaoEcologica com recuperar valores exatos.

## Fluxo que liga os quatro padrões e SQLite

Título do slide 12: **Os quatro padrões no fluxo do simulador**. Há operações distintas, não quatro padrões simultâneos em todo clique:

1. **Construir:** BuilderDialog configura TerraBuilder → SimulacaoService.criar chama build → captura origem → confirma.
2. **Capturar manualmente:** SimulacaoService.capturar → Terra.capturar → confirma gravação do retrato; não publica mudança ambiental.
3. **Aplicar evento:** SimulacaoService.aplicar → Creator.criar → captura automática → evento.aplicar → Terra candidata → calcular indicadores → SQLite → atualizar Terra viva → publicar Observer → UI apresenta.
4. **Restaurar depois:** candidata.restaurar valida UUID → captura retorno → confirma estado restaurado → publica nova mudança.

**Confirmar em código:**

**Fonte:** [application/SimulacaoService.java](../src/main/java/com/projekttellus/application/SimulacaoService.java), linhas 99–117; literal.

```java
    private ResultadoOperacao confirmar(Terra destino, Terra candidato, EstadoPlanetario antes,
                                         String descricao, double intensidade, TerraMemento snapshot,
                                         ArrayList<Passo> passos, boolean notificar) {
        var indicadores = ModeloPlanetario.calcular(candidato.estado());
        passos.add(new Passo("Condições planetárias recalculadas", "Sobrevivência humana, potencial biológico e sustentabilidade foram avaliados separadamente; população e continente determinam a ocupação do suporte estimado.", ""));
        var registro = new RegistroEvento(UUID.randomUUID(), candidato.id(), Instant.now(), descricao, intensidade, candidato.estado().anos());
        repository.salvar(candidato, registro, snapshot);
        destino.renomear(candidato.nome()); destino.atualizar(candidato.estado());
        passos.add(new Passo("Mundo e histórico preservados", "O estado e o histórico foram gravados juntos. Quando há um retrato, ele integra a mesma confirmação.", ""));
        if (notificar) {
            var entrega = observerBus.publicar(new MudancaPlanetaria(destino.id(), destino.estado(), indicadores, descricao));
            ultimoMundo = destino.id();
            for (String nome : entrega.notificados())
                passos.add(new Passo("Sistema notificado: " + nome, "Este assinante recebeu a mudança e executou sua responsabilidade, sem o planeta conhecer sua implementação.", "Observer"));
            if (!entrega.falhas().isEmpty()) passos.add(new Passo("Uma atualização auxiliar não foi concluída",
                    "O mundo foi salvo; " + entrega.falhas().size() + " assinante(s) não concluíram sua atualização.", "Observer"));
        }
        return new ResultadoOperacao(destino, antes, destino.estado(), passos, indicadores, descricao);
    }
```

**Quem grava o quê:** MundoRepository.salvar recebe Terra candidata, RegistroEvento e TerraMemento opcional. SqliteMundoRepository.salvar verifica vínculo dos ids e usa TransacaoSqlite para salvar estado atual e inserir histórico/retrato na mesma transação. PreparedStatement recebe as colunas dos componentes. O serviço depende do contrato; JDBC/SQLite ficam na implementação.

**Falha explicada com precisão:** erro de validação/criação/aplicação antes de confirmar não chega à gravação. Falha SQL antes do commit tenta rollback; como salvar não retornou normalmente, o serviço não chega à atualização/publicação. Há teste escrito de trigger antes de UPDATE (`ApplicationTest.failedTransactionLeavesLiveEarthAndObserversUntouched`). Não concluo que toda exceção possível prova ausência de commit: falha de comunicação/limpeza após confirmação é outro limite; não existe transação distribuída banco/memória/GUI. Falha RuntimeException do observador acontece depois da persistência e é auxiliar.

## Evidência real para explicar o slide 14

Fonte anterior, relida nesta revisão: [measurements.csv](capturas/run-20261115-124710/measurements.csv), linhas `antes`, `evento_0` e `restaurado`, e [visual-values.csv](capturas/run-20261115-124710/visual-values.csv). O ensaio anterior da GUI registra:

| Grandeza | Antes | Asteroide 25% | Após Memento |
|---|---:|---:|---:|
| Humana H (%) | 100 | 64,72968169602449 | 100 |
| Temperatura (°C) | 15 | 8 | 15 |
| População | 8.000.000.000 | 6.500.000.000 | 8.000.000.000 |

Fala curta: “Eu mostro um evento que muda os valores e um retrato que os recupera. Não é um evento inverso: é a recuperação do estado preservado.” O CSV sustenta os números completos; a UI pode arredondar o percentual. A exatidão do estado inteiro exige comparação dos campos/checagens do ensaio, não só três números nem cor do globo. Recibo anterior de reabertura: [sqlite-reopen.txt](capturas/run-20261115-124710/sqlite-reopen.txt). Não foi uma nova execução nesta revisão.

A limitação de população residual de duas pessoas fica nas notas de apoio: resulta de arredondamento inteiro no cenário sem O₂, não de previsão real de sobrevivência. O modelo permanece didático, sem calibração científica. Não transformar essa limitação na chamada principal do slide 14.

## Como responder sobre IA sem inventar um histórico

“Eu posso apontar o contrato, seguir as chamadas e distinguir o que o código promete do que o recibo mediu.” Só usar “eu executei/testei” quando corresponder à ação pessoal real. Para evidência anterior, dizer “o recibo registra”. As 62 perguntas anteriores são apoio; a nova sessão de arguição tem respostas curtas e aprofundadas, inclusive dificuldade pessoal, que só o estudante pode relatar. Esta revisão atualizou os materiais sem reexecutar Maven ou modificar o motor.
