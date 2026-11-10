package com.projekttellus.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoSqliteTest {
    @TempDir Path temp;

    @Test
    void rollbackEFechamentoMalsucedidosNuncaHabilitamAutocommit() throws Exception {
        try (Connection real = DriverManager.getConnection("jdbc:sqlite:" + temp.resolve("falha-driver.db"))) {
            try (var statement = real.createStatement()) {
                statement.execute("CREATE TABLE valores (valor TEXT NOT NULL)");
            }
            AtomicBoolean habilitouAutocommit = new AtomicBoolean();
            // Conexão real para provar que o restabelecimento prematuro de autocommit confirmaria dados.
            // Somente as duas operações que simulam falha do driver são interceptadas.
            Connection falha = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                        if (method.getName().equals("rollback")) throw new SQLException("rollback recusado");
                        if (method.getName().equals("close")) throw new SQLException("fechamento recusado");
                        if (method.getName().equals("setAutoCommit") && Boolean.TRUE.equals(args[0]))
                            habilitouAutocommit.set(true);
                        try { return method.invoke(real, args); }
                        catch (InvocationTargetException erro) { throw erro.getCause(); }
                    });
            SQLException original = new SQLException("falha na operação");
            SQLException recebida = assertThrows(SQLException.class, () -> TransacaoSqlite.executar(falha, () -> {
                try (var statement = falha.prepareStatement("INSERT INTO valores (valor) VALUES (?)")) {
                    statement.setString(1, "Não confirmar");
                    statement.executeUpdate();
                }
                throw original;
            }));
            assertSame(original, recebida);
            assertEquals(2, recebida.getSuppressed().length);
            assertFalse(habilitouAutocommit.get(), "Autocommit confirmaria a gravação que deveria ser desfeita");
            real.rollback();
            real.setAutoCommit(true);
            try (var statement = real.createStatement(); var rows = statement.executeQuery("SELECT count(*) FROM valores")) {
                assertTrue(rows.next());
                assertEquals(0, rows.getInt(1));
            }
        }
    }
}
