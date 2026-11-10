package com.projekttellus.ui;

import atlantafx.base.theme.PrimerDark;
import com.projekttellus.domain.*;
import com.projekttellus.persistence.*;
import com.projekttellus.patterns.memento.TerraMemento;
import com.projekttellus.application.*;
import com.projekttellus.events.*;
import javafx.application.*;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Sphere;
import javafx.scene.paint.PhongMaterial;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in integration test with a real display: mvn -Pgui-tests test.
 * Uses actual controls/dialogs, real service and a disposable SQLite database. */
@Tag("gui")
class DesktopFlowTest {
    @TempDir Path temporary;
    private Stage stage;
    private Parent root;
    private MainController controller;
    private final AtomicReference<Throwable> asynchronousError = new AtomicReference<>();

    @BeforeAll static void startToolkit() {
        Platform.startup(() -> { }); Platform.setImplicitExit(false);
    }
    @AfterAll static void stopToolkit() { Platform.exit(); }

    private <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action); Platform.runLater(task);
        return task.get(25, TimeUnit.SECONDS);
    }
    private void await(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < end) {
            if (asynchronousError.get() != null) throw new AssertionError("Falha assíncrona na interface", asynchronousError.get());
            if (fx(condition::getAsBoolean)) return;
            Thread.sleep(40);
        }
        fail("A interface não concluiu a operação no prazo.");
    }
    private void idle() throws Exception { await(() -> !root.lookup("#interactionRoot").isDisabled()); }
    @SuppressWarnings("unchecked") private <T extends Node> T node(String id) { return (T) root.lookup("#" + id); }
    @SuppressWarnings("unchecked") private <T> ListView<T> list(String id) { return (ListView<T>) (ListView<?>) node(id); }
    private Button button(Parent parent, String text) {
        return parent.lookupAll(".button").stream().filter(n -> n instanceof Button b && b.getText().equals(text))
                .map(n -> (Button) n).findFirst().orElseThrow(() -> new AssertionError("Botão ausente: " + text));
    }
    private void action(String text) throws Exception { fx(() -> { button(root, text).fire(); return null; }); idle(); }
    private void dialog(String action, String name) throws Exception {
        fx(() -> {
            // Wall-clock scheduling also works when a CI desktop suppresses animation pulses.
            CompletableFuture.delayedExecutor(350, TimeUnit.MILLISECONDS).execute(() -> Platform.runLater(() -> {
                try {
                    DialogPane pane = List.copyOf(Window.getWindows()).stream().filter(w -> w != stage && w.isShowing())
                            .map(w -> w.getScene().getRoot()).map(p -> p instanceof DialogPane d ? d : (DialogPane) p.lookup(".dialog-pane"))
                            .filter(Objects::nonNull).findFirst().orElseThrow();
                    if (name != null) for (Node n : pane.lookupAll(".text-field")) {
                        if (n instanceof TextField t && ("Nome do novo mundo".equals(t.getAccessibleText())
                                || pane.lookupAll(".text-field").size() == 1)) t.setText(name);
                    }
                    ButtonType ok = pane.getButtonTypes().stream().filter(t -> t.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
                    ((Button) pane.lookupButton(ok)).fire();
                } catch (Throwable error) { asynchronousError.set(error); Window.getWindows().stream()
                        .filter(w -> w != stage).toList().forEach(Window::hide); }
            }));
            button(root, action).fire(); return null;
        });
        idle();
    }
    private void open() throws Exception {
        fx(() -> {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            root = loader.load(); controller = loader.getController(); stage = new Stage();
            stage.setScene(new Scene(root, 1440, 960)); stage.show(); return null;
        }); idle();
    }
    private void picture(String name) throws Exception {
        fx(() -> {
            root.applyCss(); root.layout(); WritableImage image = root.snapshot(null, null);
            BufferedImage png = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < png.getHeight(); y++) for (int x = 0; x < png.getWidth(); x++)
                png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            Path out = Path.of("target", "gui-verification"); Files.createDirectories(out);
            ImageIO.write(png, "png", out.resolve(name + ".png").toFile()); return null;
        });
    }
    private long globeFingerprint() {
        ArrayDeque<Node> nodes = new ArrayDeque<>(); nodes.add(node("globeHost"));
        while (!nodes.isEmpty()) {
            Node n = nodes.remove();
            if (n instanceof Sphere sphere) {
                var map = ((PhongMaterial) sphere.getMaterial()).getDiffuseMap(); long hash = 1;
                for (int y = 0; y < map.getHeight(); y += 8) for (int x = 0; x < map.getWidth(); x += 8)
                    hash = 31 * hash + map.getPixelReader().getArgb(x, y);
                return hash;
            }
            if (n instanceof SubScene s) nodes.add(s.getRoot());
            if (n instanceof Parent p) nodes.addAll(p.getChildrenUnmodifiable());
        }
        throw new AssertionError("Globo ausente");
    }

    private PlanetView globe() { return (PlanetView) ((javafx.scene.layout.StackPane) node("globeHost")).getChildren().getFirst(); }
    private void animatedIdle() throws Exception { idle(); await(() -> ((Button) node("skipAnimation")).isDisabled()); }

    @Test void animatedEventsAndMementoOnRealDesktop() throws Exception {
        Path db = temporary.resolve("cinema.db");
        String previous = System.getProperty("projekttellus.db"); System.setProperty("projekttellus.db", db.toString());
        UUID id; EstadoPlanetario original;
        try (var repo = new SqliteMundoRepository(db)) {
            var terra = new SimulacaoService(repo).criar(new TerraBuilder().nome("Cinema · verificação")).terra();
            id = terra.id(); original = terra.estado();
        }
        try {
            open(); assertTrue(fx(() -> Platform.isSupported(ConditionalFeature.SCENE3D)), "Verificação visual requer 3D real");
            fx(() -> { list("worldList").getSelectionModel().selectFirst(); return null; });
            dialog("Carregar selecionado", null); long baseline = fx(this::globeFingerprint);
            fx(() -> { ((Slider) node("intensitySlider")).setValue(100); return null; });
            StringBuilder timing = new StringBuilder("event,frames,elapsed_ms,mean_frame_ms,max_frame_ms\n");
            for (int i = 0; i < 5; i++) {
                int index = i;
                fx(() -> { ((ComboBox<?>) node("eventChoice")).getSelectionModel().select(index); return null; });
                action("Aplicar evento");
                await(() -> !globe().effectLayer().getChildren().isEmpty());
                long[] pulse = new long[4];
                AnimationTimer meter = new AnimationTimer() {
                    @Override public void handle(long now) {
                        if (pulse[0] == 0) pulse[0] = now;
                        if (pulse[1] != 0) pulse[3] = Math.max(pulse[3], now - pulse[1]);
                        pulse[1] = now; pulse[2]++;
                    }
                };
                fx(() -> { meter.start(); return null; });
                Thread.sleep(i == 0 ? 1100 : 1700); picture("event-" + i + "-action");
                if (i == 0) { Thread.sleep(520); picture("event-0-impact"); }
                if (i == 1) { Thread.sleep(700); picture("event-1-ash"); }
                animatedIdle();
                fx(() -> { meter.stop(); return null; });
                assertTrue(pulse[2] >= 10, "O desktop deve renderizar quadros intermediários reais");
                timing.append(String.format(Locale.ROOT, "%d,%d,%.1f,%.2f,%.2f%n", i, pulse[2], (pulse[1] - pulse[0]) / 1e6,
                        (pulse[1] - pulse[0]) / 1e6 / Math.max(1, pulse[2] - 1), pulse[3] / 1e6));
                try (var repo = new SqliteMundoRepository(db)) {
                    var expected = repo.carregar(id).orElseThrow().estado();
                    assertEquals(expected, fx(() -> globe().displayedState()));
                    assertTrue(fx(() -> globe().effectLayer().getChildren().isEmpty()));
                    assertEquals(i + 2, repo.historico(id).size(), "A animação não deve duplicar operações");
                }
                picture("event-" + i + "-final");
            }
            Files.writeString(Path.of("target/gui-verification/pulse-timing.csv"), timing);
            // Zero intensity leaves both texture and real world intact, without any effects.
            long finalBeforeZero = fx(this::globeFingerprint);
            fx(() -> { ((Slider) node("intensitySlider")).setValue(0); return null; });
            action("Aplicar evento"); animatedIdle();
            assertEquals(finalBeforeZero, fx(this::globeFingerprint));
            assertTrue(fx(() -> globe().effectLayer().getChildren().isEmpty()));

            fx(() -> {
                ((TabPane) node("activityTabs")).getSelectionModel().select(1);
                this.<TerraMemento>list("snapshotList").getSelectionModel().select(this.<TerraMemento>list("snapshotList")
                        .getItems().stream().filter(m -> m.rotulo().equals("Origem do mundo")).findFirst().orElseThrow()); return null;
            });
            dialog("Restaurar selecionado", null); await(() -> !globe().effectLayer().getChildren().isEmpty());
            Thread.sleep(900); picture("memento-pulse"); animatedIdle();
            assertEquals(original, fx(() -> globe().displayedState())); assertEquals(baseline, fx(this::globeFingerprint));
            // Real key, scroll and light handlers remain active.
            fx(() -> {
                globe().requestFocus();
                javafx.event.Event.fireEvent(globe(), new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.RIGHT, false, false, false, false));
                javafx.event.Event.fireEvent(globe(), new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.ADD, false, false, false, false));
                ((CheckBox) node("lightToggle")).fire(); return null;
            }); picture("rotation-zoom-light");

            // An actual failed SQL transaction must not start a cinematic or alter the current projection.
            try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + db); var stmt = conn.createStatement()) {
                stmt.execute("CREATE TRIGGER fail_animation BEFORE UPDATE ON mundos BEGIN SELECT RAISE(ABORT, 'animation rollback'); END");
            }
            fx(() -> { ((Slider) node("intensitySlider")).setValue(100); return null; });
            dialog("Aplicar evento", null); animatedIdle();
            assertEquals(original, fx(() -> globe().displayedState()));
            assertTrue(fx(() -> globe().effectLayer().getChildren().isEmpty()));
            assertTrue(fx(() -> ((Label) node("statusLabel")).getText().contains("não foi concluída")));
        } finally {
            fx(() -> { if (controller != null) controller.close(); if (stage != null) stage.close(); return null; });
            if (previous == null) System.clearProperty("projekttellus.db"); else System.setProperty("projekttellus.db", previous);
        }
    }

    @Test void animatorCancellationReplacementAccessibilityAndClose() throws Exception {
        try (var repo = new SqliteMundoRepository(temporary.resolve("lifecycle.db"))) {
            var service = new SimulacaoService(repo); var terra = service.criar(new TerraBuilder()).terra();
            var first = service.aplicar(terra, new AsteroideCreator(), 1);
            var second = service.aplicar(terra, new InvasaoAlienigenaCreator(), .7);
            var restored = service.restaurar(terra, repo.snapshots(terra.id()).getFirst());
            int histories = repo.historico(terra.id()).size();
            long notifications = service.estatisticas().para(terra.id()).mudancas();
            PlanetView view = fx(PlanetView::new);
            PlanetEventAnimator animator = fx(() -> new PlanetEventAnimator(view));
            try {
                // Replacement while the old textures are still being prepared.
                fx(() -> {
                    animator.play(first, AnimationKind.ASTEROID, 1, AnimationMode.FULL);
                    animator.play(second, AnimationKind.ALIEN, .7, AnimationMode.FULL);
                    animator.cancel(); return null;
                });
                await(() -> !animator.isRunning()); assertEquals(second.depois(), fx(view::displayedState));
                assertTrue(fx(() -> view.effectLayer().getChildren().isEmpty()));
                fx(() -> { animator.play(first, AnimationKind.ASTEROID, 1, AnimationMode.FULL); return null; });
                await(() -> !view.effectLayer().getChildren().isEmpty());
                fx(() -> { animator.skip(); return null; });
                assertEquals(first.depois(), fx(view::displayedState)); assertFalse(fx(animator::isRunning));
                fx(() -> { animator.play(restored, AnimationKind.MEMENTO, 0, AnimationMode.REDUCED); return null; });
                await(() -> !animator.isRunning()); assertEquals(restored.depois(), fx(view::displayedState));
                fx(() -> { animator.play(second, AnimationKind.ALIEN, .7, AnimationMode.OFF); return null; });
                await(() -> !animator.isRunning()); assertEquals(second.depois(), fx(view::displayedState));
                // A context switch invalidates pending callbacks, including after close.
                fx(() -> {
                    animator.play(first, AnimationKind.ASTEROID, 1, AnimationMode.FULL);
                    animator.discard(); view.update(restored.depois()); return null;
                });
                Thread.sleep(350); assertEquals(restored.depois(), fx(view::displayedState));
                fx(() -> { animator.play(second, AnimationKind.ALIEN, .7, AnimationMode.FULL); animator.close(); return null; });
                Thread.sleep(350); assertEquals(restored.depois(), fx(view::displayedState));
                assertFalse(fx(animator::isRunning)); assertTrue(fx(() -> view.effectLayer().getChildren().isEmpty()));
                assertEquals(histories, repo.historico(terra.id()).size());
                assertEquals(notifications, service.estatisticas().para(terra.id()).mudancas());
                assertEquals(restored.depois(), terra.estado());
                assertEquals(terra.estado(), repo.carregar(terra.id()).orElseThrow().estado());
            } finally { fx(() -> { animator.close(); return null; }); }
        }
    }

    @Test void approachRemainsOutsideSurfaceAfterRotationAndEffectsKeepCenterStable() throws Exception {
        fx(() -> {
            PlanetView view = new PlanetView(); view.resize(600, 400); view.reset();
            Parent rotating = view.effectLayer().getParent();
            var latitude = (javafx.scene.transform.Rotate) rotating.getTransforms().get(0);
            var longitude = (javafx.scene.transform.Rotate) rotating.getTransforms().get(1);
            for (double x : new double[]{-80, 0, 80}) for (double y : new double[]{0, 90, 180, 270}) {
                latitude.setAngle(x); longitude.setAngle(y);
                var hit = view.frontPoint(24, -18);
                var start = view.cameraOffset(hit, new javafx.geometry.Point3D(-230, -175, -290));
                for (int step = 0; step <= 100; step++) {
                    var point = start.add(hit.subtract(start).multiply(step / 100.0));
                    assertTrue(point.magnitude() >= 130 - 1e-8, "A aproximação deve permanecer fora da esfera até o impacto");
                }
                var center = rotating.localToScene(javafx.geometry.Point3D.ZERO);
                var effect = new PlanetEventEffect(view, AnimationPlan.of(AnimationKind.ASTEROID, 1, AnimationMode.FULL));
                effect.frame(.15);
                assertEquals(center, rotating.localToScene(javafx.geometry.Point3D.ZERO), "Partículas não podem mover o pivô de escala");
                effect.dispose();
            }
            return null;
        });
    }

    @Test void completeDesktopWorkflowAndReopen() throws Exception {
        String previous = System.getProperty("projekttellus.db"); Path db = temporary.resolve("desktop.db");
        System.setProperty("projekttellus.db", db.toString());
        try {
            open(); assertTrue(fx(() -> list("worldList").getItems().isEmpty()));
            dialog("＋ Criar mundo", "Terra Alfa");
            UUID alpha = fx(() -> this.<MundoResumo>list("worldList").getItems().getFirst().id());
            assertEquals("100,0%", fx(() -> ((Label) node("humanValue")).getText()));
            assertTrue(fx(() -> ((Label) node("observerLabel")).getText().contains("notificação real")));
            picture("sandbox");

            fx(() -> { for (Node n : node("environmentHost").lookupAll(".text-field"))
                if (n instanceof TextField t && t.getAccessibleText().startsWith("Oxigênio")) t.setText("90"); return null; });
            assertTrue(fx(() -> ((Button) node("applyButton")).isDisabled()));
            fx(() -> { ((EnvironmentEditor) ((javafx.scene.layout.VBox) node("environmentHost")).getChildren().getFirst())
                    .setState(new TerraBuilder().build().estado()); return null; });

            dialog("Duplicar", "Terra Beta");
            UUID beta = fx(() -> this.<MundoResumo>list("worldList").getSelectionModel().getSelectedItem().id());
            assertNotEquals(alpha, beta);
            dialog("Renomear", "Beta Renomeada");
            fx(() -> { for (Node n : node("environmentHost").lookupAll(".text-field"))
                if (n instanceof TextField t && t.getAccessibleText().startsWith("Temperatura")) t.setText("30"); return null; });
            action("Aplicar condições");
            String preservedHuman = fx(() -> ((Label) node("humanValue")).getText());
            assertNotEquals("100,0%", preservedHuman);
            fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().select(1); return null; });
            dialog("＋ Capturar retrato", "Antes da catástrofe");
            TerraMemento memento = fx(() -> this.<TerraMemento>list("snapshotList").getItems().stream()
                    .filter(m -> m.rotulo().equals("Antes da catástrofe")).findFirst().orElseThrow());
            for (int i = 0; i < 5; i++) {
                int index = i; fx(() -> { ((ComboBox<?>) node("eventChoice")).getSelectionModel().select(index); return null; });
                action("Aplicar evento");
            }
            assertNotEquals("100,0%", fx(() -> ((Label) node("humanValue")).getText()));
            action("Avançar tempo →"); assertEquals("ANO 10,0", fx(() -> ((Label) node("yearLabel")).getText()));
            fx(() -> { this.<TerraMemento>list("snapshotList").getSelectionModel().select(memento); return null; });
            dialog("Restaurar selecionado", null);
            animatedIdle();
            assertEquals(preservedHuman, fx(() -> ((Label) node("humanValue")).getText()));
            assertEquals("ANO 0,0", fx(() -> ((Label) node("yearLabel")).getText()));
            fx(() -> { ((ToggleButton) node("presentationToggle")).fire(); return null; });
            assertEquals("Apresentação", fx(() -> ((TabPane) node("activityTabs")).getSelectionModel().getSelectedItem().getText()));
            action("Próximo →");
            assertTrue(fx(() -> node("presentationHost").lookupAll(".label").stream()
                    .anyMatch(n -> n instanceof Label l && l.getText().contains("Uma Terra anterior foi restaurada"))));
            long finalGlobe = fx(this::globeFingerprint);
            action("Ver globo antes"); assertNotEquals(finalGlobe, fx(this::globeFingerprint));
            fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().select(1); return null; });
            assertEquals(finalGlobe, fx(this::globeFingerprint), "Sair da apresentação deve mostrar o estado atual");
            fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().select(2); return null; });
            action("Ver globo depois"); picture("presentation");
            dialog("Salvar", null);

            fx(() -> { this.<MundoResumo>list("worldList").getSelectionModel().select(this.<MundoResumo>list("worldList")
                    .getItems().stream().filter(w -> w.id().equals(alpha)).findFirst().orElseThrow()); return null; });
            dialog("Carregar selecionado", null);
            assertEquals("Terra Alfa", fx(() -> ((Label) node("worldTitle")).getText()));
            assertEquals("100,0%", fx(() -> ((Label) node("humanValue")).getText()));
            fx(() -> { this.<MundoResumo>list("worldList").getSelectionModel().select(this.<MundoResumo>list("worldList")
                    .getItems().stream().filter(w -> w.id().equals(beta)).findFirst().orElseThrow()); return null; });
            dialog("Excluir", null); assertEquals(1, fx(() -> list("worldList").getItems().size()));
            fx(() -> { controller.close(); stage.close(); return null; });
            open(); assertEquals(1, fx(() -> list("worldList").getItems().size()));
            fx(() -> { list("worldList").getSelectionModel().selectFirst(); return null; });
            dialog("Carregar selecionado", null);
            assertEquals("Terra Alfa", fx(() -> ((Label) node("worldTitle")).getText()));
            assertEquals(1, fx(() -> list("snapshotList").getItems().size()));
        } finally {
            fx(() -> { if (controller != null) controller.close(); if (stage != null) stage.close(); return null; });
            if (previous == null) System.clearProperty("projekttellus.db"); else System.setProperty("projekttellus.db", previous);
        }
    }
}
