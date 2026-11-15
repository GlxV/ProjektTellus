package com.projekttellus.ui;

import atlantafx.base.theme.PrimerDark;
import com.projekttellus.application.*;
import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.patterns.memento.TerraMemento;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.persistence.*;
import com.projekttellus.simulation.*;
import javafx.animation.AnimationTimer;
import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Standalone evidence harness: real FXML/controller/SQLite on a private Windows desktop.
 * No JUnit, Robot, global input, shared Maven output, or personal database is used.
 * Reflection observes state and controls the existing animation clock; it never calls a controller action.
 */
public final class AcademicDemoCapture {
    private Path output, db;
    private Stage stage;
    private Parent root;
    private MainController controller;
    private final AtomicReference<Throwable> asyncError = new AtomicReference<>();
    private final List<String> checks = new ArrayList<>(), shots = new ArrayList<>();
    private final StringBuilder measurements = new StringBuilder("scenario,o2,n2,co2,he,pressure,temperature,aerosol,radiation,water,ice,vegetation,population,years,human,biological,civil,capacity,history,snapshots,notifications,alerts\n");
    private final StringBuilder snapshotIndex = new StringBuilder("checkpoint,index_zero_based,position_one_based,id,label,world_name,years\n");
    private final StringBuilder frames = new StringBuilder("event,progress,seconds,effect_nodes,visible_nodes,method\n");
    private final StringBuilder visualValues = new StringBuilder("file,scope,o2,n2,co2,he,pressure,temperature,aerosol,radiation,water,ice,vegetation,population,years,human,biological,civil,capacity\n");
    private static final String NAME = "Terra · demo acadêmica", SAVED = "Antes da catástrofe · demo";

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args[0]);
        try (PrintStream log = new PrintStream(Files.newOutputStream(out.resolve("execution.log")), true, java.nio.charset.StandardCharsets.UTF_8)) {
            System.setOut(log); System.setErr(log);
            var watchdog = Executors.newSingleThreadScheduledExecutor(r -> { var t = new Thread(r, "demo-watchdog"); t.setDaemon(true); return t; });
            watchdog.schedule(() -> { System.err.println("FAILED: watchdog 150 s"); System.exit(2); }, 150, TimeUnit.SECONDS);
            AcademicDemoCapture demo = new AcademicDemoCapture(); demo.output = out;
            demo.db = Path.of(args[1]).resolve("controller-demo.db");
            int status = 1;
            try {
                System.setProperty("projekttellus.db", demo.db.toString());
                Platform.startup(() -> {}); Platform.setImplicitExit(false);
                demo.run(); status = 0;
                Files.writeString(out.resolve("SUCCESS.txt"), "PASS: controller, service comparison, SQLite reopen, layout, five controlled mid-effect frames.\n");
            } catch (Throwable error) {
                error.printStackTrace(); Files.writeString(out.resolve("FAILED.txt"), error.toString());
            } finally {
                try { demo.fx(() -> { if (demo.controller != null) demo.controller.close(); if (demo.stage != null) demo.stage.close(); return null; }); }
                catch (Throwable error) { error.printStackTrace(); }
                demo.flush(); Platform.exit(); watchdog.shutdownNow();
            }
            System.exit(status);
        }
    }

    private <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action); Platform.runLater(task); return task.get(20, TimeUnit.SECONDS);
    }
    private void waitFor(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < end) {
            if (asyncError.get() != null) throw new AssertionError("Falha assíncrona", asyncError.get());
            if (fx(condition::getAsBoolean)) return;
            Thread.sleep(30);
        }
        throw new AssertionError("Operação não terminou em 20 s");
    }
    private void idle() throws Exception { waitFor(() -> !node("interactionRoot").isDisabled()); }
    @SuppressWarnings("unchecked") private <T extends Node> T node(String id) { return (T) root.lookup("#" + id); }
    @SuppressWarnings("unchecked") private <T> ListView<T> list(String id) { return (ListView<T>) node(id); }
    private Button button(String text) { return button(root, text); }
    private Button button(Parent parent, String text) {
        return parent.lookupAll(".button").stream().filter(n -> n instanceof Button b && b.getText().equals(text))
                .map(n -> (Button) n).findFirst().orElseThrow(() -> new AssertionError("Botão ausente: " + text));
    }
    private void fire(String text) throws Exception {
        fx(() -> { Button b = button(text); require(!b.isDisabled(), "habilitado: " + text); b.fire(); return null; }); idle();
    }
    private static Object read(Object target, String name) {
        try { Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
    private PlanetEventAnimator animator() { return (PlanetEventAnimator) read(controller, "animator"); }
    private PlanetView globe() { return (PlanetView) read(controller, "globe"); }
    private Terra current() { return (Terra) read(controller, "current"); }
    private SimulacaoService service() { return (SimulacaoService) read(controller, "service"); }
    private PresentationPane presentation() { return (PresentationPane) read(controller, "presentation"); }
    private void require(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
        checks.add("PASS\t" + description); System.out.println("PASS " + description);
    }
    private void settled() throws Exception {
        waitFor(() -> read(animator(), "afterPixels") != null || !animator().isRunning());
        fx(() -> { if (animator().isRunning()) ((Button)node("skipAnimation")).fire(); return null; });
        waitFor(() -> !animator().isRunning());
    }
    private void tab(int index) throws Exception {
        fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().select(index); root.applyCss(); root.layout(); return null; });
    }
    private void inspector(double value) throws Exception {
        fx(() -> { ((ScrollPane) node("inspectorScroll")).setVvalue(value); root.applyCss(); root.layout(); return null; });
    }

    /** Scheduled on the FX queue even during showAndWait's nested event loop. */
    private void dialog(String action, String name, String capture) throws Exception {
        CompletableFuture.delayedExecutor(300, TimeUnit.MILLISECONDS).execute(() -> Platform.runLater(() -> {
            try {
                DialogPane pane = Window.getWindows().stream().filter(w -> w != stage && w.isShowing())
                        .map(w -> w.getScene().getRoot()).filter(p -> p instanceof DialogPane).map(p -> (DialogPane)p).findFirst().orElseThrow();
                if (name != null) for (Node n : pane.lookupAll(".text-field")) {
                    if (n instanceof TextField t && ("Nome do novo mundo".equals(t.getAccessibleText()) || pane.lookupAll(".text-field").size() == 1)) t.setText(name);
                }
                if (action.equals("＋ Criar mundo")) {
                    EnvironmentEditor editor = (EnvironmentEditor) ((VBox)((ScrollPane)pane.getContent()).getContent()).getChildren().getLast();
                    require(editor.read(0).equals(new TerraBuilder().build().estado()), "Builder real: 21/78.96/.04/0, 1 atm, 15 °C, água 71, gelo 3, vegetação 65, 8 bi");
                }
                if (capture != null) pictureFx(pane, capture);
                ButtonType ok = pane.getButtonTypes().stream().filter(t -> t.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
                Button b = (Button) pane.lookupButton(ok); require(!b.isDisabled(), "diálogo confirma: " + action); b.fire();
            } catch (Throwable error) {
                asyncError.set(error); List.copyOf(Window.getWindows()).stream().filter(w -> w != stage).forEach(Window::hide);
            }
        }));
        fx(() -> { button(action).fire(); return null; }); idle();
        if (asyncError.get() != null) throw new AssertionError(asyncError.get());
    }

    private void picture(String name) throws Exception { fx(() -> { pictureFx(root, name); return null; }); }
    private void pictureFx(Parent subject, String name) throws Exception {
        subject.applyCss(); subject.layout();
        SnapshotParameters parameters=new SnapshotParameters(); parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
        WritableImage image = subject.snapshot(parameters, null);
        BufferedImage png = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < png.getHeight(); y++) for (int x = 0; x < png.getWidth(); x++) png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        Path path = output.resolve(name + ".png");
        require(ImageIO.write(png, "png", path.toFile()), "PNG gravado: " + name);
        shots.add(name + ".png\t" + png.getWidth() + "\t" + png.getHeight());
        EstadoPlanetario state = null;
        String scope = "sem mundo carregado";
        if (subject instanceof PlanetView && current() != null) {
            state=current().estado(); scope="PlanetView auxiliar isolado 768x768; mesmo estado confirmado do controller; fundo transparente";
        } else if (subject == root && current() != null) {
            state = current().estado();
            scope = name.contains("-meio") ? "indicadores/SQLite finais; globo em 50% do efeito visual, sem estado físico intermediário" : "estado confirmado exibido";
        } else if (name.equals("01-builder-dialogo")) {
            state = ((EnvironmentEditor)((VBox)((ScrollPane)((DialogPane)subject).getContent()).getContent()).getChildren().getLast()).read(0);
            scope = "entradas reais do Builder; indicadores derivados de referência, ainda sem mundo salvo";
        } else if (name.contains("uml")) scope = "diagrama auxiliar JavaFX, não UI do aplicativo; sem cenário numérico";
        visualValues.append(csv(name+".png")).append(',').append(csv(scope));
        if (state != null) {
            var a=state.atmosfera(); var c=state.clima(); var i=ModeloPlanetario.calcular(state);
            visualValues.append(',').append(String.join(",", ""+a.oxigenio(), ""+a.nitrogenio(), ""+a.co2(), ""+a.helio(), ""+a.pressao(), ""+c.temperatura(), ""+c.aerossol(), ""+c.radiacao(), ""+state.hidrosfera().agua(), ""+state.hidrosfera().gelo(), ""+state.biosfera().vegetacao(), ""+state.civilizacao().populacao(), ""+state.anos(), ""+i.humana(), ""+i.biologica(), ""+i.sustentabilidade(), ""+i.capacidadeSuporte()));
        } else visualValues.append(",".repeat(17));
        visualValues.append('\n');
    }
    private void visible(ScrollPane scroll, Node child) {
        scroll.applyCss(); scroll.layout();
        Bounds viewport = scroll.lookup(".viewport").localToScene(scroll.lookup(".viewport").getBoundsInLocal());
        Bounds item = child.localToScene(child.getBoundsInLocal());
        require(item.getMinY() >= viewport.getMinY() - 1 && item.getMaxY() <= viewport.getMaxY() + 1, "rolagem alcança " + (child instanceof Button b ? b.getText() : child.getId()));
    }
    private void measure(String scenario, EstadoPlanetario expected) throws Exception {
        fx(() -> {
            var state = current().estado(); var i = ModeloPlanetario.calcular(state);
            require(state.equals(expected), "estado completo GUI = serviço: " + scenario);
            require(((Label)node("humanValue")).getText().equals(String.format(Locale.of("pt", "BR"), "%.1f%%", i.humana())), "rótulo humano = indicador: " + scenario);
            require(((Label)node("bioValue")).getText().equals(String.format(Locale.of("pt", "BR"), "%.1f%%", i.biologica())), "rótulo biológico = indicador: " + scenario);
            require(((Label)node("civilizationValue")).getText().equals(String.format(Locale.of("pt", "BR"), "%.1f%%", i.sustentabilidade())), "rótulo civil = indicador: " + scenario);
            require(Math.abs(((ProgressBar)node("humanBar")).getProgress() - i.humana()/100) < 1e-12, "barra humana = indicador: " + scenario);
            require(Math.abs(((ProgressBar)node("bioBar")).getProgress() - i.biologica()/100) < 1e-12, "barra biológica = indicador: " + scenario);
            require(Math.abs(((ProgressBar)node("civilizationBar")).getProgress() - i.sustentabilidade()/100) < 1e-12, "barra civil = indicador: " + scenario);
            require(((Label)node("observerLabel")).getText().contains("notificação real"), "Observer real visível: " + scenario);
            var a=state.atmosfera(); var c=state.clima();
            var alerts=service().alertas(current().id());
            require(((Label)node("alertLabel")).getText().equals(String.join("  ·  ", alerts)), "alertas GUI = AlertaObserver: " + scenario);
            measurements.append(String.join(",", scenario, ""+a.oxigenio(), ""+a.nitrogenio(), ""+a.co2(), ""+a.helio(), ""+a.pressao(), ""+c.temperatura(), ""+c.aerossol(), ""+c.radiacao(), ""+state.hidrosfera().agua(), ""+state.hidrosfera().gelo(), ""+state.biosfera().vegetacao(), ""+state.civilizacao().populacao(), ""+state.anos(), ""+i.humana(), ""+i.biologica(), ""+i.sustentabilidade(), ""+i.capacidadeSuporte(), ""+list("historyList").getItems().size(), ""+list("snapshotList").getItems().size(), ""+service().estatisticas().para(current().id()).mudancas(), csv(String.join(" | ", alerts)))).append('\n');
            return null;
        });
    }
    private static String csv(String text) { return "\"" + text.replace("\"", "\"\"") + "\""; }
    private void indices(String checkpoint) throws Exception {
        fx(() -> {
            var items = this.<TerraMemento>list("snapshotList").getItems();
            for (int j=0;j<items.size();j++) {
                var m=items.get(j); snapshotIndex.append(checkpoint).append(',').append(j).append(',').append(j+1).append(',').append(m.id()).append(',').append(csv(m.rotulo())).append(',').append(csv(m.nomeMundo())).append(',').append(m.estado().anos()).append('\n');
            } return null;
        });
    }
    private TerraMemento namedSnapshot() { return this.<TerraMemento>list("snapshotList").getItems().stream().filter(m -> m.rotulo().equals(SAVED)).findFirst().orElseThrow(); }
    private void restore() throws Exception {
        tab(1);
        fx(() -> { var l=this.<TerraMemento>list("snapshotList"); l.getSelectionModel().select(namedSnapshot()); l.scrollTo(l.getSelectionModel().getSelectedIndex()); return null; });
        dialog("Restaurar selecionado", null, null); settled();
    }
    private void middle(int index) throws Exception {
        waitFor(() -> read(animator(), "effect") != null);
        fx(() -> {
            PlanetEventAnimator animator=animator();
            AnimationTimer timer=(AnimationTimer)read(animator,"timer"); timer.stop();
            Field started=PlanetEventAnimator.class.getDeclaredField("started"); started.setAccessible(true);
            // A frozen real effect frame is reproducible on inactive desktops; no FPS claim.
            long anchor=1_000_000_000L; started.setLong(animator,anchor);
            Field rendered=PlanetEventAnimator.class.getDeclaredField("rendered"); rendered.setAccessible(true); rendered.setLong(animator,0);
            var plan=(AnimationPlan)read(animator,"plan");
            timer.handle(anchor + (long)(plan.seconds()*.5*1_000_000_000L));
            int count=countNodes(globe().effectLayer(),false), visible=countNodes(globe().effectLayer(),true);
            require(plan.kind().isEvent() && count>1 && visible>1, "efeito real em 50%: " + plan.kind());
            frames.append(plan.kind()).append(",0.5,").append(plan.seconds()).append(',').append(count).append(',').append(visible).append(",real_animator_clock_controlled\n");
            pictureFx(root, String.format(Locale.ROOT,"evento-%d-%s-meio",index+1,plan.kind().name().toLowerCase(Locale.ROOT)));
            return null;
        });
    }
    private static int countNodes(Node node, boolean onlyVisible) {
        if (onlyVisible && !node.isVisible()) return 0;
        int count=1;
        if(node instanceof Parent p) for(Node child:p.getChildrenUnmodifiable()) count+=countNodes(child,onlyVisible);
        return count;
    }

    private void run() throws Exception {
        fx(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((t,e)->asyncError.set(e));
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            root=loader.load(); controller=loader.getController(); stage=new Stage(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root,1366,768)); stage.show(); return null;
        }); idle();
        fx(() -> { require(Platform.isSupported(ConditionalFeature.SCENE3D), "JavaFX SCENE3D disponível"); return null; });
        Files.writeString(output.resolve("environment.txt"), fx(() -> "OS="+System.getProperty("os.name")+" "+System.getProperty("os.arch")+"\nJava="+System.getProperty("java.version")+"\nJavaFX="+System.getProperty("javafx.version")+"\nOutputScale="+stage.getOutputScaleX()+"x"+stage.getOutputScaleY()+"\nDB temporário="+db+"\nDesktop privado; Stage UNDECORATED; nenhum SwitchDesktop.\n"));
        picture("00-vazio-1366x768");
        fx(() -> { stage.setHeight(720); root.applyCss(); root.layout(); return null; });
        waitFor(() -> root.getScene().getWidth()==1366 && root.getScene().getHeight()==720);
        // 48 px reserved for a hypothetical 768p taskbar; app content is 1366x720.
        fx(() -> { require(root.getScene().getWidth()==1366 && root.getScene().getHeight()==720,"viewport lógico 1366x720 (768p menos 48)"); return null; });
        dialog("＋ Criar mundo",NAME,"01-builder-dialogo"); settled();
        EstadoPlanetario baseline=new TerraBuilder().build().estado();
        try (var referenceRepo=new SqliteMundoRepository(db.resolveSibling("reference.db"))) {
            var reference=new SimulacaoService(referenceRepo);
            Terra referenceWorld=reference.criar(new TerraBuilder().nome(NAME)).terra();
            measure("antes",referenceWorld.estado()); picture("02-antes-asteroide");
            tab(1); dialog("＋ Capturar retrato",SAVED,null); settled();
            var referenceSnapshot=reference.capturar(referenceWorld,SAVED).terra().capturar(SAVED);
            fx(() -> { this.<TerraMemento>list("snapshotList").getSelectionModel().select(namedSnapshot()); return null; });
            indices("apos_captura"); picture("03-snapshot-selecionavel");
            fx(() -> { require(namedSnapshot().estado().equals(baseline),"snapshot manual preserva todos os componentes"); return null; });
            tab(0); inspector(1);
            fx(() -> { visible(node("inspectorScroll"),button("Avançar tempo →")); return null; });
            for (int event=0;event<5;event++) {
                int index=event;
                if(event>1) { restore(); reference.restaurar(referenceWorld,referenceSnapshot); }
                tab(0); inspector(1);
                EventoCreator creator=fx(() -> {
                    ComboBox<EventoCreator> choice=node("eventChoice"); require(choice.getItems().size()==5,"cinco criadores disponíveis");
                    choice.getSelectionModel().select(index); ((Slider)node("intensitySlider")).setValue(25);
                    require(choice.getSelectionModel().getSelectedIndex()==index,"seleção evento " + index);
                    return choice.getValue();
                });
                var expected=reference.aplicar(referenceWorld,creator,.25);
                fire("Aplicar evento"); middle(event); settled();
                measure("evento_"+event,expected.depois());
                require(fx(() -> globe().displayedState().equals(expected.depois())),"globo final = serviço evento " + event);
                picture(event==0 ? "04-depois-asteroide" : "evento-"+(event+1)+"-depois");
                if(event==0) {
                    indices("apos_asteroide"); inspectPresentation(expected);
                    restore(); reference.restaurar(referenceWorld,referenceSnapshot);
                    measure("restaurado",referenceWorld.estado()); indices("apos_primeira_restauracao");
                    fx(() -> { this.<TerraMemento>list("snapshotList").getSelectionModel().select(namedSnapshot()); return null; });
                    picture("05-restaurado-ui");
                    require(fx(() -> current().nome().equals(NAME) && current().estado().equals(baseline)),"Memento restaura nome/ambiente/população/ano exatos");
                }
            }
            restore(); reference.restaurar(referenceWorld,referenceSnapshot);
            measure("restaurado_final",referenceWorld.estado());
            // Actual year button, then undo through the same snapshot control.
            inspector(1); fire("Avançar tempo →"); settled(); reference.avancar(referenceWorld,10);
            measure("avanco_10_anos",referenceWorld.estado());
            restore(); reference.restaurar(referenceWorld,referenceSnapshot);
            tab(3); picture("06-indicadores-fatores");
            fx(() -> {
                IndicatorDetails details=(IndicatorDetails)((StackPane)node("indicatorsHost")).getChildren().getFirst();
                require(details.getContent().getLayoutBounds().getWidth()<=details.getViewportBounds().getWidth()+1,"indicadores sem transbordamento horizontal");
                details.setVvalue(1); return null;
            }); picture("07-indicadores-fim");
            navigation();
            dialog("Salvar",null,null); settled();
            UUID id=fx(() -> current().id());
            int histories=fx(() -> list("historyList").getItems().size()), snapshots=fx(() -> list("snapshotList").getItems().size());
            fx(() -> { controller.close(); stage.close(); return null; });
            // A separate JDBC connection reopens the committed database. No copied in-memory state.
            try(var reopened=new SqliteMundoRepository(db)) {
                var loaded=reopened.carregar(id).orElseThrow();
                require(loaded.estado().equals(baseline) && loaded.nome().equals(NAME),"SQLite reaberto: estado/nome íntegros");
                require(reopened.historico(id).size()==histories && reopened.snapshots(id).size()==snapshots,"SQLite reaberto: histórico/retratos íntegros");
                Files.writeString(output.resolve("sqlite-reopen.txt"), "world="+id+"\nhistory="+histories+"\nsnapshots="+snapshots+"\nstate="+loaded.estado()+"\nname="+loaded.nome()+"\n");
            }
            // Reopen through FXML and the real Carregar button as a fresh UI session.
            fx(() -> {
                FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/main.fxml")); root=loader.load(); controller=loader.getController();
                stage=new Stage(StageStyle.UNDECORATED); stage.setScene(new Scene(root,1366,720)); stage.show(); return null;
            }); idle();
            fx(() -> { list("worldList").getSelectionModel().selectFirst(); return null; }); dialog("Carregar selecionado",null,null);
            require(fx(() -> current().estado().equals(baseline)),"Carregar selecionado recupera SQLite em sessão nova");
            require(fx(() -> ((Label)node("observerLabel")).getText().contains("aguardando uma nova mudança")),"carregar não simula notificação Observer");
            tab(1); inspector(1); picture("08-sqlite-reaberto-ui"); indices("reaberto");
            planet("planeta-referencia");
            var scenarios=new LinkedHashMap<String,EstadoPlanetario>();
            scenarios.put("oceano",new TerraBuilder().hidrosfera(new Hidrosfera(100,0)).build().estado());
            scenarios.put("co2",new TerraBuilder().atmosfera(new Atmosfera(21,69,10,0,1)).build().estado());
            scenarios.put("he",new TerraBuilder().atmosfera(new Atmosfera(21,28.96,.04,50,1)).build().estado());
            scenarios.put("aerossol",new TerraBuilder().clima(new Clima(15,85,0)).build().estado());
            scenarios.put("congelado",new TerraBuilder().clima(new Clima(-40,0,0)).hidrosfera(new Hidrosfera(20,70)).biosfera(new Biosfera(5)).build().estado());
            for(var scenario:scenarios.entrySet()) {
                EstadoPlanetario state=scenario.getValue();
                fx(() -> { ((EnvironmentEditor)((VBox)node("environmentHost")).getChildren().getFirst()).setState(state); return null; });
                fire("Aplicar condições"); settled();
                reference.alterar(referenceWorld,state);
                measure("visual_"+scenario.getKey(),referenceWorld.estado());
                tab(0); inspector(0); picture("cenario-"+scenario.getKey()+"-ui"); planet("planeta-"+scenario.getKey());
            }
            restore(); reference.restaurar(referenceWorld,referenceSnapshot);
            diagram();
        }
    }

    private void planet(String name) throws Exception {
        fx(() -> {
            PlanetView view=new PlanetView(); view.update(current().estado());
            Stage isolated=new Stage(StageStyle.UNDECORATED);
            isolated.setScene(new Scene(new StackPane(view),768,768,javafx.scene.paint.Color.TRANSPARENT));
            isolated.show(); view.getParent().applyCss(); view.getParent().layout();
            pictureFx(view,name); isolated.close(); return null;
        });
    }

    private void inspectPresentation(ResultadoOperacao expected) throws Exception {
        fx(() -> { ((ToggleButton)node("presentationToggle")).fire(); return null; });
        require(fx(() -> ((TabPane)node("activityTabs")).getSelectionModel().getSelectedIndex()==2),"Modo apresentação abre aba real");
        var real=fx(() -> (ResultadoOperacao)read(presentation(),"operation"));
        require(real.antes().equals(expected.antes()) && real.depois().equals(expected.depois()),"apresentação consome resultado confirmado GUI = serviço");
        var notified=real.passos().stream().filter(p->p.titulo().startsWith("Sistema notificado:")).map(Passo::titulo).toList();
        require(notified.size()==3,"três assinantes reais: monitor, estatísticas, globo/painel");
        Files.writeString(output.resolve("observers.txt"),String.join("\n",notified)+"\nAlertas após Asteroide 25%: "+fx(() -> service().alertas(current().id()))+"\n");
        fire("Próximo →"); fire("← Anterior");
        for(int j=0;j<real.passos().size()-1;j++) fire("Próximo →");
        require(fx(() -> button("Próximo →").isDisabled()),"limite final da navegação da apresentação");
        fx(() -> { presentation().setVvalue(0); return null; }); picture("09-apresentacao-observer");
        int history=fx(() -> list("historyList").getItems().size());
        fire("▶ Reproduzir"); require(fx(() -> button("Ⅱ Pausar")!=null),"replay iniciou por botão real"); fire("Ⅱ Pausar");
        fire("Ver globo antes"); require(fx(() -> globe().displayedState().equals(expected.antes())),"comparação antes muda apenas projeção");
        fire("Ver globo depois"); require(fx(() -> globe().displayedState().equals(expected.depois())),"comparação depois muda apenas projeção");
        fx(() -> { presentation().setVvalue(1); return null; }); picture("10-apresentacao-controles");
        fire("Factory Method"); fire("Observer"); fire("Memento");
        require(fx(() -> ((TabPane)node("activityTabs")).getSelectionModel().getSelectedIndex()==1),"atalho Memento abre retratos");
        require(fx(() -> list("historyList").getItems().size()==history),"replay/comparação/atalhos não geram novas transações");
    }

    private void navigation() throws Exception {
        tab(0); inspector(0);
        fx(() -> {
            Bounds footer=root.lookup(".statusbar").localToScene(root.lookup(".statusbar").getBoundsInLocal());
            for(String id:new String[]{"mainSplit","activityTabs","alertLabel","worldList"}) {
                Bounds b=node(id).localToScene(node(id).getBoundsInLocal());
                require(b.getMinX()>=0 && b.getMaxX()<=1367 && b.getMaxY()<=footer.getMinY()+1,"layout 720p contém " + id);
            }
            javafx.event.Event.fireEvent(globe(),new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.RIGHT,false,false,false,false));
            require(((javafx.scene.transform.Rotate)read(globe(),"longitude")).getAngle()==-17,"seta direita gira globo em 8 graus");
            javafx.event.Event.fireEvent(globe(),new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.ADD,false,false,false,false));
            require(Math.abs((double)read(globe(),"zoom")-1.1)<1e-12,"tecla + altera zoom real");
            javafx.event.Event.fireEvent(globe(),new ScrollEvent(ScrollEvent.SCROLL,0,0,0,0,false,false,false,false,false,false,0,120,0,120,ScrollEvent.HorizontalTextScrollUnits.NONE,0,ScrollEvent.VerticalTextScrollUnits.NONE,0,0,null));
            require((double)read(globe(),"zoom")>1.1,"scroll local altera zoom real");
            ((CheckBox)node("lightToggle")).fire(); require(!(boolean)read(globe(),"sunEnabled"),"checkbox desliga luz real"); return null;
        }); picture("11-globo-navegacao-luz");
        fire("Recentrar"); fx(() -> { require((double)read(globe(),"zoom")==1 && ((javafx.scene.transform.Rotate)read(globe(),"longitude")).getAngle()==-25,"Recentrar restaura zoom/rotação"); ((CheckBox)node("lightToggle")).fire(); return null; });
        inspector(1); tab(1);
        fx(() -> { ScrollPane s=node("snapshotsScroll"); s.setVvalue(1); root.layout(); visible(s,button("＋ Capturar retrato")); visible(s,button("Restaurar selecionado")); return null; });
    }

    /** A clearly labelled companion diagram, captured from JavaFX nodes, never presented as app UI. */
    private void diagram() throws Exception {
        fx(() -> {
            VBox content=new VBox(18); content.setStyle("-fx-background-color: #101b22; -fx-padding: 32; -fx-font-family: 'Segoe UI';");
            Label title=new Label("ProjektTellus · quatro padrões no código"); title.setStyle("-fx-text-fill: #80dfc1; -fx-font-size: 27;");
            Label subtitle=new Label("Painel auxiliar do harness · diagrama resumido, não é uma aba da aplicação"); subtitle.setStyle("-fx-text-fill: #e1eaf2; -fx-font-size: 17;");
            content.getChildren().addAll(title,subtitle);
            String[][] cards={
                {"Builder", "BuilderDialog → TerraBuilder\n.nome() · .atmosfera() · .clima() · .hidrosfera() · .biosfera() · .civilizacao() → build(): Terra"},
                {"Factory Method", "MainController → SimulacaoService.aplicar(Terra, EventoCreator, double)\nEventoCreator.criar(intensidade) [sobrescrito no criador concreto] → EventoPlanetario.aplicar(estado)"},
                {"Observer", "SimulacaoService.confirmar → SQLite → ObserverBus.publicar(MudancaPlanetaria)\nSimulacaoObserver.aoMudar → AlertaObserver · EstatisticasObserver · assinante globo/painel"},
                {"Memento", "Terra.capturar(rotulo): TerraMemento → SqliteMundoRepository.snapshots(id)\nSimulacaoService.restaurar → Terra.restaurar(snapshot) → confirmação atômica + notificação"}
            };
            for(String[] card:cards) {
                Label name=new Label(card[0]); name.setStyle("-fx-text-fill: #80dfc1; -fx-font-size: 22;");
                Label text=new Label(card[1]); text.setWrapText(true); text.setStyle("-fx-font-family: Consolas; -fx-text-fill: #e1eaf2; -fx-font-size: 17;");
                VBox box=new VBox(8,name,text); box.setStyle("-fx-background-color: #1d3039; -fx-padding: 17; -fx-background-radius: 8;"); content.getChildren().add(box);
            }
            Stage companion=new Stage(StageStyle.UNDECORATED); companion.setScene(new Scene(content,1366,768)); companion.show();
            pictureFx(content,"12-uml-companheiro-harness"); companion.close(); return null;
        });
    }
    private void flush() throws Exception {
        Files.writeString(output.resolve("measurements.csv"),measurements);
        Files.writeString(output.resolve("snapshot-indices.csv"),snapshotIndex);
        Files.writeString(output.resolve("animation-frames.csv"),frames);
        Files.writeString(output.resolve("visual-values.csv"),visualValues);
        Files.write(output.resolve("checks.tsv"),checks);
        List<String> manifest=new ArrayList<>(); manifest.add("file\twidth\theight\tsha256");
        for(String shot:shots) {
            String file=shot.substring(0,shot.indexOf('\t'));
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(output.resolve(file)));
            manifest.add(shot+"\t"+HexFormat.of().formatHex(digest));
        }
        Files.write(output.resolve("images.tsv"),manifest);
    }
}
