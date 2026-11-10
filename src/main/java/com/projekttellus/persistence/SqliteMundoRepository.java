package com.projekttellus.persistence;

import com.projekttellus.domain.Atmosfera;
import com.projekttellus.domain.Biosfera;
import com.projekttellus.domain.Civilizacao;
import com.projekttellus.domain.Clima;
import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.domain.Hidrosfera;
import com.projekttellus.domain.Terra;
import com.projekttellus.patterns.memento.TerraMemento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite local com conexão própria. O acesso sincronizado impede intercalar transações ou fechar
 * a conexão durante uma consulta. Todos os valores externos são parâmetros JDBC.
 */
public final class SqliteMundoRepository implements MundoRepository {
    private static final String COLUNAS_ESTADO = """
            oxigenio, nitrogenio, co2, helio, pressao, temperatura, aerossol, radiacao,
            vegetacao, agua, gelo, populacao, anos
            """;
    private static final String SALVAR_MUNDO = """
            INSERT INTO mundos (id, nome, atualizado_em,
                oxigenio, nitrogenio, co2, helio, pressao, temperatura, aerossol, radiacao,
                vegetacao, agua, gelo, populacao, anos)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nome = excluded.nome, atualizado_em = excluded.atualizado_em,
                oxigenio = excluded.oxigenio, nitrogenio = excluded.nitrogenio,
                co2 = excluded.co2, helio = excluded.helio, pressao = excluded.pressao,
                temperatura = excluded.temperatura, aerossol = excluded.aerossol,
                radiacao = excluded.radiacao, vegetacao = excluded.vegetacao,
                agua = excluded.agua, gelo = excluded.gelo,
                populacao = excluded.populacao, anos = excluded.anos
            """;
    private static final String INSERIR_EVENTO = """
            INSERT INTO eventos (id, mundo_id, instante, descricao, intensidade, anos)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERIR_SNAPSHOT = """
            INSERT INTO snapshots (id, mundo_id, rotulo, nome_mundo, criado_em,
                oxigenio, nitrogenio, co2, helio, pressao, temperatura, aerossol, radiacao,
                vegetacao, agua, gelo, populacao, anos)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;
    private boolean fechado;

    /** Cria diretórios intermediários e aplica migrations antes de disponibilizar o repositório. */
    public SqliteMundoRepository(Path arquivo) {
        Connection aberta = null;
        try {
            Path caminho = Objects.requireNonNull(arquivo, "Arquivo SQLite").toAbsolutePath().normalize();
            Files.createDirectories(caminho.getParent());
            aberta = DriverManager.getConnection("jdbc:sqlite:" + caminho);
            SchemaSqlite.inicializar(aberta);
            connection = aberta;
        } catch (IOException | SQLException | RuntimeException erro) {
            if (aberta != null) {
                try { aberta.close(); }
                catch (SQLException fechamento) { erro.addSuppressed(fechamento); }
            }
            throw new PersistenciaException("Não foi possível abrir o banco de mundos.", erro);
        }
    }

    @Override
    public synchronized void salvar(Terra terra, RegistroEvento evento, TerraMemento snapshot) {
        executar("Não foi possível salvar o mundo e seus registros.", () -> {
            Objects.requireNonNull(terra, "Mundo");
            UUID id = terra.id();
            String nome = terra.nome();
            EstadoPlanetario estado = terra.estado();
            if (evento != null && !id.equals(evento.mundoId()))
                throw new PersistenciaException("O evento pertence a outro mundo.");
            if (snapshot != null && !id.equals(snapshot.mundoId()))
                throw new PersistenciaException("O retrato pertence a outro mundo.");
            TransacaoSqlite.executar(connection, () -> {
                try (var query = connection.prepareStatement(SALVAR_MUNDO)) {
                    query.setString(1, id.toString());
                    query.setString(2, nome);
                    query.setString(3, Instant.now().toString());
                    preencherEstado(query, 4, estado);
                    query.executeUpdate();
                }
                if (evento != null) inserirEvento(evento);
                if (snapshot != null) inserirSnapshot(snapshot);
            });
            return null;
        });
    }

