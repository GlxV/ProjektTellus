package com.projekttellus.events;
import com.projekttellus.domain.*;
import static com.projekttellus.domain.Validacao.limitar;

/** Operações compartilhadas sobre componentes; não calcula barras de habitabilidade. */
final class Efeitos {
    private Efeitos() { }
    static EstadoPlanetario catastrofe(EstadoPlanetario e, double i, double resfriamento,
                                       double poeira, double radiacao, double perdaVegetacao,
                                       double perdaPopulacao, double carbono, double gelo) {
        var c = e.clima();
        double congelado = Math.min(e.hidrosfera().agua(), gelo * i);
        return new EstadoPlanetario(e.atmosfera().transferirCarbono(carbono * i),
                new Clima(limitar(c.temperatura() - resfriamento * i, -150, 200),
                        limitar(c.aerossol() + poeira * i, 0, 100),
                        limitar(c.radiacao() + radiacao * i, 0, 100)),
                new Biosfera(e.biosfera().vegetacao() * (1 - perdaVegetacao * i)),
                new Hidrosfera(e.hidrosfera().agua() - congelado, e.hidrosfera().gelo() + congelado),
                new Civilizacao(Math.round(e.civilizacao().populacao() * (1 - perdaPopulacao * i))), e.anos());
    }
}
