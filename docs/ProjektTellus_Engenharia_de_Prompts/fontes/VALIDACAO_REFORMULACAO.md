# Entrega e validação da reformulação — 11/11/2026

## Resultado

Implementados três indicadores independentes e explicáveis, capacidade de suporte e população com equilíbrio coerente, geografia derivada, evolução ecológica/climática e atmosfera responsiva a gases/pressão/temperatura/partículas. O motor permanece único, com domínio imutável e os quatro padrões preservados. Eventos não mudaram suas regras; animações apresentam apenas os estados confirmados.

O caso oceânico conserva adequação humana ambiental se respiração/clima forem favoráveis, mas recebe **suporte zero e sustentabilidade zero**. Vegetação terrestre de 93% em um save oceânico corresponde a zero área vegetada, sem truncar seu valor salvo. Trocar N₂ por He mantendo pO₂/pCO₂/pressão total não pune a sobrevivência. A aparência muda por propriedades ópticas, sem uma cor própria para cada gás.

## Verificação executada

- Baseline `9c41cbf`: `mvn -B -ntp -Pgui-tests verify` — **57 testes, 0 falhas/erros/skips**.
- Entrega: mesmo comando — **69 testes, 0 falhas/erros/skips, BUILD SUCCESS**. Log integral em [verify.txt](evidencias-reformulacao/verify.txt).
- Testes primeiro falharam por ausência dos novos contratos; após implementação passaram. Uma regressão demográfica descoberta na revisão independente também foi reproduzida por teste que falhou e depois passou.
- `PlanetaryModelTest`: oceano, He, pressão parcial/total, CO₂, temperaturas extremas, suporte/superlotação, recuperação, evolução fracionária/determinística, conservação de água/gelo/continente, fatores reais e limites numéricos. A matriz extrema aplica os cinco eventos máximos a 36 combinações e evolui cada resultado por 1.000 anos: 180 trajetórias, além dos cenários individuais.
- `LegacyPlanetTest`: fixture V001 criada diretamente via SQL, campos antigos inalterados, restauração/reabertura, nome, indicadores e óptica. Schema e versão continuam V001; nenhuma migration ou conversão de dados é necessária.
- `AtmosphereOpticsTest`: vácuo, atmosfera rarefeita/densa, composição com He/CO₂, temperatura, partículas, transmissão e endpoints de interpolação.
- `DesktopFlowTest`: **8 testes reais de desktop**, incluindo o fluxo anterior, cinco eventos a 100%, Memento, SQL com rollback induzido, callbacks obsoletos, substituição, cancelamento, pular, modos reduzido/desativado, textura/óptica finais e ausência de alterações duplicadas.
- Matriz de doze atmosferas/superfícies: cada cenário aplicado pelo editor e serviço reais; estado final e propriedades ópticas comparados com o modelo; capturas completas distintas; precisão de pCO₂ no painel explicativo conferida. Cache superficial compartilhado entre atmosferas com solo igual.
- Layout: janelas externas 1366×720 (área útil conservadora para tela 1366×768), 1920×1032, 1536×825 e 1000×560; rolagem dos controles/retratos/apresentação e popup nativo por mouse/teclado. Nesta execução, escala **1,00×1,00**, cena **1350×681**, `SCENE3D=true`. Não foi reexecutado um desktop físico diferente a 125% nesta entrega.

As exceções de SQL/observador no log são induzidas pelos testes de isolamento e rollback; o resumo final registra zero erros. Avisos de configuração JavaFX em unnamed module e recursos sobrepostos do Shade já estavam no baseline; não impediram a suíte nem o empacotamento.

## Inspeção gráfica

Foram abertas e inspecionadas as capturas da renderização real JavaFX 3D: referência, pressão densa, aerossóis, oceano/explicações, supervulcão e galeria ampliada. A galeria expôs linhas radiais no halo inicial; o desenho foi corrigido para iluminação contínua e a captura final foi reinspecionada. As demais animações têm suas capturas e assertivas na suíte.

![Doze atmosferas e superfícies renderizadas](evidencias-reformulacao/atmosphere-gallery.png)

- [Layout completo a 100%](evidencias-reformulacao/layout-1366-100.png)
- [Oceano e painel explicativo](evidencias-reformulacao/indicadores-oceano.png)
- [Supervulcão e cinzas](evidencias-reformulacao/supervulcao-cinzas.png)
- [Pulso do Memento](evidencias-reformulacao/memento-pulse.png)
- [Pulsos durante os cinco eventos](evidencias-reformulacao/pulse-timing.csv)

