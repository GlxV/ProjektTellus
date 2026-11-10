package com.projekttellus.ui;

import com.projekttellus.events.*;

/** Presentation-only routing, independent of translated labels and domain transformations. */
public enum AnimationKind {
    ASTEROID, VOLCANO, NUCLEAR, ALIEN, ECOLOGY, MEMENTO, TRANSITION;
    public boolean isEvent() { return ordinal() <= ECOLOGY.ordinal(); }
    public static AnimationKind forCreator(EventoCreator creator) {
        return switch (creator) {
            case AsteroideCreator ignored -> ASTEROID;
            case SupervulcaoCreator ignored -> VOLCANO;
            case GuerraNuclearCreator ignored -> NUCLEAR;
            case InvasaoAlienigenaCreator ignored -> ALIEN;
            case RestauracaoEcologicaCreator ignored -> ECOLOGY;
            default -> TRANSITION;
        };
    }
}
