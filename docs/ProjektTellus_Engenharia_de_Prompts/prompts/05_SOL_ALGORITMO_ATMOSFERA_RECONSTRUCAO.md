# Etapa 5 — Sol: reformulação do motor e da atmosfera

**Status: reconstrução documental.**

## Motivação concreta

Uma Terra 100% oceânica e com grandes mudanças gasosas recebia pontuação inadequadamente alta para sustentar civilização. Também faltava impacto visual consistente da composição, pressão e aerossóis na atmosfera.

## Prompt reconstruído

> Leia `ModeloPlanetario`, `EstadoPlanetario`, `Atmosfera`, `Hidrosfera`, `Biosfera`, `Civilizacao`, `TerraBuilder`, `PlanetView` e os testes. Reformule os fatores da simulação mantendo natureza didática e determinística.
>
> Separe **habitabilidade humana**, **potencial biológico** e **sustentabilidade da civilização**, explicitando fatores e limites. Use pressões parciais de O₂ e CO₂ em vez de punir arbitrariamente o hélio. Considere atmosfera, temperatura, radiação, superfície continental, água/gelo, vegetação e capacidade de suporte. Evite população crescendo sem recursos; diferencie ausência de habitantes de inabitabilidade.
>
> Melhore o avanço temporal com mudanças correlacionadas de CO₂, temperatura, gelo, vegetação e população. Mantenha limites matemáticos e comportamento determinístico. Documente simplificações e riscos de arredondamento.
>
> Desenvolva halo e névoa atmosféricos responsivos à composição, pressão, aerossóis e iluminação, sem usar a regra incorreta “cada gás possui uma cor”. Utilize aproximações visuais de espalhamento e opacidade; não bloqueie a thread de UI nem gere texturas inteiras a cada frame.
>
> Preserve saves SQLite V001 e os quatro padrões, os cinco eventos, Memento, animações e modo Apresentação. Use testes extremos de gases, pressões, atmosfera rarefeita/densa, vácuo, oceanos e superpopulação; verifique persistência antiga, captura e restauração, layout 1366×768. Execute Maven, gere JAR, imagens de evidência, documentação e commit.

## Evidência documental

`docs/VALIDACAO_REFORMULACAO.md` registra evolução de **57 para 69 testes**, fluxo JavaFX e galeria de doze cenários. O relatório especifica que a troca de N₂ por He, com pressões relevantes mantidas, não penaliza automaticamente a habitabilidade.
