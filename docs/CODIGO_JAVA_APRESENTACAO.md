# Código Java completo para estudo

Fontes da implementação do commit base `3e153f0`, exportadas diretamente do checkout. Os blocos são classes completas; dependem dos imports e demais fontes do projeto para compilar. A explicação detalhada está em [ESTUDO_PADROES.md](ESTUDO_PADROES.md).

Não ler tudo durante a apresentação. Comece por TerraBuilder, EventoCreator/AsteroideCreator, ObserverBus/SimulacaoObserver e Terra/TerraMemento. Depois acompanhe a coordenação em SimulacaoService.

## TerraBuilder

Caminho: `src/main/java/com/projekttellus/domain/TerraBuilder.java`. SHA256: `343f8f91b81ea5adec70d88ab333933d7b653361dad55f261e321666a06ac4c5`.

```java
package com.projekttellus.domain;
import java.util.UUID;

/** Builder de cenários: defaults consistentes e parâmetros opcionais validados no domínio. */
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

## Terra

Caminho: `src/main/java/com/projekttellus/domain/Terra.java`. SHA256: `9485f4f340428b4eceace5edd0bf02bad52f4f375585d79b052c8149df6d285c`.

```java
package com.projekttellus.domain;

import com.projekttellus.patterns.memento.TerraMemento;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Originador do Memento: identidade estável e posse exclusiva do estado atual. */
public final class Terra {
    private final UUID id;
    private String nome;
    private EstadoPlanetario estado;
    public Terra(UUID id, String nome, EstadoPlanetario estado) {
        this.id = Objects.requireNonNull(id);
        this.nome = Validacao.texto("Nome do mundo", nome);
        this.estado = Objects.requireNonNull(estado);
    }
    public UUID id() { return id; }
    public String nome() { return nome; }
    public EstadoPlanetario estado() { return estado; }
    public void renomear(String nome) { this.nome = Validacao.texto("Nome do mundo", nome); }
    public void atualizar(EstadoPlanetario estado) { this.estado = Objects.requireNonNull(estado); }
    public TerraMemento capturar(String rotulo) {
        return new TerraMemento(UUID.randomUUID(), id, rotulo, nome, Instant.now(), estado);
    }
    public void restaurar(TerraMemento memento) {
        Objects.requireNonNull(memento);
        if (!id.equals(memento.mundoId()))
            throw new IllegalArgumentException("Este retrato pertence a outro mundo.");
        nome = memento.nomeMundo();
        estado = memento.estado();
    }
}
```

## EstadoPlanetario

Caminho: `src/main/java/com/projekttellus/domain/EstadoPlanetario.java`. SHA256: `4c12964bf0fbf4fe35f84a7365a98c2f15c333f31bff9fc566b2da9afe131640`.

```java
package com.projekttellus.domain;
import java.util.Objects;

/** Grafo de valores profundamente imutável, sem dependências de interface ou banco. */
public record EstadoPlanetario(Atmosfera atmosfera, Clima clima, Biosfera biosfera,
                               Hidrosfera hidrosfera, Civilizacao civilizacao, double anos) {
    public EstadoPlanetario {
        Objects.requireNonNull(atmosfera); Objects.requireNonNull(clima);
        Objects.requireNonNull(biosfera); Objects.requireNonNull(hidrosfera);
        Objects.requireNonNull(civilizacao);
        Validacao.intervalo("Tempo", anos, 0, 1_000_000_000);
    }
    /** Percentual seco e não glacial da superfície total, disponível para ocupação. */
    public double superficieContinental() { return Math.max(0, 100 - hidrosfera.agua() - hidrosfera.gelo()); }
    /** Vegetação é uma fração do continente, nunca uma segunda cobertura planetária sobre o oceano. */
    public double superficieVegetada() { return superficieContinental() * biosfera.vegetacao() / 100; }
}
```

## TerraMemento

Caminho: `src/main/java/com/projekttellus/patterns/memento/TerraMemento.java`. SHA256: `2aced3e34a159a45de80380bcca911be3b5720bb3c3620f096939df5cb91f868`.

```java
package com.projekttellus.patterns.memento;

