package com.projekttellus.ui;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import java.util.*;
import java.util.function.DoubleConsumer;

/** Bounded retained 3D geometry, sharing the globe's rotation, scale, lighting and depth buffer. */
final class PlanetEventEffect implements PlanetEffect {
    private static final Color FIRE = Color.web("#ff863b"), CYAN = Color.web("#67e9ff"), GREEN = Color.web("#8dffc0");
    private final PlanetView view;
    private final AnimationPlan plan;
    private final Group root = new Group();
    private final List<DoubleConsumer> tracks = new ArrayList<>();
    private final Random random = new Random(5719);
    private final Map<Color, PhongMaterial> materials = new HashMap<>();

    PlanetEventEffect(PlanetView view, AnimationPlan plan) {
        this.view = view; this.plan = plan;
        root.setMouseTransparent(true); view.effectLayer().getChildren().add(root);
        if (plan.strength() == 0) return;
        Point3D impact = view.frontPoint(24, -18);
        switch (plan.kind()) {
            case ASTEROID -> asteroid(impact);
            case VOLCANO -> volcano(view.frontPoint(-66, -76));
            case NUCLEAR -> nuclear();
            case ALIEN -> aliens();
            case ECOLOGY, MEMENTO -> renewal(impact, plan.kind() == AnimationKind.MEMENTO ? CYAN : GREEN);
            case TRANSITION -> { }
        }
    }
    private PhongMaterial material(Color color) {
        return materials.computeIfAbsent(color, c -> {
            PhongMaterial m = new PhongMaterial(c.deriveColor(0, 1, .09, 1));
            WritableImage light = new WritableImage(1, 1); light.getPixelWriter().setColor(0, 0, c.deriveColor(0, 1, .85, 1));
            m.setSelfIlluminationMap(light); return m;
        });
    }
    private Sphere orb(double radius, Color color) {
        Sphere s = new Sphere(radius, 12); s.setMaterial(material(color)); root.getChildren().add(s); return s;
    }
    private static void position(Node node, Point3D point) {
        node.setTranslateX(point.getX()); node.setTranslateY(point.getY()); node.setTranslateZ(point.getZ());
    }
    private static void scale(Node node, double size) { node.setScaleX(size); node.setScaleY(size); node.setScaleZ(size); }
    private static double clamp(double x) { return Math.clamp(x, 0, 1); }
    private static double smooth(double x) { x = clamp(x); return x * x * (3 - 2 * x); }
    private static Point3D lerp(Point3D a, Point3D b, double t) { return a.add(b.subtract(a).multiply(t)); }

