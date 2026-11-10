package com.projekttellus.events;

public final class RestauracaoEcologicaCreator extends EventoCreator {
    public String nome() { return new RestauracaoEcologica(0).nome(); }
    @Override public EventoPlanetario criar(double intensidade) { return new RestauracaoEcologica(intensidade); }
}
