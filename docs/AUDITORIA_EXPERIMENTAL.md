# Auditoria experimental real do motor — 15/11/2026

Execução canônica: [`auditoria/experimental-20261115-final/`](auditoria/experimental-20261115-final/). **2.026 experimentos, 4.125 snapshots de estado, 252.628 checks executados e zero checks falhos.** Isso verifica os contratos exercitados; não certifica a validade científica do modelo. Há um efeito numérico reproduzido de populações pequenas que deixam de crescer/morrer, mesmo com ambiente extremo. O motor e os testes preexistentes foram preservados. Os materiais e harnesses novos integram o commit de entrega; a produção auditada permanece igual ao commit base.

Fonte executada: produção do commit `3e153f07d8e7c5a6d65975afe5b06984be9f576f`, previamente compilada pelo responsável principal em `target/classes`; JDK Temurin **25.0.5** local; SQLite JDBC **3.50.3.0**. Data UTC da rodada: `2026-11-15T15:44:16.731081700Z`. Os hashes dos fontes/classes realmente usados constam de `input-hashes.csv`. Esta frente não iniciou Maven, JUnit ou JavaFX.

## Achados prioritários e classificação

| ID | Classificação | Resultado medido | Implicação e evidência |
|---|---|---|---|
| F01 | **Suspeita de incoerência** — patamar numérico reproduzido | Com H=K=0, populações iniciais grandes terminam em **2 pessoas**; no oceano H=100/K=0 terminam em **4 pessoas**. População inicial 1 permanece **1** após 1.000 anos tanto em terra favorável quanto em ambiente seco inabitável. | `Math.round` em cada subpasso cria pontos fixos inteiros. Não viola a fórmula documentada; impede interpretar a trajetória como mortalidade contínua até extinção ou crescimento contínuo de toda população positiva. Famílias `rounding_probe` e `step_rounding`; cenários `dry`, `vacuum`, `population1`, `ocean_legacy_he20_v93_pop10b`. |
| F02 | **Simplificação documentada** — integração temporal | `avancar(base,100)` = 100 chamadas de 1 ano **exatamente**. Já 100 chamadas de 0,1 ano versus uma de 10 anos: ΔT=**+0,004623520899571076 °C**, ΔV=**+0,005721544189000838 ponto**, Δpop=**−2.152.917 pessoas**, ΔK=**+1.112.091 pessoas**. | Alterar o tamanho do passo altera a integração e o arredondamento. **Não é não determinismo**. Experimentos 1599/1600, família `partition`. |
| F03 | **Comportamento esperado** | Caso oceânico com He20/V93/pop10 bilhões: H=**100**, B=**98,81422924901186**, C=K=área vegetada=**0**. Após 100 anos: **61.441 pessoas**; após 1.000: **4**. | A adequação humana não mede terra disponível. Suporte civilizacional é zero. O resíduo de 4 é o problema numérico F01, não suporte oculto. Experimento 1254. |
| F04 | **Comportamento esperado** | Trocar N₂ por He de 0 até 78,96 mantendo O₂=21%, CO₂=0,04%, P=1 preserva H=**100**, B=**100**, C=**85,87782018658834** e K=**13.740.451.230** em todos os 80 pontos. | Óptica muda: τR **0,1161049420 → 0,0665476967**, espessura **0,0538622420 → 0,0726576061**, transmissão **0,8903817799 → 0,9356182889**. He80/100 pertence à outra série, com O₂=CO₂=0. |
| F05 | **Simplificação documentada** — biologia hipotética | Atmosferas puras He, N₂ e O₂, a 1 atm, convergem em 1.000 anos a B≈**99,96918747**, embora H=K=0. CO₂ puro chega a B≈**99,87636229**. | B representa potencial hipotético pela fórmula do motor, sem bioquímica, nutrição, necessidade de carbono ou química de gases real. Esses números não demonstram vida real nem surgimento de organismos. Experimentos 1261–1264. |
| F06 | **Simplificação documentada** — clima global | Terra de referência: T **15 → 15,54 °C**, gelo **3 → 3,34×10⁻²⁹%**, água **71 → 74%**, V **65 → aproximadamente 100%**, população **8 bilhões → 15.999.895.705**, em 1.000 anos. | Não há equilíbrio terrestre calibrado. A referência tem alvo inicial 15 °C, mas o degelo modifica o termo glacial e o alvo. Reserva hídrica 74 e continente 26 são preservados. Experimento 1253. |
| F07 | **Simplificação documentada** — condição inicial sem equilíbrio glacial | Cenário frio coerente T=−50, água=5, gelo=69, V=5: H=**0**, B=**1,6986003533** no início; T=**14,2179754523** e gelo=**0,5666492337** após 100 anos; T≈**15,54**, gelo≈**4,96×10⁻²⁷** após 1.000. | É condição inicial congelada, **não** equilíbrio glacial estável. A atmosfera terrestre e o alvo fixado pelo motor levam ao degelo. Experimento 1288. |
| F08 | **Comportamento esperado** — com simplificação | Restauração100 sobre Terra inicial aumenta C **85,87782019 → 92,36156141**, mas reduz ligeiramente B **100 → 99,70089731**. | Derreter 0,75 ponto de gelo altera o alvo em 0,135 °C enquanto T ainda fica 15; o fator de estabilidade reduz B. Portanto não se deve exigir aumento monotônico de todos os indicadores em toda restauração. Experimento de evento `earth/RestauracaoEcologicaCreator`, intensidade 1, horizonte 0. |
| F09 | **Comportamento esperado** | 36 ciclos Memento/SQLite: restauração exata de estado, indicadores e óptica; 36 rejeições de Memento de outro mundo; 36 falhas SQL provocadas com rollback preservado. | Banco criado exclusivamente dentro do diretório da rodada e removido ao final. Uma base pessoal em `data` teve o mesmo SHA-256 antes/depois. Não é certificação exaustiva da persistência nem da interface. |

