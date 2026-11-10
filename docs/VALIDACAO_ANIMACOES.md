# Validação das animações — 10/11/2026

## Build e testes

Executado com o JDK 25.0.5+8 e Maven 3.9.11 locais:

```text
mvn -B -ntp -Pgui-tests verify
Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Execução final da implementação: 18:01:26, America/Sao_Paulo. Os 47 testes originais (46 sem desktop e 1 gráfico) continuam presentes. Foram acrescentados 7 testes: quatro de plano visual e três fluxos gráficos/geométricos. Log local: `target/gui-build.log`; relatórios JUnit: `target/surefire-reports/`. A exceção SQL induzida e as falhas isoladas de Observer que aparecem no log fazem parte dos cenários de teste aprovados.

## Evidência gráfica

JavaFX confirmou SCENE3D disponível. Os cinco eventos a 100% foram executados por controles reais, com o serviço e SQLite temporário, e as seguintes capturas foram inspecionadas:

| Sequência | Evidência em `target/gui-verification/` | Observação |
|---|---|---|
| Asteroide | `event-0-action.png`, `event-0-impact.png` | Rocha com rastro em aproximação e colisão alaranjada. |
| Supervulcão | `event-1-action.png`, `event-1-ash.png` | Erupção localizada e coluna escura acima do limbo. |
| Guerra nuclear | `event-2-action.png` | Fontes distintas em estágios diferentes, com ondas e fumaça. |
| Alienígenas | `event-3-action.png` | Discos com cúpulas ao redor do globo, feixes roxos e extração ciano animada. |
| Ecologia | `event-4-action.png` | Onda verde sobre a esfera e pontos luminosos. |
| Memento | `memento-pulse.png` | Onda ciano e retorno à textura original. |

Os estados finais de todos os eventos foram confrontados com o SQLite; a restauração reproduziu também a mesma impressão digital da textura original. Capturas `event-0-final.png` até `event-4-final.png` mostram a conclusão sem partículas remanescentes. `rotation-zoom-light.png` registra os controles de teclado e iluminação.

O JAR também foi aberto como aplicativo nativo separado, usando exclusivamente `target/visual-smoke.db`. Foram verificados criação de mundo, aplicação do asteroide a 40%, rotação por arraste, zoom pela roda e alternância da luz solar. A janela de verificação foi fechada; o banco habitual `data/projekttellus.db` não foi usado nessa verificação.

`pulse-timing.csv` registrou 884–935 pulsos JavaFX por sequência de aproximadamente 3,8–3,94 s nesta execução, incluindo as pausas das capturas. Esses pulsos não são uma medição dos quadros efetivamente apresentados no monitor. A implementação limita os uploads de textura a aproximadamente 60 atualizações/s e mantém a geometria em cada pulso; não houve benchmark independente de FPS, memória ou GPU.

## Revisão e regressões

Uma revisão independente encontrou a origem do asteroide incorreta quando o planeta já estava girado. A origem agora é convertida do espaço da câmera para o espaço local, assim como o impacto. O teste verifica 12 orientações e 101 posições por trajetória: nenhuma atravessa a esfera antes da colisão. Também verifica que a geometria temporária não desloca o pivô de escala.

Cancelamento durante preparação/execução, substituição, callbacks atrasados, modos reduzido/desativado, intensidade zero, fechamento e rollback SQL estão cobertos. As animações não alteram domínio, banco, histórico ou contagem Observer. O domínio, o motor, o serviço, os eventos e a persistência não precisaram de mudanças.

## Artefato

- JAR: `target/ProjektTellus.jar` — 24.869.647 bytes, Windows x64, requer Java 25.
- SHA-256: `EDF7AB7B7EE393D7198CE565374E5C56B99BF946F0C4B94F21C36E7E2CBD1527`.
- Componentes, funcionamento e acessibilidade: [ANIMACOES.md](ANIMACOES.md).

Os efeitos são estilizados e temporários. A opção de animações é global nesta janela, com padrão configurável por propriedade Java, sem persistência da preferência entre execuções. A inspeção neste desktop não certifica o desempenho ou a renderização em outras GPUs, escalas de tela ou sistemas operacionais.
