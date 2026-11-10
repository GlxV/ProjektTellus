package com.projekttellus.ui;

import com.projekttellus.application.ResultadoOperacao;
import com.projekttellus.domain.EstadoPlanetario;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.*;
import java.util.concurrent.*;

/** Owns one visual session. Only accepts committed results; never invokes a simulation operation. */
public final class PlanetEventAnimator implements AutoCloseable {
    private final PlanetView view;
    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper();
    private final ExecutorService textures = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "tellus-texturas"); thread.setDaemon(true); return thread;
    });
    private final AnimationTimer timer = new AnimationTimer() {
        @Override public void handle(long now) {
            if (started == 0) started = now;
            double progress = Math.min(1, (now - started) / (plan.seconds() * 1_000_000_000));
            // Bound large pixel uploads; geometry still follows every display pulse without frame division.
            if (progress >= 1 || rendered == 0 || now - rendered >= 16_000_000) {
                rendered = now;
                view.blend(beforePixels, afterPixels, plan.surfaceProgress(progress));
            }
            if (effect != null) effect.frame(progress);
            if (progress >= 1) finish();
        }
    };
    private AnimationPlan plan;
    private EstadoPlanetario target;
    private int[] beforePixels, afterPixels;
    private PlanetEffect effect;
    private long generation, started, rendered;
    private boolean skip, closed;
    private Future<?> preparation;

    public PlanetEventAnimator(PlanetView view) { this.view = view; }
    public ReadOnlyBooleanProperty runningProperty() { return running.getReadOnlyProperty(); }
    public boolean isRunning() { return running.get(); }

    public void play(ResultadoOperacao result, AnimationKind kind, double intensity, AnimationMode mode) {
        if (closed) return;
        discard();
        plan = AnimationPlan.of(kind, intensity, mode); target = result.depois();
        // The old world is already confirmed. The captured endpoints alone drive this presentation.
        int[] cachedBefore = view.pixelsFor(result.antes()), cachedAfter = view.pixelsFor(result.depois());
        long token = generation; running.set(true);
        preparation = textures.submit(() -> {
            try {
                int[] a = cachedBefore == null ? view.prepareTexture(result.antes()) : cachedBefore;
                if (Thread.currentThread().isInterrupted()) return;
                int[] b = cachedAfter == null ? view.prepareTexture(result.depois()) : cachedAfter;
                if (Thread.currentThread().isInterrupted()) return;
                Platform.runLater(() -> {
                    if (closed || token != generation) return;
                    beforePixels = a; afterPixels = b;
                    if (skip || plan.seconds() == 0 || result.antes().equals(result.depois()) && kind == AnimationKind.TRANSITION) { finish(); return; }
                    view.blend(a, b, 0);
                    effect = new PlanetEventEffect(view, plan); effect.frame(0);
                    started = 0; rendered = 0; timer.start();
                });
            } catch (RuntimeException failure) {
                Platform.runLater(() -> {
                    if (closed || token != generation) return;
                    discard(); view.update(result.depois());
                    failure.printStackTrace(System.err);
                });
            }
        });
    }
    /** Skip/cancel settles the already committed endpoint, even during asynchronous preparation. */
    public void skip() {
        if (!isRunning()) return;
        skip = true; timer.stop(); clearEffect();
        if (afterPixels != null) finish();
    }
    public void cancel() { skip(); }
    /** Context switch: the caller will display its own state. Stale worker callbacks cannot repaint it. */
    public void discard() {
        generation++; timer.stop();
        if (preparation != null) preparation.cancel(true);
        preparation = null; clearEffect(); beforePixels = null; afterPixels = null;
        target = null; skip = false; running.set(false);
    }
    private void finish() {
        timer.stop(); clearEffect();
        if (target != null && afterPixels != null) view.showPrepared(target, afterPixels);
        running.set(false); beforePixels = null; afterPixels = null; target = null;
    }
    private void clearEffect() { if (effect != null) { effect.dispose(); effect = null; } else view.clearEffects(); }
    @Override public void close() { if (!closed) { discard(); closed = true; textures.shutdownNow(); } }
}