O host informa AMD Radeon(TM) Graphics e NVIDIA GeForce RTX 4060 Ti; a consulta não identifica qual adaptador o pipeline JavaFX escolheu. Os eventos mantiveram pulsos durante ~3,8 s, com captura de estados intermediários; o CSV inclui custo das próprias capturas. **Pulsos não são uma garantia de FPS**, nem certificação de desempenho em computadores acadêmicos mais lentos. O orçamento foi mantido limitado, sem engine adicional ou texturas procedurais por frame.

## Artefato

- `target/ProjektTellus.jar`, 24.888.792 bytes, gerado pelo `verify` completo em 11/11/2026, 21:26:59 (São Paulo).
- SHA-256: `2AE9AD6DC68C3D46EBE4F3FE5AD5AC77A8AA82EEAAD7B2BFC76CA34E78196935`.
- JAR com dependências para Windows x64; requer Java 25. O lançador existente continua disponível.
- Commit local solicitado; push/release remoto não fazem parte desta entrega. O commit que contém este relatório identifica a revisão entregue. Artefato em `target/` é gerado, não versionado.

## Revisão e limites

Uma revisão independente encontrou dupla penalização da habitabilidade na demografia: população podia declinar abaixo de K em ambiente estável. A equação foi corrigida e ganhou teste de equilíbrio em 1.000 anos. A revisão final não encontrou achados relevantes restantes nas fórmulas, compatibilidade ou integração inspecionadas. A documentação foi conferida contra o motor.

Modelo educacional sem validação científica; K não estima a população suportável real da Terra. Água superficial não distingue salinidade; reservatório água/gelo conserva área abstrata, não massa física. Sem evaporação, hidrologia regional, agricultura detalhada, tecnologia, migração ou evolução de espécies. Óptica estilizada, sem solver espectral, nuvens ou espalhamento múltiplo físico. Uma preparação pontual de textura ainda ocorre ao abrir um estado superficial sem cache; eventos preparam endpoints no executor dedicado. As medições deste host não substituem teste em outra GPU.

Detalhes: [modelo matemático](MODELO.md), [atmosfera](ATMOSFERA.md), [arquitetura](ARQUITETURA.md), [comparativos reproduzíveis](COMPARATIVOS_REFORMULACAO.md).

## Arquivos alterados

### Núcleo e integração

- `src/main/java/com/projekttellus/domain/Biosfera.java`, `EstadoPlanetario.java`: semântica e áreas derivadas.
- `src/main/java/com/projekttellus/simulation/ModeloPlanetario.java`, `Indicadores.java`, `FatorAmbiental.java`: fórmulas, suporte, evolução e explicações.
- `src/main/java/com/projekttellus/application/SimulacaoService.java`: passos didáticos com os três indicadores.
- `src/main/java/com/projekttellus/patterns/observer/AlertaObserver.java`: alertas de superlotação/área disponível.

### Interface e renderização

- `src/main/java/com/projekttellus/ui/AtmosphereOptics.java`, `PlanetView.java`, `PlanetEventAnimator.java`: óptica, camadas, cache, geografia visual e transições.
- `src/main/java/com/projekttellus/ui/IndicatorDetails.java`, `MainController.java`, `EnvironmentEditor.java`, `PresentationPane.java`: explicações, terceiro indicador, unidades e comparações.
- `src/main/resources/fxml/main.fxml`: métrica civilizacional, suporte e aba de indicadores com rolagem.

### Verificação e documentação

- `src/test/java/com/projekttellus/PlanetaryModelTest.java`, `ScenarioReport.java`.
- `src/test/java/com/projekttellus/persistence/LegacyPlanetTest.java`.
- `src/test/java/com/projekttellus/ui/AtmosphereOpticsTest.java`, `DesktopFlowTest.java`.
- `README.md`; `docs/MODELO.md`, `ATMOSFERA.md`, `ARQUITETURA.md`, `CONTRATOS.md`, `ASSETS.md`, `ANIMACOES.md`, `COMPARATIVOS_REFORMULACAO.md`, `PLANO_REFORMULACAO.md`, este relatório e `docs/evidencias-reformulacao/`.

Nenhuma dependência, migration ou regra dos cinco eventos foi alterada.
