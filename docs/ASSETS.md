# Assets visuais

O globo usa uma textura equiretangular original gerada em tempo de execução por
`ui/PlanetView.java`, com Canvas/WritableImage. As silhuetas dos continentes são
desenhos estilizados produzidos para este projeto; não são mapas geográficos
precisos. Não há imagens, texturas, fontes ou outros recursos de terceiros
baixados ou incorporados pela interface.

O algoritmo altera qualitativamente a cobertura de água/gelo, a cor da vegetação,
oceanos e temperatura conforme o estado real do simulador. Aerossóis e gases atuam na camada atmosférica; radiação afeta ecologia/indicadores, sem pintar a superfície de roxo.
As cores e áreas não representam medições científicas nem cartografia validada.
A esfera, a iluminação, a rotação e o zoom são recursos nativos do JavaFX.

Autoria: implementação original do ProjektTellus, gerada com assistência de IA.
Licença desta textura procedural: CC0 1.0 Universal; disponibilizada para uso,
modificação e redistribuição sem exigência de atribuição.

A atmosfera usa desenho vetorial original e propriedades descritas em [ATMOSFERA.md](ATMOSFERA.md). Não atribui cores próprias a O₂, N₂, CO₂ ou He.
