package com.projekttellus.events;

public final class SupervulcaoCreator extends EventoCreator {
    public String nome() { return new Supervulcao(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new Supervulcao(intensidade); }
}
