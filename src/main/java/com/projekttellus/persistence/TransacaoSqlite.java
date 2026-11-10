package com.projekttellus.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/** Compartilha a disciplina de commit/rollback entre migrations e gravações. */
final class TransacaoSqlite {
    private TransacaoSqlite() { }

    @FunctionalInterface
    interface Acao { void executar() throws SQLException; }

    static void executar(Connection connection, Acao acao) throws SQLException {
        connection.setAutoCommit(false);
        Throwable falha = null;
        boolean podeRestaurarAutocommit = true;
        try {
            acao.executar();
            connection.commit();
        } catch (SQLException | RuntimeException | Error erro) {
            falha = erro;
            try {
                connection.rollback();
            } catch (SQLException rollback) {
                erro.addSuppressed(rollback);
                podeRestaurarAutocommit = false;
                // Nunca habilitar autocommit após rollback malsucedido: isso poderia confirmar dados.
                fecharAposFalha(connection, erro);
            }
            throw erro;
        } finally {
            try {
                if (podeRestaurarAutocommit && !connection.isClosed()) connection.setAutoCommit(true);
            } catch (SQLException restauracao) {
                fecharAposFalha(connection, restauracao);
                if (falha != null) falha.addSuppressed(restauracao);
                else throw restauracao;
            }
        }
    }

    private static void fecharAposFalha(Connection connection, Throwable erro) {
        try { connection.close(); }
        catch (SQLException fechamento) { erro.addSuppressed(fechamento); }
    }
}
