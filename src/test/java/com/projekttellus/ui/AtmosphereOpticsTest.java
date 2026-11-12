package com.projekttellus.ui;

import com.projekttellus.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AtmosphereOpticsTest {
    @Test void vacuumHasNeitherHaloNorHazeEvenWithLegacyParticleIndex() {
        var s = new TerraBuilder().atmosfera(new Atmosfera(21, 78.96, .04, 0, 0))
                .clima(new Clima(15, 100, 0)).build().estado();
        var p = AtmosphereOptics.from(s);
        assertEquals(0, p.rayleigh()); assertEquals(0, p.particles()); assertEquals(0, p.haloOpacity());
        assertEquals(1, p.transmission());
    }
    @Test void densityCompositionTemperatureAndParticlesChangeOpticalProperties() {
        var earth = AtmosphereOptics.from(new TerraBuilder().build().estado());
        var thin = AtmosphereOptics.from(new TerraBuilder().atmosfera(new Atmosfera(21, 78.96, .04, 0, .01)).build().estado());
        var thick = AtmosphereOptics.from(new TerraBuilder().atmosfera(new Atmosfera(21, 78.96, .04, 0, 5)).build().estado());
        assertTrue(thin.haloOpacity() < earth.haloOpacity()); assertTrue(thick.haloOpacity() > earth.haloOpacity());
        assertTrue(thick.transmission() < earth.transmission());
        var helium = AtmosphereOptics.from(new TerraBuilder().atmosfera(new Atmosfera(21, 58.96, .04, 20, 1)).build().estado());
        var pureHelium = AtmosphereOptics.from(new TerraBuilder().atmosfera(new Atmosfera(0, 0, 0, 100, 1)).build().estado());
        var co2 = AtmosphereOptics.from(new TerraBuilder().atmosfera(new Atmosfera(0, 0, 100, 0, 1)).build().estado());
        assertNotEquals(earth, helium); assertNotEquals(earth.rayleigh(), helium.rayleigh());
        assertTrue(pureHelium.rayleigh() < earth.rayleigh()); assertTrue(co2.rayleigh() > earth.rayleigh());
        var hot = AtmosphereOptics.from(new TerraBuilder().clima(new Clima(150, 0, 0)).build().estado());
        assertTrue(hot.thickness() > earth.thickness());
        var dusty = AtmosphereOptics.from(new TerraBuilder().clima(new Clima(15, 100, 0)).build().estado());
        assertTrue(dusty.particles() > earth.particles()); assertTrue(dusty.transmission() < earth.transmission());
        assertEquals(earth, earth.interpolate(dusty, 0)); assertEquals(dusty, earth.interpolate(dusty, 1));
        assertTrue(earth.interpolate(dusty, .5).particles() > earth.particles());
    }
}
