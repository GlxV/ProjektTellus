# Ambiente de apresentação

Recomendação para o ensaio: **`Apresentar-ProjektTellus.cmd`**, na raiz. Ele chama `scripts/Start-AcademicDemo.ps1`, encontra Java 25 e usa `target/ProjektTellus.jar` já compilado. Cada execução cria uma biblioteca SQLite própria em `%TEMP%/ProjektTellus-apresentacao-<UUID>/demo.db`; o caminho e os parâmetros ficam em `launch.json`. O lançador não recompila automaticamente nem abre o banco pessoal. Uma nova abertura inicia outro ensaio vazio.

Para conferir caminhos sem abrir a GUI:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-AcademicDemo.ps1 -PrepareOnly
```

O ambiente efetivamente usado nas capturas foi **Windows 11 x64, Java 25.0.5, JavaFX 25.0.1**, escala lógica 1×1, tema PrimerDark. O JAR fornecido pela integração contém as dependências Windows (JavaFX, AtlantaFX e SQLite JDBC). SHA-256 do JAR testado: `78d044f525fcfd2ec0acf36b420442b68159949c909003345dfb0bb19ef5a4a8`. Não há garantia de execução desse pacote em Linux, macOS ou Windows ARM.

Para transportar e executar offline, mantenha a estrutura com `target/ProjektTellus.jar`, os lançadores, `scripts/Tools.ps1` e `scripts/Start-AcademicDemo.ps1`, além de um JDK 25 Windows x64 acessível pelo mecanismo de busca de `Tools.ps1` (neste workspace, `.tools/jdk-25.0.5+8`). O JAR sombreado já leva as bibliotecas; Maven e downloads são necessários apenas na preparação/build, se ausentes. Nesta frente não houve acesso à rede nem execução Maven. Falta de DLL, permissões de escrita/extração em `%TEMP%`, driver 3D e a imagem real da máquina da faculdade ainda precisam de ensaio físico.

O banco padrão do aplicativo é **`data/projekttellus.db` relativo ao diretório de trabalho**, salvo override `-Dprojekttellus.db`. O caminho do JAR é independente do caminho do banco. Para reabrir um ensaio em outro processo, guarde o caminho de banco que o lançador informou e use explicitamente o mesmo arquivo temporário:

```powershell
# Na raiz do pacote. Substitua o UUID pelo caminho real mostrado pelo lançador.
& '.\.tools\jdk-25.0.5+8\bin\java.exe' --enable-native-access=ALL-UNNAMED `
  '-Dprojekttellus.db=C:\Users\Admin\AppData\Local\Temp\ProjektTellus-apresentacao-UUID\demo.db' `
  -jar '.\target\ProjektTellus.jar'
```

Esse comando é para o ensaio manual. Para gerar as evidências sem ocupar o desktop ativo, o harness tem seu próprio lançador:

```powershell
& .\scripts\Capture-AcademicDemo.ps1 -Classpath (Resolve-Path .\target\ProjektTellus.jar).Path
```

Ele compila somente `AcademicDemoCapture.java` com `javac`, em pasta exclusiva de `%TEMP%`, e cria outro desktop Win32 com `CreateDesktop`/`CreateProcess`. Nunca chama `SwitchDesktop`, Robot, entrada global ou alteração de resolução. As janelas e os diálogos reais JavaFX existem nesse desktop privado. SQLite também fica em `%TEMP%/tellus-demo-<UUID>/controller-demo.db`; a referência independente do serviço usa `reference.db` ao lado. O script não remove esses bancos e recusa sobrescrever a pasta de evidências. Se `-Classpath` for omitido, compila fontes em pasta isolada usando os JARs já existentes de `~/.m2`; não chama Maven nem baixa dependências.

As capturas integrais têm **1366×720 pixels lógicos**, reservando 48 pixels hipotéticos de uma tela 1366×768 para a barra do sistema. A janela do harness é `UNDECORATED`, de modo que toda a área medida pertence ao conteúdo. Há também uma captura inicial 1366×768, um diálogo Builder 466×638 e globos isolados 768×768. Isso verifica o layout equivalente à área de trabalho de 768p; **não é um ensaio da moldura nativa do Windows, da barra de tarefas ou de DPI 125%**. A resolução global permaneceu intocada.

Resultado canônico: [latest.json](capturas/latest.json) aponta para `run-20261115-124710`. O fluxo Builder → retrato → Asteroide 25% → Observer → restauração foi comparado com uma instância independente de `SimulacaoService`. Todos os campos do estado e indicadores coincidiram. Foram verificados cinco criadores selecionáveis, três rótulos e barras, fatores, limites de layout, rolagem do inspector/retratos, setas/zoom/scroll/luz/recentrar e navegação/comparação na Apresentação. O replay foi iniciado/pausado por botões; sua cadência não foi aferida.

No marco da reabertura, uma nova conexão SQLite recuperou **15 entradas de histórico e 14 retratos**, com nome e estado inicial exatos; uma nova instância FXML/controller carregou o mesmo banco pelo botão **Carregar selecionado**. Veja [sqlite-reopen.txt](capturas/run-20261115-124710/sqlite-reopen.txt). Depois desse marco, foram executados os cinco cenários ambientais adicionais e uma restauração; os contadores posteriores crescem e não devem ser confundidos com os do marco. Carregar não publicou mudança: a nova sessão mostrou “aguardando uma nova mudança”, como esperado.

Os três assinantes reais no impacto são **Monitor de condições de vida**, **Estatísticas da sessão** e **Globo e painel de indicadores**. O CSV registra notificações por mundo/sessão, não uma contagem de alertas críticos. O monitor retornou estabilidade após o Asteroide 25%; Supervulcão gerou alerta de suporte populacional e Guerra nuclear também gerou radiação elevada. A evidência consta em [observers.txt](capturas/run-20261115-124710/observers.txt) e [measurements.csv](capturas/run-20261115-124710/measurements.csv).

Não há prova de fluidez, desempenho no projetor, funcionamento do lançador por duplo clique na máquina da faculdade ou aceitação científica dos indicadores. O computador físico da faculdade está **NÃO TESTADO**. Leve o JDK/pacote pronto, os PNGs e o PDF/deck como apoio. Um vídeo feito dessas imagens deve ser identificado como sequência de capturas reais; esta frente não gravou vídeo contínuo.


## Abertura do JAR empacotado — verificação adicional da integração

Além do fluxo FXML/controller, `scripts/Smoke-PackagedApp.ps1` executou **o JAR real** com Java 25 e `-Dprojekttellus.db` em caminho temporário inexistente. [package-launch.json](evidencias/package-launch.json) registra janela com título ProjektTellus, processo vivo, esquema SQLite V1 com tabelas mundos/eventos/snapshots vazias e `quick_check=ok`. O script fechou somente a janela do PID criado via WM_CLOSE e observou saída zero. O desktop privado nunca foi ativado. Isso valida a entrada empacotada neste host; permanece separado da validação física da faculdade e da execução por duplo clique.


## Materiais da defesa individual r3

Aluno: Matheus Luiz Bueno; turma 4P; professor EDER MAGALHAES MACHADO; disciplina ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS; 2º Bimestre de 2026. Use o [guia de defesa](GUIA_DEFESA_INDIVIDUAL.md), os [atalhos de código](ATALHOS_DEFESA.md), o [PPTX r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pptx) e o [PDF r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pdf). Os ensaios de aplicativo descritos acima pertencem à base anterior; a revisão de slides não modificou o motor nem afirma nova validação física. O vídeo de contingência anterior é reutilizado e preservado.
