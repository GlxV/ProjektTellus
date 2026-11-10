package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public final class Asteroide extends EventoBase {
    public Asteroide(double intensidade) { super(intensidade); }
    public String nome() { return "Impacto de asteroide"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        return Efeitos.catastrofe(e, i, 28, 85, 0, .85, .75, .12, 12);
    }
}
