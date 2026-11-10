package com.projekttellus.domain;

import com.projekttellus.patterns.memento.TerraMemento;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Originador do Memento: identidade estável e posse exclusiva do estado atual. */
public final class Terra {
    private final UUID id;
    private String nome;
    private EstadoPlanetario estado;
    public Terra(UUID id, String nome, EstadoPlanetario estado) {
        this.id = Objects.requireNonNull(id);
        this.nome = Validacao.texto("Nome do mundo", nome);
        this.estado = Objects.requireNonNull(estado);
    }
    public UUID id() { return id; }
    public String nome() { return nome; }
    public EstadoPlanetario estado() { return estado; }
    public void renomear(String nome) { this.nome = Validacao.texto("Nome do mundo", nome); }
    public void atualizar(EstadoPlanetario estado) { this.estado = Objects.requireNonNull(estado); }
    public TerraMemento capturar(String rotulo) {
        return new TerraMemento(UUID.randomUUID(), id, rotulo, nome, Instant.now(), estado);
    }
    public void restaurar(TerraMemento memento) {
        Objects.requireNonNull(memento);
        if (!id.equals(memento.mundoId()))
            throw new IllegalArgumentException("Este retrato pertence a outro mundo.");
        nome = memento.nomeMundo();
        estado = memento.estado();
    }
}
