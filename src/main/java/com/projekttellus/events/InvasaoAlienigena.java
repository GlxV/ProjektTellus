package com.projekttellus.events;
import com.projekttellus.domain.*;
/** Evento deliberadamente fictício de extração atmosférica e ocupação. */
public final class InvasaoAlienigena extends EventoBase {
    public InvasaoAlienigena(double intensidade) { super(intensidade); }
    public String nome() { return "Invasão alienígena · ficção"; }
    protected EstadoPlanetario transformar(EstadoPlanetario e, double i) {
        var atingido = Efeitos.catastrofe(e, i, -12, 15, 25, .6, .65, 0, 0);
        var a = e.atmosfera();
        double extraido = a.oxigenio() * .6 * i;
        return new EstadoPlanetario(new Atmosfera(a.oxigenio() - extraido, a.nitrogenio(),
                a.co2(), a.helio() + extraido, a.pressao() * (1 - .3 * i)),
                atingido.clima(), atingido.biosfera(), atingido.hidrosfera(), atingido.civilizacao(), e.anos());
    }
}
