package com.projekttellus.ui;

import com.projekttellus.domain.EstadoPlanetario;

/** Pure optical visualization parameters. Dimensionless, artistically amplified, not a spectral solver. */
public record AtmosphereOptics(double rayleigh, double particles, double thickness) {
    public AtmosphereOptics {
        if (!Double.isFinite(rayleigh) || !Double.isFinite(particles) || !Double.isFinite(thickness)
                || rayleigh < 0 || particles < 0 || thickness < 0)
            throw new IllegalArgumentException("Propriedades ópticas inválidas.");
    }
    public static AtmosphereOptics from(EstadoPlanetario s) {
        var a = s.atmosfera();
        if (a.pressao() == 0) return new AtmosphereOptics(0, 0, 0);
        double mass = (32 * a.oxigenio() + 28 * a.nitrogenio() + 44 * a.co2() + 4 * a.helio()) / 100;
        // Relative scattering weights chosen for teaching; not measured cross-sections.
        double scattering = (.85 * a.oxigenio() + a.nitrogenio() + 2.5 * a.co2() + .014 * a.helio()) / 100;
        double column = a.pressao() * 28.8 / mass;
        double verticalScale = (s.clima().temperatura() + 273.15) / 288.15 * 28.8 / mass;
        return new AtmosphereOptics(.12 * column * scattering,
                1.8 * s.clima().aerossol() / 100 * a.pressao() / (a.pressao() + .05),
                Math.clamp(.022 + .018 * Math.log1p(a.pressao()) + .028 * Math.log1p(verticalScale), .02, .16));
    }
    public double haloOpacity() { return -Math.expm1(-3 * rayleigh - .65 * particles); }
    public double transmission() { return Math.exp(-rayleigh - particles); }
    public AtmosphereOptics interpolate(AtmosphereOptics other, double t) {
        if (t <= 0) return this;
        if (t >= 1) return other;
        return new AtmosphereOptics(rayleigh + (other.rayleigh - rayleigh) * t,
                particles + (other.particles - particles) * t, thickness + (other.thickness - thickness) * t);
    }
}
