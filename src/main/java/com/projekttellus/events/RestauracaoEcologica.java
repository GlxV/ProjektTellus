package com.projekttellus.events;
import com.projekttellus.domain.*;
public final class RestauracaoEcologica extends EventoBase {
    public RestauracaoEcologica(double intensidade) { super(intensidade); }
    public String nome() { return "Restauração ecológica"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        var c = e.clima();
        double degelo = e.hidrosfera().gelo() * .25 * i;
        return new EstadoPlanetario(e.atmosfera().transferirCarbono(-Math.max(0, e.atmosfera().co2() - .04) * .6 * i),
                new Clima(c.temperatura() + (15 - c.temperatura()) * .45 * i,
                        c.aerossol() * (1 - .7 * i), c.radiacao() * (1 - .65 * i)),
                new Biosfera(e.biosfera().vegetacao() + (100 - e.biosfera().vegetacao()) * .45 * i),
                new Hidrosfera(e.hidrosfera().agua() + degelo, e.hidrosfera().gelo() - degelo),
                e.civilizacao(), e.anos());
    }
}
