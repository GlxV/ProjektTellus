package com.projekttellus;

import com.projekttellus.application.*;
import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.patterns.observer.*;
import com.projekttellus.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    @TempDir Path dir;

    @Test void fullDemonstrationSurvivesRestartAndKeepsWorldsIndependent() {
        Path db = dir.resolve("universo.db");
        java.util.UUID id, outroId;
        EstadoPlanetario original;
        try (var repo = new SqliteMundoRepository(db)) {
            var service = new SimulacaoService(repo);
            var criado = service.criar(new TerraBuilder().nome("Aurora"));
            Terra terra = criado.terra(); id = terra.id(); original = terra.estado();
            assertTrue(criado.passos().stream().anyMatch(p -> p.padrao().equals("Builder")));
            var outro = service.duplicar(terra, "Reserva").terra(); outroId = outro.id();
            var retrato = repo.snapshots(id).getFirst();
            var impacto = service.aplicar(terra, new AsteroideCreator(), .9);
            assertTrue(impacto.indicadores().humana() < criado.indicadores().humana());
            assertTrue(impacto.passos().getFirst().padrao().equals("Factory Method"));
            assertEquals(2, impacto.passos().stream().filter(p -> p.padrao().equals("Observer")).count());
            assertEquals(original, repo.carregar(outroId).orElseThrow().estado());
            assertEquals(original, retrato.estado());
            assertEquals(2, service.estatisticas().para(id).mudancas());
            assertFalse(service.alertas(id).isEmpty());
            service.renomear(terra, "Aurora alterada");
            service.restaurar(terra, retrato);
            assertEquals(original, terra.estado());
            assertEquals("Aurora", terra.nome());
            service.avancar(terra, 10);
            service.salvar(terra);
        }
        try (var repo = new SqliteMundoRepository(db)) {
            var terra = repo.carregar(id).orElseThrow();
            assertEquals(10, terra.estado().anos());
            assertTrue(repo.historico(id).stream().anyMatch(e -> e.descricao().equals("Impacto de asteroide")));
            assertTrue(repo.snapshots(id).stream().anyMatch(m -> m.estado().equals(original)));
            assertEquals(original, repo.carregar(outroId).orElseThrow().estado());
            assertEquals(1, repo.snapshots(outroId).size());
        }
    }

    @Test void failedTransactionLeavesLiveEarthAndObserversUntouched() throws Exception {
        Path db = dir.resolve("falha.db");
        try (var repo = new SqliteMundoRepository(db)) {
            var service = new SimulacaoService(repo);
            var terra = service.criar(new TerraBuilder()).terra();
            var antes = terra.estado();
            int eventos = repo.historico(terra.id()).size(), snapshots = repo.snapshots(terra.id()).size();
            var notificacoes = new AtomicInteger();
            service.observerBus().inscrever(new SimulacaoObserver() {
                public String nome() { return "Teste de atomicidade"; }
                public void aoMudar(MudancaPlanetaria m) { notificacoes.incrementAndGet(); }
            });
            try (var conn = DriverManager.getConnection("jdbc:sqlite:" + db);
                 var stmt = conn.createStatement()) {
                stmt.execute("CREATE TRIGGER falhar_estado BEFORE UPDATE ON mundos BEGIN SELECT RAISE(ABORT, 'falha induzida'); END");
            }
            assertThrows(PersistenciaException.class, () -> service.aplicar(terra, new GuerraNuclearCreator(), 1));
            assertEquals(antes, terra.estado());
            assertEquals(antes, repo.carregar(terra.id()).orElseThrow().estado());
            assertEquals(0, notificacoes.get());
            assertEquals(eventos, repo.historico(terra.id()).size());
            assertEquals(snapshots, repo.snapshots(terra.id()).size());
        }
    }

    @Test void foreignMementoRejectedBeforeAnyHistoryOrStateChanges() {
        try (var repo = new SqliteMundoRepository(dir.resolve("isolamento.db"))) {
            var service = new SimulacaoService(repo);
            var a = service.criar(new TerraBuilder().nome("A")).terra();
            var b = service.criar(new TerraBuilder().nome("B")).terra();
            assertThrows(IllegalArgumentException.class, () -> service.restaurar(b, repo.snapshots(a.id()).getFirst()));
            assertEquals(1, repo.historico(b.id()).size());
            assertEquals(1, repo.snapshots(b.id()).size());
            assertEquals("B", b.nome());
        }
    }

    @Test void manualSnapshotAndSaveDoNotPretendToNotifyObservers() {
        try (var repo = new SqliteMundoRepository(dir.resolve("trilha.db"))) {
            var service = new SimulacaoService(repo);
            var terra = service.criar(new TerraBuilder()).terra();
            var resultado = service.capturar(terra, "Marco");
            assertTrue(resultado.passos().stream().anyMatch(p -> p.padrao().equals("Memento")));
            assertTrue(resultado.passos().stream().noneMatch(p -> p.padrao().equals("Observer")));
            var salvo = service.salvar(terra);
            assertTrue(salvo.passos().stream().noneMatch(p -> p.padrao().equals("Memento")));
            assertEquals(1, service.estatisticas().para(terra.id()).mudancas());
            assertThrows(UnsupportedOperationException.class, () -> salvo.passos().clear());
        }
    }
    @Test void observerWithBrokenNameCannotTurnCommittedChangeIntoFailure() {
        try (var repo = new SqliteMundoRepository(dir.resolve("observer.db"))) {
            var service = new SimulacaoService(repo);
            var terra = service.criar(new TerraBuilder()).terra();
            var recebido = new AtomicInteger();
            service.observerBus().inscrever(new SimulacaoObserver() {
                public String nome() { throw new IllegalStateException("Nome indisponível"); }
                public void aoMudar(MudancaPlanetaria m) { }
            });
            service.observerBus().inscrever(new SimulacaoObserver() {
                public String nome() { return "Próximo assinante"; }
                public void aoMudar(MudancaPlanetaria m) { recebido.incrementAndGet(); }
            });
            assertDoesNotThrow(() -> service.aplicar(terra, new AsteroideCreator(), .5));
            assertEquals(1, recebido.get());
            assertEquals(terra.estado(), repo.carregar(terra.id()).orElseThrow().estado());
        }
    }
    @Test void nearPureGasesRoundTripAndRemainUsableForEvents() {
        try (var repo = new SqliteMundoRepository(dir.resolve("pureza.db"))) {
            var service = new SimulacaoService(repo);
            var terra = service.criar(new TerraBuilder().atmosfera(new Atmosfera(99.99999910566146, 0, 0, 0, 1))).terra();
            assertEquals(terra.estado(), repo.carregar(terra.id()).orElseThrow().estado());
            assertDoesNotThrow(() -> service.aplicar(terra, new AsteroideCreator(), .5));
            assertEquals(terra.estado(), repo.carregar(terra.id()).orElseThrow().estado());
        }
    }
}
