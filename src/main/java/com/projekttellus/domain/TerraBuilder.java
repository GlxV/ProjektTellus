package com.projekttellus.domain;
import java.util.UUID;

/** Builder de cenários: defaults consistentes e parâmetros opcionais validados no domínio. */
public final class TerraBuilder {
    private String nome = "Nova Terra";
    private Atmosfera atmosfera = new Atmosfera(21, 78.96, .04, 0, 1);
    private Clima clima = new Clima(15, 0, 0);
    private Biosfera biosfera = new Biosfera(65);
    private Hidrosfera hidrosfera = new Hidrosfera(71, 3);
    private Civilizacao civilizacao = new Civilizacao(8_000_000_000L);
    private double anos;
    public TerraBuilder nome(String valor) { nome = valor; return this; }
    public TerraBuilder atmosfera(Atmosfera valor) { atmosfera = valor; return this; }
    public TerraBuilder clima(Clima valor) { clima = valor; return this; }
    public TerraBuilder biosfera(Biosfera valor) { biosfera = valor; return this; }
    public TerraBuilder hidrosfera(Hidrosfera valor) { hidrosfera = valor; return this; }
    public TerraBuilder civilizacao(Civilizacao valor) { civilizacao = valor; return this; }
    public TerraBuilder anos(double valor) { anos = valor; return this; }
    public Terra build() {
        return new Terra(UUID.randomUUID(), nome,
                new EstadoPlanetario(atmosfera, clima, biosfera, hidrosfera, civilizacao, anos));
    }
}
