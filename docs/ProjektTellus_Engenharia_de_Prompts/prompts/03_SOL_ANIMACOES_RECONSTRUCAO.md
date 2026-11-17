# Etapa 3 — Sol: animações dos eventos planetários

**Status: reconstrução documental a partir do prompt apresentado na conversa. Não usar como prova de transcrição integral.**

## Prompt reconstruído

> Implemente animações identificáveis e fluidas para os cinco eventos já existentes no ProjektTellus: impacto de asteroide (aproximação/rastro/colisão), supervulcão (erupção/cinzas), guerra nuclear (múltiplas explosões), invasão alienígena (naves/feixes/extração atmosférica fictícia) e restauração ecológica (onda verde/recuperação). Adicione pulso ciano para restaurar um Memento.
>
> Leia antes `PlanetView`, `MainController`, `PresentationPane`, `SimulacaoService`, `ResultadoOperacao` e os eventos. Preserve motor, domínio, quatro padrões e SQLite. A aplicação do evento é confirmada pelo serviço, e a animação usa os estados antes/depois apenas para interpolação visual. Não reexecute o evento nem persista estados intermediários fictícios. Evite recomputar texturas completas a cada frame.
>
> Escale partículas e efeitos conforme intensidade de 0% a 100%, sem catástrofes visuais em intensidade zero. Garanta cancelar ao trocar mundo, fechamento, restauração e modo reduzido/desligado; possibilite pular. Não bloqueie a thread JavaFX. Teste cancelamento, intensidade, Memento, estado final exato e ausência de efeitos em operações que falham. Execute `mvn -B -ntp -Pgui-tests verify`, inspecione graficamente, gere JAR, registre limitações e crie commit.

## Evidência documental

`docs/VALIDACAO_ANIMACOES.md` registra **54 testes aprovados**, screenshots de cada evento e do pulso Memento, teste de cancelamento e preservação do banco habitual. O documento ressalva que pulsos JavaFX não são medição de FPS.
