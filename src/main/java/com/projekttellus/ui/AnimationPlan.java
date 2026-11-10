package com.projekttellus.ui;

import java.util.Objects;

/** Bounded visual budget. Contains no mutable world or simulation operation. */
public record AnimationPlan(AnimationKind kind, double intensity, double seconds,
                            int particles, int sources, double strength) {
    public static AnimationPlan of(AnimationKind kind, double intensity, AnimationMode mode) {
        Objects.requireNonNull(kind); Objects.requireNonNull(mode);
        if (!Double.isFinite(intensity) || intensity < 0 || intensity > 1)
            throw new IllegalArgumentException("Intensidade visual fora de 0–1");
        if (mode == AnimationMode.OFF || (kind.isEvent() && intensity == 0))
            return new AnimationPlan(kind, intensity, 0, 0, 0, 0);
        if (mode == AnimationMode.REDUCED)
            return new AnimationPlan(kind, intensity, .65, 0, 0, 0);
        int sources = switch (kind) {
            case NUCLEAR -> 2 + (int) Math.round(10 * intensity);
            case ALIEN -> 1 + (int) Math.round(5 * intensity);
            default -> 1;
        };
        return new AnimationPlan(kind, intensity, kind == AnimationKind.TRANSITION ? .8 : kind == AnimationKind.MEMENTO ? 2.4 : 3.8,
                (int) (30 + 150 * intensity), sources, kind.isEvent() ? intensity : .55);
    }
    public double surfaceProgress(double progress) {
        double start = switch (kind) {
            case ASTEROID -> .35;
            case VOLCANO -> .3;
            case NUCLEAR, ALIEN -> .2;
            default -> 0;
        };
        double t = Math.clamp((progress - start) / (1 - start), 0, 1);
        return t * t * (3 - 2 * t);
    }
}
