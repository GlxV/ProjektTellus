package com.projekttellus.patterns.observer;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Publicador desacoplado: uma falha não impede os outros assinantes. */
public final class ObserverBus {
    private final CopyOnWriteArrayList<SimulacaoObserver> observers = new CopyOnWriteArrayList<>();
    public AutoCloseable inscrever(SimulacaoObserver observer) {
        Objects.requireNonNull(observer);
        observers.addIfAbsent(observer);
        return () -> remover(observer);
    }
    public void remover(SimulacaoObserver observer) { observers.remove(observer); }
    public Entrega publicar(MudancaPlanetaria mudanca) {
        var notificados = new ArrayList<String>();
        var falhas = new ArrayList<String>();
        for (var observer : observers) {
            String nome = nomeSeguro(observer);
            try {
                observer.aoMudar(mudanca);
                notificados.add(nome);
            } catch (RuntimeException ex) {
                falhas.add(nome);
                System.getLogger(ObserverBus.class.getName()).log(System.Logger.Level.WARNING,
                        "Observador não recebeu a mudança: " + nome, ex);
            }
        }
        return new Entrega(notificados, falhas);
    }
    private static String nomeSeguro(SimulacaoObserver observer) {
        try {
            String nome = observer.nome();
            if (nome != null && !nome.isBlank()) return nome;
        } catch (RuntimeException ignorado) {
            // Metadados defeituosos não podem abortar uma mudança já confirmada.
        }
        return "Assinante sem identificação";
    }
    public record Entrega(List<String> notificados, List<String> falhas) {
        public Entrega { notificados = List.copyOf(notificados); falhas = List.copyOf(falhas); }
    }
}
