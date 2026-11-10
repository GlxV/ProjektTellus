package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public final class GuerraNuclear extends EventoBase {
    public GuerraNuclear(double intensidade) { super(intensidade); }
    public String nome() { return "Guerra nuclear"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        return Efeitos.catastrofe(e, i, 22, 70, 90, .8, .9, .08, 10);
    }
}
