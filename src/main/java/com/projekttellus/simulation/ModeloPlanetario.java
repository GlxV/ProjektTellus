package com.projekttellus.simulation;

import com.projekttellus.domain.*;
import java.util.List;
import static com.projekttellus.domain.Validacao.limitar;

/** Modelo educacional global, determinístico. Constantes e aproximações em docs/MODELO.md. */
public final class ModeloPlanetario {
    private ModeloPlanetario() { }

    private static double suave(double x) { x = limitar(x, 0, 1); return x * x * (3 - 2 * x); }
    private static double faixa(double x, double min, double bomMin, double bomMax, double max) {
        return suave((x - min) / (bomMin - min)) * suave((max - x) / (max - bomMax));
    }
    private static double produto(List<FatorAmbiental> fatores) {
        return limitar(100 * fatores.stream().mapToDouble(FatorAmbiental::adequacao).reduce(1, (a,b) -> a*b), 0, 100);
    }
    private static FatorAmbiental fator(String nome, double valor, String unidade, double adequacao, String explicacao) {
        return new FatorAmbiental(nome, valor, unidade, limitar(adequacao, 0, 1), explicacao);
    }

    public static Indicadores calcular(EstadoPlanetario e) {
        var a = e.atmosfera(); var c = e.clima(); var h = e.hidrosfera();
        double agua = suave(h.agua() / 15);
        double radiacao = 1 - c.radiacao() / 100;
        var humanos = List.of(
            fator("Oxigênio disponível", a.pressaoParcialOxigenio(), "atm pO₂", faixa(a.pressaoParcialOxigenio(), .06, .16, .30, .60),
                    "Pressão parcial, não a identidade do gás diluente; insuficiência e excesso limitam a respiração."),
            fator("Pressão total", a.pressao(), "atm", faixa(a.pressao(), .15, .7, 1.5, 6),
                    "Pressões muito baixas ou elevadas limitam humanos sem proteção."),
            fator("CO₂ inspirado", a.pressaoParcialCo2(), "atm pCO₂", 1 / (1 + Math.pow(Math.max(0, a.pressaoParcialCo2() - .0004) / .003, 2)),
                    "O excesso de pressão parcial de CO₂ reduz a adequação, mesmo havendo O₂."),
            fator("Temperatura", c.temperatura(), "°C", faixa(c.temperatura(), -40, 5, 25, 65),
                    "Faixa global de conforto estilizada; extremos térmicos tornam a sobrevivência inviável."),
            fator("Água líquida", h.agua(), "% da superfície", agua,
                    "Disponibilidade superficial simplificada; não distingue água doce e salinidade."),
            fator("Radiação", c.radiacao(), "índice / 100", radiacao,
                    "Índice abstrato de dano ambiental, sem conversão em dose real."),
            fator("Partículas", c.aerossol(), "índice / 100", Math.exp(-2 * c.aerossol() / 100),
                    "Aerossóis prejudicam exposição humana e transmissão luminosa."));
        var biologicos = List.of(
            fator("Janela térmica", c.temperatura(), "°C", faixa(c.temperatura(), -60, 0, 45, 130),
                    "Admite organismos hipotéticos com tolerância mais ampla que humanos; extremos continuam restritivos."),
            fator("Água líquida", h.agua(), "% da superfície", suave(h.agua() / 10),
                    "O modelo representa vida associada à água líquida, inclusive oceânica."),
            fator("Meio atmosférico", a.pressao(), "atm", suave(a.pressao() / .15) / (1 + Math.max(0, a.pressao() - 5) / 10),
                    "Não exige O₂; pressão quase nula ou extremamente elevada limita o potencial."),
            fator("Radiação", c.radiacao(), "índice / 100", Math.sqrt(radiacao),
                    "Organismos hipotéticos toleram mais radiação, mas não imunidade total."),
            fator("Luz disponível", c.aerossol(), "índice de partículas", Math.exp(-1.2 * c.aerossol() / 100),
                    "Atenuação simplificada da fonte de energia; não simula metabolismo de espécies."),
            fator("Estabilidade climática", Math.abs(temperaturaAlvo(e) - c.temperatura()), "°C até equilíbrio", 1 / (1 + Math.abs(temperaturaAlvo(e) - c.temperatura()) / 45),
                    "Distância ao alvo climático do próprio motor; desequilíbrios limitam a adaptação."));
        double humana = produto(humanos), biologica = produto(biologicos);
        double terra = e.superficieContinental() / 100;
        double produtividade = Math.sqrt(.25 + .75 * e.biosfera().vegetacao() / 100);
        long capacidade = Math.round(limitar(16_000_000_000.0 * terra / .26 * produtividade
                * agua * humana / 100 * Math.sqrt(biologica / 100), 0, 100_000_000_000L));
        double ocupacao = e.civilizacao().populacao() == 0 ? 1 : Math.min(1, (double) capacidade / e.civilizacao().populacao());
        var civilizacao = List.of(
            fator("Ambiente humano", humana, "%", humana / 100, "Sobrevivência ambiental é necessária à civilização convencional."),
            fator("Continente disponível", e.superficieContinental(), "% da superfície", suave(terra / .10),
                    "Oceanos e gelo não oferecem área convencional de ocupação; menos de 10% de terra reduz a diversidade de recursos."),
            fator("Recursos hídricos", h.agua(), "% da superfície", agua, "O acesso à água limita a produtividade; sem hidrologia regional."),
            fator("Produtividade terrestre", e.biosfera().vegetacao(), "% do continente", produtividade,
                    "Produtividade de base 25% mais contribuição vegetal; sem terra, a capacidade continua zero."),
            fator("Potencial ambiental", biologica, "%", Math.sqrt(biologica / 100), "Ambientes adequados à vida favorecem suporte ecológico."),
            fator("Ocupação da capacidade", e.civilizacao().populacao(), "pessoas", ocupacao,
                    "Adequação = mínimo entre 1 e capacidade/população. Capacidade é uma estimativa educacional."));
        return new Indicadores(humana, biologica, produto(civilizacao), capacidade, humanos, biologicos, civilizacao);
    }

