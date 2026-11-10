package com.projekttellus.ui;

import com.projekttellus.application.*;
import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.patterns.memento.TerraMemento;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.persistence.*;
import com.projekttellus.simulation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** UI coordination only. Domain operations and every repository access share one worker. */
public final class MainController implements AutoCloseable {
    @FXML private HBox header;
    @FXML private BorderPane interactionRoot;
    @FXML private StackPane globeHost, presentationHost;
    @FXML private VBox environmentHost;
    @FXML private ListView<MundoResumo> worldList;
    @FXML private ListView<RegistroEvento> historyList;
    @FXML private ListView<TerraMemento> snapshotList;
    @FXML private Label worldCount, worldTitle, yearLabel, humanValue, bioValue, alertLabel, observerLabel, statusLabel, intensityLabel;
    @FXML private ProgressBar humanBar, bioBar;
    @FXML private ProgressIndicator busyIndicator;
    @FXML private Button applyButton;
    @FXML private ToggleButton presentationToggle;
    @FXML private CheckBox lightToggle;
    @FXML private ComboBox<EventoCreator> eventChoice;
    @FXML private Slider intensitySlider;
    @FXML private Spinner<Integer> yearsSpinner;
    @FXML private TabPane activityTabs;
    @FXML private Tab presentationTab;
    @FXML private SplitPane mainSplit;

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "tellus-operacoes-locais"); thread.setDaemon(false); return thread;
    });
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final BooleanProperty ready = new SimpleBooleanProperty();
    private final BooleanProperty hasWorld = new SimpleBooleanProperty();
    private final Map<UUID, MudancaPlanetaria> observed = new HashMap<>(); // FX thread only
    private final EnvironmentEditor editor = new EnvironmentEditor();
    private PlanetView globe;
    private PresentationPane presentation;
    private SimulacaoService service; // mutations on worker; only thread-safe observer projections read on FX
    private AutoCloseable subscription;
    private Terra current;
    private volatile boolean closed;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss").withZone(ZoneId.systemDefault());

    @FXML private void initialize() {
        globe = new PlanetView(); globeHost.getChildren().add(globe);
        presentation = new PresentationPane(state -> {
            globe.update(state);
            statusLabel.setText("Comparação visual da última ação · O estado salvo do mundo permanece no resultado final.");
        }, this::demonstrate);
        presentationHost.getChildren().add(presentation);
        activityTabs.getSelectionModel().selectedItemProperty().addListener((o, previous, selected) -> {
            boolean presenting = selected == presentationTab;
            presentationToggle.setSelected(presenting);
            mainSplit.setDividerPositions(presenting ? .48 : .63);
            if (previous == presentationTab && !presenting) {
                presentation.stop();
                if (current != null) globe.update(current.estado());
                statusLabel.setText("Exibindo o estado atual confirmado do mundo.");
            }
        });
        environmentHost.getChildren().add(editor);
        environmentHost.disableProperty().bind(hasWorld.not());
        interactionRoot.disableProperty().bind(busy.or(ready.not())); header.disableProperty().bind(busy);
        busyIndicator.visibleProperty().bind(busy); busyIndicator.managedProperty().bind(busy);
        applyButton.disableProperty().bind(editor.validProperty().not().or(hasWorld.not()));
        lightToggle.selectedProperty().addListener((o, a, b) -> globe.setSunlight(b));
        eventChoice.getItems().setAll(new AsteroideCreator(), new SupervulcaoCreator(), new GuerraNuclearCreator(), new InvasaoAlienigenaCreator(), new RestauracaoEcologicaCreator());
        eventChoice.getSelectionModel().selectFirst();
        intensitySlider.valueProperty().addListener((o, a, b) -> intensityLabel.setText(Math.round(b.doubleValue()) + "%"));
        yearsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10, 10)); yearsSpinner.setEditable(false);
        worldList.setPlaceholder(new Label("Nenhum mundo ainda.\nCrie sua primeira Terra."));
        historyList.setPlaceholder(new Label("As ações confirmadas aparecem aqui."));
        snapshotList.setPlaceholder(new Label("Nenhum retrato preservado neste mundo."));
        worldList.setFixedCellSize(-1); historyList.setFixedCellSize(-1); snapshotList.setFixedCellSize(-1);
        worldList.setCellFactory(list -> new DetailCell<>(list,
                item -> (current != null && current.id().equals(item.id()) ? "● " : "") + item.nome(), item -> "Ano " + f(item.anos())));
        historyList.setCellFactory(list -> new DetailCell<>(list, RegistroEvento::descricao,
                item -> DATE.format(item.instante()) + "  ·  Ano " + f(item.anos()) + "  ·  Intensidade " + Math.round(item.intensidade() * 100) + "%"));
        snapshotList.setCellFactory(list -> new DetailCell<>(list, TerraMemento::rotulo,
                item -> DATE.format(item.criadoEm()) + "  ·  Ano " + f(item.estado().anos())));
        worldList.setOnMouseClicked(e -> { if (e.getClickCount() == 2) loadWorld(); });
        run("Abrindo biblioteca local…", () -> {
            MundoRepository repository = new SqliteMundoRepository(Path.of(System.getProperty("projekttellus.db", "data/projekttellus.db")));
            service = new SimulacaoService(repository);
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
            return repository.listar();
        }, worlds -> {
            ready.set(true); populateWorlds(worlds);
            statusLabel.setText(worlds.isEmpty() ? "Biblioteca pronta · Crie seu primeiro mundo." : "Biblioteca pronta · Selecione um mundo e carregue para continuar.");
        });
    }

    private Window owner() { return header.getScene() == null ? null : header.getScene().getWindow(); }
    private boolean requireWorld() {
        if (current != null) return true;
        statusLabel.setText("Crie ou carregue um mundo para usar esta ação."); return false;
    }
    private boolean discardDraft() {
        return !editor.isDirty() || UiDialogs.confirm(owner(), "Descartar edição pendente?", "Os valores digitados ainda não foram aplicados. Deseja continuar usando o estado confirmado?");
    }

    @FXML private void createWorld() {
        if (!discardDraft()) return;
        new BuilderDialog(owner()).showAndWait().ifPresent(builder -> operate("Construindo seu mundo…", () -> service.criar(builder)));
    }
    @FXML private void loadWorld() {
        MundoResumo selected = worldList.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Selecione um mundo da biblioteca para carregar."); return; }
        if (!UiDialogs.confirm(owner(), "Carregar “" + selected.nome() + "”?", "O cenário salvo será aberto. Edições ambientais ainda não aplicadas serão descartadas.")) return;
        run("Carregando mundo…", () -> {
            Terra terra = service.repository().carregar(selected.id()).orElseThrow(() -> new IllegalArgumentException("Este mundo não está mais na biblioteca. Atualize ou crie outro cenário."));
            return new Loaded(terra, library(terra.id()));
        }, loaded -> {
            current = loaded.terra(); hasWorld.set(true); presentation.clear();
            renderCurrent(); showLibrary(loaded.library());
            statusLabel.setText("“" + current.nome() + "” carregado da biblioteca local.");
        });
    }
    @FXML private void renameWorld() {
        if (!requireWorld() || !discardDraft()) return;
        UiDialogs.name(owner(), "Renomear “" + current.nome() + "”", current.nome()).ifPresent(name -> operate("Salvando novo nome…", () -> service.renomear(current, name)));
    }
    @FXML private void duplicateWorld() {
        if (!requireWorld() || !discardDraft()) return;
        UiDialogs.name(owner(), "Duplicar mundo atual", shortName(current.nome(), " · cópia")).ifPresent(name -> operate("Criando cópia independente…", () -> service.duplicar(current, name)));
    }
    @FXML private void saveWorld() {
        if (!requireWorld()) return;
        if (editor.isDirty()) { UiDialogs.error(owner(), "Há condições pendentes. Use “Aplicar condições” para validá-las e salvá-las primeiro."); return; }
        if (UiDialogs.confirm(owner(), "Salvar mundo atual?", "Confirmar a gravação de “" + current.nome() + "”? As operações anteriores já foram salvas automaticamente."))
            operate("Salvando mundo…", () -> service.salvar(current));
    }
    @FXML private void deleteWorld() {
        MundoResumo selected = worldList.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Selecione na biblioteca o mundo que deseja excluir."); return; }
        if (!UiDialogs.confirm(owner(), "Excluir “" + selected.nome() + "”?", "O mundo selecionado, seu histórico e seus retratos serão excluídos permanentemente. Os outros mundos serão preservados.")) return;
        run("Excluindo mundo selecionado…", () -> {
            service.repository().excluir(selected.id());
            try { return service.repository().listar(); }
            catch (RuntimeException ex) { ex.printStackTrace(System.err); return null; }
        }, worlds -> {
            observed.remove(selected.id());
            if (current != null && current.id().equals(selected.id())) {
                current = null; hasWorld.set(false); presentation.clear(); historyList.getItems().clear(); snapshotList.getItems().clear();
                worldTitle.setText("Seu próximo mundo começa aqui"); yearLabel.setText("ANO 0"); humanValue.setText("—"); bioValue.setText("—"); humanBar.setProgress(0); bioBar.setProgress(0);
                editor.setState(new TerraBuilder().build().estado()); globe.update(new TerraBuilder().build().estado());
                alertLabel.setText("Mundo excluído. Crie ou carregue outro cenário."); observerLabel.setText("Observer · aguardando mudanças");
            }
            if (worlds != null) populateWorlds(worlds);
            else { worldList.getItems().removeIf(w -> w.id().equals(selected.id())); worldCount.setText(Integer.toString(worldList.getItems().size())); }
            statusLabel.setText("Mundo excluído da biblioteca local." + (worlds == null ? " A biblioteca não pôde ser relida; reabra o aplicativo para atualizar a lista." : ""));
        });
    }
    @FXML private void applyEnvironment() {
        if (!requireWorld()) return;
        try { var state = editor.read(current.estado().anos()); operate("Aplicando condições ambientais…", () -> service.alterar(current, state)); }
        catch (IllegalArgumentException ex) { UiDialogs.error(owner(), ex.getMessage()); }
    }
    @FXML private void applyEvent() {
        if (!requireWorld() || !discardDraft()) return;
        EventoCreator creator = eventChoice.getValue(); double intensity = intensitySlider.getValue() / 100;
        operate("Aplicando evento ao mundo simulado…", () -> service.aplicar(current, creator, intensity));
    }
    @FXML private void advanceTime() {
        if (!requireWorld() || !discardDraft()) return;
        int years = yearsSpinner.getValue(); operate("Simulando " + years + " anos…", () -> service.avancar(current, years));
    }
    @FXML private void captureSnapshot() {
        if (!requireWorld() || !discardDraft()) return;
        UiDialogs.name(owner(), "Preservar retrato do estado confirmado", "Retrato · ano " + f(current.estado().anos()))
                .ifPresent(name -> operate("Preservando retrato completo…", () -> service.capturar(current, name)));
    }
    @FXML private void restoreSnapshot() {
        if (!requireWorld()) return;
        TerraMemento snapshot = snapshotList.getSelectionModel().getSelectedItem();
        if (snapshot == null) { statusLabel.setText("Selecione um retrato na aba “Retratos salvos”."); return; }
        if (!UiDialogs.confirm(owner(), "Restaurar “" + snapshot.rotulo() + "”?", "Nome, ambiente, população e ano voltarão a esse retrato. O estado atual será preservado em outro retrato; edições não aplicadas serão descartadas. Outros mundos não mudam.")) return;
        operate("Restaurando retrato…", () -> service.restaurar(current, snapshot));
    }
    @FXML private void resetGlobe() { if (current != null) globe.update(current.estado()); globe.reset(); }
    @FXML private void togglePresentation() {
        if (presentationToggle.isSelected()) activityTabs.getSelectionModel().select(presentationTab);
        else activityTabs.getSelectionModel().selectFirst();
    }
    private void demonstrate(String pattern) {
        switch (pattern) {
            case "Builder" -> createWorld();
            case "Factory Method" -> { statusLabel.setText("Escolha um evento e sua intensidade à direita, depois clique em “Aplicar evento”. A criação real aparecerá neste fluxo."); eventChoice.requestFocus(); }
            case "Observer" -> { statusLabel.setText("Altere condições e clique em “Aplicar condições”, ou aplique um evento. O globo e os indicadores receberão a notificação real."); environmentHost.requestFocus(); }
            case "Memento" -> {
                activityTabs.getSelectionModel().select(1);
                statusLabel.setText("Capture um retrato, aplique uma catástrofe e restaure o retrato selecionado. Volte a “Apresentação” para rever os passos reais.");
            }
            default -> { }
        }
    }

    private void operate(String message, Callable<ResultadoOperacao> action) {
        presentation.stop();
        run(message, () -> {
            ResultadoOperacao result = action.call();
            // A refresh failure must never be reported as failure of an already committed operation.
            Library library = null;
            try { library = library(result.terra().id()); }
            catch (RuntimeException ex) { ex.printStackTrace(System.err); }
            return new Completed(result, library);
        }, completed -> {
            current = completed.result().terra(); hasWorld.set(true); renderCurrent(); presentation.show(completed.result());
            if (completed.library() != null) showLibrary(completed.library());
            else { historyList.getItems().clear(); snapshotList.getItems().clear(); }
            statusLabel.setText(completed.result().descricao() + " · Confirmado e salvo." +
                    (completed.library() == null ? " Não foi possível atualizar as listas; recarregue o mundo para consultá-las." : ""));
            if (presentationToggle.isSelected()) activityTabs.getSelectionModel().select(presentationTab);
        });
    }

    private Library library(UUID id) {
        return new Library(service.repository().listar(), service.repository().historico(id), service.repository().snapshots(id));
    }
    private void showLibrary(Library library) {
        populateWorlds(library.worlds()); historyList.getItems().setAll(library.history()); snapshotList.getItems().setAll(library.snapshots());
        if (current != null) worldList.getItems().stream().filter(w -> w.id().equals(current.id())).findFirst().ifPresent(w -> worldList.getSelectionModel().select(w));
    }
    private void populateWorlds(List<MundoResumo> worlds) { worldList.getItems().setAll(worlds); worldCount.setText(Integer.toString(worlds.size())); }
    private void renderCurrent() {
        editor.setState(current.estado()); worldTitle.setText(current.nome()); yearLabel.setText("ANO " + f(current.estado().anos()));
        var notification = observed.get(current.id());
        if (notification != null && notification.estado().equals(current.estado())) renderObserved(notification);
        else { renderState(current.estado(), ModeloPlanetario.calcular(current.estado())); observerLabel.setText("Observer · mundo carregado; aguardando uma nova mudança nesta sessão."); }
        var alerts = service.alertas(current.id());
        alertLabel.setText(alerts.isEmpty() ? "Indicadores didáticos calculados para este cenário. Execute uma alteração para atualizar os monitores da sessão." : String.join("  ·  ", alerts));
    }
    private void renderObserved(MudancaPlanetaria change) {
        renderState(change.estado(), change.indicadores());
        var stats = service.estatisticas().para(change.mundoId());
        observerLabel.setText("Observer · globo e indicadores atualizados por notificação real." + (stats == null ? "" :
                "\nSessão: " + stats.mudancas() + " mudanças · humanos " + f(stats.minima()) + "% a " + f(stats.maxima()) + "%"));
    }
    private void renderState(EstadoPlanetario state, Indicadores indicators) {
        globe.update(state); humanValue.setText(f(indicators.humana()) + "%"); bioValue.setText(f(indicators.biologica()) + "%");
        humanBar.setProgress(indicators.humana() / 100); bioBar.setProgress(indicators.biologica() / 100);
    }
    private <T> void run(String message, Callable<T> action, Consumer<T> success) {
        if (closed || busy.get()) return;
        presentation.stop();
        busy.set(true); statusLabel.setText(message);
        worker.submit(() -> {
            try {
                T result = action.call();
                Platform.runLater(() -> { if (closed) return; busy.set(false); success.accept(result); });
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                Platform.runLater(() -> {
                    if (closed) return; busy.set(false);
                    String text = ex instanceof IllegalArgumentException ? ex.getMessage() :
                            "Não foi possível acessar a biblioteca local. Confira a permissão e o espaço na pasta do aplicativo e tente novamente. Se outro aplicativo estiver usando a biblioteca, feche-o.";
                    statusLabel.setText(ready.get() ? "A ação não foi concluída. Seu estado anterior foi preservado." : "Biblioteca indisponível. Feche e reabra o aplicativo após conferir a pasta de dados.");
                    UiDialogs.error(owner(), text);
                });
            }
        });
    }
    /** Closing queues resource release behind any in-flight transaction; never blocks the FX thread. */
    @Override public void close() {
        if (closed) return; closed = true; presentation.close();
        worker.submit(() -> {
            try { if (subscription != null) subscription.close(); }
            catch (Exception ex) { ex.printStackTrace(System.err); }
            finally {
                try { if (service != null) service.repository().close(); }
                catch (RuntimeException ex) { ex.printStackTrace(System.err); }
            }
        });
        worker.shutdown();
    }
    public boolean canClose() { return !editor.isDirty() || UiDialogs.confirm(owner(), "Fechar com edição pendente?", "Os valores não aplicados serão descartados. Todas as operações confirmadas já estão salvas."); }
    private static String shortName(String name, String suffix) { return name.substring(0, Math.min(name.length(), 100 - suffix.length())) + suffix; }
    private static String f(double value) { return String.format(Locale.of("pt", "BR"), "%.1f", value); }
    private record Library(List<MundoResumo> worlds, List<RegistroEvento> history, List<TerraMemento> snapshots) { }
    private record Loaded(Terra terra, Library library) { }
    private record Completed(ResultadoOperacao result, Library library) { }
}
