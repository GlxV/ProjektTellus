package com.projekttellus.ui;

import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.domain.TerraBuilder;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/** A native lit Sphere with an original, intentionally simplified equirectangular map. */
public final class PlanetView extends StackPane {
    private static final int W = 768, H = 384;
    private final Sphere sphere = new Sphere(130, 96);
    private final PhongMaterial surface = new PhongMaterial();
    private final Rotate longitude = new Rotate(-25, Rotate.Y_AXIS);
    private final Rotate latitude = new Rotate(-12, Rotate.X_AXIS);
    private final PointLight sunlight = new PointLight(Color.web("#fff5df"));
    private final AmbientLight ambient = new AmbientLight(Color.color(.32, .38, .45));
    private final Group planet = new Group(sphere);
    private final double[] land = new double[W * H];
    private double pressX, pressY, startX, startY, zoom = 1;

    public PlanetView() {
        setMinSize(100, 150); setFocusTraversable(true);
        setAccessibleText("Globo planetário. Arraste para girar, use a roda para zoom. Setas giram; mais e menos aproximam e afastam.");
        createContinents();
        sphere.setMaterial(surface); sphere.getTransforms().addAll(latitude, longitude);
        sunlight.setTranslateX(-280); sunlight.setTranslateY(-180); sunlight.setTranslateZ(-340);
        Group sceneRoot = new Group(planet, sunlight, ambient);
        SubScene scene = new SubScene(sceneRoot, 500, 350, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.TRANSPARENT); scene.setCamera(new ParallelCamera());
        scene.widthProperty().bind(widthProperty()); scene.heightProperty().bind(heightProperty());
        planet.translateXProperty().bind(widthProperty().divide(2));
        planet.translateYProperty().bind(heightProperty().divide(2));
        widthProperty().addListener((o, a, b) -> resizeGlobe()); heightProperty().addListener((o, a, b) -> resizeGlobe());
        getChildren().add(scene);
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            Label warning = new Label("A aceleração 3D não está disponível neste computador.");
            warning.getStyleClass().add("notice"); getChildren().add(warning);
        }
        setOnMousePressed(e -> { requestFocus(); pressX = e.getSceneX(); pressY = e.getSceneY(); startX = latitude.getAngle(); startY = longitude.getAngle(); });
        setOnMouseDragged(e -> { longitude.setAngle(startY + (e.getSceneX() - pressX) * .45); latitude.setAngle(Math.clamp(startX - (e.getSceneY() - pressY) * .4, -85, 85)); });
        setOnScroll(e -> { zoom = Math.clamp(zoom * Math.exp(e.getDeltaY() * .0018), .55, 1.65); resizeGlobe(); e.consume(); });
        setOnKeyPressed(e -> {
            KeyCode key = e.getCode();
            switch (key) {
                case LEFT -> longitude.setAngle(longitude.getAngle() - 8);
                case RIGHT -> longitude.setAngle(longitude.getAngle() + 8);
                case UP -> latitude.setAngle(Math.max(-85, latitude.getAngle() - 8));
                case DOWN -> latitude.setAngle(Math.min(85, latitude.getAngle() + 8));
                case ADD, PLUS, EQUALS -> { zoom = Math.min(1.65, zoom + .1); resizeGlobe(); }
                case SUBTRACT, MINUS -> { zoom = Math.max(.55, zoom - .1); resizeGlobe(); }
                case HOME -> reset();
                default -> { return; }
            }
            e.consume();
        });
        update(new TerraBuilder().build().estado());
    }

    private void resizeGlobe() {
        double scale = Math.max(.1, Math.min(getWidth(), getHeight()) / 310) * zoom;
        planet.setScaleX(scale); planet.setScaleY(scale); planet.setScaleZ(scale);
    }
    public void reset() { longitude.setAngle(-25); latitude.setAngle(-12); zoom = 1; resizeGlobe(); }
    public void setSunlight(boolean enabled) {
        sunlight.setLightOn(enabled); ambient.setColor(enabled ? Color.color(.32, .38, .45) : Color.color(.85, .88, .9));
    }

    private void createContinents() {
        Canvas canvas = new Canvas(W, H); var g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK); g.fillRect(0, 0, W, H); g.setFill(Color.WHITE);
        // Original hand-drawn continent silhouettes, coordinates in equirectangular fractions.
        polygon(g, .06,.20, .13,.13, .21,.14, .28,.22, .25,.30, .20,.34, .19,.41, .14,.36, .12,.29, .08,.30);
        polygon(g, .20,.40, .27,.44, .31,.52, .28,.64, .25,.75, .23,.83, .20,.68, .21,.57, .18,.46);
        polygon(g, .32,.12, .39,.10, .41,.17, .37,.27, .34,.23);
        polygon(g, .44,.25, .48,.20, .53,.23, .55,.32, .51,.38, .45,.34);
        polygon(g, .45,.36, .54,.35, .60,.43, .58,.59, .54,.71, .50,.67, .47,.52, .43,.44);
        polygon(g, .53,.22, .61,.15, .71,.16, .77,.20, .85,.18, .92,.27, .88,.34, .80,.37, .75,.46, .69,.39, .66,.49, .60,.38, .55,.36);
        polygon(g, .72,.47, .76,.51, .80,.55, .79,.59, .74,.57);
        polygon(g, .79,.63, .86,.60, .91,.65, .90,.72, .84,.75, .78,.71);
        polygon(g, .93,.74, .95,.69, .96,.73, .94,.79);
        WritableImage mask = canvas.snapshot(null, null); PixelReader reader = mask.getPixelReader();
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) land[y * W + x] = reader.getColor(x, y).getRed();
    }
    private void polygon(javafx.scene.canvas.GraphicsContext g, double... pairs) {
        int n = pairs.length / 2; double[] x = new double[n], y = new double[n];
        for (int i = 0; i < n; i++) { x[i] = pairs[i * 2] * W; y[i] = pairs[i * 2 + 1] * H; }
        g.fillPolygon(x, y, n);
    }

    public void update(EstadoPlanetario state) {
        WritableImage texture = new WritableImage(W, H); PixelWriter pixels = texture.getPixelWriter();
        double water = state.hidrosfera().agua() / 100, ice = state.hidrosfera().gelo() / 100;
        double vegetation = state.biosfera().vegetacao() / 100;
        double heat = Math.clamp((state.clima().temperatura() - 25) / 130, 0, 1);
        double cold = Math.clamp((-state.clima().temperatura()) / 150, 0, 1);
        double haze = Math.clamp(state.clima().aerossol() / 130, 0, .75);
        double radiation = state.clima().radiacao() / 100;
        Color soil = Color.web("#b69866").interpolate(Color.web("#36825b"), vegetation).interpolate(Color.web("#6c342d"), heat * .7);
        Color sea = Color.web("#124b70").interpolate(Color.web("#593e32"), heat * .8).interpolate(Color.web("#8cc4d8"), cold * .55);
        for (int y = 0; y < H; y++) {
            double polar = Math.abs(y - H / 2.0) / (H / 2.0);
            for (int x = 0; x < W; x++) {
                double noise = (.5 + .23 * Math.sin(x * .061 + Math.sin(y * .053) * 3) + .17 * Math.cos(y * .093 + Math.cos(x * .04) * 2));
                boolean continent = land[y * W + x] > .5;
                // Coverage is qualitative rather than a geographic/physical projection.
                boolean wet = continent ? water > .72 && noise < (water - .72) * 3.5 : noise < Math.min(1, water / .71);
                Color c = wet ? sea : soil;
                if (polar > 1 - ice * 1.7 - cold * .08) c = Color.web("#dcecf0");
                c = c.deriveColor(0, 1, .86 + noise * .24, 1);
                c = c.interpolate(Color.web("#7c6b5b"), haze * (.45 + noise * .5));
                c = c.interpolate(Color.web("#8f658e"), radiation * .25);
                if (!wet && vegetation > .1 && noise > .65) c = c.interpolate(Color.web("#76a866"), vegetation * .3);
                pixels.setColor(x, y, c);
            }
        }
        surface.setDiffuseMap(texture); surface.setSpecularColor(Color.color(.24, .34, .40)); surface.setSpecularPower(18);
    }
}
