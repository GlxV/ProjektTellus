package com.projekttellus;

import com.projekttellus.domain.*;
import com.projekttellus.events.*;
import com.projekttellus.simulation.ModeloPlanetario;
import java.nio.file.*;
import java.util.*;

/** Offline documentation generator. Historical scores below only reproduce HEAD 9c41cbf for comparison. */
public final class ScenarioReport {
    private ScenarioReport() { }
    public static void main(String[] args) throws Exception {
        var scenarios = new LinkedHashMap<String,EstadoPlanetario>();
        var earth = new TerraBuilder().build().estado();
        scenarios.put("Referência terrestre",earth);
        scenarios.put("Oceano 100%, V=93%, He=20%, 10 bilhões",new TerraBuilder().hidrosfera(new Hidrosfera(100,0))
                .biosfera(new Biosfera(93)).atmosfera(new Atmosfera(21,58.96,.04,20,1)).civilizacao(new Civilizacao(10_000_000_000L)).build().estado());
        scenarios.put("He 20%, pO₂/pCO₂ iguais",new TerraBuilder().atmosfera(new Atmosfera(21,58.96,.04,20,1)).build().estado());
        scenarios.put("Sem atmosfera",new TerraBuilder().atmosfera(new Atmosfera(21,78.96,.04,0,0)).build().estado());
        scenarios.put("CO₂ 50%, 1 atm",new TerraBuilder().atmosfera(new Atmosfera(21,29,50,0,1)).build().estado());
        scenarios.put("Pressão 5 atm",new TerraBuilder().atmosfera(new Atmosfera(21,78.96,.04,0,5)).build().estado());
        scenarios.put("O₂ 2%, 1 atm",new TerraBuilder().atmosfera(new Atmosfera(2,97.96,.04,0,1)).build().estado());
        scenarios.put("Temperatura −150 °C",new TerraBuilder().clima(new Clima(-150,0,0)).build().estado());
        scenarios.put("Temperatura 200 °C",new TerraBuilder().clima(new Clima(200,0,0)).build().estado());
        scenarios.put("Vegetação terrestre 0%",new TerraBuilder().biosfera(new Biosfera(0)).build().estado());
        scenarios.put("População 80 bilhões",new TerraBuilder().civilizacao(new Civilizacao(80_000_000_000L)).build().estado());
        scenarios.put("Sem água nem gelo",new TerraBuilder().hidrosfera(new Hidrosfera(0,0)).build().estado());
        scenarios.put("Supervulcão 100%",new SupervulcaoCreator().criar(1).aplicar(earth));
        scenarios.put("Restauração após guerra nuclear 100%",new RestauracaoEcologicaCreator().criar(1).aplicar(new GuerraNuclearCreator().criar(1).aplicar(earth)));
        StringBuilder report = new StringBuilder("""
                # Comparativos reproduzíveis — revisão planetária

                Gerado por `src/test/java/com/projekttellus/ScenarioReport.java`. Todos os cenários partem dos defaults, salvo parâmetros explicitados. Antes = fórmulas de `9c41cbf`; depois = motor atual. H/B/C são percentuais, K/população em bilhões de pessoas. A população após 100 anos usa apenas o motor novo, incluindo recuperação e alterações do clima/ecologia.

                | Cenário | H antes | H depois | B antes | B depois | C novo | K novo (bi) | Pop. inicial (bi) | Pop. +100 anos (bi) |
                |---|---:|---:|---:|---:|---:|---:|---:|---:|
                """);
        for(var entry:scenarios.entrySet()) {
            var s=entry.getValue();var i=ModeloPlanetario.calcular(s);var next=ModeloPlanetario.avancar(s,100);
            report.append(String.format(Locale.ROOT,"| %s | %.2f | %.2f | %.2f | %.2f | %.2f | %.3f | %.3f | %.3f |%n",entry.getKey(),oldHuman(s),i.humana(),oldBio(s),i.biologica(),i.sustentabilidade(),i.capacidadeSuporte()/1e9,s.civilizacao().populacao()/1e9,next.civilizacao().populacao()/1e9));
        }
        report.append("""

                O oceano conserva alta adequação respiratória/térmica porque esses parâmetros são favoráveis. Sua civilização recebe C=0 e K=0 por não haver terra; a população declina com o tempo. Não se corrige superlotação reduzindo arbitrariamente o indicador de sobrevivência. V=93 corresponde a zero área vegetada quando não há continente.

                He=20% não reduz H se pO₂/pCO₂/pressão total forem iguais; sua aparência muda pelas propriedades ópticas. B admite ausência de O₂, mas não dá pontuação perfeita a vácuo ou extremos térmicos. A Terra de referência tem H/B=100 por ser o ponto de calibração educacional; C depende de produtividade e suporte.

                O caso observado pelo usuário citava H≈98,9%, mas não especificava todos os parâmetros climáticos. O cenário reproduzível acima usa T=15 °C e índices A=R=0, resultando H antigo=100%; não é uma reconstrução exata daquela captura.

                **Limites:** números são escolhas do simulador, não previsões científicas ou capacidade real da Terra. Não há comparação de trajetória antiga porque o motor antigo não tinha suporte populacional. O código histórico deste gerador serve somente à documentação, não integra o aplicativo nem constitui outro motor de simulação.

                Reproduzir com JDK 25, depois de `mvn test-compile`:

                ```powershell
                java -cp 'target/classes;target/test-classes' com.projekttellus.ScenarioReport docs/COMPARATIVOS_REFORMULACAO.md
                ```
                """);
        Files.writeString(Path.of(args[0]),report);
    }
    private static double comfort(double x,double ideal,double tolerance){return Math.clamp(1-Math.abs(x-ideal)/tolerance,0,1);}
    private static double oldHuman(EstadoPlanetario s){var a=s.atmosfera();var c=s.clima();return 100*comfort(a.pressaoParcialOxigenio(),.21,.16)*comfort(a.pressao(),1,1)*comfort(c.temperatura(),15,55)*Math.clamp(s.hidrosfera().agua()/30,0,1)/(1+250*Math.max(0,a.pressaoParcialCo2()-.0004))*(1-c.radiacao()/100)*(1-.6*c.aerossol()/100);}
    private static double oldBio(EstadoPlanetario s){return 100*comfort(s.clima().temperatura(),20,100)*Math.clamp(s.hidrosfera().agua()/30,0,1)*Math.clamp(s.atmosfera().pressao()/.2,0,1)*Math.sqrt(1-s.clima().radiacao()/100)*(.7+.3*s.biosfera().vegetacao()/100);}
}
