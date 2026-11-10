package com.projekttellus.ui;

import com.projekttellus.events.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationPlanTest {
    @Test void selectsEachConcreteCreatorWithoutApplyingAnEvent() {
        assertEquals(AnimationKind.ASTEROID, AnimationKind.forCreator(new AsteroideCreator()));
        assertEquals(AnimationKind.VOLCANO, AnimationKind.forCreator(new SupervulcaoCreator()));
        assertEquals(AnimationKind.NUCLEAR, AnimationKind.forCreator(new GuerraNuclearCreator()));
        assertEquals(AnimationKind.ALIEN, AnimationKind.forCreator(new InvasaoAlienigenaCreator()));
        assertEquals(AnimationKind.ECOLOGY, AnimationKind.forCreator(new RestauracaoEcologicaCreator()));
    }
    @Test void zeroIntensityHasNoParticlesShipsDetonationsOrDuration() {
        for (var kind : AnimationKind.values()) {
            if (!kind.isEvent()) continue;
            var plan = AnimationPlan.of(kind, 0, AnimationMode.FULL);
            assertEquals(0, plan.particles()); assertEquals(0, plan.sources());
            assertEquals(0, plan.seconds()); assertEquals(0, plan.strength());
        }
    }
    @Test void scalesAndBoundsVisualLoad() {
        for (var kind : AnimationKind.values()) {
            if (!kind.isEvent()) continue;
            var small = AnimationPlan.of(kind, .1, AnimationMode.FULL);
            var large = AnimationPlan.of(kind, 1, AnimationMode.FULL);
            assertTrue(large.particles() > small.particles());
            assertTrue(large.sources() >= small.sources());
            assertTrue(large.particles() <= 180); assertTrue(large.sources() <= 12);
            assertTrue(large.seconds() >= 2 && large.seconds() <= 4);
        }
        assertThrows(IllegalArgumentException.class, () -> AnimationPlan.of(AnimationKind.ASTEROID, Double.NaN, AnimationMode.FULL));
        assertThrows(IllegalArgumentException.class, () -> AnimationPlan.of(AnimationKind.ASTEROID, 1.1, AnimationMode.FULL));
    }
    @Test void accessibilityModesAndMementoAreExplicit() {
        var reduced = AnimationPlan.of(AnimationKind.NUCLEAR, 1, AnimationMode.REDUCED);
        assertEquals(0, reduced.particles()); assertEquals(0, reduced.sources());
        assertTrue(reduced.seconds() <= 1); assertEquals(0, reduced.strength());
        assertEquals(0, AnimationPlan.of(AnimationKind.ASTEROID, 1, AnimationMode.OFF).seconds());
        assertTrue(AnimationPlan.of(AnimationKind.MEMENTO, 0, AnimationMode.FULL).seconds() > 0);
    }
}