import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.domain.Validacao;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Retrato imutável; nenhuma estrutura mutável é compartilhada com o originador. */
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

## EventoCreator

Caminho: `src/main/java/com/projekttellus/events/EventoCreator.java`. SHA256: `c34f81f2a55d79197dff5bbbcac350cb37a5962216447d04d825cb6ea9bf2c77`.

```java
package com.projekttellus.events;

/** Creator do Factory Method. Cada subclasse decide qual produto concreto criar. */
public abstract class EventoCreator {
    public abstract String nome();
    public abstract EventoPlanetario criar(double intensidade);
    @Override public final String toString() { return nome(); }
}
```

## EventoPlanetario

Caminho: `src/main/java/com/projekttellus/events/EventoPlanetario.java`. SHA256: `31da8902cb5c800bc2645035816a97e881eab3e732d7d8dc3f6c61838e1ef854`.

```java
package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public interface EventoPlanetario {
    String nome();
    double intensidade();
    EstadoPlanetario aplicar(EstadoPlanetario estado);
}
```

## EventoBase

Caminho: `src/main/java/com/projekttellus/events/EventoBase.java`. SHA256: `6125c5541ff729a14a2770363a8a2d53b6a051261919fc22b3d35a04ebaef355`.

```java
package com.projekttellus.events;
import com.projekttellus.domain.*;
import java.util.Objects;

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

## AsteroideCreator

Caminho: `src/main/java/com/projekttellus/events/AsteroideCreator.java`. SHA256: `ba546cf8db530241b9b79c855234bfa42094dd2bfbded696fbe1e08bd9eb6e90`.

```java
package com.projekttellus.events;

