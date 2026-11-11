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
                    if (action.equals("＋ Criar mundo")) {
                        Window dialog = pane.getScene().getWindow();
                        assertTrue(dialog.getHeight() <= stage.getHeight(), "Criação deve caber na altura da janela de inspeção");
                        var area = WindowLayout.workArea(dialog);
                        assertTrue(dialog.getY() >= area.getMinY() - 1 && dialog.getY() + dialog.getHeight() <= area.getMaxY() + 1);
                    }
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
            stage.setScene(new Scene(root));
            WindowLayout.configure(stage, new javafx.geometry.Rectangle2D(0, 0,
                    Double.parseDouble(System.getProperty("test.window.width", "1366")),
                    Double.parseDouble(System.getProperty("test.window.height", "720"))));
            stage.show();
            String expectedScale = System.getProperty("test.expectedScale");
            if (expectedScale != null) assertEquals(Double.parseDouble(expectedScale), stage.getOutputScaleX(), .01);
            System.out.printf(Locale.ROOT, "GUI outer %.0fx%.0f; scene %.0fx%.0f; output scale %.2fx%.2f; SCENE3D=%s%n",
                    stage.getWidth(), stage.getHeight(), stage.getScene().getWidth(), stage.getScene().getHeight(),
                    stage.getOutputScaleX(), stage.getOutputScaleY(), Platform.isSupported(ConditionalFeature.SCENE3D));
            return null;
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

    @Test void initialWindowFitsVisualWorkArea() throws Exception {
        var app = new com.projekttellus.ProjektTellusApp();
        String previous = System.getProperty("projekttellus.db");
        System.setProperty("projekttellus.db", temporary.resolve("initial.db").toString());
        try {
            fx(() -> {
                stage = new Stage(); app.start(stage);
                var area = Screen.getPrimary().getVisualBounds();
                assertTrue(stage.getWidth() <= area.getWidth(), "Janela deve caber na largura útil");
                assertTrue(stage.getHeight() <= area.getHeight(), "Janela deve caber na altura útil");
                assertTrue(stage.getMinHeight() <= 600, "Mínimo deve permitir a área útil de 768p");
                return null;
            });
        } finally {
            fx(() -> { app.stop(); if (stage != null) stage.close(); return null; });
            if (previous == null) System.clearProperty("projekttellus.db"); else System.setProperty("projekttellus.db", previous);
        }
    }

    @Test void controlsAndPopupFitLaptopWorkArea() throws Exception {
        String previous = System.getProperty("projekttellus.db");
        System.setProperty("projekttellus.db", temporary.resolve("responsive.db").toString());
        try {
            open();
            fx(() -> { stage.setWidth(1366); stage.setHeight(720); return null; });
            Thread.sleep(180);
            fx(() -> {
                root.applyCss(); root.layout();
                var tabs = node("activityTabs").localToScene(node("activityTabs").getBoundsInLocal());
                var footer = root.lookup(".statusbar").localToScene(root.lookup(".statusbar").getBoundsInLocal());
                assertTrue(tabs.getMaxY() <= footer.getMinY(), "Abas devem caber acima do rodapé");
                var scroll = (ScrollPane) root.lookup(".inspector");
                scroll.setVvalue(.65);
                return null;
            });
            Thread.sleep(120);
            fx(() -> { ((ComboBox<?>) node("eventChoice")).show(); return null; });
            Thread.sleep(120);
            fx(() -> {
                var popup = Window.getWindows().stream().filter(w -> w instanceof PopupWindow && w.isShowing()).findFirst().orElseThrow();
                var list = (ListView<?>) popup.getScene().getRoot().lookup(".list-view");
                assertTrue(list.getBackground().getFills().stream().anyMatch(fill ->
                        fill.getFill() instanceof javafx.scene.paint.Color color && color.getOpacity() == 1),
                        "Lista do popup deve ter fundo opaco, mesmo sobre outros controles");
                assertEquals(5, list.getItems().size());
                var cell = list.lookupAll(".list-cell").stream().filter(n -> n instanceof ListCell<?> c && c.isSelected() && !c.isEmpty())
                        .map(n -> (ListCell<?>) n).findFirst().orElseThrow();
                assertEquals(javafx.scene.paint.Color.web("#244239"), cell.getBackground().getFills().getFirst().getFill(),
                        "Seleção precisa se distinguir do fundo opaco");
                cell.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("hover"), true);
                list.applyCss();
                assertEquals(javafx.scene.paint.Color.web("#396451"), cell.getBackground().getFills().getFirst().getFill());
                cell.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("pressed"), true);
                list.applyCss();
                assertEquals(javafx.scene.paint.Color.web("#80dfc1"), cell.getBackground().getFills().getFirst().getFill());
                ((ComboBox<?>) node("eventChoice")).hide(); return null;
            });
        } finally {
            fx(() -> { if (controller != null) controller.close(); if (stage != null) stage.close(); return null; });
            if (previous == null) System.clearProperty("projekttellus.db"); else System.setProperty("projekttellus.db", previous);
        }
    }

    @Test void responsiveMatrixAndNativePopupInteraction() throws Exception {
        String previous = System.getProperty("projekttellus.db");
        System.setProperty("projekttellus.db", temporary.resolve("matrix.db").toString());
        try {
            open(); dialog("＋ Criar mundo", "Terra responsiva · faculdade");
            // Outer logical sizes reserve 48 physical pixels for the taskbar.
            // The 125% case is also rerun in a separate JVM with glass.win.uiScale=1.25.
            for (int[] size : new int[][]{{1366, 720}, {1920, 1032}, {1536, 825}, {1000, 560}, {1366, 720}}) {
                fx(() -> {
                    ((TabPane) node("activityTabs")).getSelectionModel().selectFirst();
                    stage.setMaximized(false);
                    WindowLayout.configure(stage, new javafx.geometry.Rectangle2D(0, 0, size[0], size[1]));
                    // configure caps the preferred size at 1440×960; exercise the full work area too.
                    stage.setWidth(size[0]); stage.setHeight(size[1]);
                    return null;
                });
                Thread.sleep(160);
                fx(() -> {
                    root.applyCss(); root.layout();
                    var footer = root.lookup(".statusbar").localToScene(root.lookup(".statusbar").getBoundsInLocal());
                    assertTrue(stage.getWidth() <= size[0] + 1 && stage.getHeight() <= size[1] + 1);
                    for (String id : new String[]{"mainSplit", "activityTabs", "alertLabel", "worldList"}) {
                        Node child = node(id); var bounds = child.localToScene(child.getBoundsInLocal());
                        assertTrue(bounds.getMaxY() <= footer.getMinY() + 1, id + " deve caber acima do rodapé em " + Arrays.toString(size));
                        assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= root.getScene().getWidth() + 1, id + " deve caber na largura");
                    }
                    assertTrue(node("globeHost").getLayoutBounds().getWidth() >= 200);
                    assertTrue(node("globeHost").getLayoutBounds().getHeight() >= 100);
                    ScrollPane inspector = node("inspectorScroll");
                    assertTrue(inspector.getContent().getLayoutBounds().getHeight() > inspector.getViewportBounds().getHeight());
                    inspector.setVvalue(inspector.getVmax()); return null;
                });
                Thread.sleep(80);
                fx(() -> { assertVisibleInScroll(node("inspectorScroll"), button(root, "Avançar tempo →")); return null; });
                picture("layout-" + size[0] + "x" + size[1]);
                for (int tab = 0; tab < 3; tab++) {
                    final int selected = tab;
                    fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().select(selected); return null; });
                    Thread.sleep(60);
                    fx(() -> {
                        root.applyCss(); root.layout();
                        if (selected == 1) {
                            ScrollPane snapshots = node("snapshotsScroll"); snapshots.setVvalue(1); snapshots.layout();
                            for (String text : new String[]{"＋ Capturar retrato", "Restaurar selecionado"}) {
                                assertVisibleInScroll(snapshots, button(root, text));
                            }
                        }
                        if (selected == 2) {
                            var presentation = (PresentationPane) ((javafx.scene.layout.StackPane) node("presentationHost")).getChildren().getFirst();
                            presentation.setVvalue(1); presentation.layout();
                            assertTrue(presentation.getContent().getLayoutBounds().getWidth() <= presentation.getViewportBounds().getWidth() + 1,
                                    "Apresentação deve quebrar linhas sem rolagem horizontal");
                        }
                        return null;
                    });
                }
                picture("presentation-" + size[0] + "x" + size[1]);
            }
            fx(() -> { ((TabPane) node("activityTabs")).getSelectionModel().selectFirst(); return null; });
            testNativeEventPopup(false);
            testNativeEventPopup(true);
            fx(() -> { stage.setMaximized(true); return null; }); Thread.sleep(180);
            picture("layout-maximized");
            fx(() -> { stage.setMaximized(false); return null; }); Thread.sleep(180);
            assertTrue(fx(() -> node("activityTabs").isVisible()));
        } finally {
            fx(() -> { if (controller != null) controller.close(); if (stage != null) stage.close(); return null; });
            if (previous == null) System.clearProperty("projekttellus.db"); else System.setProperty("projekttellus.db", previous);
        }
    }

    private void assertVisibleInScroll(ScrollPane scroll, Node child) {
        Node viewport = scroll.lookup(".viewport");
        var visible = viewport.localToScene(viewport.getBoundsInLocal());
        var bounds = child.localToScene(child.getBoundsInLocal());
        assertTrue(bounds.getMinY() >= visible.getMinY() - 1 && bounds.getMaxY() <= visible.getMaxY() + 1,
                "Controle deve estar acessível pela rolagem: " + child);
    }

    private void testNativeEventPopup(boolean nearScreenBottom) throws Exception {
        fx(() -> {
            ScrollPane inspector = node("inspectorScroll");
            if (nearScreenBottom) {
                var area = WindowLayout.workArea(stage);
                stage.setY(area.getMaxY() - stage.getHeight());
            }
            var bounds = inspector.getContent().sceneToLocal(node("eventChoice").localToScene(node("eventChoice").getBoundsInLocal()));
            double desiredY = nearScreenBottom ? inspector.getViewportBounds().getHeight() - 40 : 220;
            inspector.setVvalue(Math.clamp((bounds.getMinY() - desiredY) /
                    (inspector.getContent().getLayoutBounds().getHeight() - inspector.getViewportBounds().getHeight()), 0, 1));
            return null;
        }); Thread.sleep(100);
        int historyBefore = fx(() -> list("historyList").getItems().size());
        double intensityBefore = fx(() -> ((Slider) node("intensitySlider")).getValue());
        for (int i = 0; i < 5; i++) {
            final int index = i;
            fx(() -> { stage.toFront(); stage.requestFocus(); ((ComboBox<?>) node("eventChoice")).requestFocus();
                ((ComboBox<?>) node("eventChoice")).show(); return null; }); Thread.sleep(180);
            if (i == 0) popupPicture(nearScreenBottom ? "event-popup-bottom" : "event-popup");
            fx(() -> {
                var popup = Window.getWindows().stream().filter(w -> w instanceof PopupWindow && w.isShowing()).findFirst().orElseThrow();
                var area = Screen.getScreensForRectangle(popup.getX(), popup.getY(), popup.getWidth(), popup.getHeight()).getFirst().getVisualBounds();
                assertTrue(popup.getY() >= area.getMinY() - 1 && popup.getY() + popup.getHeight() <= area.getMaxY() + 1,
                        "Popup deve reposicionar dentro da área útil");
                if (nearScreenBottom) assertTrue(popup.getY() < node("eventChoice").localToScreen(node("eventChoice").getBoundsInLocal()).getMinY(),
                        "Sem espaço embaixo, o popup deve abrir acima do seletor");
                var popupList = (ListView<?>) popup.getScene().getRoot().lookup(".list-view");
                var cell = popupList.lookupAll(".list-cell").stream()
                        .filter(n -> n instanceof ListCell<?> c && c.getListView() == popupList && c.getIndex() == index && !c.isEmpty()
                                && c.isVisible() && c.getParent().getStyleClass().contains("sheet"))
                        .findFirst().orElseThrow();
                var bounds = cell.localToScreen(cell.getBoundsInLocal());
                assertTrue(bounds.getWidth() > 100 && bounds.getHeight() > 20, "Mouse deve atingir a célula visível, não a célula de medição do VirtualFlow");
                var robot = new javafx.scene.robot.Robot();
                robot.mouseMove(bounds.getCenterX(), bounds.getCenterY());
                return null;
            }); Thread.sleep(70);
            fx(() -> { new javafx.scene.robot.Robot().mousePress(javafx.scene.input.MouseButton.PRIMARY); return null; });
            Thread.sleep(70);
            fx(() -> { new javafx.scene.robot.Robot().mouseRelease(javafx.scene.input.MouseButton.PRIMARY); return null; });
            Thread.sleep(90);
            assertEquals(i, fx(() -> ((ComboBox<?>) node("eventChoice")).getSelectionModel().getSelectedIndex()));
            assertFalse(fx(() -> ((ComboBox<?>) node("eventChoice")).isShowing()));
            assertEquals(intensityBefore, fx(() -> ((Slider) node("intensitySlider")).getValue()), "Mouse index " + i);
        }
        fx(() -> {
            var choice = (ComboBox<?>) node("eventChoice"); choice.requestFocus(); choice.show();
            new javafx.scene.robot.Robot().keyType(javafx.scene.input.KeyCode.HOME); return null;
        }); Thread.sleep(100);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            fx(() -> {
                var choice = (ComboBox<?>) node("eventChoice"); choice.show();
                var robot = new javafx.scene.robot.Robot();
                robot.keyType(javafx.scene.input.KeyCode.HOME);
                for (int j = 0; j < index; j++) robot.keyType(javafx.scene.input.KeyCode.DOWN);
                robot.keyType(javafx.scene.input.KeyCode.ENTER); return null;
            }); Thread.sleep(90);
            assertEquals(i, fx(() -> ((ComboBox<?>) node("eventChoice")).getSelectionModel().getSelectedIndex()));
            assertEquals(intensityBefore, fx(() -> ((Slider) node("intensitySlider")).getValue()), "Keyboard index " + i);
        }
        assertEquals(historyBefore, fx(() -> list("historyList").getItems().size()), "Selecionar não aplica evento atrás do popup");
        assertEquals(intensityBefore, fx(() -> ((Slider) node("intensitySlider")).getValue()));
    }

    private void popupPicture(String name) throws Exception {
        fx(() -> {
            var popup = Window.getWindows().stream().filter(w -> w instanceof PopupWindow && w.isShowing()).findFirst().orElseThrow();
            WritableImage image = new javafx.scene.robot.Robot().getScreenCapture(null,
                    popup.getX(), popup.getY(), popup.getWidth(), popup.getHeight(), false);
            BufferedImage png = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < png.getHeight(); y++) for (int x = 0; x < png.getWidth(); x++) png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            Path out = Path.of("target", "gui-verification"); Files.createDirectories(out);
            ImageIO.write(png, "png", out.resolve(name + ".png").toFile()); return null;
        });
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