    /** Alvo reduzido de balanço térmico: CO₂, pressão, partículas e albedo glacial. */
    public static double temperaturaAlvo(EstadoPlanetario e) {
        return limitar(15 + 4 * Math.log((e.atmosfera().pressaoParcialCo2() + .00001) / .00041)
                + 3 * Math.log(Math.max(.05, e.atmosfera().pressao()))
                - .35 * e.clima().aerossol() - .18 * (e.hidrosfera().gelo() - 3), -100, 120);
    }

    public static EstadoPlanetario avancar(EstadoPlanetario inicial, double anos) {
        Validacao.intervalo("Avanço de tempo", anos, .01, 1000);
        Validacao.intervalo("Tempo total", inicial.anos() + anos, 0, 1_000_000_000);
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
        double temp = e.clima().temperatura() + (temperaturaAlvo(e) - e.clima().temperatura()) * -Math.expm1(-dt / 20);
        double totalAgua = e.hidrosfera().agua() + e.hidrosfera().gelo();
        // Reserva hídrica superficial fixa. Não modela vapor, relevo, expansão volumétrica ou evaporação.
        double geloAlvo = totalAgua * suave((10 - temp) / 55);
        double gelo = limitar(e.hidrosfera().gelo() + (geloAlvo - e.hidrosfera().gelo()) * -Math.expm1(-dt / 15), 0, totalAgua);
        var clima = new Clima(temp, aerossol, radiacao);
        var agua = new Hidrosfera(totalAgua - gelo, gelo);
        var parcial = new EstadoPlanetario(e.atmosfera(), clima, e.biosfera(), agua, e.civilizacao(), e.anos());
        var indic = calcular(parcial);
        double vegetacaoAlvo = indic.biologica() * suave(agua.agua() / 20);
        // Sem terra, não surge área vegetal. O índice terrestre latente também decai gradualmente.
        if (parcial.superficieContinental() < 1e-9) vegetacaoAlvo = 0;
        double vegetacao = limitar(e.biosfera().vegetacao() + (vegetacaoAlvo - e.biosfera().vegetacao()) * -Math.expm1(-dt / 40), 0, 100);
        var ecologico = new EstadoPlanetario(e.atmosfera(), clima, new Biosfera(vegetacao), agua, e.civilizacao(), e.anos());
        var suporte = calcular(ecologico);
        long p = e.civilizacao().populacao();
        double k = suporte.capacidadeSuporte();
        double humana = suporte.humana() / 100;
        // H já reduz K: não aplicar mortalidade ambiental novamente abaixo de K.
        // Assim, em ambiente estável K é também o equilíbrio demográfico apresentado ao usuário.
        double pressaoRecursos = k == 0 ? (p == 0 ? 0 : 1) : Math.max(0, (p - k) / (p + k));
        double taxa = .012 * humana * (k == 0 ? 0 : Math.max(0, 1 - p / k))
                - .12 * pressaoRecursos - (k == 0 ? .10 * (1 - humana) : 0);
        long populacao = Math.round(limitar(p * Math.exp(taxa * dt), 0, 100_000_000_000L));
        // Não cruzar a capacidade por arredondamento numérico em crescimento.
        if (p <= k) populacao = Math.min(populacao, Math.max(p, suporte.capacidadeSuporte()));
        return new EstadoPlanetario(e.atmosfera(), clima, ecologico.biosfera(), agua, new Civilizacao(populacao), e.anos());
    }
}
