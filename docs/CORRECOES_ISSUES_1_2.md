# Correções de responsividade e eventos — issues #1 e #2

## Alterações

- `ProjektTellusApp` configura a **janela externa**, incluindo bordas/título, por `Screen.getVisualBounds()`. Preferência de 1440×960 limitada à área útil; mínimo de 1000×560 também limitado ao monitor. As coordenadas JavaFX já são lógicas e respeitam DPI, sem multiplicação manual pela escala do Windows.
- As laterais mantêm larguras proporcionais, com limites de legibilidade. O centro pode encolher sem empurrar abas/rodapé para fora. O divisor vertical volta à proporção apropriada depois do redimensionamento.
- O painel direito inteiro continua em uma única rolagem vertical: ambiente, eventos, intensidade, modos de animação e avanço de tempo. Seu conteúdo conserva a altura necessária em vez de ser comprimido.
- Globo e retratos têm rolagem de segurança para janelas especialmente baixas. O globo adapta a escala à região disponível; rotação, iluminação, zoom e animações mantêm a implementação existente.
- Apresentação e botões de retratos quebram linhas. O diálogo Builder limita a altura à menor área entre janela proprietária e monitor, conservando os botões de confirmação fora da rolagem do formulário.
- O modo compacto ajusta **espaçamentos**, sem reduzir fontes. Cores, tipografia e identidade visual foram preservadas.
- O CSS genérico de listas transparentes atingia a lista interna do ComboBox. O popup agora tem superfície e células opacas, borda, sombra e cores próprias para hover, selecionado, selecionado+hover e pressionado. O popup nativo JavaFX conserva posicionamento automático e rolagem.

Não há alteração no domínio, cálculos, eventos, persistência ou padrões de projeto.

## Método e limites

Validação realizada em desktop Windows x64, Java 25 e JavaFX 25.0.1, com `SCENE3D=true`. O monitor físico **não foi reconfigurado** e não se utilizou o computador da faculdade. Foram usadas janelas externas equivalentes, reservando 48 pixels físicos para a barra de tarefas e descontando também as decorações reais do Windows.

| Alvo | Janela externa de teste, em pixels lógicos | Método |
|---|---|---|
| 1366×768 / 100% | 1366×720; conteúdo 1350×681 | Janela equivalente, JavaFX com escala de saída 1.00 |
| 1920×1080 / 100% | 1920×1032 na matriz; abertura preferida limitada a 1440×960 | Janela equivalente e percurso gráfico completo |
| 1920×1080 / 125% | 1536×825 na matriz; abertura preferida limitada a 1440×826 por arredondamento | JVM separada com `glass.win.uiScale=1.25`, escala de saída 1.25 verificada por asserção |

O tamanho da barra de tarefas é uma hipótese conservadora para a equivalência, **não um valor fixado no aplicativo**. Na execução normal o tamanho inicial usa a área útil informada pelo sistema. O teste a 125% força a escala do JavaFX; não equivale a alterar globalmente a escala do Windows ou verificar troca de monitor com DPI diferente.

Esses resultados demonstram o comportamento nas janelas e no renderizador testados. Não constituem certificação da GPU, Java instalado ou configuração física do computador da faculdade.

## Verificação reproduzível

Com JDK 25 e Maven 3.9+ no PATH:

```powershell
mvn -B -ntp -Pgui-tests verify
mvn -B -ntp -Pgui-tests '-Dtest=DesktopFlowTest' '-Dtest.window.width=1920' '-Dtest.window.height=1032' '-Dtest.expectedScale=1.0' test
mvn -B -ntp -Pgui-tests '-Dtest=DesktopFlowTest' '-Dtest.jvmArgs=-Dglass.win.uiScale=1.25' '-Dtest.expectedScale=1.25' '-Dtest.window.width=1536' '-Dtest.window.height=825' test
```

Os testes gráficos usam um desktop desbloqueado e banco SQLite temporário. O mouse e o foco precisam ficar disponíveis durante os testes nativos do popup.