public final class AsteroideCreator extends EventoCreator {
    public String nome() { return new Asteroide(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new Asteroide(intensidade); }
}
```

## Asteroide

Caminho: `src/main/java/com/projekttellus/events/Asteroide.java`. SHA256: `e8a8ebfa8fd631df603f9c9bed0a31542c31cb405288231b5c22e353643dc55b`.

```java
package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public final class Asteroide extends EventoBase {
    public Asteroide(double intensidade) { super(intensidade); }
    public String nome() { return "Impacto de asteroide"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        return Efeitos.catastrofe(e, i, 28, 85, 0, .85, .75, .12, 12);
    }
}
```

## SimulacaoObserver

Caminho: `src/main/java/com/projekttellus/patterns/observer/SimulacaoObserver.java`. SHA256: `587daed61d4603111988e9f5f57fe44b8309db2445b64754cf8dc3b4cdb2e94b`.

```java
package com.projekttellus.patterns.observer;
public interface SimulacaoObserver {
    String nome();
    void aoMudar(MudancaPlanetaria mudanca);
}
```

## ObserverBus

Caminho: `src/main/java/com/projekttellus/patterns/observer/ObserverBus.java`. SHA256: `a55341166220dc09bb3cc2b7b4a5f768f941bba18fb00de07aad829cd2c0dd57`.

```java
package com.projekttellus.patterns.observer;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Publicador desacoplado: uma falha não impede os outros assinantes. */
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

## MudancaPlanetaria

Caminho: `src/main/java/com/projekttellus/patterns/observer/MudancaPlanetaria.java`. SHA256: `2c23fe09174c55723276e80fe72ce8f4448caa0acf75506a44e4a49ff760512d`.

```java
package com.projekttellus.patterns.observer;
import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.simulation.Indicadores;
import java.util.UUID;
import java.util.Objects;
public record MudancaPlanetaria(UUID mundoId, EstadoPlanetario estado, Indicadores indicadores, String descricao) {
    public MudancaPlanetaria {
        Objects.requireNonNull(mundoId); Objects.requireNonNull(estado);
        Objects.requireNonNull(indicadores); Objects.requireNonNull(descricao);
    }
}
```

## AlertaObserver

Caminho: `src/main/java/com/projekttellus/patterns/observer/AlertaObserver.java`. SHA256: `58fae5084a11321133d1287c7b8ca6328ac15bf606b7fcf6f7c52c9c63282c98`.

```java
package com.projekttellus.patterns.observer;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Subsistema de alertas: produz recomendações distintas da renderização da interface. */
public final class AlertaObserver implements SimulacaoObserver {
    private final ConcurrentHashMap<UUID, List<String>> alertas = new ConcurrentHashMap<>();
    public String nome() { return "Monitor de condições de vida"; }
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
    public List<String> para(UUID id) { return alertas.getOrDefault(id, List.of()); }
}
```

## EstatisticasObserver

Caminho: `src/main/java/com/projekttellus/patterns/observer/EstatisticasObserver.java`. SHA256: `7c7af9b4e05c04bfb7e3682f215f51face8442d4df19d01ca3f21564e140a585`.

```java
package com.projekttellus.patterns.observer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Projeção de sessão: registra quantidade de mudanças e extremos de habitabilidade por mundo. */
public final class EstatisticasObserver implements SimulacaoObserver {
    private final ConcurrentHashMap<UUID, Estatistica> mundos = new ConcurrentHashMap<>();
    public String nome() { return "Estatísticas da sessão"; }
    public void aoMudar(MudancaPlanetaria m) {
        mundos.compute(m.mundoId(), (id, anterior) -> anterior == null
                ? new Estatistica(1, m.indicadores().humana(), m.indicadores().humana())
                : new Estatistica(anterior.mudancas() + 1,
                Math.min(anterior.minima(), m.indicadores().humana()),
                Math.max(anterior.maxima(), m.indicadores().humana())));
    }
    public Estatistica para(UUID id) { return mundos.get(id); }
    public record Estatistica(long mudancas, double minima, double maxima) { }
}
```

## SimulacaoService

Caminho: `src/main/java/com/projekttellus/application/SimulacaoService.java`. SHA256: `c05e683fcc16ed42661e4bcd5f9d440a948f00902452c1c12e1dfaadbdb9a981`.

```java
package com.projekttellus.application;

import com.projekttellus.domain.*;
import com.projekttellus.events.EventoCreator;
import com.projekttellus.patterns.memento.TerraMemento;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.persistence.*;
import com.projekttellus.simulation.ModeloPlanetario;
import java.time.Instant;
import java.util.*;

/** Casos de uso e caretaker do Memento. SQLite confirma tudo antes da mudança pública.
 * Sem referência a JavaFX nem ao modo Apresentação: os passos descrevem fatos concluídos. */
public final class SimulacaoService {
    private final MundoRepository repository;
    private final ObserverBus observerBus = new ObserverBus();
    private final AlertaObserver monitor = new AlertaObserver();
    private final EstatisticasObserver estatisticas = new EstatisticasObserver();
    private UUID ultimoMundo;

    public SimulacaoService(MundoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        observerBus.inscrever(monitor);
        observerBus.inscrever(estatisticas);
    }
    public MundoRepository repository() { return repository; }
    public ObserverBus observerBus() { return observerBus; }
    public EstatisticasObserver estatisticas() { return estatisticas; }
    public List<String> alertas() { return ultimoMundo == null ? List.of() : monitor.para(ultimoMundo); }
    public List<String> alertas(UUID mundoId) { return monitor.para(mundoId); }

    public ResultadoOperacao criar(TerraBuilder builder) {
        var passos = new ArrayList<Passo>();
        var terra = Objects.requireNonNull(builder).build();
        passos.add(new Passo("Condições iniciais reunidas", "Atmosfera, clima, água, vida e população formam um único cenário.", "Builder"));
        passos.add(new Passo("Cenário validado", "As proporções e os limites foram conferidos antes da construção.", "Builder"));
        passos.add(new Passo("Uma nova Terra foi construída", "O cenário ganhou identidade própria e não compartilha mudanças com outros mundos.", "Builder"));
        var snapshot = terra.capturar("Origem do mundo");
        passos.add(preservado());
        return confirmar(terra, terra, terra.estado(), "Mundo criado", 0, snapshot, passos, true);
    }
    public ResultadoOperacao alterar(Terra terra, EstadoPlanetario estado) {
        Objects.requireNonNull(estado);
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes da alteração ambiental"); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), estado);
        passos.add(new Passo("Condições planetárias alteradas", "Os componentes receberam os valores escolhidos e mantiveram suas regras de consistência.", ""));
        return confirmar(terra, candidato, terra.estado(), "Condições ambientais alteradas", 0, snapshot, passos, true);
    }
    public ResultadoOperacao aplicar(Terra terra, EventoCreator creator, double intensidade) {
        var passos = new ArrayList<Passo>();
        var evento = Objects.requireNonNull(creator).criar(intensidade);
        passos.add(new Passo("Evento criado", "O criador de “" + evento.nome() + "” preparou um evento com a intensidade escolhida.", "Factory Method"));
        var snapshot = terra.capturar("Antes: " + evento.nome()); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));
        passos.add(new Passo("Efeitos aplicados ao planeta", "O evento modificou os componentes ambientais; os indicadores serão derivados dessas condições.", "Factory Method"));
        return confirmar(terra, candidato, terra.estado(), evento.nome(), intensidade, snapshot, passos, true);
    }
    public ResultadoOperacao avancar(Terra terra, double anos) {
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes do avanço do tempo"); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), ModeloPlanetario.avancar(terra.estado(), anos));
        passos.add(new Passo("O tempo avançou", "Clima, gelo, vegetação e população evoluíram em intervalos estáveis e determinísticos.", ""));
        return confirmar(terra, candidato, terra.estado(), "Tempo avançado em " + anos + " anos", 0, snapshot, passos, true);
    }
    public ResultadoOperacao restaurar(Terra terra, TerraMemento snapshot) {
        var candidato = new Terra(terra.id(), terra.nome(), terra.estado());
        candidato.restaurar(snapshot); // Verifica a identidade ANTES de qualquer gravação.
        var passos = new ArrayList<Passo>();
        var retorno = terra.capturar("Antes da restauração"); passos.add(preservado());
        passos.add(new Passo("Uma Terra anterior foi restaurada", "Todos os componentes, o tempo e o nome voltaram ao retrato selecionado. Outros mundos permanecem independentes.", "Memento"));
        return confirmar(terra, candidato, terra.estado(), "Restaurado: " + snapshot.rotulo(), 0, retorno, passos, true);
    }
    public ResultadoOperacao capturar(Terra terra, String rotulo) {
        var snapshot = terra.capturar(rotulo);
        var passos = new ArrayList<Passo>(); passos.add(preservado());
        return confirmar(terra, terra, terra.estado(), "Retrato preservado: " + snapshot.rotulo(), 0, snapshot, passos, false);
    }
    public ResultadoOperacao salvar(Terra terra) {
        return confirmar(terra, terra, terra.estado(), "Mundo salvo", 0, null, new ArrayList<>(), false);
    }
    public ResultadoOperacao renomear(Terra terra, String nome) {
        var candidato = new Terra(terra.id(), nome, terra.estado());
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes da mudança de nome"); passos.add(preservado());
        passos.add(new Passo("Mundo renomeado", "O mundo manteve sua identidade e recebeu o novo nome.", ""));
        return confirmar(terra, candidato, terra.estado(), "Mundo renomeado", 0, snapshot, passos, true);
    }
    public ResultadoOperacao duplicar(Terra terra, String nome) {
        var copia = new Terra(UUID.randomUUID(), nome, terra.estado());
        var passos = new ArrayList<Passo>();
        passos.add(new Passo("Um mundo independente foi criado", "A cópia começa nas mesmas condições, com identidade e retratos próprios.", "Memento"));
        var snapshot = copia.capturar("Origem da cópia"); passos.add(preservado());
        return confirmar(copia, copia, terra.estado(), "Mundo duplicado", 0, snapshot, passos, true);
    }
    private Passo preservado() {
        return new Passo("Estado anterior preservado", "Um retrato imutável completo foi capturado, sem alterar a Terra.", "Memento");
    }
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
}
```

## MundoRepository

Caminho: `src/main/java/com/projekttellus/persistence/MundoRepository.java`. SHA256: `27f586472fae1ac556f6f4c93cca27f5ccc3cc04e6ae36658be87cfe3d2372e6`.

```java
package com.projekttellus.persistence;

import com.projekttellus.domain.Terra;
import com.projekttellus.patterns.memento.TerraMemento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Armazenamento de mundos independentes, sem dependências da interface gráfica. */
public interface MundoRepository extends AutoCloseable {
    /**
     * Persiste o estado atual e, quando presentes, acrescenta evento e snapshot numa só transação.
     * Os dois registros devem pertencer à Terra; seus identificadores são únicos e imutáveis.
     * Uma falha não deixa nenhuma das três gravações aplicada.
     */
    void salvar(Terra terra, RegistroEvento evento, TerraMemento snapshot);

    /** Mundos pela atualização mais recente; lista imutável. */
    List<MundoResumo> listar();

    Optional<Terra> carregar(UUID id);

    /** Eventos em ordem cronológica, com precisão de nanossegundos; lista imutável. */
    List<RegistroEvento> historico(UUID id);

    /** Retratos em ordem de captura; lista imutável. */
    List<TerraMemento> snapshots(UUID id);

    /** Exclui também histórico e snapshots. Um identificador ausente não é erro. */
    void excluir(UUID id);

    /** Idempotente; operações posteriores falham com PersistenciaException. */
    @Override void close();
}
```

## BuilderDialog

Caminho: `src/main/java/com/projekttellus/ui/BuilderDialog.java`. SHA256: `5a4a28763461b0c76bb5341953d2e4f4b02c1159b17f6bb2a6137040139edde3`.

```java
package com.projekttellus.ui;

import com.projekttellus.domain.TerraBuilder;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Window;

/** The same editor collects actual Builder inputs, never a demonstration-only mock. */
public final class BuilderDialog extends Dialog<TerraBuilder> {
    public BuilderDialog(Window owner) {
        UiDialogs.style(this, owner);
        setTitle("Construir um mundo"); setHeaderText("Monte as condições iniciais da sua Terra");
        TextField name = new TextField("Nova Terra"); name.setAccessibleText("Nome do novo mundo");
        EnvironmentEditor editor = new EnvironmentEditor();
        Label explanation = new Label("Builder · Reúna as condições, confira os limites e construa um mundo independente.");
        explanation.setWrapText(true); explanation.getStyleClass().add("notice");
        VBox content = new VBox(12, explanation, new Label("Nome do mundo"), name, editor); content.setPadding(new Insets(16));
        ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true);
        scroll.setMinHeight(0); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportWidth(Math.min(430, WindowLayout.workArea(owner).getWidth() - 100));
        double availableHeight = WindowLayout.workArea(owner).getHeight();
        if (owner != null) availableHeight = Math.min(availableHeight, owner.getHeight());
        scroll.setPrefViewportHeight(Math.max(180, Math.min(590, availableHeight - 210)));
        getDialogPane().setContent(scroll);
        ButtonType create = new ButtonType("Construir e salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(create, ButtonType.CANCEL);
        getDialogPane().lookupButton(create).disableProperty().bind(editor.validProperty().not()
                .or(name.textProperty().isEmpty()).or(name.textProperty().length().greaterThan(100)));
        setResultConverter(button -> {
            if (button != create) return null;
            var state = editor.read(0);
            return new TerraBuilder().nome(name.getText()).atmosfera(state.atmosfera()).clima(state.clima())
                    .biosfera(state.biosfera()).hidrosfera(state.hidrosfera()).civilizacao(state.civilizacao());
        });
        // Reject whitespace-only names before the dialog closes.
        getDialogPane().lookupButton(create).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (name.getText().isBlank()) { name.setPromptText("Informe um nome"); name.clear(); e.consume(); }
        });
        setResizable(true);
    }
}
```
