package com.projekttellus.events;

public final class AsteroideCreator extends EventoCreator {
    public String nome() { return new Asteroide(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new Asteroide(intensidade); }
}
