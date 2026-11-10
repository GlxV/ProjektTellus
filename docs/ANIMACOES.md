# Animações planetárias

As operações continuam sendo calculadas e gravadas por `SimulacaoService`. O controlador recebe um `ResultadoOperacao` confirmado e entrega seus dois estados imutáveis ao animador. O módulo visual não recebe Repository, Service, Terra mutável ou ObserverBus.

## Componentes

| Componente em `ui/` | Responsabilidade |
|---|---|
| `AnimationKind` | Seleciona a identidade visual pelo tipo concreto de Creator, sem comparar textos traduzidos nem criar/reaplicar eventos. Memento é uma opção explícita. |
| `AnimationMode` | Opção global: completas, movimento reduzido ou desativadas. |
| `AnimationPlan` | Orçamento puro e testável: duração, intensidade, partículas, fontes e curva de transição. |
| `PlanetEventAnimator` | Um executor para preparar texturas e um AnimationTimer para apresentar quadros. Possui cancelamento, substituição, conclusão exata e fechamento. |
| `PlanetEffect` | Contrato de efeito temporário: apresentar um progresso normalizado e liberar os recursos. |
| `PlanetEventEffect` | Geometria 3D retida, trajetórias, partículas, anéis geodésicos, naves, feixes e iluminação temporária. |
| `PlanetView` | Preserva a esfera nativa, mapa procedural, luz solar, mouse, teclado e zoom. Expõe uma camada 3D que compartilha a rotação e um Canvas transparente para halos suaves. |

## Sequências

- **Asteroide:** rocha irregular em aproximação acelerada com profundidade e rastro quente; impacto na esfera, explosão, duas ondas sobre a superfície, ejeção de partículas e tremor proporcional à intensidade.
- **Supervulcão:** foco vermelho crescente junto ao limbo, erupção incandescente, cinzas que sobem e se expandem, névoa gradual e condições ambientais finais.
- **Guerra nuclear:** 2–12 detonações escalonadas em pontos distintos. Cada uma possui clarão, onda, partículas e fumaça; o orçamento de partículas é compartilhado entre as fontes.
- **Invasão alienígena:** 1–6 discos com cúpulas e luzes. Aproximam-se em trajetórias oscilantes, projetam feixes roxos e recolhem partículas ciano da superfície para as naves antes da retirada. É uma representação artística da extração fictícia de oxigênio.
- **Restauração ecológica:** duas frentes verdes percorrem a esfera, acompanhadas de pontos luminosos. A transição revela exclusivamente a recuperação que o evento calculou, inclusive a população preservada.
- **Memento:** duas ondas ciano e uma transição até o estado completo do retrato selecionado. Capturar um retrato não reproduz uma restauração.

Eventos completos duram 3,8 s; Memento, 2,4 s; outras mudanças ambientais, 0,8 s. Movimento reduzido usa apenas uma transição de 0,65 s, sem explosões, flashes, tremores ou partículas. Desativadas e eventos a 0% exibem o resultado sem sequência. A intensidade controla dimensões, quantidade de fontes/partículas, clarões, ondas e tremor. O limite principal é de 180 partículas, com quantidades menores para cinzas, restauração e emissões individuais.

## Consistência e concorrência

Os indicadores Observer mostram o resultado confirmado imediatamente. A notificação não pinta o globo antecipadamente: a aparência é coordenada pelo retorno bem-sucedido da operação. Falhas de persistência não chegam a `play`.

As duas texturas de 768 × 384 são preparadas em um executor dedicado. Durante os quadros, somente os canais RGB dessas imagens são interpolados em um buffer reutilizado; o mapa procedural não é recalculado e nenhum EstadoPlanetario intermediário é criado. Ao concluir ou pular, a textura preparada do estado final substitui a mistura, sem erro acumulado de interpolação. Temperatura, vegetação, água, gelo, aerossóis e radiação continuam usando as mesmas fórmulas de aparência.

Os uploads de pixels são limitados a aproximadamente 60 Hz mesmo quando o JavaFX fornece pulsos em frequência maior. Partículas, naves, ondas e tremor acompanham cada pulso para manter a fluidez em telas de alta frequência. A duração segue o relógio monotônico; uma queda de frequência não alonga a operação.

“Pular animação” conclui a apresentação sem reaplicar a operação. Se a textura ainda está sendo preparada, o botão solicita a conclusão assim que ela estiver pronta, sem esperar na thread JavaFX. Uma nova ação encerra a sequência anterior e pode substituí-la depois da própria confirmação. Carregar/excluir o mundo ativo, comparar antes/depois, sair da apresentação e fechar a janela invalidam callbacks antigos. Um contador de geração impede que uma preparação atrasada pinte outro contexto. O executor verifica interrupção durante a preparação e é encerrado ao fechar.

A escala usa origem fixa: os limites variáveis das partículas não deslocam o centro do globo. Ondas são malhas sobre uma esfera de raio ligeiramente maior, com oclusão pelo depth buffer. Halos de superfície não aparecem no hemisfério oculto. Nenhuma cratera ou marca passa a integrar a geografia persistida.

## Uso

Escolha a opção **Animações** no painel de eventos. Ela afeta todos os eventos e restaurações nesta janela. Para definir o padrão de uma execução:

```powershell
java --enable-native-access=ALL-UNNAMED -Dprojekttellus.animationMode=REDUCED -jar target/ProjektTellus.jar
```

Valores: `FULL`, `REDUCED`, `OFF`. A seleção não altera o banco e não é gravada entre execuções. Mouse, roda, setas, `+`, `-`, `Home`, “Recentrar” e “Luz solar” continuam disponíveis durante as sequências.

## Verificação reproduzível

```powershell
mvn -B -ntp -Pgui-tests verify
```

`AnimationPlanTest` verifica roteamento, intensidade zero, limites e acessibilidade. `DesktopFlowTest` percorre os controles reais com SQLite temporário, executa os cinco eventos a 100%, restaura o Memento e compara o estado/textura final. Também cobre substituição durante preparação, pular durante execução, cancelamento, callbacks antigos, fechamento, modos reduzido/desativado, ausência de operações/notificações adicionais e falha SQL induzida sem animação de sucesso. O fluxo de desktop anterior foi preservado e agora aguarda a conclusão visual antes de comparar a textura restaurada.

Capturas em `target/gui-verification/` incluem aproximação/impacto, erupção/cinzas, detonações, naves, renovação, Memento, rotação/zoom/luz e estados finais. `pulse-timing.csv` registra pulsos reais de JavaFX durante os cinco eventos; inclui o custo das capturas e não constitui benchmark de GPU ou garantia universal de FPS.

Os modelos são geométricos estilizados e a atmosfera é ilustrativa. A verificação gráfica depende de JavaFX SCENE3D e de um desktop disponível; testes unitários isolados não certificam qualidade visual ou desempenho em outros computadores.

Referências de implementação: [PhongMaterial JavaFX 25](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/paint/PhongMaterial.html) e [Node JavaFX 25](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/Node.html).
