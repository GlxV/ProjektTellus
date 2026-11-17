# Apresentações — ProjektTellus

Materiais de apresentação do simulador planetário. O projeto e a documentação técnica estão no [README principal](../../README.md).

## Slides

| Edição | PowerPoint | PDF |
| --- | --- | --- |
| **r4 — versão mais recente** | [Abrir PPTX](ProjektTellus-Defesa-Individual-r4.pptx) | [Abrir PDF](ProjektTellus-Defesa-Individual-r4.pdf) |
| r3 | [Abrir PPTX](ProjektTellus-Defesa-Individual-r3.pptx) | [Abrir PDF](ProjektTellus-Defesa-Individual-r3.pdf) |
| r2 | [Abrir PPTX](ProjektTellus-Apresentacao-r2.pptx) | [Abrir PDF](ProjektTellus-Apresentacao.pdf) |

O [vídeo de demonstração](ProjektTellus-Demo-Capturas.mp4) é uma sequência legendada de capturas da aplicação, não uma gravação contínua de tela.

## Código e diagramas

A edição mais recente apresenta quatro padrões de projeto implementados no código:

| Padrão | Código principal | UML |
| --- | --- | --- |
| Builder | [TerraBuilder.java](../../src/main/java/com/projekttellus/domain/TerraBuilder.java) | [PNG](../uml/02-builder.png) |
| Factory Method | [EventoCreator.java](../../src/main/java/com/projekttellus/events/EventoCreator.java) | [PNG](../uml/03-factory-method.png) |
| Observer | [ObserverBus.java](../../src/main/java/com/projekttellus/patterns/observer/ObserverBus.java) | [PNG](../uml/04-observer.png) |
| Memento | [TerraMemento.java](../../src/main/java/com/projekttellus/patterns/memento/TerraMemento.java) | [PNG](../uml/05-memento.png) |

Os sete diagramas, com fontes Mermaid e exportações PNG/SVG, estão em [docs/uml](../uml/README.md).

A [documentação de engenharia de prompts](../ProjektTellus_Engenharia_de_Prompts/README.md) registra a metodologia de desenvolvimento assistido por IA, os prompts documentados, as fontes e as limitações de proveniência. Os prompts reconstruídos estão identificados como reconstruções, não transcrições literais. A arquitetura e as decisões de implementação estão descritas em [ARQUITETURA.md](../ARQUITETURA.md) e [PADROES.md](../PADROES.md).

## Executar a demonstração

No Windows, execute `Abrir-ProjektTellus.cmd` na raiz. O lançador prepara Java 25 e Maven quando necessário, compila e abre a aplicação. Para ensaiar com um banco SQLite temporário independente, depois de compilar use `Apresentar-ProjektTellus.cmd`.

Se não for possível executar JavaFX no computador disponível, use os slides PDF/PPTX, os [diagramas](../uml/README.md) e as [capturas](../capturas/README.md) como material de apoio.
