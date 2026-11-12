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
    private final Canvas atmosphereLayer = new Canvas();
    private AtmosphereOptics optics = new AtmosphereOptics(0, 0, 0), opticsBefore = optics, opticsAfter = optics;
    private boolean sunEnabled = true;
    private record SurfaceKey(double water, double ice, double vegetation, double temperature) {
        static SurfaceKey from(EstadoPlanetario s) { return new SurfaceKey(s.hidrosfera().agua(), s.hidrosfera().gelo(), s.biosfera().vegetacao(), s.clima().temperatura()); }
    }
    private final java.util.Map<SurfaceKey, int[]> textureCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<>(6, .75f, true) {
                @Override protected boolean removeEldestEntry(java.util.Map.Entry<SurfaceKey, int[]> e) { return size() > 6; }
            });
    private final double[] land = new double[W * H];
    private final double[] relief = new double[W * H];
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
        rotating.translateXProperty().addListener((o,a,b) -> drawAtmosphere());
        rotating.translateYProperty().addListener((o,a,b) -> drawAtmosphere());
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
        atmosphereLayer.setMouseTransparent(true);
        atmosphereLayer.widthProperty().bind(widthProperty()); atmosphereLayer.heightProperty().bind(heightProperty());
        // Canvas sizes settle after the parent's resize listener; repaint against the final projection.
        atmosphereLayer.widthProperty().addListener((o,a,b) -> drawAtmosphere());
        atmosphereLayer.heightProperty().addListener((o,a,b) -> drawAtmosphere());
        getChildren().addAll(scene, atmosphereLayer, glowLayer);
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
        drawAtmosphere();
    }
    public void reset() { longitude.setAngle(-25); latitude.setAngle(-12); zoom = 1; resizeGlobe(); }
    public void setSunlight(boolean enabled) {
        sunEnabled = enabled;
        sunlight.setLightOn(enabled); ambient.setColor(enabled ? Color.color(.32, .38, .45) : Color.color(.85, .88, .9));
        drawAtmosphere();
    }

    /** Orthographic projection of a homogeneous atmosphere. Its limb follows zoom, resize and event shake.
     * Looking through the limb lengthens the optical path. A uniform gas shell does not rotate with land. */
    private void drawAtmosphere() {
        var g = atmosphereLayer.getGraphicsContext2D();
        g.clearRect(0, 0, atmosphereLayer.getWidth(), atmosphereLayer.getHeight());
        if (optics.haloOpacity() < .00001) return;
        double scale = globeScale.getX(), r = 130 * scale;
        double x = getWidth() / 2 + effects.getParent().getTranslateX() * scale;
        double y = getHeight() / 2 + effects.getParent().getTranslateY() * scale;
        if (r <= 0) return;
        double outer = r * (1 + optics.thickness() * 2.8);
        double dust = -Math.expm1(-optics.particles());
        Color molecular = Color.color(.26, .58, 1);
        Color particles = Color.color(.65, .57, .46); // One documented generic ash/dust population, never a gas hue.
        Color tint = molecular.interpolate(particles, dust * .88);
        // The halo is integrated by angle so the illuminated limb faces the same PointLight as the sphere.
        double lx = sunlight.getTranslateX() - x, ly = sunlight.getTranslateY() - y;
        double length = Math.hypot(lx, ly); lx /= length; ly /= length;
        // Continuous illumination gradient avoids radial seams between angular sectors.
        double inner = r * .94;
        int rings = Math.max(16, Math.min(72, (int)Math.ceil(outer-inner)));
        double step = (outer-inner)/rings;
        g.setLineWidth(step + .15);
        for (int i = 0; i < rings; i++) {
            double radius = inner + (i+.5)*step;
            double radial = radius < r ? (radius-inner)/(r-inner)
                    : Math.exp(-4 * (radius-r)/(outer-r)) * (outer-radius)/(outer-r);
            double alpha = Math.min(.85, optics.haloOpacity() * radial);
            double dark = sunEnabled ? .08 : .75, bright = sunEnabled ? 1 : .75;
            g.setStroke(new LinearGradient(x+lx*outer,y+ly*outer,x-lx*outer,y-ly*outer,false,CycleMethod.NO_CYCLE,
                    new Stop(0,tint.deriveColor(0,1,1,alpha*bright)),
                    new Stop(.5,tint.deriveColor(0,1,1,alpha*(sunEnabled?.22:.75))),
                    new Stop(1,tint.deriveColor(0,1,1,alpha*dark))));
            g.strokeOval(x-radius,y-radius,2*radius,2*radius);
        }
        // Front column: denser paths toward the limb partially veil the lit surface. No texture regeneration.
        double haze = Math.min(.86, (1 - optics.transmission()) * .82);
        g.setFill(new RadialGradient(0, 0, x-r*.25, y-r*.20, r*1.35, false, CycleMethod.NO_CYCLE,
                new Stop(0, tint.deriveColor(0, .65, 1.15, haze*.30)),
                new Stop(.65, tint.deriveColor(0, .8, .85, haze*.65)),
                new Stop(1, tint.deriveColor(0, .8, .45, haze))));
        g.fillOval(x-r, y-r, 2*r, 2*r);
    }

    void beginAtmosphereTransition(EstadoPlanetario before, EstadoPlanetario after) {
        opticsBefore = AtmosphereOptics.from(before); opticsAfter = AtmosphereOptics.from(after);
        optics = opticsBefore; drawAtmosphere();
    }
    AtmosphereOptics atmosphereProperties() { return optics; }

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
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            land[y * W + x] = reader.getColor(x, y).getRed();
            relief[y * W + x] = (land[y * W + x] > .5 ? .55 : 0) + .45 * noise(x, y);
        }
    }
    private void polygon(javafx.scene.canvas.GraphicsContext g, double... pairs) {
        int n = pairs.length / 2; double[] x = new double[n], y = new double[n];
        for (int i = 0; i < n; i++) { x[i] = pairs[i * 2] * W; y[i] = pairs[i * 2 + 1] * H; }
        g.fillPolygon(x, y, n);
    }
    private static double noise(int x, int y) {
        return .5 + .23 * Math.sin(x * .061 + Math.sin(y * .053) * 3) + .17 * Math.cos(y * .093 + Math.cos(x * .04) * 2);
    }

    /** Pure pixel preparation: may run on the animator worker; topology is immutable after construction. */
    int[] prepareTexture(EstadoPlanetario state) {
        SurfaceKey key = SurfaceKey.from(state);
        int[] cached = textureCache.get(key);
        if (cached != null) return cached;
        int[] pixels = new int[W * H];
        double water = state.hidrosfera().agua() / 100, ice = state.hidrosfera().gelo() / 100;
        double vegetation = state.biosfera().vegetacao() / 100;
        double heat = Math.clamp((state.clima().temperatura() - 25) / 130, 0, 1);
        double cold = Math.clamp((-state.clima().temperatura()) / 150, 0, 1);
        Color soil = Color.web("#b69866").interpolate(Color.web("#36825b"), vegetation).interpolate(Color.web("#6c342d"), heat * .7);
        Color sea = Color.web("#124b70").interpolate(Color.web("#593e32"), heat * .8).interpolate(Color.web("#8cc4d8"), cold * .55);
        // Area-weighted waterline over the non-glacial sphere. Equirectangular pixels near poles cover less area.
        double[] histogram = new double[2048]; double available = 0;
        for (int y = 0; y < H; y++) {
            double latitudeSin = Math.abs(Math.cos(Math.PI * (y + .5) / H));
            if (latitudeSin >= 1 - ice) continue;
            double area = Math.sin(Math.PI * (y + .5) / H);
            for (int x = 0; x < W; x++) histogram[Math.min(2047, (int)(relief[y*W+x]*2048))] += area;
            available += W * area;
        }
        double target = available * Math.min(1, water / Math.max(1e-12, 1-ice)), accumulated = 0;
        int level = 0;
        while (level < 2048 && accumulated < target) accumulated += histogram[level++];
        double waterline = level / 2048.0;
        for (int y = 0; y < H; y++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException("Textura substituída");
            double polar = Math.abs(Math.cos(Math.PI * (y + .5) / H));
            for (int x = 0; x < W; x++) {
                double noise = noise(x, y);
                boolean wet = relief[y * W + x] < waterline;
                Color c = wet ? sea : soil;
                boolean frozen = ice >= 1 || polar >= 1 - ice;
                if (frozen) c = Color.web("#dcecf0");
                c = c.deriveColor(0, 1, .86 + noise * .24, 1);
                if (!wet && !frozen && vegetation > .1 && noise > .65) c = c.interpolate(Color.web("#76a866"), vegetation * .3);
                pixels[y * W + x] = 0xff000000 | ((int) Math.round(c.getRed() * 255) << 16)
                        | ((int) Math.round(c.getGreen() * 255) << 8) | (int) Math.round(c.getBlue() * 255);
            }
        }
        textureCache.put(key, pixels);
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
        optics = AtmosphereOptics.from(state); drawAtmosphere();
    }
    void blend(int[] before, int[] after, double progress) {
        optics = opticsBefore.interpolate(opticsAfter, progress); drawAtmosphere();
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