Os dois testes iniciais falharam na versão anterior por mínimo de altura incompatível e popup transparente; passaram após a correção. A suíte anterior continha 54 testes e passou antes das alterações. A suíte completa com os três testes de regressão contém 57 testes.

O percurso gráfico existente agora abre por padrão na janela equivalente a 1366×768. Ele verifica criação, edição ambiental, os cinco eventos animados, estados intermediários e finais, Memento, SQLite após reabertura, apresentação, modos reduzido/desativado, cancelamento e preservação em falha transacional. A matriz adicional verifica abas, botões alcançáveis por rolagem, limites do layout, maximização/restauração e janela mínima de 1000×560.

O Robot nativo seleciona **cada uma das cinco opções** por mouse e por Home/Down/Enter. Confere índice, fechamento do popup, intensidade inalterada e ausência de operações no histórico. Repete perto do limite inferior real da tela para exigir abertura acima do seletor. As capturas da cena não incluem o popup separado; `event-popup*.png` é captura nativa da tela.

Para inspeção interativa com o JAR gerado e ferramentas já existentes no projeto:

```powershell
& '.tools/jdk-25.0.5+8/bin/java.exe' --enable-native-access=ALL-UNNAMED '-Dprojekttellus.db=target/inspecao.db' -cp 'target/test-classes;target/ProjektTellus.jar' com.projekttellus.ui.LayoutPreview 1366 720
```

`LayoutPreview` pertence aos testes, não ao JAR de distribuição. Para o uso normal: `Abrir-ProjektTellus.cmd` ou `java -jar target/ProjektTellus.jar`. O JAR continua exigindo Java 25 e contém as dependências Windows x64.

## Evidências e resultado final

Os logs e capturas completos ficam em `target/`, e as evidências selecionadas publicadas com o código ficam em [evidencias-issues-1-2](evidencias-issues-1-2/).

A inspeção interativa usou o **JAR empacotado**, por meio de `LayoutPreview`, em janela externa 1366×720 / conteúdo 1350×681, escala de saída 1.00. Foram observados Builder com confirmação acessível; carregamento do mundo salvo; rolagem até eventos; cinco opções legíveis em superfície opaca com seleção destacada; alteração da intensidade; aplicação de asteroide; rotação e zoom por mouse; abas; captura e restauração real de retrato; avanço de etapas e rolagem até comparação/demonstrações da Apresentação. Os quadros de impacto e restauração também foram inspecionados nas capturas produzidas pelo percurso automatizado real.

Evidências interativas:

- [Popup sobre a região de intensidade/aplicação, com seleção destacada](evidencias-issues-1-2/manual-popup.jpg).
- [Apresentação: navegação e explicações](evidencias-issues-1-2/manual-apresentacao.jpg).
- [Apresentação: comparação e quatro demonstrações acessíveis pela rolagem](evidencias-issues-1-2/manual-apresentacao-final.jpg).

A seleção nativa foi repetida com o popup abrindo acima do seletor, tanto a 100% quanto a 125%. O teste filtra as células visíveis do `VirtualFlow`, evitando clicar na célula invisível que o JavaFX mantém apenas para medição. O teste CSS confere as cores efetivamente calculadas para seleção, hover e pressionado, além da opacidade.

Uma rodada durante automação de acessibilidade registrou `NullPointerException` interno do JavaFX em `WinTextRangeProvider.GetText` após descarte de controles, sem impedir a conclusão da suíte. A rodada subsequente `verify-delivery.log` passou 57 testes sem essa exceção; a inspeção interativa do JAR não a reproduziu. O fechamento do primeiro harness também expôs saída prematura do toolkit; o harness de inspeção foi corrigido, sem alteração no ciclo de vida do aplicativo distribuído.

Os resultados consolidados e o SHA-256 do JAR estão em [resultados.txt](evidencias-issues-1-2/resultados.txt). O banco pessoal de `data/` não foi usado nos testes nem alterado pela inspeção.
