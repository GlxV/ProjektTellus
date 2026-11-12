# Atmosfera dinâmica — visualização estilizada

O₂, N₂, CO₂ e He não recebem cores próprias. A aparência aproxima a preferência de espalhamento molecular por comprimentos de onda curtos e a atenuação/espalhamento por uma população genérica de poeira e cinzas. É uma visualização artística intensificada, **não um simulador espectral ou medida de visibilidade**.

## Propriedades derivadas

`AtmosphereOptics.from(EstadoPlanetario)` depende exclusivamente do estado salvo. Frações dos gases são percentuais/100; P em atm; T em °C; A é índice abstrato [0,100].

- Massa molecular média aproximada: `M = 32fO₂ + 28fN₂ + 44fCO₂ + 4fHe`.
- Peso de espalhamento: `s = 0,85fO₂ + fN₂ + 2,5fCO₂ + 0,014fHe`.
- Coluna relativa: `N = P × 28,8/M`, assumindo gravidade de referência fixa.
- Escala vertical relativa: `Z = ((T+273,15)/288,15) × 28,8/M`.
- Profundidade molecular visual: `τR = 0,12Ns`.
- Profundidade de partículas visual: `τP = 1,8(A/100)P/(P+0,05)`.
- Espessura relativa estilizada: `d=clamp(0,022+0,018ln(1+P)+0,028ln(1+Z),0,02,0,16)`.
- Transmissão relativa: `exp(−τR−τP)`; opacidade-base do halo: `1−exp(−3τR−0,65τP)`.

**Os pesos, profundidades, limites e amplificações são escolhidos para legibilidade; não são seções de choque medidas.** A massa média afeta tanto coluna quanto espessura, portanto substituir N₂ por He não é apenas reduzir um canal de cor. Misturas com He espalham menos e podem ter camada mais extensa; CO₂ aumenta a dispersão aproximada. A temperatura altera a extensão vertical, sem modificar arbitrariamente a cor do gás. A pressão quase nula reduz continuamente a opacidade; P=0 retorna propriedades exatamente nulas e transmissão=1, mesmo se um save ainda possuir A>0.

## Renderização

1. A Sphere e o PhongMaterial existentes mantêm superfície, rotação, luz e zoom.
2. Um Canvas transparente apresenta o halo projetado na câmera paralela. São até 72 anéis de traço fino com gradientes contínuos de iluminação: caminho óptico visual mais forte no limbo, queda suave externa e intensidade angular voltada à mesma PointLight do globo. A luz desligada usa exposição uniforme, acompanhando o modo de inspeção existente.
3. Uma camada frontal de névoa cobre parcialmente o disco, conforme a transmissão. Azul é uma aproximação de Rayleigh; cinza/ocre é a poeira genérica, sem inferir mineralogia ou poluição específica. Espalhamento múltiplo é representado apenas por saturação de opacidade e dessaturação por partículas.
4. O centro e raio vêm do mesmo centro/Scale da esfera. A camada acompanha tamanho, zoom e tremor. Rotacionar um globo de composição uniforme não move uma névoa pintada sobre continentes; a geometria do limbo e a direção da iluminação permanecem coerentes com o observador.
5. O Canvas de clarões e a geometria 3D de eventos são independentes. A transição interpola τR/τP/d dos endpoints confirmados pelo progresso existente. Pular/concluir usa exatamente as propriedades finais, sem poeira visual residual não salva.

Não há luz solar espectral, refração, nuvens meteorológicas, absorção por bandas, cálculo de altitude real, sombras volumétricas ou ray marching. Gravidade e raio planetário não são parâmetros editáveis. O fundo do observatório é decorativo; não representa atmosfera no vácuo.

## Superfície e custo

A linha d’água é obtida de um histograma de relevo procedural ponderado por `sin(colatitude)`, excluindo a região glacial. Gelo ocupa calotas com `abs(sin(latitude)) ≥ 1−fraçãoGlacial`; a cobertura líquida é distribuída sobre o restante. A discretização de 768×384 e 2.048 faixas de relevo produz pequenos erros de área; não é cartografia. Vegetação altera somente solo não glacial e não oceânico.

Cache LRU por condições superficiais: seis buffers de 768×384×4 bytes, cerca de 6,75 MiB por globo. Pressão, gases e aerossóis modificam apenas propriedades/camada óptica. População e ano não regeneram texturas. Animações preparam endpoints no executor de texturas e misturam buffers, com uploads limitados a ~60 Hz. Não há regeneração de relevo ou texturas procedurais por frame. Nenhum novo motor ou dependência gráfica foi adicionado. Na abertura/carregamento sem textura em cache há uma preparação pontual; não é uma garantia de FPS para qualquer GPU.

## Referências conceituais

- [NASA — Wave Behaviors](https://science.nasa.gov/ems/03_behaviors/): espalhamento preferencial de comprimentos curtos por moléculas e observação de aerossóis.
- [NASA — Rayleigh scattering in exoplanet atmospheres](https://science.nasa.gov/resource/rayleigh-scattering-in-exoplanet-atmospheres/): diferença entre espalhamento azul e vermelho numa atmosfera estendida.
- [NASA Planetary Spectrum Generator — Atmospheres](https://psg.gsfc.nasa.gov/helpatm.php): processos moleculares e por partículas; esse solver físico **não** é implementado aqui.
- [JavaFX 25 — Canvas](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/canvas/Canvas.html), [RadialGradient](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/paint/RadialGradient.html), [ParallelCamera](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/ParallelCamera.html).

As referências sustentam os conceitos; não validam os coeficientes artísticos nem os escores ambientais do ProjektTellus.
