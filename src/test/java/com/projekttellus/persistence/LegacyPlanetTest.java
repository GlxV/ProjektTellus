package com.projekttellus.persistence;

import com.projekttellus.domain.*;
import com.projekttellus.application.SimulacaoService;
import com.projekttellus.simulation.ModeloPlanetario;
import com.projekttellus.ui.AtmosphereOptics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LegacyPlanetTest {
    @TempDir Path temp;
    @Test void originalV001WorldsAndSnapshotsLoadWithoutReinterpretingOrUpdatingColumns() throws Exception {
        var db = temp.resolve("legacy.db"); var id = UUID.randomUUID();
        // Build the old database without invoking current initialization or serialization code.
        try (var c = DriverManager.getConnection("jdbc:sqlite:"+db); var st = c.createStatement()) {
            String sql;
            try (var in = getClass().getResourceAsStream("/db/migrations/V001__mundos.sql")) { sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); }
            for (String part : sql.split(";")) if (!part.isBlank()) st.execute(part);
            st.execute("PRAGMA user_version=1");
            st.execute("INSERT INTO mundos VALUES ('"+id+"','Oceano V001','2026-11-10T00:00:00Z',21,58.96,.04,20,1,15,0,0,93,100,0,10000000000,8.5)");
            st.execute("INSERT INTO snapshots VALUES ('"+UUID.randomUUID()+"','"+id+"','Retrato V001','Terra antiga','2026-11-10T00:00:00Z',21,78.96,.04,0,1,15,20,10,65,71,3,8000000000,0)");
        }
        var expected = new TerraBuilder().atmosfera(new Atmosfera(21,58.96,.04,20,1))
                .biosfera(new Biosfera(93)).hidrosfera(new Hidrosfera(100,0)).civilizacao(new Civilizacao(10_000_000_000L)).anos(8.5).build().estado();
        EstadoPlanetario restored;
        try (var repo = new SqliteMundoRepository(db)) {
            var world = repo.carregar(id).orElseThrow(); assertEquals(expected, world.estado());
            assertEquals(93, world.estado().biosfera().vegetacao()); assertEquals(0, world.estado().superficieVegetada());
            assertEquals(0, ModeloPlanetario.calcular(world.estado()).sustentabilidade());
            var snapshot = repo.snapshots(id).getFirst(); restored = snapshot.estado();
            new SimulacaoService(repo).restaurar(world,snapshot);
            assertEquals("Terra antiga",world.nome()); assertEquals(restored,world.estado());
        }
        try (var repo = new SqliteMundoRepository(db)) {
            var state = repo.carregar(id).orElseThrow().estado();
            assertEquals(restored,state); assertEquals(ModeloPlanetario.calcular(restored),ModeloPlanetario.calcular(state));
            assertEquals(AtmosphereOptics.from(restored),AtmosphereOptics.from(state));
            assertTrue(repo.snapshots(id).stream().anyMatch(s -> s.estado().equals(expected)));
        }
        try (var c = DriverManager.getConnection("jdbc:sqlite:"+db); var st = c.createStatement();var r=st.executeQuery("PRAGMA user_version")) {
            assertEquals(1,r.getInt(1));
        }
    }
}
