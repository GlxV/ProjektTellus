# Estudo completo dos padrões — defesa do Java real

Leitura estática em 15/11/2026. Caminhos relativos à raiz ProjektTellus. Todos os blocos Java abaixo foram extraídos das fontes existentes; imports e contexto podem estar fora dos recortes. O manifesto [snippets-deck.json](uml-apresentacao/snippets-deck.json) guarda caminho, linhas e SHA256 de cada fonte e recorte. Não executar esses fragmentos isoladamente como se fossem classes completas.

A fonte acadêmica primária é a [transcrição integral das duas páginas](ENUNCIADO_ORIGINAL_TRANSCRITO.md). A [matriz de conformidade](CONFORMIDADE_TRABALHO.md) separa documentação, exigências e validação de execução. Esta frente não executou Maven, testes ou aplicativo; nomes de testes abaixo indicam cobertura escrita existente, não resultados atuais.

## Mapa dos objetos e contratos

| Responsabilidade | Classe real e caminho sob src/main/java/com/projekttellus/ | Papel na defesa |
|---|---|---|
| Montar cenário | domain/TerraBuilder.java | Builder concreto, mutável durante configuração |
| Coletar condições | ui/BuilderDialog.java; ui/EnvironmentEditor.java | Cliente do Builder, coleta e lê valores validados |
| Identidade e estado atual | domain/Terra.java | Produto do Builder e originador do Memento |
| Grafo de valores | domain/EstadoPlanetario.java e Atmosfera/Clima/Biosfera/Hidrosfera/Civilizacao.java | Records finais, campos de valor e componentes imutáveis |
| Contrato de criação | events/EventoCreator.java | Creator abstrato; criar é Factory Method |
| Contrato de produto | events/EventoPlanetario.java | nome, intensidade e aplicar |
| Parte comum do produto | events/EventoBase.java | Valida intensidade e encaminha transformação |
| Entrega de mudanças | patterns/observer/ObserverBus.java | Subject/publicador separado da Terra |
| Contrato de assinante | patterns/observer/SimulacaoObserver.java | nome e aoMudar |
| Projeções de sessão | patterns/observer/AlertaObserver.java; EstatisticasObserver.java | Reações diferentes, indexadas por mundo |
| Snapshot | patterns/memento/TerraMemento.java | Record público, legível e imutável |
| Casos de uso/caretaker | application/SimulacaoService.java | Orquestra criação, captura, aplicação, persistência e publicação |
| Persistência | persistence/MundoRepository.java; SqliteMundoRepository.java; TransacaoSqlite.java | Contrato, implementação e commit/rollback |
| Cliente gráfico | ui/MainController.java | Worker único, inscrição da UI e agendamento na thread FX |
| Regras numéricas | simulation/ModeloPlanetario.java | cálculo e avanço determinístico |

## 1. Builder — propósito, problema e solução

**Classificação:** criação. Montar um objeto composto em etapas compreensíveis. No problema real, um cenário reúne nome, atmosfera, clima, biosfera, hidrosfera, civilização e tempo. Repetir construtores longos e posicionais na tela tornaria a configuração difícil de ler. `TerraBuilder` acumula componentes em métodos nomeados e o `build()` produz uma Terra identificada.

