package com.projekttellus.simulation;

import com.projekttellus.domain.*;
import static com.projekttellus.domain.Validacao.limitar;

/** Modelo didático global e determinístico; não faz previsões científicas. */
public final class ModeloPlanetario {
    private ModeloPlanetario() { }
    private static double conforto(double valor, double ideal, double tolerancia) {
        return limitar(1 - Math.abs(valor - ideal) / tolerancia, 0, 1);
    }
    public static Indicadores calcular(EstadoPlanetario e) {
        var a = e.atmosfera(); var c = e.clima();
        double oxigenio = conforto(a.pressaoParcialOxigenio(), .21, .16);
        double pressao = conforto(a.pressao(), 1, 1);
        double temperatura = conforto(c.temperatura(), 15, 55);
        double agua = limitar(e.hidrosfera().agua() / 30, 0, 1);
        double toxidade = 1 / (1 + 250 * Math.max(0, a.pressaoParcialCo2() - .0004));
        double radiacao = 1 - c.radiacao() / 100;
        double humana = 100 * oxigenio * pressao * temperatura * agua * toxidade * radiacao
                * (1 - .6 * c.aerossol() / 100);
        // O potencial biológico admite organismos sem oxigênio e temperaturas mais amplas.
        double biologica = 100 * conforto(c.temperatura(), 20, 100) * agua
                * limitar(a.pressao() / .2, 0, 1) * Math.sqrt(radiacao)
                * (.7 + .3 * e.biosfera().vegetacao() / 100);
        return new Indicadores(limitar(humana, 0, 100), limitar(biologica, 0, 100));
    }

    public static EstadoPlanetario avancar(EstadoPlanetario inicial, double anos) {
        Validacao.intervalo("Avanço de tempo", anos, .01, 1000);
        Validacao.intervalo("Tempo total", inicial.anos() + anos, 0, 1_000_000_000);
        // Passos de no máximo um ano evitam saltos instáveis. Último passo fracionário é preservado.
        var estado = inicial;
        int passos = (int) Math.ceil(anos);
        double dt = anos / passos;
        for (int i = 0; i < passos; i++) estado = evoluir(estado, dt);
        return new EstadoPlanetario(estado.atmosfera(), estado.clima(), estado.biosfera(),
                estado.hidrosfera(), estado.civilizacao(), inicial.anos() + anos);
    }
    private static EstadoPlanetario evoluir(EstadoPlanetario e, double dt) {
        double aerossol = e.clima().aerossol() * Math.exp(-dt / 8);
        double radiacao = e.clima().radiacao() * Math.exp(-dt / 30);
        double alvo = 15 + 5 * Math.log(Math.max(.00001, e.atmosfera().pressaoParcialCo2()) / .0004)
                - .4 * aerossol - .12 * e.hidrosfera().gelo();
        double temp = limitar(e.clima().temperatura()
                + (alvo - e.clima().temperatura()) * (1 - Math.exp(-dt / 20)), -150, 200);
        double totalAgua = e.hidrosfera().agua() + e.hidrosfera().gelo();
        double geloAlvo = totalAgua * limitar((5 - temp) / 50, 0, 1);
        double gelo = limitar(e.hidrosfera().gelo() + (geloAlvo - e.hidrosfera().gelo())
                * (1 - Math.exp(-dt / 15)), 0, totalAgua);
        var clima = new Clima(temp, aerossol, radiacao);
        var agua = new Hidrosfera(totalAgua - gelo, gelo);
        var parcial = new EstadoPlanetario(e.atmosfera(), clima, e.biosfera(), agua, e.civilizacao(), e.anos());
        var indic = calcular(parcial);
        double vegetacao = limitar(e.biosfera().vegetacao()
                + (indic.biologica() - e.biosfera().vegetacao()) * (1 - Math.exp(-dt / 40)), 0, 100);
        double taxa = .012 * (indic.humana() / 100) - .10 * (1 - indic.humana() / 100);
        long populacao = Math.round(limitar(e.civilizacao().populacao() * Math.exp(taxa * dt), 0, 100_000_000_000L));
        return new EstadoPlanetario(e.atmosfera(), clima, new Biosfera(vegetacao), agua,
                // O relógio é atualizado uma única vez, pelo endpoint validado em avancar.
                new Civilizacao(populacao), e.anos());
    }
}