Não houve divergência comprovada entre implementação e os oráculos algébricos documentados exercitados. **“Zero checks falhos” não elimina F01 nem transforma simplificações em ciência.** F01 é um candidato a defeito comportamental do modelo. O usuário autorizou correções pequenas comprovadas; nesta entrega foi preservado o comportamento auditado, pois escolher entre acumulador fracionário, política de extinção ou aproximação estocástica muda a evolução e merece uma regressão comportamental específica. A recomendação está registrada sem uma mudança silenciosa do motor. F02 já é compatível com a ressalva de integração de `MODELO.md`.

### Reprodução e explicação de F01

No código [`ModeloPlanetario.java`](../src/main/java/com/projekttellus/simulation/ModeloPlanetario.java), o cálculo demográfico termina com `Math.round(p * exp(taxa * dt))`. Com K=0/H=0, a taxa é −0,22 por ano; `round(2×exp(−0,22))=2`. Com K=0/H=1, a taxa é −0,12; `round(4×exp(−0,12))=4`. Na Terra favorável com população muito menor que K, a taxa é próxima de +0,012; `round(41×exp(0,012))=41`, mas população 42 avança para 43 no primeiro ano. Os experimentos confirmam esses valores usando o motor real, sem substituir a evolução por uma fórmula de teste.

Para mortalidade fixa e passo anual, os resíduos de 2 e 4 decorrem dos limiares `0,5/(1−exp(−0,22))≈2,53` e `0,5/(1−exp(−0,12))≈4,42`. Os 33 experimentos `rounding_probe` cobrem Terra, ambiente seco e oceano × populações 0/1/2/3/4/5/10/41/42/43/100 × horizontes 0/1/10/100/1000. Há outros 21 experimentos de um passo para isolar a primeira atualização. Zero continua zero em todos os casos pertinentes; não houve criação espontânea de população.

Em F02, a trajetória com dez passos de 1 ano termina em **8.409.886.360** pessoas; com cem passos de 0,1 ano termina em **8.407.733.443** (−0,025600%) e relógio `9,99999999999998` por soma de doubles. O desvio de relógio é −1,95399×10⁻¹⁴ ano. Cada chamada repetida com a mesma entrada/parâmetros retornou estado exatamente igual. Não se exige igualdade entre algoritmos com discretizações distintas.

