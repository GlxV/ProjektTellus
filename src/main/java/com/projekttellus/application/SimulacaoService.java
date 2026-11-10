package com.projekttellus.application;

import com.projekttellus.domain.*;
import com.projekttellus.events.EventoCreator;
import com.projekttellus.patterns.memento.TerraMemento;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.persistence.*;
import com.projekttellus.simulation.ModeloPlanetario;
import java.time.Instant;
import java.util.*;

/** Casos de uso e caretaker do Memento. SQLite confirma tudo antes da mudança pública.
 * Sem referência a JavaFX nem ao modo Apresentação: os passos descrevem fatos concluídos. */
public final class SimulacaoService {
    private final MundoRepository repository;
    private final ObserverBus observerBus = new ObserverBus();
    private final AlertaObserver monitor = new AlertaObserver();
    private final EstatisticasObserver estatisticas = new EstatisticasObserver();
    private UUID ultimoMundo;

    public SimulacaoService(MundoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        observerBus.inscrever(monitor);
        observerBus.inscrever(estatisticas);
    }
    public MundoRepository repository() { return repository; }
    public ObserverBus observerBus() { return observerBus; }
    public EstatisticasObserver estatisticas() { return estatisticas; }
    public List<String> alertas() { return ultimoMundo == null ? List.of() : monitor.para(ultimoMundo); }
    public List<String> alertas(UUID mundoId) { return monitor.para(mundoId); }

