package com.projekttellus.patterns.observer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Projeção de sessão: registra quantidade de mudanças e extremos de habitabilidade por mundo. */
public final class EstatisticasObserver implements SimulacaoObserver {
    private final ConcurrentHashMap<UUID, Estatistica> mundos = new ConcurrentHashMap<>();
    public String nome() { return "Estatísticas da sessão"; }
    public void aoMudar(MudancaPlanetaria m) {
        mundos.compute(m.mundoId(), (id, anterior) -> anterior == null
                ? new Estatistica(1, m.indicadores().humana(), m.indicadores().humana())
                : new Estatistica(anterior.mudancas() + 1,
                Math.min(anterior.minima(), m.indicadores().humana()),
                Math.max(anterior.maxima(), m.indicadores().humana())));
    }
    public Estatistica para(UUID id) { return mundos.get(id); }
    public record Estatistica(long mudancas, double minima, double maxima) { }
}