## Reproduzir sem competir com Maven

Após o responsável principal confirmar `target/classes` pronto, na raiz do projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Run-ExperimentalAudit.ps1
```

Ou fornecer nome **novo**:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Run-ExperimentalAudit.ps1 -RunName experimental-minha-repeticao
```

O script usa [`Tools.ps1`](../scripts/Tools.ps1)/`Find-TellusJava` para descobrir Java 25, encontra `javac` no mesmo JDK, compila **apenas** [`ExperimentalAudit.java`](../src/test/java/com/projekttellus/ExperimentalAudit.java) com `-encoding UTF-8 -cp target/classes`, e executa com classes do audit + `target/classes` + SQLite JDBC local (fat jar anterior como fallback). Não executa Maven nem instala ferramentas. Recusa reaproveitar diretório de evidências. O `main` recusa saída fora de um subdiretório de `docs/auditoria`.

Saída não zero significa falha de compilação/execução ou check falho; conferir `execution.json`, `execution.log`, `checks.csv.gz` e `checks-summary.csv`. O diretório canônico não deve ser sobrescrito. Os rascunhos r1/r2/r3 foram movidos para `target/audit-drafts`, que já é ignorado pelo Git.

A repetição independente do processo em `experimental-20261115-reproduce`, com o runner já usando descoberta do Java, repetiu **todas as contagens**, e produziu arquivos idênticos por SHA-256: snapshots, fatores gzip, resumo de checks e rejeições. Essa rodada de verificação foi arquivada em `target/audit-drafts`; sua prova compacta está em [`reproducibility.json`](auditoria/experimental-20261115-final/reproducibility.json). A compressão Java escreve gzip **mtime=0**, confirmado nos headers. Os checks brutos de Memento incluem UUID/Instant e não são arquivos determinísticos byte a byte; comparar o resumo agregado. `summary.json` possui timestamp e deve ser comparado excluindo metadados temporais.

SHA-256 canônico de `snapshots.csv`: `817efbb92ec179d34ec9af8198de5d7bee087f8f30597a6c6dcb7f166e20338f`.

## Contagem correta e inventário de evidências

Um **experimento** é uma entrada controlada/combinação de cenário ou evento, ou uma operação de rejeição/comparação persistente explicitamente indexada. Uma trajetória de cinco horizontes continua sendo um experimento e produz cinco snapshots. Reexecuções de determinismo contam como checks, não como novos experimentos; o total tampouco é multiplicado pela repetição do processo. Um **snapshot** aqui é uma linha antes/depois no CSV; não é um Memento salvo. Há 36 Mementos efetivamente persistidos, distintos dos 4.125 snapshots do relatório.

| Grupo de experimentos | Quantidade | Snapshots |
|---|---:|---:|
| Varreduras amplas (14 famílias `sweep_*`) | 1.244 | 1.244 |
| Valores exatos complementares `request_*` | 8 | 8 |
| Cenários nomeados | 36 | 180 |
| Eventos: 5 × 8 bases × 6 intensidades | 240 | 1.200 |
| Estados pseudoaleatórios, seed fixa 20261115 | 250 | 1.250 |
| Rejeições (65 domínio/evento + 72 Memento/rollback) | 137 | 0 |
| Avanço fracionário | 5 | 5 |
| Comparações de partição | 2 | 2 |
| Normalização dentro de tolerância | 6 | 6 |
| Populações pequenas, trajetórias | 33 | 165 |
| Sensibilidade controlada | 8 | 8 |
| Populações pequenas, um passo | 21 | 21 |
| Memento/SQLite temporário, reabrir/restaurar | 36 | 36 |
| **Total** | **2.026** | **4.125** |

Os checks somam **252.628**, incluindo 37 verificações de finitude por snapshot. São verificações repetidas por entrada, não 252.628 hipóteses independentes. Os fatores somam **156.750 linhas**: (7 humanos + 6 biológicos + 6 de civilização) × 2 lados × 4.125 snapshots. As 137 rejeições esperadas passaram; elas não são falhas do motor.

