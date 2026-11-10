package com.projekttellus.patterns.observer;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Subsistema de alertas: produz recomendações distintas da renderização da interface. */
public final class AlertaObserver implements SimulacaoObserver {
    private final ConcurrentHashMap<UUID, List<String>> alertas = new ConcurrentHashMap<>();
    public String nome() { return "Monitor de condições de vida"; }
    public void aoMudar(MudancaPlanetaria m) {
        var lista = new ArrayList<String>();
        if (m.indicadores().humana() < 30) lista.add("Condições críticas para humanos.");
        if (m.estado().atmosfera().pressaoParcialOxigenio() < .12) lista.add("Pouco oxigênio disponível para respirar.");
        if (m.estado().clima().radiacao() > 20) lista.add("Radiação elevada; recuperação será lenta.");
        if (m.estado().hidrosfera().agua() < 10) lista.add("Pouca água líquida na superfície.");
        if (m.indicadores().biologica() < 20) lista.add("Potencial biológico muito reduzido.");
        if (lista.isEmpty()) lista.add("Condições estáveis neste modelo didático.");
        alertas.put(m.mundoId(), List.copyOf(lista));
    }
    public List<String> para(UUID id) { return alertas.getOrDefault(id, List.of()); }
}
