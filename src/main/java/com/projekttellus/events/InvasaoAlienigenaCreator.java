package com.projekttellus.events;

public final class InvasaoAlienigenaCreator extends EventoCreator {
    public String nome() { return new InvasaoAlienigena(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new InvasaoAlienigena(intensidade); }
}
