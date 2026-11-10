package com.projekttellus;

import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.simulation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {
    static Stream<EventoCreator> creators() {
        return Stream.of(new AsteroideCreator(), new SupervulcaoCreator(), new GuerraNuclearCreator(),
                new InvasaoAlienigenaCreator(), new RestauracaoEcologicaCreator());
    }
    @ParameterizedTest @MethodSource("creators")
    void realFactoriesCreateIndependentPolymorphicEvents(EventoCreator creator) {
        var evento = creator.criar(.7);
        assertNotSame(evento, creator.criar(.7));
        assertEquals(.7, evento.intensidade());
        var inicial = new TerraBuilder().clima(new Clima(25, 20, 30)).biosfera(new Biosfera(20)).build().estado();
        var depois = evento.aplicar(inicial);
        assertNotEquals(inicial, depois);
        assertEquals(inicial.anos(), depois.anos());
        assertEquals(inicial, creator.criar(0).aplicar(inicial));
        assertThrows(IllegalArgumentException.class, () -> creator.criar(-.1));
        assertThrows(IllegalArgumentException.class, () -> creator.criar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> creator.criar(1.1));
    }
    @Test void catastrophesAlterEnvironmentAndRestorationRepairsIt() {
        var inicial = new TerraBuilder().build().estado();
        var impacto = new AsteroideCreator().criar(1).aplicar(inicial);
        assertTrue(impacto.clima().aerossol() > inicial.clima().aerossol());
        assertTrue(impacto.civilizacao().populacao() < inicial.civilizacao().populacao());
        assertTrue(ModeloPlanetario.calcular(impacto).humana() < ModeloPlanetario.calcular(inicial).humana());
        var nuclear = new GuerraNuclearCreator().criar(1).aplicar(inicial);
        assertTrue(nuclear.clima().radiacao() > 0);
        var reparado = new RestauracaoEcologicaCreator().criar(1).aplicar(nuclear);
        assertTrue(reparado.clima().radiacao() < nuclear.clima().radiacao());
        assertTrue(reparado.biosfera().vegetacao() > nuclear.biosfera().vegetacao());
        assertTrue(ModeloPlanetario.calcular(reparado).humana() > ModeloPlanetario.calcular(nuclear).humana());
    }
    @Test void deterministicTimeRemovesAerosolsAndConservesWater() {
        var inicial = new TerraBuilder().clima(new Clima(-20, 80, 50)).build().estado();
        var a = ModeloPlanetario.avancar(inicial, 100);
        assertEquals(a, ModeloPlanetario.avancar(inicial, 100));
        assertEquals(100, a.anos());
        assertEquals(74, a.hidrosfera().agua() + a.hidrosfera().gelo(), 1e-9);
        assertTrue(a.clima().aerossol() < inicial.clima().aerossol());
        assertTrue(a.clima().radiacao() < inicial.clima().radiacao());
        assertThrows(IllegalArgumentException.class, () -> ModeloPlanetario.avancar(inicial, 0));
        assertThrows(IllegalArgumentException.class, () -> ModeloPlanetario.avancar(inicial, Double.POSITIVE_INFINITY));
    }
    @Test void observerUnsubscriptionAndFailuresAreIsolated() throws Exception {
        var bus = new ObserverBus();
        var chamadas = new AtomicInteger();
        var bom = new SimulacaoObserver() {
            public String nome() { return "Teste"; }
            public void aoMudar(MudancaPlanetaria mudanca) { chamadas.incrementAndGet(); }
        };
        var erro = new SimulacaoObserver() {
            public String nome() { return "Falha isolada"; }
            public void aoMudar(MudancaPlanetaria mudanca) { throw new IllegalStateException("teste"); }
        };
        bus.inscrever(erro);
        var assinatura = bus.inscrever(bom);
        Terra terra = new TerraBuilder().build();
        var mudanca = new MudancaPlanetaria(terra.id(), terra.estado(), ModeloPlanetario.calcular(terra.estado()), "Teste");
        assertEquals(List.of("Teste"), bus.publicar(mudanca).notificados());
        assertEquals(List.of("Falha isolada"), bus.publicar(mudanca).falhas());
        assertEquals(2, chamadas.get());
        assinatura.close();
        bus.publicar(mudanca);
        assertEquals(2, chamadas.get());
    }
    @Test void extremeValidScenariosStayFiniteAfterAllEventsAndTime() {
        var extremo = new TerraBuilder().atmosfera(new Atmosfera(0, 0, 100, 0, 20))
                .clima(new Clima(200, 100, 100)).hidrosfera(new Hidrosfera(0, 100))
                .civilizacao(new Civilizacao(100_000_000_000L)).build().estado();
        for (EventoCreator creator : creators().toList()) {
            var resultado = ModeloPlanetario.avancar(creator.criar(1).aplicar(extremo), 1000);
            assertTrue(Double.isFinite(ModeloPlanetario.calcular(resultado).humana()));
            assertTrue(Double.isFinite(ModeloPlanetario.calcular(resultado).biologica()));
        }
    }
    @Test void toleratedFloatingPointExcessIsNormalizedBeforeTransformations() {
        for (var agua : List.of(new Hidrosfera(100, .0000005), new Hidrosfera(.0000005, 100))) {
            var estado = new TerraBuilder().hidrosfera(agua).build().estado();
            assertDoesNotThrow(() -> ModeloPlanetario.avancar(estado, 1));
            for (var creator : creators().toList()) assertDoesNotThrow(() -> creator.criar(1).aplicar(estado));
            assertTrue(agua.agua() + agua.gelo() <= 100);
        }
        var atmosfera = new Atmosfera(.0000005, 0, 0, 100, 1);
        var estado = new TerraBuilder().atmosfera(atmosfera).build().estado();
        assertDoesNotThrow(() -> new InvasaoAlienigenaCreator().criar(1).aplicar(estado));
    }
    @Test void fractionalAdvanceAtMaximumTimeDoesNotAccumulateClockError() {
        var estado = new TerraBuilder().anos(1_000_000_000 - 2.1).build().estado();
        assertEquals(1_000_000_000, ModeloPlanetario.avancar(estado, 2.1).anos());
    }
}
