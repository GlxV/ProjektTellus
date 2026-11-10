package com.projekttellus.events;
import com.projekttellus.domain.EstadoPlanetario;
public interface EventoPlanetario {
    String nome();
    double intensidade();
    EstadoPlanetario aplicar(EstadoPlanetario estado);
}