    public ResultadoOperacao criar(TerraBuilder builder) {
        var passos = new ArrayList<Passo>();
        var terra = Objects.requireNonNull(builder).build();
        passos.add(new Passo("Condições iniciais reunidas", "Atmosfera, clima, água, vida e população formam um único cenário.", "Builder"));
        passos.add(new Passo("Cenário validado", "As proporções e os limites foram conferidos antes da construção.", "Builder"));
        passos.add(new Passo("Uma nova Terra foi construída", "O cenário ganhou identidade própria e não compartilha mudanças com outros mundos.", "Builder"));
        var snapshot = terra.capturar("Origem do mundo");
        passos.add(preservado());
        return confirmar(terra, terra, terra.estado(), "Mundo criado", 0, snapshot, passos, true);
    }
    public ResultadoOperacao alterar(Terra terra, EstadoPlanetario estado) {
        Objects.requireNonNull(estado);
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes da alteração ambiental"); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), estado);
        passos.add(new Passo("Condições planetárias alteradas", "Os componentes receberam os valores escolhidos e mantiveram suas regras de consistência.", ""));
        return confirmar(terra, candidato, terra.estado(), "Condições ambientais alteradas", 0, snapshot, passos, true);
    }
    public ResultadoOperacao aplicar(Terra terra, EventoCreator creator, double intensidade) {
        var passos = new ArrayList<Passo>();
        var evento = Objects.requireNonNull(creator).criar(intensidade);
        passos.add(new Passo("Evento criado", "O criador de “" + evento.nome() + "” preparou um evento com a intensidade escolhida.", "Factory Method"));
        var snapshot = terra.capturar("Antes: " + evento.nome()); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));
        passos.add(new Passo("Efeitos aplicados ao planeta", "O evento modificou os componentes ambientais; os indicadores serão derivados dessas condições.", "Factory Method"));
        return confirmar(terra, candidato, terra.estado(), evento.nome(), intensidade, snapshot, passos, true);
    }
    public ResultadoOperacao avancar(Terra terra, double anos) {
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes do avanço do tempo"); passos.add(preservado());
        var candidato = new Terra(terra.id(), terra.nome(), ModeloPlanetario.avancar(terra.estado(), anos));
        passos.add(new Passo("O tempo avançou", "Clima, gelo, vegetação e população evoluíram em intervalos estáveis e determinísticos.", ""));
        return confirmar(terra, candidato, terra.estado(), "Tempo avançado em " + anos + " anos", 0, snapshot, passos, true);
    }
    public ResultadoOperacao restaurar(Terra terra, TerraMemento snapshot) {
        var candidato = new Terra(terra.id(), terra.nome(), terra.estado());
        candidato.restaurar(snapshot); // Verifica a identidade ANTES de qualquer gravação.
        var passos = new ArrayList<Passo>();
        var retorno = terra.capturar("Antes da restauração"); passos.add(preservado());
        passos.add(new Passo("Uma Terra anterior foi restaurada", "Todos os componentes, o tempo e o nome voltaram ao retrato selecionado. Outros mundos permanecem independentes.", "Memento"));
        return confirmar(terra, candidato, terra.estado(), "Restaurado: " + snapshot.rotulo(), 0, retorno, passos, true);
    }
    public ResultadoOperacao capturar(Terra terra, String rotulo) {
        var snapshot = terra.capturar(rotulo);
        var passos = new ArrayList<Passo>(); passos.add(preservado());
        return confirmar(terra, terra, terra.estado(), "Retrato preservado: " + snapshot.rotulo(), 0, snapshot, passos, false);
    }
    public ResultadoOperacao salvar(Terra terra) {
        return confirmar(terra, terra, terra.estado(), "Mundo salvo", 0, null, new ArrayList<>(), false);
    }
    public ResultadoOperacao renomear(Terra terra, String nome) {
        var candidato = new Terra(terra.id(), nome, terra.estado());
        var passos = new ArrayList<Passo>();
        var snapshot = terra.capturar("Antes da mudança de nome"); passos.add(preservado());
        passos.add(new Passo("Mundo renomeado", "O mundo manteve sua identidade e recebeu o novo nome.", ""));
        return confirmar(terra, candidato, terra.estado(), "Mundo renomeado", 0, snapshot, passos, true);
    }
    public ResultadoOperacao duplicar(Terra terra, String nome) {
        var copia = new Terra(UUID.randomUUID(), nome, terra.estado());
        var passos = new ArrayList<Passo>();
        passos.add(new Passo("Um mundo independente foi criado", "A cópia começa nas mesmas condições, com identidade e retratos próprios.", "Memento"));
        var snapshot = copia.capturar("Origem da cópia"); passos.add(preservado());
        return confirmar(copia, copia, terra.estado(), "Mundo duplicado", 0, snapshot, passos, true);
    }
    private Passo preservado() {
        return new Passo("Estado anterior preservado", "Um retrato imutável completo foi capturado, sem alterar a Terra.", "Memento");
    }
    private ResultadoOperacao confirmar(Terra destino, Terra candidato, EstadoPlanetario antes,
                                         String descricao, double intensidade, TerraMemento snapshot,
                                         ArrayList<Passo> passos, boolean notificar) {
        var indicadores = ModeloPlanetario.calcular(candidato.estado());
        passos.add(new Passo("Condições planetárias recalculadas", "Habitabilidade humana e potencial biológico foram avaliados separadamente a partir do ambiente.", ""));
        var registro = new RegistroEvento(UUID.randomUUID(), candidato.id(), Instant.now(), descricao, intensidade, candidato.estado().anos());
        repository.salvar(candidato, registro, snapshot);
        destino.renomear(candidato.nome()); destino.atualizar(candidato.estado());
        passos.add(new Passo("Mundo e histórico preservados", "O estado e o histórico foram gravados juntos. Quando há um retrato, ele integra a mesma confirmação.", ""));
        if (notificar) {
            var entrega = observerBus.publicar(new MudancaPlanetaria(destino.id(), destino.estado(), indicadores, descricao));
            ultimoMundo = destino.id();
            for (String nome : entrega.notificados())
                passos.add(new Passo("Sistema notificado: " + nome, "Este assinante recebeu a mudança e executou sua responsabilidade, sem o planeta conhecer sua implementação.", "Observer"));
            if (!entrega.falhas().isEmpty()) passos.add(new Passo("Uma atualização auxiliar não foi concluída",
                    "O mundo foi salvo; " + entrega.falhas().size() + " assinante(s) não concluíram sua atualização.", "Observer"));
        }
        return new ResultadoOperacao(destino, antes, destino.estado(), passos, indicadores, descricao);
    }
}