    @Override
    public synchronized List<MundoResumo> listar() {
        return executar("Não foi possível listar os mundos.", () -> {
            List<MundoResumo> mundos = new ArrayList<>();
            try (var query = connection.prepareStatement("SELECT id, nome, anos, atualizado_em FROM mundos");
                 var rows = query.executeQuery()) {
                while (rows.next()) {
                    mundos.add(new MundoResumo(UUID.fromString(rows.getString("id")), rows.getString("nome"),
                            rows.getDouble("anos"), Instant.parse(rows.getString("atualizado_em"))));
                }
            }
            mundos.sort(Comparator.comparing(MundoResumo::atualizadoEm).reversed().thenComparing(MundoResumo::id));
            return List.copyOf(mundos);
        });
    }

    @Override
    public synchronized Optional<Terra> carregar(UUID id) {
        return executar("Não foi possível carregar o mundo.", () -> {
            try (var query = connection.prepareStatement("SELECT id, nome, " + COLUNAS_ESTADO
                    + " FROM mundos WHERE id = ?")) {
                query.setString(1, Objects.requireNonNull(id, "Identificador do mundo").toString());
                try (var rows = query.executeQuery()) {
                    if (!rows.next()) return Optional.empty();
                    return Optional.of(new Terra(UUID.fromString(rows.getString("id")),
                            rows.getString("nome"), lerEstado(rows)));
                }
            }
        });
    }

