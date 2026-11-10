package com.projekttellus.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/** Migration inicial versionada e atômica. Versões futuras nunca são rebaixadas silenciosamente. */
final class SchemaSqlite {
    private static final int VERSAO = 1;

    private SchemaSqlite() { }

    static void inicializar(Connection connection) throws SQLException, IOException {
        executar(connection, "PRAGMA busy_timeout = 5000");
        executar(connection, "PRAGMA foreign_keys = ON");
        try (var query = connection.prepareStatement("PRAGMA foreign_keys");
             var result = query.executeQuery()) {
            if (!result.next() || result.getInt(1) != 1)
                throw new SQLException("Não foi possível habilitar a integridade referencial.");
        }
        String migration;
        try (InputStream input = SchemaSqlite.class.getResourceAsStream("/db/migrations/V001__mundos.sql")) {
            if (input == null) throw new IOException("Recurso da migration inicial não encontrado.");
            migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        TransacaoSqlite.executar(connection, () -> {
            int atual;
            try (var query = connection.prepareStatement("PRAGMA user_version");
                 var result = query.executeQuery()) {
                if (!result.next()) throw new SQLException("Versão do banco indisponível.");
                atual = result.getInt(1);
            }
            if (atual > VERSAO || atual < 0)
                throw new SQLException("A versão do banco não é suportada por este aplicativo: " + atual);
            if (atual == 0) {
                // O recurso contém somente DDL estático, sem valores ou SQL fornecidos pelo usuário.
                for (String sql : migration.split(";")) {
                    if (!sql.isBlank()) executar(connection, sql);
                }
                executar(connection, "PRAGMA user_version = 1");
            }
        });
    }

    private static void executar(Connection connection, String sql) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) { statement.execute(); }
    }
}