| Arquivo canônico | Conteúdo |
|---|---|
| [`snapshots.csv`](auditoria/experimental-20261115-final/snapshots.csv) | 4.125 linhas, 80 colunas; aproximadamente 3,64 MB decimais. Identificador, família, caso, operação, intensidade, horizonte e 37 valores numéricos antes/depois. |
| [`checks.csv.gz`](auditoria/experimental-20261115-final/checks.csv.gz) | Todos os 252.628 resultados, atual/esperado/tolerância; aproximadamente 2,72 MB comprimidos. |
| [`checks-summary.csv`](auditoria/experimental-20261115-final/checks-summary.csv) | Contagens/falhas por regra e máximo erro absoluto quando a comparação é numérica. |
| [`factors.csv.gz`](auditoria/experimental-20261115-final/factors.csv.gz) | Todos os valores/unidades/adequações dos 19 fatores antes/depois; aproximadamente 1,01 MB. |
| [`rejections.csv`](auditoria/experimental-20261115-final/rejections.csv) | Entrada inválida/operação provocada, tipo de exceção esperado/recebido e resultado. |
| [`experiment-index.csv`](auditoria/experimental-20261115-final/experiment-index.csv) | 2.026 identificadores únicos consecutivos; família, entrada, número de snapshots e horizontes. |
| [`exact-request-coverage.csv`](auditoria/experimental-20261115-final/exact-request-coverage.csv) | Mapeamento explícito dos valores exatos de cobertura para seus experimentos. |
| [`summary.json`](auditoria/experimental-20261115-final/summary.json) | Contagens do executável e categorias. |
| `input-hashes.csv`, `output-hashes.csv`, `execution.json` | Proveniência do fonte/classes, hashes dos artefatos da execução e comando/runtime/exit code. O manifesto original precede os índices derivados escritos para este relatório. |
| `personal-db-before.csv`, `personal-db-after.csv` | Hash idêntico de uma base pessoal; somente leitura. |

As colunas numéricas incluem todos os 13 campos do estado, áreas continental/vegetada, reserva água+gelo, soma dos gases, pO₂/pCO₂, temperatura alvo, H/B/C/K, massa molecular, peso de espalhamento, coluna/escala vertical, τR/τP/espessura/transmissão/opacidade e H/B/C/K recalculados pelo oráculo algébrico. Unidades seguem `MODELO.md`/`ATMOSFERA.md`; população/K são pessoas, pressões em atm, gases/áreas/vegetação em %, temperatura em °C. Intensidade é a fração **0..1**, não a porcentagem da interface.

Em varreduras, o lado `before` é a Terra de referência e `after` é a entrada alterada. Em trajetórias normais, `before` é a condição inicial. Em eventos, `before` é o estado **antes do evento** em todos os horizontes; horizonte 0 é o estado imediatamente após o evento, e os demais são a evolução desse estado pós-evento. Para recuperar a condição pós-evento inicial, selecionar o mesmo `experiment` com `horizon_years=0`. Em `partition`, `before` e `after` são dois endpoints de integrações comparadas, e não um avanço adicional.

## Varreduras exatas e limites de comparação

Todos os demais parâmetros são os defaults (O₂/N₂/CO₂/He 21/78,96/0,04/0; P=1; T=15; A=R=0; água=71/gelo=3; V=65; população=8 bilhões; ano=0), salvo os campos nomeados na família/caso.

