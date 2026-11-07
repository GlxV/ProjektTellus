package com.projekttellus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Smoke test de recursos da estrutura inicial; nao valida o simulador. */
class ProjectStructureTest {
    @Test
    void telaInicialEstaNoClasspath() {
        assertNotNull(getClass().getResource("/fxml/main.fxml"));
    }

    @Test
    void estiloInicialEstaNoClasspath() {
        assertNotNull(getClass().getResource("/styles/app.css"));
    }
}
