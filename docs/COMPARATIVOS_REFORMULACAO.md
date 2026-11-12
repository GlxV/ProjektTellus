# Comparativos reproduzíveis — revisão planetária

Gerado por `src/test/java/com/projekttellus/ScenarioReport.java`. Todos os cenários partem dos defaults, salvo parâmetros explicitados. Antes = fórmulas de `9c41cbf`; depois = motor atual. H/B/C são percentuais, K/população em bilhões de pessoas. A população após 100 anos usa apenas o motor novo, incluindo recuperação e alterações do clima/ecologia.

| Cenário | H antes | H depois | B antes | B depois | C novo | K novo (bi) | Pop. inicial (bi) | Pop. +100 anos (bi) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Referência terrestre | 100.00 | 100.00 | 85.03 | 100.00 | 85.88 | 13.740 | 8.000 | 12.030 |
| Oceano 100%, V=93%, He=20%, 10 bilhões | 100.00 | 100.00 | 93.00 | 98.81 | 0.00 | 0.000 | 10.000 | 0.000 |
| He 20%, pO₂/pCO₂ iguais | 100.00 | 100.00 | 85.03 | 100.00 | 85.88 | 13.740 | 8.000 | 12.030 |
| Sem atmosfera | 0.00 | 0.00 | 0.00 | 0.00 | 0.00 | 0.000 | 8.000 | 0.000 |
| CO₂ 50%, 1 atm | 0.79 | 0.00 | 85.03 | 61.29 | 0.00 | 0.000 | 8.000 | 0.000 |
| Pressão 5 atm | 0.00 | 0.00 | 85.03 | 80.09 | 0.00 | 0.000 | 8.000 | 0.000 |
| O₂ 2%, 1 atm | 0.00 | 0.00 | 85.03 | 100.00 | 0.00 | 0.000 | 8.000 | 0.000 |
| Temperatura −150 °C | 0.00 | 0.00 | 0.00 | 0.00 | 0.00 | 0.000 | 8.000 | 0.093 |
| Temperatura 200 °C | 0.00 | 0.00 | 0.00 | 0.00 | 0.00 | 0.000 | 8.000 | 0.056 |
| Vegetação terrestre 0% | 100.00 | 100.00 | 66.50 | 100.00 | 50.00 | 8.000 | 8.000 | 11.345 |
| População 80 bilhões | 100.00 | 100.00 | 85.03 | 100.00 | 14.75 | 13.740 | 80.000 | 15.731 |
| Sem água nem gelo | 0.00 | 0.00 | 0.00 | 0.00 | 0.00 | 0.000 | 8.000 | 0.000 |
| Supervulcão 100% | 14.46 | 4.93 | 60.66 | 27.38 | 0.09 | 0.283 | 5.600 | 3.004 |
| Restauração após guerra nuclear 100% | 43.24 | 44.22 | 58.76 | 56.94 | 26.72 | 4.275 | 0.800 | 2.051 |

O oceano conserva alta adequação respiratória/térmica porque esses parâmetros são favoráveis. Sua civilização recebe C=0 e K=0 por não haver terra; a população declina com o tempo. Não se corrige superlotação reduzindo arbitrariamente o indicador de sobrevivência. V=93 corresponde a zero área vegetada quando não há continente.

He=20% não reduz H se pO₂/pCO₂/pressão total forem iguais; sua aparência muda pelas propriedades ópticas. B admite ausência de O₂, mas não dá pontuação perfeita a vácuo ou extremos térmicos. A Terra de referência tem H/B=100 por ser o ponto de calibração educacional; C depende de produtividade e suporte.

O caso observado pelo usuário citava H≈98,9%, mas não especificava todos os parâmetros climáticos. O cenário reproduzível acima usa T=15 °C e índices A=R=0, resultando H antigo=100%; não é uma reconstrução exata daquela captura.

**Limites:** números são escolhas do simulador, não previsões científicas ou capacidade real da Terra. Não há comparação de trajetória antiga porque o motor antigo não tinha suporte populacional. O código histórico deste gerador serve somente à documentação, não integra o aplicativo nem constitui outro motor de simulação.

Reproduzir com JDK 25, depois de `mvn test-compile`:

```powershell
java -cp 'target/classes;target/test-classes' com.projekttellus.ScenarioReport docs/COMPARATIVOS_REFORMULACAO.md
```