| Eixo | Valores efetivamente executados |
|---|---|
| O₂ com CO₂=0,04, He=0, N₂ residual | Todos os inteiros 0..99, mais **99,96**. A série binária O₂/N₂ também percorre todos os inteiros **0..100**, com CO₂=He=0. |
| CO₂ com O₂=21, He=0, N₂ residual | Inteiros **0..79**, mais **0,1** no experimento 1251 (`request_co2`). **0,04** é a entrada de referência compartilhada, experimento 1253/`earth`, e as séries He fixo. A série binária CO₂/N₂ percorre 0..100 com O₂=He=0. |
| He com O₂=21 e CO₂=0,04 fixos | Inteiros **0..78**, mais **78,96**; N₂ substitui He com soma 100. **He80/100 não cabem nessa composição.** |
| He binário | Todos os inteiros **0..100**, N₂=100−He e O₂=CO₂=0. Inclui **20/40/60/80/100**; não comparar H diretamente à série fixa como se o O₂ permanecesse igual. |
| Mistura ternária O₂/CO₂/He | Malha em passos de 5%, N₂=0, soma 100: **231 composições**. |
| Pressão | **0, 10⁻¹², 10⁻⁹, 0,001, 0,01, 0,06, 0,1, 0,15, 0,3, 0,5, 0,7, 1, 1,5, 2, 3, 5, 6, 10, 20**. O valor **0,1** está em `request_pressure`, experimento 1245. |
| Temperatura | **−150, −100, −60, −50, −40, −20, 0, 5, 10, 15, 25, 30, 45, 50, 65, 80, 100, 120, 130, 150, 200**. −100 está na varredura ampla; **−50/30/50/80/120** em `request_temperature`, experimentos 1246–1250. |
| Água/gelo | Inteiros **0..100** em três séries: água=x/gelo=0; água=0/gelo=x; água=100−x/gelo=x (reserva 100). As duas primeiras também alteram continente; a terceira conserva reserva e continente. |
| Vegetação | Todos os inteiros **0..100**, fração do continente disponível, não cobertura planetária adicional. |
| População | **0, 1, 10, 1.000, 1 milhão, 100 milhões, 1/8/10/16/50/100 bilhões**. 100 milhões está em `request_population`, experimento 1252. Os probes demográficos acrescentam 2/3/4/5/41/42/43/100 pessoas. |

P=0 conserva as frações no record apenas como **referência de composição**: não significa moléculas nem coluna atmosférica presentes. A óptica retorna τR=τP=espessura=opacidade=0 e transmissão=1; H=B=K=0. Água líquida persistir no vácuo é consequência da ausência de evaporação/vapor no modelo e não prova de plausibilidade física.

As varreduras são instantâneas para isolar a sensibilidade; os horizontes 0/1/10/100/1000 aplicam-se aos 36 cenários, 240 combinações de evento, 250 entradas pseudoaleatórias e 33 probes demográficos. Horizonte 0 é uma leitura, pois o domínio rejeita `avancar(...,0)`; essa rejeição também foi executada.

## Mapa dos cenários e eventos exigidos

Todos os 36 cenários abaixo têm cinco estados em 0/1/10/100/1000 anos. Este mapa deixa explícitos mais de vinte casos, inclusive os devastados: eles existem tanto como cenários nomeados quanto nas linhas de eventos. Definições completas em `ExperimentalAudit.scenarios()`; valores iniciais completos no CSV, sem inferir parâmetros pelo nome.

| Experimento | Caso no CSV | Condição que isola |
|---:|---|---|
| 1253 | `earth` | Terra de referência |
| 1254 | `ocean_legacy_he20_v93_pop10b` | Água100, V93 latente, He20, população10 bilhões |
| 1255 | `ocean` | Água100, sem continente |
| 1256 | `all_ice` | Água0/gelo100, T inicial15; teste de degelo, não estado frio em equilíbrio |
| 1257 | `dry` | Água=gelo=0 |
| 1258/1259/1260 | `vacuum` / `thin` / `pressure20` | P=0 / 0,01 / 20 |
| 1261/1262/1263/1264 | `pure_o2` / `pure_co2` / `pure_he` / `pure_n2` | Gases puros, P=1 |
| 1265/1266 | `anoxic` / `o2_2` | O₂=0 / 2 com CO₂=0,04 e N₂ residual |
| 1267/1268 | `co2_50` / `he20` | CO₂50 / troca N₂ por He20 |
| 1269/1270 | `cold_min` / `hot_max` | T=−150 / 200 |
| 1271/1272 | `radiation100` / `aerosol100` | Índices extremos separados |
| 1273/1274 | `vegetation0` / `vegetation100` | Cobertura terrestre mínima/máxima |
| 1275/1276/1277 | `population0` / `population1` / `population100b` | Sem população / unidade / superlotação |
| 1278/1279 | `land1` / `water1` | Continente1 / água1 |
| 1280 | `max_degraded` | CO₂100/P20/T200/A100/R100/água0/gelo100/V0/pop100 bilhões |
| 1281 | `cold_vacuum_dry` | He100/P0/T−150/A100/R100/água0/gelo0/V100/pop1 |
| 1282 | `mixed_extreme` | He100/P20/T−150/A100/R100/água100/gelo0/V100/pop100 bilhões |
| 1283/1284/1285/1286 | `asteroid100` / `supervolcano100` / `nuclear100` / `alien100` | Evento100 aplicado à Terra antes de iniciar a trajetória |
| 1287 | `restoration_after_nuclear100` | Guerra100 → restauração100 → evolução |
| 1288 | `frozen_cold_water5_ice69_v5` | T−50/água5/gelo69/V5; continente26 |

