package com.projekttellus;

import com.projekttellus.domain.*;
import com.projekttellus.simulation.ModeloPlanetario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {
    @Test void builderProducesIndependentCoherentEarths() {
        Terra a = new TerraBuilder().nome("Aurora").build();
        Terra b = new TerraBuilder().nome("Oásis").build();
        assertNotEquals(a.id(), b.id());
        assertEquals(100, a.estado().atmosfera().oxigenio() + a.estado().atmosfera().nitrogenio()
                + a.estado().atmosfera().co2() + a.estado().atmosfera().helio(), 1e-6);
        assertTrue(ModeloPlanetario.calcular(a.estado()).humana() > 90);
        a.atualizar(new TerraBuilder().clima(new Clima(90, 20, 20)).build().estado());
        assertEquals(15, b.estado().clima().temperatura());
    }
    @Test void rejectsInvalidGasMixturesAndNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> new Atmosfera(50, 80, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Atmosfera(Double.NaN, 79, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Atmosfera(21, 79, 0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Clima(Double.POSITIVE_INFINITY, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Hidrosfera(90, 20));
        assertThrows(IllegalArgumentException.class, () -> new Civilizacao(-1));
        assertThrows(IllegalArgumentException.class, () -> new TerraBuilder().nome(" ").build());
        assertThrows(IllegalArgumentException.class, () -> new TerraBuilder().anos(Double.NaN).build());
    }
    @Test void zeroPressureAndNoOxygenDoNotSupportHumansButLifeIsSeparate() {
        var noOxygen = new TerraBuilder().atmosfera(new Atmosfera(0, 99.96, .04, 0, 1)).build().estado();
        assertEquals(0, ModeloPlanetario.calcular(noOxygen).humana());
        assertTrue(ModeloPlanetario.calcular(noOxygen).biologica() > 50);
        var vacuum = new TerraBuilder().atmosfera(new Atmosfera(21, 78.96, .04, 0, 0)).build().estado();
        assertEquals(0, ModeloPlanetario.calcular(vacuum).humana());
        assertEquals(0, ModeloPlanetario.calcular(vacuum).biologica());
    }
    @Test void mementoIsImmutableCompleteAndWorldBound() {
        Terra a = new TerraBuilder().nome("Terra A").build();
        var before = a.estado();
        var snapshot = a.capturar("Antes");
        a.renomear("Depois");
        a.atualizar(new TerraBuilder().anos(30).clima(new Clima(-40, 80, 5)).build().estado());
        assertEquals(before, snapshot.estado());
        assertThrows(IllegalArgumentException.class, () -> new TerraBuilder().build().restaurar(snapshot));
        a.restaurar(snapshot);
        assertEquals(before, a.estado());
        assertEquals("Terra A", a.nome());
    }
    @Test void nearPureGasNormalizationIsBoundedAndIdempotent() {
        var a = new Atmosfera(99.99999910566146, 0, 0, 0, 1);
        assertTrue(a.oxigenio() <= 100);
        assertEquals(a, new Atmosfera(a.oxigenio(), a.nitrogenio(), a.co2(), a.helio(), a.pressao()));
    }
}
