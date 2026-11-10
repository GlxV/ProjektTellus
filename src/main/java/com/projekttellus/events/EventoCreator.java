package com.projekttellus.events;

/** Creator do Factory Method. Cada subclasse decide qual produto concreto criar. */
public abstract class EventoCreator {
    public abstract String nome();
    public abstract EventoPlanetario criar(double intensidade);
    @Override public final String toString() { return nome(); }
}
