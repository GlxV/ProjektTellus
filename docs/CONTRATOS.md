# Contratos internos de integração

Pacotes todos sob `com.projekttellus`. Java 25. Componentes abaixo são records imutáveis com validação no construtor.

## Domínio

- `domain.Atmosfera(double oxigenio, double nitrogenio, double co2, double helio, double pressao)` — gases em %, soma 100 (tolerância 1e-6), pressão em atm [0,20].
- `domain.Clima(double temperatura, double aerossol, double radiacao)` — °C [-150,200], aerossol e radiação [0,100].
- `domain.Biosfera(double vegetacao)` — % [0,100].
- `domain.Hidrosfera(double agua, double gelo)` — cobertura em % [0,100], soma <=100.
- `domain.Civilizacao(long populacao)` — [0,100000000000].
- `domain.EstadoPlanetario(Atmosfera atmosfera, Clima clima, Biosfera biosfera, Hidrosfera hidrosfera, Civilizacao civilizacao, double anos)` — anos >=0, finito.
- `domain.Terra`: `Terra(UUID id, String nome, EstadoPlanetario estado)`, `id()`, `nome()`, `estado()`, `renomear(String)`, `atualizar(EstadoPlanetario)`, `capturar(String rotulo)` retorna TerraMemento, `restaurar(TerraMemento)` verifica mundoId e restaura nome e estado.
- `domain.TerraBuilder`: construtor sem parâmetros; métodos encadeados `nome(String)`, `atmosfera(Atmosfera)`, `clima(Clima)`, `biosfera(Biosfera)`, `hidrosfera(Hidrosfera)`, `civilizacao(Civilizacao)`, `anos(double)`, `build()` retorna nova Terra com UUID. Defaults terrestres: gases 21/78.96/.04/0, 1 atm; clima 15/0/0; vegetação 65; água 71/gelo 3; população 8 bilhões; ano zero.
- `patterns.memento.TerraMemento(UUID id, UUID mundoId, String rotulo, String nomeMundo, Instant criadoEm, EstadoPlanetario estado)`.

## Persistência (tarefa delegada)

- `persistence.RegistroEvento(UUID id, UUID mundoId, Instant instante, String descricao, double intensidade, double anos)`.
- `persistence.MundoResumo(UUID id, String nome, double anos, Instant atualizadoEm)`.
- `persistence.MundoRepository extends AutoCloseable`: `void salvar(Terra terra, RegistroEvento evento, TerraMemento snapshot)` aceita evento/snapshot nulos; transação atômica. `List<MundoResumo> listar()`, `Optional<Terra> carregar(UUID id)`, `List<RegistroEvento> historico(UUID id)`, `List<TerraMemento> snapshots(UUID id)`, `void excluir(UUID id)`, `void close()` (sem checked exceptions).
- `persistence.SqliteMundoRepository(Path arquivo)` implementa tudo, cria diretório/schema. SQLite relational: estado armazenado em colunas, sem JSON obrigatório. Erros em `PersistenciaException extends RuntimeException`.

## API para interface (agente principal implementa)

- `application.SimulacaoService(MundoRepository repository)`; `repository()` disponível; `observerBus()`; `alertas()` retorna List<String>.
- `criar(TerraBuilder builder)`, `alterar(Terra terra, EstadoPlanetario estado)`, `aplicar(Terra terra, events.EventoCreator creator, double intensidade)`, `avancar(Terra terra, double anos)`, `restaurar(Terra terra, TerraMemento snapshot)`, `salvar(Terra terra)`, `capturar(Terra terra, String rotulo)`, `duplicar(Terra terra, String nome)` retornam `application.ResultadoOperacao`.
- `ResultadoOperacao(Terra terra, EstadoPlanetario antes, EstadoPlanetario depois, List<Passo> passos, simulation.Indicadores indicadores, String descricao)`.
- `Passo(String titulo, String detalhe, String padrao)`; vazio para nenhum padrão.
- `simulation.Indicadores(double humana, double biologica)` (%); `simulation.ModeloPlanetario.calcular(EstadoPlanetario)` estático.
- `events.EventoCreator`: abstrato, `String nome()`, `EventoPlanetario criar(double intensidade)` Factory Method sobrescrito.
- Criadores em `events`: `AsteroideCreator`, `SupervulcaoCreator`, `GuerraNuclearCreator`, `InvasaoAlienigenaCreator`, `RestauracaoEcologicaCreator`. `toString()` retorna nome amigável. Intensidade [0,1].
- Observer: `patterns.observer.SimulacaoObserver` com `String nome()` e `void aoMudar(MudancaPlanetaria mudanca)`; `ObserverBus.inscrever(observer)` retorna AutoCloseable, `remover(observer)`.

## Responsabilidade UI (tarefa delegada)

`ui.MainController`, `ui.PlanetView` e helpers UI, `fxml/main.fxml`, `styles/app.css`; atualizar `ProjektTellusApp` para carregar controller e liberar recursos no fechamento. A interface cria serviço/repositório e usa a API acima; banco default `data/projekttellus.db`, override system property `projekttellus.db`. Não alterar núcleo/persistência/pom/tests de outros responsáveis. Interface FXML mostra modo apresentação com Passos reais, avanço manual/replay, comparações. Operações SQLite em executor de uma thread com busy state. UI deve inscrever observador com responsabilidade de atualização real.