É um **Builder fluente concreto**, sem Director, interface Builder abstrata ou subclasses de Builder. Um Director é uma opção de certas variantes, não uma classe existente aqui. A interface fluente é a forma de encadear as etapas; a responsabilidade de reunir a configuração e só então construir o produto é o argumento de Builder. O cliente real é o diálogo, seguido do serviço. [Referência conceitual: Builder](https://refactoring.guru/design-patterns/builder).

### Código real e leitura importante

**Trecho B-completo — `src/main/java/com/projekttellus/domain/TerraBuilder.java` (linhas 5–24).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class TerraBuilder {
    private String nome = "Nova Terra";
    private Atmosfera atmosfera = new Atmosfera(21, 78.96, .04, 0, 1);
    private Clima clima = new Clima(15, 0, 0);
    private Biosfera biosfera = new Biosfera(65);
    private Hidrosfera hidrosfera = new Hidrosfera(71, 3);
    private Civilizacao civilizacao = new Civilizacao(8_000_000_000L);
    private double anos;
    public TerraBuilder nome(String valor) { nome = valor; return this; }
    public TerraBuilder atmosfera(Atmosfera valor) { atmosfera = valor; return this; }
    public TerraBuilder clima(Clima valor) { clima = valor; return this; }
    public TerraBuilder biosfera(Biosfera valor) { biosfera = valor; return this; }
    public TerraBuilder hidrosfera(Hidrosfera valor) { hidrosfera = valor; return this; }
    public TerraBuilder civilizacao(Civilizacao valor) { civilizacao = valor; return this; }
    public TerraBuilder anos(double valor) { anos = valor; return this; }
    public Terra build() {
        return new Terra(UUID.randomUUID(), nome,
                new EstadoPlanetario(atmosfera, clima, biosfera, hidrosfera, civilizacao, anos));
    }
}
```

- `private String nome` e os cinco componentes iniciam defaults: atmosfera 21/78,96/0,04/0% a 1 atm; clima 15°C sem aerossóis/radiação; vegetação 65%; água 71%, gelo 3%; população 8 bilhões; anos começa em zero.
- Cada método fluente atribui o argumento e retorna `this`, isto é, o mesmo Builder. **Não valida os limites ali**. Um componente inválido é normalmente rejeitado quando seu record é construído antes da chamada; nulos podem ser guardados e serão rejeitados na montagem do estado.
- `build()` executa `UUID.randomUUID()` para a identidade do novo mundo. Não significa que a evolução física usa aleatoriedade.
- `new EstadoPlanetario(...)` verifica componentes não nulos e tempo finito entre 0 e 1 bilhão. Cada componente já carrega as suas próprias invariantes.
- `new Terra(...)` valida id, nome e estado. Nome passa por `Validacao.texto`, que rejeita branco e mais de 100 caracteres após strip.
- `Atmosfera` exige cada gás em 0..100 e soma 100 com tolerância 1e-6; normaliza pequeno ruído. `Hidrosfera` exige água+gelo ≤100, também com tolerância e normalização. `Validacao.intervalo` rejeita NaN e infinito.
- O Builder não é resetado após `build()`. Pode ser reutilizado; cada chamada gera UUID novo, mas componentes imutáveis podem ser as mesmas referências. Não afirmar que todos os componentes são clonados nem que Builder é thread-safe.

### Cliente verdadeiro

**Trecho B-dialogo — `src/main/java/com/projekttellus/ui/BuilderDialog.java` (linhas 30–35).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
        setResultConverter(button -> {
            if (button != create) return null;
            var state = editor.read(0);
            return new TerraBuilder().nome(name.getText()).atmosfera(state.atmosfera()).clima(state.clima())
                    .biosfera(state.biosfera()).hidrosfera(state.hidrosfera()).civilizacao(state.civilizacao());
        });
```

O conversor só retorna Builder se o botão escolhido for “Construir e salvar”. `editor.read(0)` produz os componentes; o diálogo devolve a configuração, ainda não uma Terra persistida. `MainController.createWorld` chama `service.criar(builder)` no worker; `SimulacaoService.criar` chama build, captura “Origem do mundo” e confirma a gravação. Essa sequência separa coleta, construção e persistência.

**Objetos/chamadas:** usuário → BuilderDialog → TerraBuilder configurado → SimulacaoService.criar → build → EstadoPlanetario → Terra → snapshot de origem → repository.salvar.

**Vantagens:** parâmetros nomeados, defaults centralizados e separação entre configuração e identidade. **Limites/custo:** classe extra; não impede uso direto do construtor de Terra; validação distribuída no domínio precisa ser entendida; Builder mutável não serve para compartilhamento concorrente sem coordenação.

**Teste escrito de apoio:** `src/test/java/com/projekttellus/DomainTest.java`, `builderProducesIndependentCoherentEarths` e `rejectsInvalidGasMixturesAndNonFiniteValues`. Não executados nesta frente.

**Perguntas que preciso dominar:** quem retorna this? Quando ocorre a validação? O Builder tem Director? Dois builds compartilham estado mutável? Por que o construtor de Terra continua público?

**UML para o slide 4:** [04-builder.mmd](uml-apresentacao/04-builder.mmd). Associação Terra → EstadoPlanetario, sem composição exclusiva: o estado pode ser o mesmo de um retrato.

## 2. Factory Method — propósito, problema e solução

**Classificação:** criação. O problema real é selecionar e criar cinco eventos e aplicá-los pelo mesmo contrato. `EventoCreator` declara um método abstrato e cada subclasse o sobrescreve para escolher o produto. `SimulacaoService` chama o contrato do Creator, depois o contrato do produto. Este mecanismo de decisão por subclasses sustenta Factory Method; não é uma fábrica estática baseada em switch. [Referência conceitual: Factory Method](https://refactoring.guru/design-patterns/factory-method).

### Os cinco pares concretos reais

| Creator | Produto instanciado | Localização sob events/ |
|---|---|---|
| AsteroideCreator | Asteroide | AsteroideCreator.java / Asteroide.java |
| SupervulcaoCreator | Supervulcao | SupervulcaoCreator.java / Supervulcao.java |
| GuerraNuclearCreator | GuerraNuclear | GuerraNuclearCreator.java / GuerraNuclear.java |
| InvasaoAlienigenaCreator | InvasaoAlienigena | InvasaoAlienigenaCreator.java / InvasaoAlienigena.java |
| RestauracaoEcologicaCreator | RestauracaoEcologica | RestauracaoEcologicaCreator.java / RestauracaoEcologica.java |

### Contrato e implementações copiados

**Trecho F-contrato — `src/main/java/com/projekttellus/events/EventoCreator.java` (linhas 4–8).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public abstract class EventoCreator {
    public abstract String nome();
    public abstract EventoPlanetario criar(double intensidade);
    @Override public final String toString() { return nome(); }
}
```

**Trecho F-Asteroide — `src/main/java/com/projekttellus/events/AsteroideCreator.java` (linhas 3–6).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class AsteroideCreator extends EventoCreator {
    public String nome() { return new Asteroide(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new Asteroide(intensidade); }
}
```

**Trecho F-Supervulcao — `src/main/java/com/projekttellus/events/SupervulcaoCreator.java` (linhas 3–6).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class SupervulcaoCreator extends EventoCreator {
    public String nome() { return new Supervulcao(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new Supervulcao(intensidade); }
}
```

**Trecho F-GuerraNuclear — `src/main/java/com/projekttellus/events/GuerraNuclearCreator.java` (linhas 3–6).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class GuerraNuclearCreator extends EventoCreator {
    public String nome() { return new GuerraNuclear(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new GuerraNuclear(intensidade); }
}
```

**Trecho F-InvasaoAlienigena — `src/main/java/com/projekttellus/events/InvasaoAlienigenaCreator.java` (linhas 3–6).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class InvasaoAlienigenaCreator extends EventoCreator {
    public String nome() { return new InvasaoAlienigena(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new InvasaoAlienigena(intensidade); }
}
```

**Trecho F-RestauracaoEcologica — `src/main/java/com/projekttellus/events/RestauracaoEcologicaCreator.java` (linhas 3–6).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class RestauracaoEcologicaCreator extends EventoCreator {
    public String nome() { return new RestauracaoEcologica(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new RestauracaoEcologica(intensidade); }
}
```

- `abstract class EventoCreator` não é instanciada diretamente. `criar(double)` devolve `EventoPlanetario`, o contrato do produto.
- Cada `extends EventoCreator` herda o contrato e cada `@Override` fornece a escolha concreta com `new`. O tipo dinâmico do Creator determina qual sobrescrita executa.
- `nome()` existe para apresentação/seleção; nos criadores reais cria um produto de intensidade zero para consultar o nome. Não é o Factory Method discutido e não realiza efeito ambiental.
- Os produtos não são singletons: cada criar instancia outro evento. Intensidade pertence ao produto criado, não é um campo compartilhado no Creator.
- `toString()` do Creator é final e retorna nome; facilita mostrar o item no ComboBox sem transformar UI em fábrica de produtos.

### Contrato do produto e transformação

`EventoPlanetario` declara `String nome()`, `double intensidade()` e `EstadoPlanetario aplicar(EstadoPlanetario estado)`. A implementação comum é:

**Trecho F-base — `src/main/java/com/projekttellus/events/EventoBase.java` (linhas 5–16).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public abstract class EventoBase implements EventoPlanetario {
    private final double intensidade;
    protected EventoBase(double intensidade) {
        this.intensidade = Validacao.intervalo("Intensidade", intensidade, 0, 1);
    }
    public final double intensidade() { return intensidade; }
    public final EstadoPlanetario aplicar(EstadoPlanetario estado) {
        Objects.requireNonNull(estado);
        return intensidade == 0 ? estado : transformar(estado, intensidade);
    }
    protected abstract EstadoPlanetario transformar(EstadoPlanetario estado, double intensidade);
}
```

**Trecho F-produto — `src/main/java/com/projekttellus/events/Asteroide.java` (linhas 3–9).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class Asteroide extends EventoBase {
    public Asteroide(double intensidade) { super(intensidade); }
    public String nome() { return "Impacto de asteroide"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        return Efeitos.catastrofe(e, i, 28, 85, 0, .85, .75, .12, 12);
    }
}
```

**Trecho F-efeitos — `src/main/java/com/projekttellus/events/Efeitos.java` (linhas 8–20).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    static EstadoPlanetario catastrofe(EstadoPlanetario e, double i, double resfriamento,
                                       double poeira, double radiacao, double perdaVegetacao,
                                       double perdaPopulacao, double carbono, double gelo) {
        var c = e.clima();
        double congelado = Math.min(e.hidrosfera().agua(), gelo * i);
        return new EstadoPlanetario(e.atmosfera().transferirCarbono(carbono * i),
                new Clima(limitar(c.temperatura() - resfriamento * i, -150, 200),
                        limitar(c.aerossol() + poeira * i, 0, 100),
                        limitar(c.radiacao() + radiacao * i, 0, 100)),
                new Biosfera(e.biosfera().vegetacao() * (1 - perdaVegetacao * i)),
                new Hidrosfera(e.hidrosfera().agua() - congelado, e.hidrosfera().gelo() + congelado),
                new Civilizacao(Math.round(e.civilizacao().populacao() * (1 - perdaPopulacao * i))), e.anos());
    }
```

- O construtor de EventoBase chama `Validacao.intervalo` para intensidade em [0,1], finita. A UI divide o slider de porcentagem por 100 em `MainController.applyEvent`.
- `aplicar` é final: testa estado não nulo; zero retorna exatamente a mesma referência de entrada; demais valores chamam `transformar`. A aplicação não altera os records recebidos.
- `Asteroide.transformar` escolhe os coeficientes e delega ao helper Efeitos: queda térmica, partículas, vegetação, população, carbono e congelamento. Esses coeficientes são educacionais.
- `congelado = min(água, gelo*i)` impede retirar mais água do que existe. Água e gelo trocam área. População usa `Math.round`; o tempo é preservado.
- `transferirCarbono` transfere fração entre N₂ e CO₂; é uma abstração de composição, não uma reação química validada.
- RestauracaoEcologica reduz perturbações e aproxima variáveis de alvos; não reconstitui necessariamente um estado exato anterior e não ressuscita população. Memento restaura valores exatos de um retrato.
- A estrutura de `EventoBase.aplicar/transformar` lembra Template Method, mas ele não integra os quatro padrões defendidos nem precisa entrar no deck.

### Cliente e ordem real

**Trecho I-aplicar — `src/main/java/com/projekttellus/application/SimulacaoService.java` (linhas 50–58).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public ResultadoOperacao aplicar(Terra terra, EventoCreator creator, double intensidade) {
        var passos = new ArrayList<Passo>();
        var evento = Objects.requireNonNull(creator).criar(intensidade);
        passos.add(new Passo("Evento criado", "O criador de “" + evento.nome() + "” preparou um evento com a intensidade escolhida.", "Factory Method"));
        var snapshot = terra.capturar("Antes: " + evento.nome()); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));
        passos.add(new Passo("Efeitos aplicados ao planeta", "O evento modificou os componentes ambientais; os indicadores serão derivados dessas condições.", "Factory Method"));
        return confirmar(terra, candidato, terra.estado(), evento.nome(), intensidade, snapshot, passos, true);
    }
```

Linha a linha: cria produto pelo Creator → registra passo descritivo → captura estado anterior → registra preservação → calcula efeito e constrói Terra candidata com **o mesmo id** → registra aplicação → chama confirmar. **O produto é criado antes da captura**, embora o cenário precise ter sido construído antes dessa operação. Passos de apresentação são descrições de fatos executados; não são o motor que causa os efeitos.

**Objetos/chamadas:** ComboBox guarda EventoCreator → serviço recebe o Creator selecionado → criar(intensidade) retorna evento concreto → evento.aplicar(estado) despacha transformar → novo EstadoPlanetario → Terra candidata → confirmar.

**Vantagens:** aplicação polimórfica e criação localizada; novo produto segue contratos conhecidos. **Limites/custo:** cinco pares de classes; um evento novo exige produto, Creator e registro no catálogo explícito em `MainController.initialize`. Logo, “adicionar sem mudar nada no sistema” seria exagero. O serviço evita decidir por tipo de produto, mas a composição da UI conhece os criadores.

**Teste escrito:** `SimulationTest.realFactoriesCreateIndependentPolymorphicEvents`, parametrizado pelos cinco criadores, verifica instâncias distintas, efeito, tempo preservado, intensidade zero e valores inválidos. Não executado nesta frente.

**Perguntas:** por que new no Creator não invalida o padrão? Quem escolhe o Creator? Factory Method e Builder resolvem o mesmo problema? O que muda para um sexto evento?

**UML slide 6:** [06-factory-method.mmd](uml-apresentacao/06-factory-method.mmd), cinco pares explícitos. Herança de Creator e EventoBase, realização de EventoPlanetario e dependência de criação são relações diferentes.

## 3. Observer — propósito, problema e solução

**Classificação:** comportamento. Alertas, estatísticas e interface precisam reagir a uma mesma mudança confirmada. Se Terra chamasse todas as telas e monitores concretos, teria responsabilidades e dependências de apresentação. O serviço publica um dado de mudança em um bus; assinantes obedecem ao contrato `SimulacaoObserver`. [Referência conceitual: Observer](https://refactoring.guru/design-patterns/observer).

O publicador é `ObserverBus`, não a entidade Terra. `SimulacaoObserver` tem `nome()` e `aoMudar(MudancaPlanetaria)`. O record de mudança contém UUID, estado, indicadores e descrição, todos não nulos. `SimulacaoService` instancia e inscreve `AlertaObserver` e `EstatisticasObserver` no construtor. A UI é um terceiro assinante quando MainController inicializa; no teste só de serviço são dois.

### Código completo do bus

**Trecho O-bus — `src/main/java/com/projekttellus/patterns/observer/ObserverBus.java` (linhas 8–44).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
public final class ObserverBus {
    private final CopyOnWriteArrayList<SimulacaoObserver> observers = new CopyOnWriteArrayList<>();
    public AutoCloseable inscrever(SimulacaoObserver observer) {
        Objects.requireNonNull(observer);
        observers.addIfAbsent(observer);
        return () -> remover(observer);
    }
    public void remover(SimulacaoObserver observer) { observers.remove(observer); }
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
    private static String nomeSeguro(SimulacaoObserver observer) {
        try {
            String nome = observer.nome();
            if (nome != null && !nome.isBlank()) return nome;
        } catch (RuntimeException ignorado) {
            // Metadados defeituosos não podem abortar uma mudança já confirmada.
        }
        return "Assinante sem identificação";
    }
    public record Entrega(List<String> notificados, List<String> falhas) {
        public Entrega { notificados = List.copyOf(notificados); falhas = List.copyOf(falhas); }
    }
}
```

- `CopyOnWriteArrayList` permite iterar sobre uma visão estável e alterar inscrições; não torna todos os observadores automaticamente thread-safe.
- `inscrever` rejeita null, evita duplicidade segundo igualdade e devolve um AutoCloseable cuja ação remove o assinante. Não é fechamento do banco.
- `publicar` cria listas locais de nomes, percorre referências do contrato e obtém nomeSeguro.
- `observer.aoMudar(mudanca)` é a chamada polimórfica decisiva. Só após retorno normal o nome entra em notificados.
- `catch (RuntimeException)` registra nome em falhas e loga. O laço prossegue; um observador defeituoso não desfaz SQLite nem invalida os outros. Não captura todo Error nem promete entrega em outro processo.
- `nomeSeguro` tolera nome vazio, null ou exceção de metadado. Isso não transforma falha no callback em entrega bem-sucedida.
- `Entrega` usa `List.copyOf`, bloqueando mutação das listas retornadas.
- O bus é **síncrono**: callbacks lentos retardam a operação. Não há fila persistente, retry, broker, garantia exactly-once ou atomicidade entre banco e notificações.

### Dois observadores com responsabilidades distintas

**Trecho O-alertas — `src/main/java/com/projekttellus/patterns/observer/AlertaObserver.java` (linhas 11–24).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public void aoMudar(MudancaPlanetaria m) {
        var lista = new ArrayList<String>();
        if (m.indicadores().humana() < 30) lista.add("Condições críticas para humanos.");
        if (m.estado().atmosfera().pressaoParcialOxigenio() < .12) lista.add("Pouco oxigênio disponível para respirar.");
        if (m.estado().clima().radiacao() > 20) lista.add("Radiação elevada; recuperação será lenta.");
        if (m.estado().hidrosfera().agua() < 10) lista.add("Pouca água líquida na superfície.");
        if (m.indicadores().biologica() < 20) lista.add("Potencial biológico muito reduzido.");
        if (m.estado().civilizacao().populacao() > m.indicadores().capacidadeSuporte())
            lista.add("População acima do suporte estimado; recursos limitam o crescimento e causam declínio gradual.");
        if (m.estado().superficieContinental() < 1)
            lista.add("Quase nenhuma terra disponível para uma civilização convencional.");
        if (lista.isEmpty()) lista.add("Condições estáveis neste modelo didático.");
        alertas.put(m.mundoId(), List.copyOf(lista));
    }
```

**Trecho O-estatisticas — `src/main/java/com/projekttellus/patterns/observer/EstatisticasObserver.java` (linhas 9–15).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public void aoMudar(MudancaPlanetaria m) {
        mundos.compute(m.mundoId(), (id, anterior) -> anterior == null
                ? new Estatistica(1, m.indicadores().humana(), m.indicadores().humana())
                : new Estatistica(anterior.mudancas() + 1,
                Math.min(anterior.minima(), m.indicadores().humana()),
                Math.max(anterior.maxima(), m.indicadores().humana())));
    }
```

No monitor, cada condição lê o mesmo evento: habitabilidade humana <30; pO₂ <0,12; radiação >20; água <10; potencial biológico <20; população acima do suporte; continente <1. A mensagem de estabilidade só é adicionada quando não há outro alerta. Uma lista imutável substitui a projeção anterior por UUID.

Estatísticas usa `ConcurrentHashMap.compute`: primeira mudança inicia contagem 1 e extremos iguais ao valor; as seguintes somam 1 e usam min/max. São estatísticas **da sessão**, não uma reconstrução persistida do histórico. Trocar de mundo não mistura os UUIDs. Mapas concorrentes protegem essas projeções; o motor continua serializado no worker da UI.

### Assinante gráfico e limite da entrega

**Trecho O-ui — `src/main/java/com/projekttellus/ui/MainController.java` (linhas 128–137).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

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

O objeto criado por `new SimulacaoObserver() { ... }` implementa a interface; **MainController não a implementa**. O callback roda no worker e chama `Platform.runLater`, que agenda execução na thread FX. O Runnable verifica closed, guarda a mudança no mapa de UI e só renderiza se o UUID corresponder ao mundo atual. O nome entra como notificado quando aoMudar conclui o agendamento, antes de confirmar renderização de pixels.

`MainController.run` submete operações e acessos ao repositório ao executor de uma thread, depois agenda o callback de sucesso na FX. `operate` recebe ResultadoOperacao, atualiza current, mostra a trilha e aciona animação. `close` enfileira remoção da subscription e fechamento do repositório atrás da operação em andamento, sem bloquear a thread FX.

**Objetos/chamadas:** confirmar persiste → atualiza destino → bus.publicar(mudança) → monitor.aoMudar e estatisticas.aoMudar → callback anônimo → runLater → atualização gráfica. `capturar` manual e `salvar` usam notificar=false; não fingir que essas ações provocaram alteração ambiental nos assinantes. Carregar mundo também não publica pelo serviço: a UI calcula/renderiza o estado carregado e aguarda nova mudança na sessão.

**Vantagens:** extensibilidade dos interessados e separação entre domínio e tela; mesma mensagem para responsabilidades distintas. **Limites:** gestão do ciclo de vida das inscrições; custo do CopyOnWrite nas alterações; sequência síncrona; assinante pode falhar depois de o mundo já estar salvo; projeções não substituem o banco.

**Testes escritos:** `SimulationTest.observerUnsubscriptionAndFailuresAreIsolated`; `ApplicationTest.observerWithBrokenNameCannotTurnCommittedChangeIntoFailure`; `manualSnapshotAndSaveDoNotPretendToNotifyObservers`. Não executados nesta frente.

**Perguntas:** quem é Subject? Observer implica threads? O que acontece se um callback falha? Por que salvar não notifica? Como remover a UI ao fechar?

**UML slide 8:** [08-observer.mmd](uml-apresentacao/08-observer.mmd). Dependência Controller→contrato para criar objeto anônimo não é realização Controller→interface.

## 4. Memento — propósito, problema e solução

**Classificação:** comportamento. Uma catástrofe altera vários componentes; aplicar um evento “oposto” não recupera necessariamente o que foi perdido. O originador Terra captura nome e estado completo; o caretaker SimulacaoService escolhe quando preservar/restaurar e coordena a persistência. O Memento é o valor TerraMemento. [Referência conceitual: Memento](https://refactoring.guru/design-patterns/memento).

**Variante conscientemente legível:** TerraMemento é um record público com acesso público a estado(). Não garante a opacidade estrita da apresentação clássica GoF. Sua proteção prática é a imutabilidade do grafo: UUID, String, Instant e os records de domínio não expõem estruturas mutáveis. O código pode compartilhar a referência ao estado antigo, mas não modificá-lo. Um record com uma lista mutável não teria essa garantia automaticamente.

### Record e originador

**Trecho M-record — `src/main/java/com/projekttellus/patterns/memento/TerraMemento.java` (linhas 10–19).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

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

**Trecho M-capturar — `src/main/java/com/projekttellus/domain/Terra.java` (linhas 23–25).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public TerraMemento capturar(String rotulo) {
        return new TerraMemento(UUID.randomUUID(), id, rotulo, nome, Instant.now(), estado);
    }
```

**Trecho M-restaurar — `src/main/java/com/projekttellus/domain/Terra.java` (linhas 26–32).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public void restaurar(TerraMemento memento) {
        Objects.requireNonNull(memento);
        if (!id.equals(memento.mundoId()))
            throw new IllegalArgumentException("Este retrato pertence a outro mundo.");
        nome = memento.nomeMundo();
        estado = memento.estado();
    }
```

- O id do record identifica o retrato; mundoId identifica o mundo de origem. São identidades com finalidades diferentes.
- O construtor compacto valida ids, instante e estado não nulos; valida textos de rótulo e nome. Os accessors públicos são gerados pelo record. toString retorna só o rótulo para a lista gráfica.
- capturar gera id de retrato e timestamp reais e aponta para o estado atual imutável. Não grava no banco por si e não clona todo o grafo.
- restaurar primeiro rejeita null, depois compara o UUID da Terra ao mundoId do snapshot. Não muda o id final da Terra.
- Se a identidade for incompatível, lança IllegalArgumentException antes de reassociar nome/estado.
- As duas últimas atribuições restauram nome e o EstadoPlanetario completo, incluindo atmosfera, clima, biosfera, hidrosfera, civilização e anos. Não restaura a animação, câmera, estatísticas da sessão ou histórico de ações.
- O estado é substituível em Terra; o valor dentro do record continua imutável. Encapsular as referências do objeto vivo não é o mesmo que esconder todos os dados do snapshot.

### Caretaker e segurança da restauração

**Trecho M-servico — `src/main/java/com/projekttellus/application/SimulacaoService.java` (linhas 66–73).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

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

O serviço cria candidato com o id atual e o estado atual; chama candidato.restaurar(snapshot) antes de qualquer gravação, validando a identidade. Captura também o estado que está prestes a ser substituído, com rótulo “Antes da restauração”. A chamada confirmar persiste o candidato restaurado e o retrato de retorno na mesma transação e, só depois, atualiza o destino e notifica. Assim, restaurar acrescenta uma ação ao histórico; não apaga o passado.

**Objetos/chamadas:** Terra atual → capturar → TerraMemento; usuário escolhe retrato carregado do repositório → service.restaurar → Terra candidata.restaurar → captura do retorno → salvar → destino atualizado → Observer.

**Memento versus persistência:** captura em memória já é snapshot; SQLite acrescenta durabilidade e consulta. O repositório armazena e reconstitui colunas dos componentes, não executa uma sequência de eventos para reconstruir o mundo. Logo, não é Event Sourcing. Duplicar usa UUID novo e retrato próprio, mesmo compartilhando inicialmente valores imutáveis; restaurar retrato do mundo original na cópia é rejeitado.

**Vantagens:** restauração exata dos valores guardados sem operação inversa; isolamento por mundo; memória pode compartilhar valores imutáveis. **Limites:** snapshots e histórico crescem; política de retenção não é apresentada como existente; record público permite leitura, não oferece sigilo; restrição de UUID não é autenticação. Não existe pilha automática de desfazer/refazer por Ctrl+Z: há seleção explícita de retratos e captura do retorno.

**Testes escritos:** `DomainTest.mementoIsImmutableCompleteAndWorldBound`; `ApplicationTest.foreignMementoRejectedBeforeAnyHistoryOrStateChanges`; `fullDemonstrationSurvivesRestartAndKeepsWorldsIndependent`. Não executados nesta frente.

**Perguntas:** por que não há deep copy? Que campos voltam? Por que SQLite não é o Memento? Caretaker pode ler estado? O que acontece ao usar um snapshot de outro mundo?

**UML slide 10:** [10-memento.mmd](uml-apresentacao/10-memento.mmd). Evitar alegações de “ninguém fora de Terra lê o snapshot”.

## 5. Integração: persistir, publicar e apresentar

O ponto que liga os padrões à persistência é confirmar:

**Trecho I-confirmar — `src/main/java/com/projekttellus/application/SimulacaoService.java` (linhas 99–117).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

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

**Trecho P-salvar — `src/main/java/com/projekttellus/persistence/SqliteMundoRepository.java` (linhas 84–107).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public synchronized void salvar(Terra terra, RegistroEvento evento, TerraMemento snapshot) {
        executar("Não foi possível salvar o mundo e seus registros.", () -> {
            Objects.requireNonNull(terra, "Mundo");
            UUID id = terra.id();
            String nome = terra.nome();
            EstadoPlanetario estado = terra.estado();
            if (evento != null && !id.equals(evento.mundoId()))
                throw new PersistenciaException("O evento pertence a outro mundo.");
            if (snapshot != null && !id.equals(snapshot.mundoId()))
                throw new PersistenciaException("O retrato pertence a outro mundo.");
            TransacaoSqlite.executar(connection, () -> {
                try (var query = connection.prepareStatement(SALVAR_MUNDO)) {
                    query.setString(1, id.toString());
                    query.setString(2, nome);
                    query.setString(3, Instant.now().toString());
                    preencherEstado(query, 4, estado);
                    query.executeUpdate();
                }
                if (evento != null) inserirEvento(evento);
                if (snapshot != null) inserirSnapshot(snapshot);
            });
            return null;
        });
    }
```

1. Calcula indicadores do candidato antes de gravar, usando o mesmo ModeloPlanetario da aplicação.
2. Monta RegistroEvento com id novo, mundo id, timestamp, descrição, intensidade e anos.
3. Chama repository.salvar(candidato, registro, snapshot). O contrato MundoRepository exige gravação conjunta; SqliteMundoRepository valida vínculos dos ids e usa TransacaoSqlite.
4. A transação faz salvar mundo, inserir evento quando presente e inserir snapshot quando presente; o helper desabilita autocommit, executa a ação e faz commit. Em SQLException/RuntimeException/Error tenta rollback e relança. Se rollback falha, fecha a conexão sem habilitar autocommit; erros são preservados como suppressed.
5. Após retorno normal de salvar, renomeia e atualiza destino. Essa é a fronteira observada no serviço; não alegar transação distribuída entre memória, banco e GUI.
6. Se notificar é true, publica a mudança já salva; atualiza ultimoMundo e transforma o recibo de cada assinante em passos. Falha auxiliar é descrita sem fingir que a gravação falhou.
7. ResultadoOperacao entrega Terra, estado antes/depois, passos, indicadores e descrição. A UI recebe esse resultado; animações e apresentação reproduzem a operação concluída.

O teste escrito `ApplicationTest.failedTransactionLeavesLiveEarthAndObserversUntouched` induz falha de UPDATE por trigger e compara estado em memória/banco, contagens de histórico/retratos e notificações. Sua existência mostra a intenção verificável; o recibo da execução precisa vir da frente que executa os testes. Não afirmar que esta inspeção cobriu todas as falhas de hardware/commit.

**Ordem para o slide 12:** construção inicial via Builder (outro caso de uso) → ao aplicar: criar evento → capturar estado → gerar candidato e indicadores → persistir → atualizar Terra → notificar → UI apresenta resultado. Restaurar é um caso de uso posterior. O diagrama [12-integracao.mmd](uml-apresentacao/12-integracao.mmd) é sequência da aplicação, não a ordem arbitrária de botões em um desenho genérico.

## 6. Algoritmo: explicação suficiente para a defesa

O modelo é global, didático, determinístico e não calibrado cientificamente. Os padrões organizam objetos e comunicação; os coeficientes estão no motor e produtos, não na definição dos padrões.

- calcular reúne fatores de adequação em [0,1] e usa produto ×100, limitado a 0..100. Não são probabilidades ou médias de opinião.
- H: pressão parcial de O₂, pressão total, CO₂ inspirado, temperatura, água, radiação e partículas. Pressão parcial = pressão × fração do gás; trocar N₂ por He sem mudar pressões relevantes não cria uma penalidade arbitrária.
- B: janela térmica mais ampla, água, atmosfera, radiação, luz e distância ao alvo climático. Não exige O₂ nem vegetação já existente; não prevê surgimento de vida.
- K: suporte estimado depende de continente, produtividade vegetal, água, H e raiz de B; a escala de 16 bilhões é narrativa. C considera também ocupação K/população. Sem continente, K e C são zero, mesmo com H alto.
- avancar valida intervalo 0,01..1000 e tempo total; usa ceil(anos) para que dt ≤1 ano. Mesma entrada e mesmo intervalo produzem a mesma saída; fatiar em chamadas diferentes pode mudar arredondamentos.

**Trecho A-avancar — `src/main/java/com/projekttellus/simulation/ModeloPlanetario.java` (linhas 80–89).** Copiado literalmente; recorte de fonte, não arquivo Java autônomo.

```java
    public static EstadoPlanetario avancar(EstadoPlanetario inicial, double anos) {
        Validacao.intervalo("Avanço de tempo", anos, .01, 1000);
        Validacao.intervalo("Tempo total", inicial.anos() + anos, 0, 1_000_000_000);
        var estado = inicial;
        int passos = (int) Math.ceil(anos);
        double dt = anos / passos;
        for (int i = 0; i < passos; i++) estado = evoluir(estado, dt);
        return new EstadoPlanetario(estado.atmosfera(), estado.clima(), estado.biosfera(),
                estado.hidrosfera(), estado.civilizacao(), inicial.anos() + anos);
    }
```

Dentro de evoluir: aerossol/radiação decaem exponencialmente; temperatura se aproxima do alvo; água e gelo trocam uma reserva total de área constante; recalculam-se condições, vegetação e suporte; população muda gradualmente por ambiente/recursos, com arredondamento e limites. A atmosfera não evolui quimicamente nesse loop. A soma de água+gelo é uma cobertura de superfície, não massa em kg. Sem população inicial, não há nascimento espontâneo. Eventos não avançam anos por si.

**Evidência escrita:** `SimulationTest.deterministicTimeRemovesAerosolsAndConservesWater`; `extremeValidScenariosStayFiniteAfterAllEventsAndTime`; `fractionalAdvanceAtMaximumTimeDoesNotAccumulateClockError`, além de PlanetaryModelTest. Recibos externos lidos nesta frente: `docs/auditoria/experimental-20261115-final/summary.json` e `execution.json` registram 2.026 experimentos, 252.628 checagens, zero falhas e ExitCode 0. São resultados da frente experimental, não execução desta frente nem contagem JUnit. O arquivo de entradas/saídas disponível é `snapshots.csv` nesse diretório, não `docs/auditoria/experimentos.csv`. Conferir as linhas selecionadas para o slide 14 sem confundir estabilidade numérica com validade científica.

## 7. Pacote de trechos selecionados para o deck

| Slide | IDs no manifesto | Uso sugerido |
|---|---|---|
| 5 | B-fluente-deck e B-build-deck | Setter retorna this; build cria UUID e reúne estado |
| 7 | F-contrato, F-Asteroide e I-aplicar | Mostrar criar sobrescrito e duas chamadas do serviço; demais pares listados no UML |
| 9 | O-publicar-deck | Recorte central aoMudar + registro de entrega; bloco completo no estudo mostra catch |
| 11 | M-capturar e M-restaurar | Captura e checagem de mundo antes das atribuições |
| 12 | I-aplicar e I-confirmar | Fonte de conferência da ordem; não encher slide com método completo |
| 13 | A-avancar | Fonte para explicar dt; fórmula/fluxo simples cabe melhor que todas as linhas |

**Conferência do gerador recebido:** `scripts/Build-AcademicDeck.mjs` extrai Java da própria fonte. Seu recorte de Observer termina antes do catch, portanto é fragmento propositalmente incompleto; a nota precisa explicar a continuação. O recorte de Memento começa dentro de restaurar após o null-check; não apresentá-lo como método completo. No slide 12, rótulos originalmente mostravam Memento antes de Factory Method; na operação aplicar a criação ocorre antes da captura. Usar a ordem acima. O slide 6 pode simplificar visualmente, mas precisa representar/identificar os cinco pares, como a fonte Mermaid desta frente. Sem edição do script pela frente acadêmica.

**Notação e relações:** triângulo vazio contínuo de subclasse para classe base = herança; triângulo vazio tracejado de EventoBase/observador para interface = realização; dependência tracejada Creator→produto = cria. Não desenhar Terra compondo exclusivamente EstadoPlanetario, pois snapshots/duplicação podem reter a mesma referência imutável. Não confundir MainController com sua implementação anônima de observador. Na arquitetura, o domínio não depende de SQLite/JavaFX, mas Terra conhece TerraMemento.

## 8. Referências e uso honesto

A consulta às referências serve ao estudo; a demo deve funcionar offline. Definições acima são paráfrases curtas; papéis e limitações são inferências fundamentadas no Java local, não validação do projeto por essas páginas.

- [Catálogo Refactoring Guru em português — referência literal do enunciado](https://refactoring.guru/pt-br/design-patterns/catalog).
- [Guia DevMedia de padrões em Java — referência literal do enunciado](https://www.devmedia.com.br/guia/padroes-de-projeto-em-java/34456).
- [Builder — propósito e estrutura](https://refactoring.guru/design-patterns/builder).
- [Factory Method — criação pelas subclasses](https://refactoring.guru/design-patterns/factory-method).
- [Observer — inscrição e notificação](https://refactoring.guru/design-patterns/observer).
- [Memento — preservação e restauração](https://refactoring.guru/design-patterns/memento). Confrontar a descrição clássica de opacidade com a variante legível real.
- [DevMedia — Conheça os Padrões de Projeto](https://www.devmedia.com.br/conheca-os-padroes-de-projeto/957), apoio para contexto, problema, solução e consequências. Não substitui a evidência do código.

O enunciado autoriza aproveitar IA e exige entender o código na apresentação. Estes materiais tiveram apoio de IA nesta frente; não inferem autoria anterior do projeto nem atestam, por si, a natureza individual da execução. Nesta defesa: Matheus Luiz Bueno, turma 4P, professor EDER MAGALHAES MACHADO, disciplina ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS, 2º Bimestre de 2026. A referência histórica à Prof. Célia foi preservada no enunciado. Para os três níveis de explicação e arguição individual, consulte [DEFESA_CODIGO_JAVA.md](DEFESA_CODIGO_JAVA.md) e [GUIA_DEFESA_INDIVIDUAL.md](GUIA_DEFESA_INDIVIDUAL.md).
