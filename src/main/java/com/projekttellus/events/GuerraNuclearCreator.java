package com.projekttellus.events;

public final class GuerraNuclearCreator extends EventoCreator {
    public String nome() { return new GuerraNuclear(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new GuerraNuclear(intensidade); }
}