A matriz de eventos usa **Asteroide, Supervulcão, Guerra nuclear, Invasão alienígena e Restauração ecológica**, cada um com intensidades **0/1/25/50/75/100%** (frações 0/0,01/0,25/0,5/0,75/1), em **oito bases**: `earth`, `ocean_legacy_he20_v93_pop10b`, `cold_vacuum_dry`, `max_degraded`, `mixed_extreme`, `pure_o2`, `pure_co2`, `population0`. Cada uma das 240 entradas parte novamente da sua base, aplica um evento e evolui 1/10/100/1000 anos; não é acumulação de seis intensidades.

Há **40 casos de intensidade zero**, todos preservando o estado exatamente; eventos não avançaram relógio. Os efeitos imediatos dos 13 campos foram comparados aos coeficientes de `MODELO.md`, incluindo saturações, água disponível, transferência N₂/CO₂, O₂/He e arredondamento da população.

## Efeito imediato e recuperação dos eventos na Terra

Intensidade 100%; percentuais dos indicadores não são probabilidades. Populações abaixo são pessoas; o horizonte parte do instante pós-evento.

| Evento | H imediato | B imediato | C imediato | População imediata | H após 1.000 anos | População após 1.000 anos |
|---|---:|---:|---:|---:|---:|---:|
| Asteroide | 10,205080 | 30,651179 | 0,825164 | 2.000.000.000 | 86,206897 | 13.789.308.648 |
| Supervulcão | 4,934450 | 27,376955 | 0,089395 | 5.600.000.000 | 35,999882 | 5.653.215.257 |
| Guerra nuclear | 1,898421 | 13,129366 | 0,032886 | 800.000.000 | 93,360996 | 14.930.900.873 |
| Invasão alienígena | 0 | 50,307348 | 0 | 2.800.000.000 | 0 | 2 |
| Restauração | 100 | 99,700897 | 92,361561 | 8.000.000.000 | 100 | 15.999.901.342 |

Aerossóis/radiação decaem, mas o CO₂ alterado e o O₂ extraído não são revertidos automaticamente. Isso explica H ainda reduzido após mil anos, mesmo com poeira/radiação quase removidas. A invasão deixa pO₂=0,0588 atm, abaixo do limiar de H; recuperação de B/V não restaura O₂. A restauração não ressuscita população. Guerra100 seguida de restauração100 começa em H=44,2196833/B=56,9361727/C=26,7165957, ainda com 800 milhões de pessoas, e chega a H=98,875022 após mil anos.

## Contratos efetivamente verificados e alcance

