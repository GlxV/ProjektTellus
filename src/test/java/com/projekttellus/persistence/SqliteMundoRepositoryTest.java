package com.projekttellus.persistence;

import com.projekttellus.domain.*;
import com.projekttellus.patterns.memento.TerraMemento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SqliteMundoRepositoryTest {
    @TempDir Path temp;

    @Test
    void reabreBancoERestauraTodosOsCamposHistoricoESnapshots() {
        Path db = temp.resolve("subdiretorio/mundos.db");
        Terra terra = terra("Terra azul", 12.5);
        EstadoPlanetario original = terra.estado();
        TerraMemento snapshot = terra.capturar("Antes da mudança");
        RegistroEvento primeiro = evento(terra, "Primeiro evento", 0.25, "2026-11-10T03:00:00.000000001Z");
        RegistroEvento segundo = evento(terra, "Segundo evento", 0.75, "2026-11-10T03:00:00.000000002Z");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra, primeiro, snapshot);
            terra.renomear("Terra transformada");
            terra.atualizar(estado(88));
            repo.salvar(terra, segundo, null);
        }
        try (var repo = new SqliteMundoRepository(db)) {
            Terra recuperada = repo.carregar(terra.id()).orElseThrow();
            assertEquals(terra.id(), recuperada.id());
            assertEquals("Terra transformada", recuperada.nome());
            assertEquals(terra.estado(), recuperada.estado());
            assertEquals(List.of(primeiro, segundo), repo.historico(terra.id()));
            assertEquals(List.of(snapshot), repo.snapshots(terra.id()));
            assertEquals(1, repo.listar().size());
            MundoResumo resumo = repo.listar().getFirst();
            assertEquals(terra.id(), resumo.id());
            assertEquals(88, resumo.anos());
            assertEquals("Terra transformada", resumo.nome());
            assertNotNull(resumo.atualizadoEm());
            recuperada.restaurar(repo.snapshots(terra.id()).getFirst());
            assertEquals(original, recuperada.estado());
            assertEquals("Terra azul", recuperada.nome());
            repo.salvar(recuperada, null, null);
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(original, repo.carregar(terra.id()).orElseThrow().estado());
            assertEquals(2, repo.historico(terra.id()).size());
        }
    }

    @Test
    void doisMundosMantemEstadosHistoricosESnapshotsIndependentes() {
        Path db = temp.resolve("dois.db");
        Terra a = terra("Mundo A", 1);
        Terra b = terra("Mundo B", 2);
        var snapshotA = a.capturar("A");
        var snapshotB = b.capturar("B");
        var eventoA = evento(a, "Evento A", 0.5, "2026-11-10T00:00:00Z");
        var eventoB = evento(b, "Evento B", 0.5, "2026-11-10T00:00:00Z");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(a, eventoA, snapshotA);
            repo.salvar(b, eventoB, snapshotB);
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(2, repo.listar().size());
            assertEquals(a.estado(), repo.carregar(a.id()).orElseThrow().estado());
            assertEquals(b.estado(), repo.carregar(b.id()).orElseThrow().estado());
            assertEquals(List.of(eventoA), repo.historico(a.id()));
            assertEquals(List.of(eventoB), repo.historico(b.id()));
            assertEquals(List.of(snapshotA), repo.snapshots(a.id()));
            assertEquals(List.of(snapshotB), repo.snapshots(b.id()));
            assertThrows(IllegalArgumentException.class, () -> b.restaurar(snapshotA));
            assertEquals(b.estado(), repo.carregar(b.id()).orElseThrow().estado());
        }
    }

    @Test
    void rejeitaEventoOuSnapshotDeOutroMundoSemSalvarAlteracoes() {
        try (var repo = new SqliteMundoRepository(temp.resolve("limites.db"))) {
            Terra a = terra("A", 1);
            Terra b = terra("B", 2);
            repo.salvar(a, null, null);
            a.renomear("Não persistir");
            assertThrows(PersistenciaException.class, () -> repo.salvar(a, null, b.capturar("B")));
            assertThrows(PersistenciaException.class,
                    () -> repo.salvar(a, evento(b, "Errado", 1, "2026-11-10T00:00:00Z"), null));
            assertEquals("A", repo.carregar(a.id()).orElseThrow().nome());
            assertTrue(repo.carregar(b.id()).isEmpty());
            assertTrue(repo.historico(a.id()).isEmpty());
            assertTrue(repo.snapshots(a.id()).isEmpty());
        }
    }

    @Test
    void falhaAoInserirSnapshotDesfazEstadoEEventoEPermiteNovoSave() throws Exception {
        Path db = temp.resolve("rollback.db");
        Terra terra = terra("Original", 1);
        EstadoPlanetario original = terra.estado();
        var snapshot = terra.capturar("Imutável");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra, null, snapshot);
            terra.renomear("Alterado");
            terra.atualizar(estado(99));
            var evento = evento(terra, "Será revertido", 0.8, "2026-11-10T00:00:00Z");
            PersistenciaException erro = assertThrows(PersistenciaException.class,
                    () -> repo.salvar(terra, evento, snapshot)); // duplicate PK: last statement fails
            assertNotNull(erro.getCause());
            assertEquals(original, repo.carregar(terra.id()).orElseThrow().estado());
            assertEquals("Original", repo.carregar(terra.id()).orElseThrow().nome());
            assertTrue(repo.historico(terra.id()).isEmpty());
            assertEquals(List.of(snapshot), repo.snapshots(terra.id()));
            repo.salvar(terra, evento, terra.capturar("Novo"));
            assertEquals(99, repo.carregar(terra.id()).orElseThrow().estado().anos());
            assertEquals(List.of(evento), repo.historico(terra.id()));
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(99, repo.carregar(terra.id()).orElseThrow().estado().anos());
            assertEquals(2, repo.snapshots(terra.id()).size());
        }
    }

    @Test
    void falhaDurantePrimeiroSaveNaoDeixaMundoOuEventoParcial() throws Exception {
        Path db = temp.resolve("primeiro-save.db");
        try (var repo = new SqliteMundoRepository(db)) {
            try (Connection conn = abrir(db); var statement = conn.createStatement()) {
                statement.execute("CREATE TRIGGER falha_snapshot BEFORE INSERT ON snapshots "
                        + "BEGIN SELECT RAISE(ABORT, 'falha controlada'); END");
            }
            Terra terra = terra("Não deve existir", 1);
            assertThrows(PersistenciaException.class, () -> repo.salvar(terra,
                    evento(terra, "Evento", 1, "2026-11-10T00:00:00Z"), terra.capturar("Snapshot")));
            assertTrue(repo.carregar(terra.id()).isEmpty());
            assertTrue(repo.listar().isEmpty());
            assertTrue(repo.historico(terra.id()).isEmpty());
            assertTrue(repo.snapshots(terra.id()).isEmpty());
        }
    }

    @Test
    void eventoDuplicadoNaoAtualizaMundoNemCriaSnapshot() {
        try (var repo = new SqliteMundoRepository(temp.resolve("evento-duplicado.db"))) {
            Terra terra = terra("Original", 1);
            var evento = evento(terra, "Original", 0.4, "2026-11-10T00:00:00Z");
            repo.salvar(terra, evento, null);
            terra.renomear("Reverter");
            assertThrows(PersistenciaException.class, () -> repo.salvar(terra, evento, terra.capturar("Reverter")));
            assertEquals("Original", repo.carregar(terra.id()).orElseThrow().nome());
            assertEquals(List.of(evento), repo.historico(terra.id()));
            assertTrue(repo.snapshots(terra.id()).isEmpty());
        }
    }

    @Test
    void idDeSnapshotExistenteNaoPodeSerReatribuidoAOutroMundo() {
        try (var repo = new SqliteMundoRepository(temp.resolve("snapshot-ownership.db"))) {
            Terra a = terra("A", 1);
            Terra b = terra("B", 2);
            var original = a.capturar("Original");
            repo.salvar(a, null, original);
            repo.salvar(b, null, null);
            var forjado = new TerraMemento(original.id(), b.id(), "Forjado", b.nome(),
                    original.criadoEm(), b.estado());
            b.renomear("Reverter");
            assertThrows(PersistenciaException.class, () -> repo.salvar(b, null, forjado));
            assertEquals(List.of(original), repo.snapshots(a.id()));
            assertTrue(repo.snapshots(b.id()).isEmpty());
            assertEquals("B", repo.carregar(b.id()).orElseThrow().nome());
        }
    }

    @Test
    void excluiEmCascataSomenteOMundoSolicitado() throws Exception {
        Path db = temp.resolve("exclusao.db");
        Terra a = terra("A", 1);
        Terra b = terra("B", 2);
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(a, evento(a, "A", 1, "2026-11-10T00:00:00Z"), a.capturar("A"));
            repo.salvar(b, evento(b, "B", 1, "2026-11-10T00:00:00Z"), b.capturar("B"));
            repo.excluir(a.id());
            repo.excluir(a.id());
            assertTrue(repo.carregar(a.id()).isEmpty());
            assertTrue(repo.historico(a.id()).isEmpty());
            assertTrue(repo.snapshots(a.id()).isEmpty());
            assertEquals(1, repo.listar().size());
            assertEquals(1, repo.historico(b.id()).size());
            assertEquals(1, repo.snapshots(b.id()).size());
            try (var conn = abrir(db); var query = conn.prepareStatement("PRAGMA foreign_key_check");
                 var result = query.executeQuery()) {
                assertFalse(result.next());
            }
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(b.id(), repo.listar().getFirst().id());
            assertTrue(repo.carregar(a.id()).isEmpty());
        }
    }

    @Test
    void nomesRotulosEDescricoesSaoLiteraisMesmoComSqlEUnicode() {
        Path db = temp.resolve("injection.db");
        String texto = "Terra d'água 🌍'); DROP TABLE mundos; --\nsegunda linha";
        Terra terra = terra(texto, 1);
        var snapshot = terra.capturar(texto);
        var evento = evento(terra, texto, 0.5, "2026-11-10T00:00:00Z");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra, evento, snapshot);
            repo.salvar(terra("Intacto", 2), null, null);
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(texto, repo.carregar(terra.id()).orElseThrow().nome());
            assertEquals(texto, repo.historico(terra.id()).getFirst().descricao());
            assertEquals(texto, repo.snapshots(terra.id()).getFirst().rotulo());
            assertEquals(2, repo.listar().size());
        }
    }

    @Test
    void historicoOrdenaInstantesComPrecisaoDeNanosIndependentementeDaInsercao() {
        Terra terra = terra("Precisão", 1);
        RegistroEvento primeiro = evento(terra, "1", 1, "2026-11-10T00:00:00Z");
        RegistroEvento segundo = evento(terra, "2", 1, "2026-11-10T00:00:00.000000001Z");
        RegistroEvento terceiro = evento(terra, "3", 1, "2026-11-10T00:00:00.999999999Z");
        try (var repo = new SqliteMundoRepository(temp.resolve("ordem.db"))) {
            repo.salvar(terra, terceiro, null);
            repo.salvar(terra, segundo, null);
            repo.salvar(terra, primeiro, null);
            assertEquals(List.of(primeiro, segundo, terceiro), repo.historico(terra.id()));
            assertThrows(UnsupportedOperationException.class, () -> repo.historico(terra.id()).clear());
        }
    }

    @Test
    void consultasAusentesRetornamColecoesVaziasEOptionalVazio() {
        try (var repo = new SqliteMundoRepository(temp.resolve("vazio.db"))) {
            UUID id = UUID.randomUUID();
            assertTrue(repo.carregar(id).isEmpty());
            assertTrue(repo.historico(id).isEmpty());
            assertTrue(repo.snapshots(id).isEmpty());
            assertTrue(repo.listar().isEmpty());
            repo.excluir(id);
        }
    }

    @Test
    void fechamentoIdempotenteEOperacoesFechadasFalhamComErroDaCamada() {
        var repo = new SqliteMundoRepository(temp.resolve("fechado.db"));
        repo.close();
        assertDoesNotThrow(repo::close);
        UUID id = UUID.randomUUID();
        assertThrows(PersistenciaException.class, repo::listar);
        assertThrows(PersistenciaException.class, () -> repo.carregar(id));
        assertThrows(PersistenciaException.class, () -> repo.historico(id));
        assertThrows(PersistenciaException.class, () -> repo.snapshots(id));
        assertThrows(PersistenciaException.class, () -> repo.excluir(id));
        assertThrows(PersistenciaException.class, () -> repo.salvar(terra("X", 0), null, null));
    }

    @Test
    void inicializacaoIdempotenteMantemVersaoEEsquemaRelacional() throws Exception {
        Path db = temp.resolve("schema.db");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra("Persistido", 0), null, null);
        }
        try (var repo = new SqliteMundoRepository(db); var conn = abrir(db)) {
            assertEquals(1, repo.listar().size());
            try (var query = conn.prepareStatement("PRAGMA user_version"); var result = query.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
            try (var query = conn.prepareStatement("SELECT nome, oxigenio, populacao, anos FROM mundos");
                 var result = query.executeQuery()) {
                assertTrue(result.next());
                assertEquals("Persistido", result.getString("nome"));
                assertEquals(18.5, result.getDouble("oxigenio"));
                assertEquals(7_654_321_012L, result.getLong("populacao"));
            }
            try (var query = conn.prepareStatement("SELECT sql FROM sqlite_master WHERE type = 'table'");
                 var result = query.executeQuery()) {
                while (result.next()) assertFalse(result.getString(1).toLowerCase().contains("json"));
            }
        }
    }

    @Test
    void rejeitaVersaoFuturaSemAlterarBanco() throws Exception {
        Path db = temp.resolve("futuro.db");
        try (var conn = abrir(db); var statement = conn.createStatement()) {
            statement.execute("PRAGMA user_version = 99");
        }
        assertThrows(PersistenciaException.class, () -> new SqliteMundoRepository(db));
        try (var conn = abrir(db); var statement = conn.createStatement();
             var result = statement.executeQuery("PRAGMA user_version")) {
            assertTrue(result.next());
            assertEquals(99, result.getInt(1));
        }
    }

    @Test
    void arquivoInvalidoOuDiretorioComoBancoProduzErroComCausa() throws Exception {
        Path arquivo = temp.resolve("nao-sqlite.db");
        Files.writeString(arquivo, "Isto não é um banco de dados.");
        assertNotNull(assertThrows(PersistenciaException.class,
                () -> new SqliteMundoRepository(arquivo)).getCause());
        assertThrows(PersistenciaException.class, () -> new SqliteMundoRepository(temp));
        Path paiArquivo = temp.resolve("arquivo-pai");
        Files.writeString(paiArquivo, "arquivo");
        assertThrows(PersistenciaException.class,
                () -> new SqliteMundoRepository(paiArquivo.resolve("db.sqlite")));
    }

    @Test
    void falhaNoCommitDesfazAsTresGravacoesEPreservaConexaoUtilizavel() throws Exception {
        Path db = temp.resolve("commit.db");
        Terra terra = terra("Atômica", 1);
        var evento = evento(terra, "Evento", 1, "2026-11-10T00:00:00Z");
        var snapshot = terra.capturar("Snapshot");
        try (var repo = new SqliteMundoRepository(db)) {
            try (var conn = abrir(db); var statement = conn.createStatement()) {
                statement.execute("CREATE TABLE auditoria (mundo_id TEXT REFERENCES mundos(id) "
                        + "DEFERRABLE INITIALLY DEFERRED)");
                statement.execute("CREATE TRIGGER falha_commit AFTER INSERT ON mundos BEGIN "
                        + "INSERT INTO auditoria(mundo_id) VALUES ('mundo-inexistente'); END");
            }
            // A referência adiada só é conferida no COMMIT, depois de estado/evento/snapshot.
            assertThrows(PersistenciaException.class, () -> repo.salvar(terra, evento, snapshot));
            assertTrue(repo.carregar(terra.id()).isEmpty());
            assertTrue(repo.historico(terra.id()).isEmpty());
            assertTrue(repo.snapshots(terra.id()).isEmpty());
            try (var conn = abrir(db); var statement = conn.createStatement()) {
                try (var result = statement.executeQuery("SELECT count(*) FROM auditoria")) {
                    assertTrue(result.next());
                    assertEquals(0, result.getInt(1));
                }
                statement.execute("DROP TRIGGER falha_commit");
            }
            repo.salvar(terra, evento, snapshot);
            assertEquals(List.of(evento), repo.historico(terra.id()));
            assertEquals(List.of(snapshot), repo.snapshots(terra.id()));
        }
    }

    @Test
    void falhaNaExclusaoEmCascataPreservaMundoHistoricoESnapshot() throws Exception {
        Path db = temp.resolve("exclusao-rollback.db");
        Terra terra = terra("Preservar", 1);
        var evento = evento(terra, "Evento", 1, "2026-11-10T00:00:00Z");
        var snapshot = terra.capturar("Snapshot");
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra, evento, snapshot);
            try (var conn = abrir(db); var statement = conn.createStatement()) {
                statement.execute("CREATE TRIGGER impedir_exclusao BEFORE DELETE ON snapshots "
                        + "BEGIN SELECT RAISE(ABORT, 'falha controlada'); END");
            }
            assertThrows(PersistenciaException.class, () -> repo.excluir(terra.id()));
            assertEquals(terra.estado(), repo.carregar(terra.id()).orElseThrow().estado());
            assertEquals(List.of(evento), repo.historico(terra.id()));
            assertEquals(List.of(snapshot), repo.snapshots(terra.id()));
            try (var conn = abrir(db); var statement = conn.createStatement()) {
                statement.execute("DROP TRIGGER impedir_exclusao");
            }
            repo.excluir(terra.id());
            assertTrue(repo.listar().isEmpty());
            assertTrue(repo.historico(terra.id()).isEmpty());
            assertTrue(repo.snapshots(terra.id()).isEmpty());
        }
    }

    @Test
    void migrationMalsucedidaNaoDeixaTabelasOuVersaoParciais() throws Exception {
        Path db = temp.resolve("migration-rollback.db");
        try (var conn = abrir(db); var statement = conn.createStatement()) {
            // A criação do índice de snapshots falhará após as outras instruções de DDL.
            statement.execute("CREATE TABLE snapshots (id TEXT PRIMARY KEY)");
        }
        assertThrows(PersistenciaException.class, () -> new SqliteMundoRepository(db));
        try (var conn = abrir(db); var statement = conn.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA user_version")) {
                assertTrue(result.next());
                assertEquals(0, result.getInt(1));
            }
            try (var result = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'")) {
                assertTrue(result.next());
                assertEquals("snapshots", result.getString(1));
                assertFalse(result.next());
            }
            // O construtor que falhou liberou a conexão e não deixou o arquivo bloqueado.
            statement.execute("DROP TABLE snapshots");
        }
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra("Recuperado", 0), null, null);
            assertEquals(1, repo.listar().size());
        }
    }

    @Test
    void multiplasConexoesCompartilhamBancoSemCompartilharCicloDeVida() {
        Path db = temp.resolve("conexoes.db");
        Terra a = terra("A", 1);
        Terra b = terra("B", 2);
        try (var primeiro = new SqliteMundoRepository(db); var segundo = new SqliteMundoRepository(db)) {
            primeiro.salvar(a, null, a.capturar("A"));
            assertEquals(a.estado(), segundo.carregar(a.id()).orElseThrow().estado());
            segundo.salvar(b, null, b.capturar("B"));
            assertEquals(2, primeiro.listar().size());
            primeiro.close();
            segundo.excluir(a.id());
            assertEquals(List.of(b.id()), segundo.listar().stream().map(MundoResumo::id).toList());
        }
    }

    @Test
    void dadosCorrompidosFalhamComCausaSemAlterarOutroMundo() throws Exception {
        Path db = temp.resolve("corrompido.db");
        Terra a = terra("A", 1);
        Terra b = terra("B", 2);
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(a, null, null);
            repo.salvar(b, null, null);
            try (var conn = abrir(db); var update = conn.prepareStatement(
                    "UPDATE mundos SET atualizado_em = ? WHERE id = ?")) {
                update.setString(1, "data inválida");
                update.setString(2, a.id().toString());
                update.executeUpdate();
            }
            assertNotNull(assertThrows(PersistenciaException.class, repo::listar).getCause());
            assertEquals(b.estado(), repo.carregar(b.id()).orElseThrow().estado());
            repo.salvar(a, null, null);
            assertEquals(2, repo.listar().size());
        }
    }

    @Test
    void preservaToleranciaDeArredondamentoAceitaPeloDominioNaHidrosfera() {
        Path db = temp.resolve("arredondamento.db");
        EstadoPlanetario base = estado(1);
        EstadoPlanetario limite = new EstadoPlanetario(base.atmosfera(), base.clima(), base.biosfera(),
                new Hidrosfera(59.0000005, 41), base.civilizacao(), base.anos());
        Terra terra = new Terra(UUID.randomUUID(), "Limite de cobertura", limite);
        try (var repo = new SqliteMundoRepository(db)) {
            repo.salvar(terra, null, terra.capturar("Limite"));
        }
        try (var repo = new SqliteMundoRepository(db)) {
            assertEquals(limite, repo.carregar(terra.id()).orElseThrow().estado());
            assertEquals(limite, repo.snapshots(terra.id()).getFirst().estado());
        }
    }

    private static Terra terra(String nome, double anos) {
        return new Terra(UUID.randomUUID(), nome, estado(anos));
    }

    private static EstadoPlanetario estado(double anos) {
        return new EstadoPlanetario(new Atmosfera(18.5, 79, 1.5, 1, 1.25),
                new Clima(-12.75, 3.25, 9.5), new Biosfera(42.125),
                new Hidrosfera(59.25, 17.125), new Civilizacao(7_654_321_012L), anos);
    }

    private static RegistroEvento evento(Terra terra, String descricao, double intensidade, String instante) {
        return new RegistroEvento(UUID.randomUUID(), terra.id(), Instant.parse(instante),
                descricao, intensidade, terra.estado().anos());
    }

    private static Connection abrir(Path db) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + db.toAbsolutePath());
    }
}
