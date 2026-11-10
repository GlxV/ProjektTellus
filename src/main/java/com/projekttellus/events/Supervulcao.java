package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public final class Supervulcao extends EventoBase {
    public Supervulcao(double intensidade) { super(intensidade); }
    public String nome() { return "Supervulcão"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        return Efeitos.catastrofe(e, i, 18, 95, 0, .55, .3, .4, 8);
    }
}