* **Limites/finitude:** 4.125 estados avaliados, gases/pressão/clima/áreas/vegetação/população/tempo nos limites, indicadores [0,100], óptica finita e transmissão/opacidade [0,1]. O erro máximo da soma dos gases para 100 foi **9,9475983×10⁻¹⁴ ponto percentual**.
* **Conservação declarada:** 2.241 comparações de evolução conservaram atmosfera inteira, reserva de área água+gelo e continente. Erros máximos: água+gelo **1,4210855×10⁻¹⁴** e continente **2,8421709×10⁻¹⁴ ponto percentual**. As 240 aplicações imediatas de evento conservaram reserva hídrica, com erro observado zero. Não se verificou conservação de massa em kg: esse contrato não existe. Energia, população, radiação, aerossóis e gases individuais em eventos tampouco são invariantes conservados.
* **Determinismo:** 2.241 avanços, 240 eventos, 4.125 cálculos de indicadores e 4.125 cálculos ópticos repetidos com igualdade exata. Além disso, snapshots/fatores repetiram byte a byte em outro processo. IDs/timestamps de Memento são deliberadamente variáveis.
* **Cálculo explicável:** H/B/C/K e alvo térmico recalculados por avaliação independente das fórmulas documentadas; erros máximos H **1,4211×10⁻¹⁴**, B/C **2,8422×10⁻¹⁴**, K **0 pessoas**. As listas de fatores recombinam seus produtos; óptica foi confrontada à fórmula de `ATMOSFERA.md`. Esses oráculos verificam aderência algébrica, não calibração científica independente.
* **Sensibilidade:** oito diferenças controladas, além das varreduras: vegetação↑ aumenta K/C preservando H/B; população↑ preserva H/B/K e reduz C; temperatura16 altera espessura e estabilidade; aerossol↑ aumenta partículas e reduz transmissão/H/B; radiação↑ reduz H/B sem alterar óptica; CO₂↑ piora H e aumenta espalhamento; anoxia zera H/K/C preservando B na base; troca água/gelo a reserva fixa altera estabilidade sem mudar óptica. Os sinais realmente testados constam das regras `sensitivity_*`, sem impor monotonicidade universal.
* **Rejeições:** NaN/infinito e limites inválidos para O₂, pressão, temperatura, água, vegetação, A/R, população, soma gasosa/hídrica; avanço zero/menor que 0,01/maior que 1000 e overflow do relógio; cinco eventos com intensidade inválida; Memento estrangeiro; conflito SQL provocado. Não é enumeração exaustiva de todas as combinações inválidas ou valores nulos de todas as APIs.
* **Memento/persistência:** 36 ciclos com estados extremos e eventos; captura, mutação de estado/nome, restauração; snapshot persistido isolado de mutação posterior; fechamento/reabertura; restauração e indicadores/óptica exatos; tentativa de gravar estado junto com evento duplicado falha e conserva estado/histórico anteriores. UUIDs/instantes dos retratos foram gerados pelo mecanismo real, não simulados.

Não foram validados nesta frente renderização/cores perceptuais, animação, cliques JavaFX, layout, desempenho GPU/FPS, implantação ou computador físico da faculdade. `AtmosphereOptics` foi executado como cálculo puro, sem abrir toolkit gráfico. Não houve comparação com medidas planetárias, solver físico independente ou ajuste dos coeficientes: o sistema permanece educacional, global, determinístico e ficcional, conforme seus próprios documentos.


## Integração com a apresentação e evidência visual

- [Sete gráficos reais](graficos/manifest.json), gerados por `scripts/Plot-ExperimentalAudit.py`, leem o CSV canônico. PNG e SVG acompanham as fontes; não usam dados ilustrativos.
- [Capturas canônicas](capturas/latest.json): 33 PNGs, 249 verificações do harness JavaFX, cinco quadros intermediários do animador real e capturas de cenários. [Demo](DEMO_APRESENTACAO.md) vincula condições/indicadores às imagens.
- Óptica numérica foi registrada nos 4.125 estados; **não houve inspeção visual humana de cada linha do CSV**. Imagens de cenários selecionados mostram renderizações reais. Os globos isolados são snapshots de PlanetView auxiliar alimentado com o estado confirmado pela interface; os quadros intermediários usam relógio controlado e não medem FPS.
- Maven final: [verify-final.txt](evidencias/verify-final.txt), 69 testes, zero falhas/erros/skips. As 252.628 checagens experimentais e as 249 verificações gráficas não são contadas como testes JUnit.
- Os fontes de produção permanecem iguais ao commit base. Este trabalho acrescenta experimentos, ferramentas e materiais; o patamar demográfico foi registrado para correção futura, sem modificar silenciosamente o motor.
