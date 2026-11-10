package com.projekttellus.ui;

import atlantafx.base.theme.PrimerDark;
import com.projekttellus.domain.*;
import com.projekttellus.persistence.*;
import com.projekttellus.patterns.memento.TerraMemento;
import javafx.application.*;
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
