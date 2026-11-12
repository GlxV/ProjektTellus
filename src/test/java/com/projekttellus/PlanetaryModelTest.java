package com.projekttellus;

import com.projekttellus.domain.*;
import com.projekttellus.simulation.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanetaryModelTest {
    @Test void stableSupportEstimateMatchesLongTermDemographicEquilibrium() {
        var s = new TerraBuilder().atmosfera(new Atmosfera(13.4,86.56,.04,0,1))
                .clima(new Clima(15.54,0,0)).hidrosfera(new Hidrosfera(74,0)).biosfera(new Biosfera(100)).build().estado();
        var i = ModeloPlanetario.calcular(s);
        assertTrue(i.humana()>80 && i.humana()<90);
        assertTrue(i.capacidadeSuporte()>s.civilizacao().populacao());
        var next = ModeloPlanetario.avancar(s,1000);
        assertTrue(next.civilizacao().populacao()>=s.civilizacao().populacao(),"Ambiente estável não deve levar à extinção abaixo do suporte apresentado");
        assertTrue(next.civilizacao().populacao()<=ModeloPlanetario.calcular(next).capacidadeSuporte());
    }
    @Test void warmingCoolingAndFractionalEvolutionConserveSurfaceWater() {
        var warm = new TerraBuilder().atmosfera(new Atmosfera(21, 77, 2, 0, 1)).hidrosfera(new Hidrosfera(30, 44)).build().estado();
        var after = ModeloPlanetario.avancar(warm, 30.25);
        assertTrue(after.clima().temperatura() > warm.clima().temperatura());
        assertTrue(after.hidrosfera().gelo() < warm.hidrosfera().gelo());
        var cold = new TerraBuilder().atmosfera(new Atmosfera(21, 79, 0, 0, 1)).clima(new Clima(-35, 50, 0)).build().estado();
        assertTrue(ModeloPlanetario.avancar(cold, 10).hidrosfera().gelo() > cold.hidrosfera().gelo());
        for (var s : new EstadoPlanetario[]{warm, cold}) {
            var a = ModeloPlanetario.avancar(s, 999.9);
            assertEquals(a, ModeloPlanetario.avancar(s, 999.9));
            assertEquals(s.hidrosfera().agua()+s.hidrosfera().gelo(), a.hidrosfera().agua()+a.hidrosfera().gelo(), 1e-9);
            assertEquals(s.superficieContinental(), a.superficieContinental(), 1e-9);
        }
    }
    @Test void allIndicatorsStayBoundedAcrossExtremesAndMaximumEvents() {
        for (double pressure : new double[]{0, .01, 1, 20}) for (double temperature : new double[]{-150, 15, 200})
            for (var hydro : new Hidrosfera[]{new Hidrosfera(100,0),new Hidrosfera(0,100),new Hidrosfera(0,0)}) {
                var s = new TerraBuilder().atmosfera(new Atmosfera(0, 0, 100, 0, pressure))
                        .clima(new Clima(temperature,100,100)).hidrosfera(hydro).civilizacao(new Civilizacao(100_000_000_000L)).build().estado();
                for (var creator : SimulationTest.creators().toList()) {
                    var event = creator.criar(1).aplicar(s);
                    var next = ModeloPlanetario.avancar(event, 1000);
                    for (var e : new EstadoPlanetario[]{event, next}) {
                        var i = ModeloPlanetario.calcular(e);
                        for (double x : new double[]{i.humana(),i.biologica(),i.sustentabilidade()})
                            assertTrue(Double.isFinite(x) && x >= 0 && x <= 100);
                        assertTrue(e.superficieVegetada() <= e.superficieContinental());
                        assertEquals(hydro.agua()+hydro.gelo(), e.hidrosfera().agua()+e.hidrosfera().gelo(),1e-9);
                    }
                }
            }
    }
    @Test void oceanAllowsSurvivalButCannotSupportConventionalCivilization() {
        var ocean = new TerraBuilder().hidrosfera(new Hidrosfera(100, 0))
                .biosfera(new Biosfera(93)).civilizacao(new Civilizacao(10_000_000_000L)).build().estado();
        var i = ModeloPlanetario.calcular(ocean);
        assertTrue(i.humana() > 80);
        assertEquals(0, i.capacidadeSuporte());
        assertEquals(0, i.sustentabilidade());
        assertEquals(0, ocean.superficieVegetada());
        assertTrue(ModeloPlanetario.avancar(ocean, 10).civilizacao().populacao() < ocean.civilizacao().populacao());
    }

    @Test void heliumDoesNotPunishSurvivalWhenPartialPressuresAreUnchanged() {
        var earth = new TerraBuilder().build().estado();
        var helium = new TerraBuilder().atmosfera(new Atmosfera(21, 58.96, .04, 20, 1)).build().estado();
        assertEquals(ModeloPlanetario.calcular(earth).humana(), ModeloPlanetario.calcular(helium).humana(), 1e-10);
        assertTrue(ModeloPlanetario.calcular(earth).sustentabilidade() > 80);
        assertTrue(ModeloPlanetario.calcular(earth).capacidadeSuporte() > 8_000_000_000L);
    }

    @Test void partialPressuresAndExtremesLimitSurvivalWithoutRequiringOxygenForAllLife() {
        var noOxygen = new TerraBuilder().atmosfera(new Atmosfera(0, 99.96, .04, 0, 1)).build().estado();
        assertEquals(0, ModeloPlanetario.calcular(noOxygen).humana());
        assertTrue(ModeloPlanetario.calcular(noOxygen).biologica() > 60);
        for (var a : new Atmosfera[]{new Atmosfera(21, 78.96, .04, 0, 0),
                new Atmosfera(21, 78.96, .04, 0, .1), new Atmosfera(21, 78.96, .04, 0, 20),
                new Atmosfera(21, 29, 50, 0, 1)}) {
            var s = new TerraBuilder().atmosfera(a).build().estado();
            assertTrue(ModeloPlanetario.calcular(s).humana() < 10);
        }
        for (double t : new double[]{-150, 200}) {
            var s = new TerraBuilder().clima(new Clima(t, 0, 0)).build().estado();
            assertEquals(0, ModeloPlanetario.calcular(s).humana());
            assertTrue(ModeloPlanetario.calcular(s).biologica() < 5);
        }
    }

    @Test void populationApproachesSupportAndDegradedEcosystemRecoversGradually() {
        var crowded = new TerraBuilder().civilizacao(new Civilizacao(80_000_000_000L)).build().estado();
        var next = ModeloPlanetario.avancar(crowded, 1);
        assertTrue(next.civilizacao().populacao() < crowded.civilizacao().populacao());
        assertTrue(next.civilizacao().populacao() > crowded.civilizacao().populacao() * .75);
        var stable = ModeloPlanetario.avancar(new TerraBuilder().build().estado(), 1000);
        assertTrue(stable.civilizacao().populacao() < 20_000_000_000L);
        var damaged = new TerraBuilder().biosfera(new Biosfera(0)).build().estado();
        var recovery = ModeloPlanetario.avancar(damaged, 1);
        assertTrue(recovery.biosfera().vegetacao() > 0 && recovery.biosfera().vegetacao() < 10);
        var dry = new TerraBuilder().hidrosfera(new Hidrosfera(0, 0)).build().estado();
        assertTrue(ModeloPlanetario.avancar(dry, 10).biosfera().vegetacao() < dry.biosfera().vegetacao());
    }

    @Test void explanationsReconstructScoresAndIdentifyRealConstraints() {
        var s = new TerraBuilder().clima(new Clima(42, 35, 80)).build().estado();
        var i = ModeloPlanetario.calcular(s);
        assertEquals(i.humana(), 100 * i.fatoresHumanos().stream().mapToDouble(FatorAmbiental::adequacao).reduce(1, (a,b) -> a*b), 1e-9);
        assertEquals(i.biologica(), 100 * i.fatoresBiologicos().stream().mapToDouble(FatorAmbiental::adequacao).reduce(1, (a,b) -> a*b), 1e-9);
        assertEquals(i.sustentabilidade(), 100 * i.fatoresCivilizacao().stream().mapToDouble(FatorAmbiental::adequacao).reduce(1, (a,b) -> a*b), 1e-9);
        assertTrue(i.fatoresHumanos().stream().anyMatch(f -> f.nome().equals("Radiação") && f.adequacao() < .3));
        assertThrows(UnsupportedOperationException.class, () -> i.fatoresHumanos().clear());
    }
}