    @Override
    public synchronized List<RegistroEvento> historico(UUID id) {
        return executar("Não foi possível carregar o histórico do mundo.", () -> {
            List<RegistroEvento> eventos = new ArrayList<>();
            try (var query = connection.prepareStatement("""
                    SELECT id, mundo_id, instante, descricao, intensidade, anos FROM eventos WHERE mundo_id = ?
                    """)) {
                query.setString(1, Objects.requireNonNull(id, "Identificador do mundo").toString());
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        eventos.add(new RegistroEvento(UUID.fromString(rows.getString("id")),
                                UUID.fromString(rows.getString("mundo_id")), Instant.parse(rows.getString("instante")),
                                rows.getString("descricao"), rows.getDouble("intensidade"), rows.getDouble("anos")));
                    }
                }
            }
            // ISO-8601 possui frações de tamanho variável. Comparar como Instant evita perder nanos
            // ou ordenar incorretamente um instante exato (terminado em Z) após sua fração seguinte.
            eventos.sort(Comparator.comparing(RegistroEvento::instante).thenComparing(RegistroEvento::id));
            return List.copyOf(eventos);
        });
    }

    @Override
    public synchronized List<TerraMemento> snapshots(UUID id) {
        return executar("Não foi possível carregar os retratos do mundo.", () -> {
            List<TerraMemento> snapshots = new ArrayList<>();
            try (var query = connection.prepareStatement("SELECT id, mundo_id, rotulo, nome_mundo, criado_em, "
                    + COLUNAS_ESTADO + " FROM snapshots WHERE mundo_id = ?")) {
                query.setString(1, Objects.requireNonNull(id, "Identificador do mundo").toString());
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        snapshots.add(new TerraMemento(UUID.fromString(rows.getString("id")),
                                UUID.fromString(rows.getString("mundo_id")), rows.getString("rotulo"),
                                rows.getString("nome_mundo"), Instant.parse(rows.getString("criado_em")), lerEstado(rows)));
                    }
                }
            }
            snapshots.sort(Comparator.comparing(TerraMemento::criadoEm).thenComparing(TerraMemento::id));
            return List.copyOf(snapshots);
        });
    }

    @Override
    public synchronized void excluir(UUID id) {
        executar("Não foi possível excluir o mundo.", () -> {
            Objects.requireNonNull(id, "Identificador do mundo");
            TransacaoSqlite.executar(connection, () -> {
                try (var query = connection.prepareStatement("DELETE FROM mundos WHERE id = ?")) {
                    query.setString(1, id.toString());
                    query.executeUpdate(); // FK ON DELETE CASCADE participa da mesma transação.
                }
            });
            return null;
        });
    }

    @Override
    public synchronized void close() {
        if (fechado) return;
        try {
            connection.close();
            fechado = true;
        } catch (SQLException erro) {
            throw new PersistenciaException("Não foi possível fechar o banco de mundos.", erro);
        }
    }

    private void inserirEvento(RegistroEvento evento) throws SQLException {
        try (var query = connection.prepareStatement(INSERIR_EVENTO)) {
            query.setString(1, evento.id().toString());
            query.setString(2, evento.mundoId().toString());
            query.setString(3, evento.instante().toString());
            query.setString(4, evento.descricao());
            query.setDouble(5, evento.intensidade());
            query.setDouble(6, evento.anos());
            query.executeUpdate();
        }
    }

    private void inserirSnapshot(TerraMemento snapshot) throws SQLException {
        try (var query = connection.prepareStatement(INSERIR_SNAPSHOT)) {
            query.setString(1, snapshot.id().toString());
            query.setString(2, snapshot.mundoId().toString());
            query.setString(3, snapshot.rotulo());
            query.setString(4, snapshot.nomeMundo());
            query.setString(5, snapshot.criadoEm().toString());
            preencherEstado(query, 6, snapshot.estado());
            query.executeUpdate();
        }
    }

    private static void preencherEstado(PreparedStatement query, int inicio, EstadoPlanetario estado)
            throws SQLException {
        int coluna = inicio;
        query.setDouble(coluna++, estado.atmosfera().oxigenio());
        query.setDouble(coluna++, estado.atmosfera().nitrogenio());
        query.setDouble(coluna++, estado.atmosfera().co2());
        query.setDouble(coluna++, estado.atmosfera().helio());
        query.setDouble(coluna++, estado.atmosfera().pressao());
        query.setDouble(coluna++, estado.clima().temperatura());
        query.setDouble(coluna++, estado.clima().aerossol());
        query.setDouble(coluna++, estado.clima().radiacao());
        query.setDouble(coluna++, estado.biosfera().vegetacao());
        query.setDouble(coluna++, estado.hidrosfera().agua());
        query.setDouble(coluna++, estado.hidrosfera().gelo());
        query.setLong(coluna++, estado.civilizacao().populacao());
        query.setDouble(coluna, estado.anos());
    }

    private static EstadoPlanetario lerEstado(ResultSet row) throws SQLException {
        return new EstadoPlanetario(new Atmosfera(row.getDouble("oxigenio"), row.getDouble("nitrogenio"),
                row.getDouble("co2"), row.getDouble("helio"), row.getDouble("pressao")),
                new Clima(row.getDouble("temperatura"), row.getDouble("aerossol"), row.getDouble("radiacao")),
                new Biosfera(row.getDouble("vegetacao")),
                new Hidrosfera(row.getDouble("agua"), row.getDouble("gelo")),
                new Civilizacao(row.getLong("populacao")), row.getDouble("anos"));
    }

    private <T> T executar(String mensagem, Consulta<T> consulta) {
        try {
            if (fechado || connection.isClosed())
                throw new PersistenciaException("O repositório de mundos está fechado.");
            if (!connection.getAutoCommit())
                throw new PersistenciaException("O banco não pôde recuperar sua transação. Reabra o repositório.");
            return consulta.executar();
        } catch (PersistenciaException erro) {
            throw erro;
        } catch (SQLException | RuntimeException erro) {
            // Inclui dados inválidos/corrompidos detectados na reconstrução do domínio.
            throw new PersistenciaException(mensagem, erro);
        }
    }

    @FunctionalInterface
    private interface Consulta<T> { T executar() throws SQLException; }
}