    private void asteroid(Point3D hit) {
        double power = plan.intensity();
        Group rock = new Group(); Sphere body = new Sphere(5 + 14 * power, 18);
        body.setMaterial(new PhongMaterial(Color.web("#6e5140"))); rock.getChildren().add(body);
        for (int i = 0; i < 7; i++) {
            Sphere bump = new Sphere(3 + 4 * power, 8); bump.setMaterial(body.getMaterial());
            position(bump, new Point3D(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5).normalize().multiply(7 + 9 * power));
            rock.getChildren().add(bump);
        }
        root.getChildren().add(rock);
        Point3D start = view.cameraOffset(hit, new Point3D(-230, -175, -290));
        List<Sphere> tail = new ArrayList<>();
        for (int n = 0; n < 20; n++) tail.add(orb((2 + 10 * power) * (1 - n / 22.0), n < 5 ? Color.web("#fff0be") : FIRE));
        tracks.add(t -> {
            double u = clamp(t / .36); Point3D at = lerp(start, hit, u * u);
            rock.setVisible(t < .36); position(rock, at); rock.setRotate(t * 350); rock.setRotationAxis(new Point3D(1, 1, .3));
            scale(rock, .5 + .5 * u);
            for (int n = 0; n < tail.size(); n++) {
                Sphere s = tail.get(n); s.setVisible(t < .38);
                double v = clamp(u - n * .012); position(s, lerp(start, hit, v * v));
                scale(s, clamp((.39 - t) * 18));
            }
            double shake = t >= .36 && t < .56 ? power * 5 * (1 - (t - .36) / .2) : 0;
            view.shake(Math.sin(t * 470) * shake, Math.cos(t * 390) * shake * .6);
        });
        burst(hit, .36, .31, plan.particles(), 22 + 48 * power, FIRE, true);
        ring(hit, .36, .35, .25 + .95 * power, FIRE);
        ring(hit, .39, .42, .4 + 1.3 * power, Color.web("#ffd49b"));
        flash(hit, .36, .12, 12 + 32 * power, FIRE);
        haze(.42, Color.web("#81756c"), .22 * power);
    }
    private void volcano(Point3D hit) {
        Point3D normal = hit.normalize(); Sphere ember = orb(13 + 16 * plan.intensity(), Color.web("#ff3b23")); position(ember, hit.multiply(1.015));
        tracks.add(t -> {
            ember.setVisible(t < .66); scale(ember, t < .3 ? .15 + 1.6 * t : Math.max(.01, 1 - (t - .3) * 2.7));
            if (t < .66) view.glow(hit, 35, FIRE, Math.min(.6, t * 2) * (1 - smooth((t - .32) / .34)));
        });
        burst(hit, .3, .42, plan.particles() * 2 / 3, 30 + 55 * plan.intensity(), FIRE, false);
        flash(hit, .3, .15, 13 + 22 * plan.intensity(), FIRE);
        ring(hit, .3, .32, .65 * plan.intensity() + .2, FIRE);
        int clouds = Math.max(5, plan.particles() / 6);
        for (int n = 0; n < clouds; n++) {
            double begin = .31 + n * .012;
            Sphere ash = orb(8 + random.nextDouble() * 9, Color.web(n % 2 == 0 ? "#5c5662" : "#827278"));
            PhongMaterial ashMaterial = new PhongMaterial(Color.web("#504957")); ashMaterial.setSpecularColor(Color.BLACK); ash.setMaterial(ashMaterial);
            Point3D drift = new Point3D(random.nextDouble() * 18 - 9, random.nextDouble() * 12 - 6, 0);
            tracks.add(t -> {
                double u = (t - begin) / .42; ash.setVisible(u >= 0 && u < 1);
                if (u < 0 || u >= 1) return;
                position(ash, hit.add(normal.multiply(u * (65 + 35 * plan.intensity()))).add(drift.multiply(u * 2)));
                scale(ash, (.3 + u * 2) * Math.min(1, (1 - u) * 5));
                ashMaterial.setDiffuseColor(Color.color(.25, .23, .28, Math.min(.65, u * 5) * Math.min(1, (1 - u) * 5)));
            });
        }
        haze(.38, Color.web("#50485a"), .32 * plan.intensity());
    }
    private void nuclear() {
        for (int n = 0; n < plan.sources(); n++) {
            double angle = n * 2.39996, radius = 24 + 72 * Math.sqrt((n + .5) / plan.sources());
            Point3D hit = view.frontPoint(Math.cos(angle) * radius, Math.sin(angle) * radius * .85);
            double begin = .13 + n * .043;
            flash(hit, begin, .1, 7 + 13 * plan.intensity(), Color.web("#ffe9ac"));
            ring(hit, begin, .24, .18 + .24 * plan.intensity(), FIRE);
            burst(hit, begin, .27, plan.particles() / plan.sources(), 16 + 26 * plan.intensity(), FIRE, true);
            Sphere smoke = orb(9 + 6 * plan.intensity(), Color.web("#61596b"));
            PhongMaterial smokeMaterial = new PhongMaterial(Color.web("#61596b")); smokeMaterial.setSpecularColor(Color.BLACK); smoke.setMaterial(smokeMaterial);
            tracks.add(t -> {
                double u = (t - begin - .04) / .33; smoke.setVisible(u >= 0 && u < 1);
                if (u < 0 || u >= 1) return;
                position(smoke, hit.add(hit.normalize().multiply(u * 32))); scale(smoke, (.3 + u) * Math.min(1, (1 - u) * 5));
                smokeMaterial.setDiffuseColor(Color.color(.3, .27, .33, .65 * Math.sin(Math.PI * u)));
            });
        }
        haze(.3, Color.web("#6d5877"), .33 * plan.intensity());
    }
    private void aliens() {
        for (int n = 0; n < plan.sources(); n++) {
            double phase = n * Math.PI * 2 / plan.sources();
            Point3D target = view.frontPoint(Math.cos(phase) * 103, Math.sin(phase) * 90);
            Group ship = new Group();
            Sphere hull = new Sphere(17, 20); hull.setScaleY(.22); hull.setMaterial(new PhongMaterial(Color.web("#aab6cf")));
            Sphere dome = new Sphere(8, 16); dome.setTranslateY(-5); dome.setScaleY(.7); dome.setMaterial(material(Color.web("#ac72ff")));
            Cylinder rim = new Cylinder(17.5, 2, 24); rim.setMaterial(material(Color.web("#9861ed")));
            ship.getChildren().addAll(hull, rim, dome);
            for (int k = 0; k < 6; k++) {
                Sphere lamp = new Sphere(1.5, 8); lamp.setMaterial(material(CYAN));
                position(lamp, new Point3D(Math.cos(k * Math.PI / 3) * 14, 0, Math.sin(k * Math.PI / 3) * 14)); ship.getChildren().add(lamp);
            }
            root.getChildren().add(ship);
            Cylinder beam = new Cylinder(1.1 + 1.3 * plan.intensity(), 1, 12); beam.setMaterial(material(Color.web("#c18aff"))); root.getChildren().add(beam);
            Sphere contact = orb(5, Color.web("#ac74ff")); position(contact, target.multiply(1.015));
            final int shipIndex = n;
            tracks.add(t -> {
                Point3D at = shipAt(target, phase, t); position(ship, at);
                ship.setRotate(Math.sin(t * 12 + phase) * 12); ship.setRotationAxis(Rotate.Z_AXIS);
                double entrance = smooth(t / .16), exit = 1 - smooth((t - .8) / .2); scale(ship, entrance * exit);
                boolean firing = t > .22 + shipIndex * .018 && t < .76;
                beam.setVisible(firing); contact.setVisible(firing);
                if (firing) { cylinderBetween(beam, at, target); scale(contact, .7 + .25 * Math.sin(t * 110)); view.glow(target, 22, Color.web("#a76aff"), .34); }
            });
            for (int k = 0; k < plan.particles() / plan.sources(); k++) {
                double offset = random.nextDouble(); Sphere oxygen = orb(.9 + random.nextDouble() * 1.4, CYAN);
                tracks.add(t -> {
                    oxygen.setVisible(t > .23 && t < .78);
                    double u = (t * 4 + offset) % 1;
                    Point3D at = shipAt(target, phase, t);
                    Point3D p = lerp(target, at, u).add(Math.sin(u * 14 + offset * 9) * 4 * (1 - u), Math.cos(u * 13) * 3 * (1 - u), 0);
                    position(oxygen, p); scale(oxygen, Math.sin(Math.PI * u));
                });
            }
            ring(target, .24 + n * .018, .36, .18 + .2 * plan.intensity(), Color.web("#b48cff"));
        }
        haze(.35, Color.web("#8c6bad"), .14 * plan.intensity());
    }
    private Point3D shipAt(Point3D target, double phase, double t) {
        double distance = 66 + (1 - smooth(t / .2)) * 180 + smooth((t - .78) / .22) * 240;
        return target.add(target.normalize().multiply(distance)).add(Math.sin(t * 7 + phase) * 22, Math.cos(t * 5 + phase) * 11, Math.sin(t * 6 + phase) * 18);
    }
    private void renewal(Point3D origin, Color color) {
        ring(origin, .02, .76, Math.PI, color);
        ring(origin, .13, .8, Math.PI, color.deriveColor(0, .65, 1, 1));
        flash(origin, .02, .35, 8 + 16 * plan.strength(), color);
        int count = plan.particles() / 2;
        for (int n = 0; n < count; n++) {
            double angle = n * 2.39996, radius = 110 * Math.sqrt((n + .5) / count);
            Point3D p = view.frontPoint(Math.cos(angle) * radius, Math.sin(angle) * radius);
            double begin = .12 + n / (double) count * .48;
            Sphere mote = orb(.7 + random.nextDouble() * 1.1, color);
            tracks.add(t -> {
                double u = (t - begin) / .32; mote.setVisible(u >= 0 && u < 1);
                if (u >= 0 && u < 1) { position(mote, p.add(p.normalize().multiply(3 + 17 * u))); scale(mote, Math.sin(u * Math.PI)); }
            });
        }
    }
    private void haze(double begin, Color color, double alpha) {
        Sphere atmosphere = new Sphere(133, 48);
        PhongMaterial haze = new PhongMaterial(Color.TRANSPARENT); haze.setSpecularColor(Color.BLACK); atmosphere.setMaterial(haze);
        root.getChildren().add(atmosphere);
        tracks.add(t -> {
            double envelope = smooth((t - begin) / .2) * (1 - smooth((t - .76) / .24));
            atmosphere.setVisible(envelope > .001);
            haze.setDiffuseColor(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * envelope));
        });
    }
    private void flash(Point3D point, double begin, double life, double size, Color color) {
        Sphere core = orb(size, color); position(core, point.multiply(1.02));
        PointLight light = new PointLight(color.deriveColor(0, 1, .18, 1)); position(light, point.multiply(1.4)); root.getChildren().add(light);
        tracks.add(t -> {
            double u = (t - begin) / life; boolean visible = u >= 0 && u < 1;
            core.setVisible(visible); light.setLightOn(visible && u < .35);
            if (visible) {
                scale(core, Math.max(.01, Math.sin(Math.PI * Math.sqrt(u))));
                view.glow(point, size * 2.6, color, .75 * Math.sin(Math.PI * Math.sqrt(u)));
            }
        });
    }
    private void burst(Point3D point, double begin, double life, int count, double speed, Color color, boolean radial) {
        Point3D normal = point.normalize();
        for (int n = 0; n < count; n++) {
            Point3D randomVector = new Point3D(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).normalize();
            Point3D velocity = randomVector.add(normal.multiply(radial ? .65 : 2.1)).multiply(speed * (.4 + random.nextDouble()));
            double delay = random.nextDouble() * .035;
            Sphere particle = orb(.8 + random.nextDouble() * (1 + 2 * plan.intensity()), n % 5 == 0 ? Color.web("#fff2c3") : color);
            tracks.add(t -> {
                double u = (t - begin - delay) / life; particle.setVisible(u >= 0 && u < 1);
                if (u < 0 || u >= 1) return;
                position(particle, point.add(velocity.multiply(u)).subtract(normal.multiply(u * u * speed * .48)));
                scale(particle, Math.max(.01, (1 - u) * 1.3));
            });
        }
    }
    /** A geodesic annulus lifted above the sphere, rather than a flat screen-space circle. */
    private void ring(Point3D origin, double begin, double life, double maxAngle, Color color) {
        int segments = 72; TriangleMesh mesh = new TriangleMesh(); float[] points = new float[(segments + 1) * 6];
        mesh.getPoints().setAll(points); mesh.getTexCoords().setAll(0, 0);
        int[] faces = new int[segments * 12];
        for (int n = 0; n < segments; n++) {
            int a = n * 2, b = a + 1, c = a + 2, d = a + 3, j = n * 12;
            int[] values = {a, 0, b, 0, c, 0, b, 0, d, 0, c, 0}; System.arraycopy(values, 0, faces, j, 12);
        }
        mesh.getFaces().setAll(faces); MeshView ring = new MeshView(mesh); ring.setCullFace(CullFace.NONE); ring.setMaterial(material(color)); root.getChildren().add(ring);
        Point3D normal = origin.normalize();
        Point3D tangent = normal.crossProduct(Math.abs(normal.getY()) < .9 ? new Point3D(0, 1, 0) : new Point3D(1, 0, 0)).normalize();
        Point3D bitangent = normal.crossProduct(tangent).normalize();
        tracks.add(t -> {
            double u = (t - begin) / life; ring.setVisible(u >= 0 && u < 1);
            if (u < 0 || u >= 1) return;
            double angle = .012 + maxAngle * smooth(u);
            double width = (.008 + .023 * plan.strength()) * Math.sin(Math.PI * u);
            for (int n = 0; n <= segments; n++) for (int edge = 0; edge < 2; edge++) {
                double theta = n * Math.PI * 2 / segments, a = angle + (edge == 0 ? -width : width);
                Point3D p = normal.multiply(Math.cos(a)).add(tangent.multiply(Math.sin(a) * Math.cos(theta))).add(bitangent.multiply(Math.sin(a) * Math.sin(theta))).multiply(132);
                int index = n * 6 + edge * 3; points[index] = (float) p.getX(); points[index + 1] = (float) p.getY(); points[index + 2] = (float) p.getZ();
            }
            mesh.getPoints().setAll(points);
        });
    }
    private static void cylinderBetween(Cylinder beam, Point3D from, Point3D to) {
        Point3D delta = to.subtract(from), axis = new Point3D(0, 1, 0).crossProduct(delta);
        beam.setHeight(delta.magnitude()); position(beam, lerp(from, to, .5));
        if (axis.magnitude() > .0001) { beam.setRotationAxis(axis); beam.setRotate(Math.toDegrees(Math.acos(Math.clamp(delta.normalize().getY(), -1, 1)))); }
    }
    @Override public void frame(double progress) { view.clearGlow(); tracks.forEach(track -> track.accept(progress)); }
    @Override public void dispose() { tracks.clear(); view.clearEffects(); materials.clear(); }
}
