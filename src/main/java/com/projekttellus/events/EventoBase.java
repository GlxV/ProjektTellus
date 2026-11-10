package com.projekttellus.events;
import com.projekttellus.domain.*;
import java.util.Objects;

public abstract class EventoBase implements EventoPlanetario {
    private final double intensidade;
    protected EventoBase(double intensidade) {
        this.intensidade = Validacao.intervalo("Intensidade", intensidade, 0, 1);
    }
    public final double intensidade() { return intensidade; }
    public final EstadoPlanetario aplicar(EstadoPlanetario estado) {
        Objects.requireNonNull(estado);
        return intensidade == 0 ? estado : transformar(estado, intensidade);
    }
    protected abstract EstadoPlanetario transformar(EstadoPlanetario estado, double intensidade);
}
