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
import javafx.scene.transform.Scale;
import javafx.geometry.Point3D;

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
    private final Group effects = new Group();
    private final Scale globeScale = new Scale(1, 1, 1, 0, 0, 0);
    private final Canvas glowLayer = new Canvas();
    private final double[] land = new double[W * H];
    private double pressX, pressY, startX, startY, zoom = 1;
    private EstadoPlanetario displayedState;
    private int[] displayedPixels;
    private final int[] blended = new int[W * H];
    private WritableImage transitionTexture;

    public PlanetView() {
        setMinSize(0, 100); setFocusTraversable(true);
        setAccessibleText("Globo planetário. Arraste para girar, use a roda para zoom. Setas giram; mais e menos aproximam e afastam.");
        createContinents();
        sphere.setMaterial(surface);
        Group rotating = new Group(sphere, effects);
        rotating.getTransforms().addAll(latitude, longitude);
        planet.getChildren().setAll(rotating);
        planet.getTransforms().add(globeScale);
        effects.setMouseTransparent(true);
        sunlight.setTranslateX(-280); sunlight.setTranslateY(-180); sunlight.setTranslateZ(-340);
        Group sceneRoot = new Group(planet, sunlight, ambient);
        SubScene scene = new SubScene(sceneRoot, 500, 350, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.TRANSPARENT); scene.setCamera(new ParallelCamera());
        scene.widthProperty().bind(widthProperty()); scene.heightProperty().bind(heightProperty());
        planet.translateXProperty().bind(widthProperty().divide(2));
        planet.translateYProperty().bind(heightProperty().divide(2));
        widthProperty().addListener((o, a, b) -> resizeGlobe()); heightProperty().addListener((o, a, b) -> resizeGlobe());
        glowLayer.setMouseTransparent(true);
        glowLayer.widthProperty().bind(widthProperty()); glowLayer.heightProperty().bind(heightProperty());
        getChildren().addAll(scene, glowLayer);
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
        globeScale.setX(scale); globeScale.setY(scale); globeScale.setZ(scale);
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

    /** Pure pixel preparation: may run on the animator worker; topology is immutable after construction. */
    int[] prepareTexture(EstadoPlanetario state) {
        int[] pixels = new int[W * H];
        double water = state.hidrosfera().agua() / 100, ice = state.hidrosfera().gelo() / 100;
        double vegetation = state.biosfera().vegetacao() / 100;
        double heat = Math.clamp((state.clima().temperatura() - 25) / 130, 0, 1);
        double cold = Math.clamp((-state.clima().temperatura()) / 150, 0, 1);
        double haze = Math.clamp(state.clima().aerossol() / 130, 0, .75);
        double radiation = state.clima().radiacao() / 100;
        Color soil = Color.web("#b69866").interpolate(Color.web("#36825b"), vegetation).interpolate(Color.web("#6c342d"), heat * .7);
        Color sea = Color.web("#124b70").interpolate(Color.web("#593e32"), heat * .8).interpolate(Color.web("#8cc4d8"), cold * .55);
        for (int y = 0; y < H; y++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException("Textura substituída");
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
                pixels[y * W + x] = 0xff000000 | ((int) Math.round(c.getRed() * 255) << 16)
                        | ((int) Math.round(c.getGreen() * 255) << 8) | (int) Math.round(c.getBlue() * 255);
            }
        }
        return pixels;
    }

    public void update(EstadoPlanetario state) {
        if (state.equals(displayedState) && transitionTexture == null) return;
        showPrepared(state, prepareTexture(state));
    }
    void showPrepared(EstadoPlanetario state, int[] pixels) {
        WritableImage texture = new WritableImage(W, H);
        texture.getPixelWriter().setPixels(0, 0, W, H, PixelFormat.getIntArgbInstance(), pixels, 0, W);
        surface.setDiffuseMap(texture); surface.setSpecularColor(Color.color(.24, .34, .40)); surface.setSpecularPower(18);
        displayedState = state; displayedPixels = pixels; transitionTexture = null;
    }
    void blend(int[] before, int[] after, double progress) {
        if (transitionTexture == null) { transitionTexture = new WritableImage(W, H); surface.setDiffuseMap(transitionTexture); }
        int weight = (int) Math.round(Math.clamp(progress, 0, 1) * 256), inverse = 256 - weight;
        for (int n = 0; n < blended.length; n++) {
            int a = before[n], b = after[n];
            int rb = (((a & 0xff00ff) * inverse + (b & 0xff00ff) * weight) >>> 8) & 0xff00ff;
            int green = (((a & 0xff00) * inverse + (b & 0xff00) * weight) >>> 8) & 0xff00;
            blended[n] = 0xff000000 | rb | green;
        }
        transitionTexture.getPixelWriter().setPixels(0, 0, W, H, PixelFormat.getIntArgbInstance(), blended, 0, W);
    }
    int[] pixelsFor(EstadoPlanetario state) { return state.equals(displayedState) ? displayedPixels : null; }
    public EstadoPlanetario displayedState() { return displayedState; }
    Group effectLayer() { return effects; }
    Point3D frontPoint(double x, double y) {
        return effects.getParent().parentToLocal(new Point3D(x, y, -Math.sqrt(130 * 130 - x * x - y * y)));
    }
    Point3D cameraOffset(Point3D origin, Point3D offset) {
        Parent rotating = effects.getParent();
        return rotating.parentToLocal(rotating.localToParent(origin).add(offset));
    }
    void shake(double x, double y) { effects.getParent().setTranslateX(x); effects.getParent().setTranslateY(y); }
    void clearGlow() { glowLayer.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight()); }
    void glow(Point3D point, double radius, Color color, double alpha) {
        Point3D p = effects.getParent().localToParent(point);
        if (p.getZ() > 0) return; // Surface flares on the back hemisphere remain occluded.
        double scale = globeScale.getX(), x = getWidth() / 2 + p.getX() * scale, y = getHeight() / 2 + p.getY() * scale;
        double r = radius * scale;
        var g = glowLayer.getGraphicsContext2D();
        g.setFill(new RadialGradient(0, 0, x, y, r, false, CycleMethod.NO_CYCLE,
                new Stop(0, color.deriveColor(0, 1, 1, alpha)), new Stop(.22, color.deriveColor(0, 1, 1, alpha * .5)), new Stop(1, Color.TRANSPARENT)));
        g.fillOval(x - r, y - r, r * 2, r * 2);
    }
    void clearEffects() { effects.getChildren().clear(); clearGlow(); shake(0, 0); }
}
