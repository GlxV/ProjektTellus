# ProjektTellus — transferência de implementação

**Data:** 10/11/2026, horário de Brasília. **Status:** implementação funcional local concluída, compilada e demonstrada em Windows x64.

## Commits

- Base recebida: `b6ae791` — estrutura inicial Maven/JavaFX.
- Implementação: `2eed2f9c37086c8ac5520fed95387f6d4f67710e` — `feat: implement ProjektTellus sandbox and architecture presentation`.
- Este handoff é registrado no commit seguinte de documentação. Identificador final: `git log -1 --format="%H %s"` (evita referência circular ao hash do próprio arquivo).
- Commits locais; não houve push nem alteração do endereço remoto existente.

## Funcionalidades concluídas

- Java 25, Maven, JavaFX/FXML, AtlantaFX, SQLite JDBC e JUnit com versões disponíveis e fixadas.
- Terra composta por Atmosfera, Clima, Biosfera, Hidrosfera e Civilizacao. Estados profundamente imutáveis, validação de limites/NaN/infinito, frações atmosféricas consistentes e pressões parciais.
- Edição de quatro gases, pressão, temperatura, aerossóis, radiação, vegetação, água/gelo e população. Soma dos gases validada e opção para completar N₂. Água+gelo não excede a superfície.
- Indicadores distintos de habitabilidade humana/potencial biológico, evolução temporal determinística e conservação de água no congelamento/degelo.
- Cinco eventos com intensidade e consequências ambientais: asteroide, supervulcão, guerra nuclear, invasão alienígena fictícia e restauração ecológica.
- Mundos independentes: criar personalizado, nomear/renomear, duplicar, carregar, salvar e excluir. Estado, histórico e retratos completos persistidos em SQLite inicializado automaticamente.
- Checkpoints antes de mudanças e restauração completa de nome/componentes/tempo, com rejeição de retratos de outro mundo. A restauração preserva também o estado substituído.
- Transação atômica para estado+registro+retrato, PreparedStatement, foreign keys/cascade, migrations versionadas e tratamento de rollback/falhas de fechamento.
- Globo JavaFX 3D iluminado, textura original ilustrativa, rotação por arrasto, zoom pela roda, navegação por teclado, recentralização e aparência dependente do ambiente.
- Sandbox e Apresentação no mesmo aplicativo/motor. Reprodução dos passos reais de cada operação, navegação manual, reprodução temporizada, identificação/explicação dos quatro padrões e comparação antes/depois.
- Observer com utilidade real: monitor de alertas, estatísticas de sessão e atualização da interface. Assinaturas removíveis e falhas isoladas.
- Banco/casos de uso fora da thread JavaFX, fila de trabalho e bloqueio de ações enquanto ocupada. Confirmação de exclusão e proteção de edição pendente.
- Executável `target/ProjektTellus.jar` com dependências; lançador Windows e preparador local de ferramentas, sem mudar o Java padrão do Windows.
- README, UML Mermaid, explicação dos padrões, documentação matemática e origem/licença dos recursos visuais.

## Funcionalidades incompletas

Nenhum requisito funcional obrigatório ficou deliberadamente sem implementação. Os limites de validação e distribuição estão descritos abaixo; não equivalem a certificação em outros computadores.

## Testes executados e resultados

Comando final, executado com JDK Temurin **25.0.5+8** e Maven **3.9.11**:

```powershell
mvn -B -ntp -Pgui-tests clean verify
```

**47 testes, zero falhas, zero erros, zero ignorados; BUILD SUCCESS.**

| Suíte | Testes | Escopo |
|---|---:|---|
| DomainTest | 5 | Builder, validações, indicadores distintos, Memento e gases quase puros |
| SimulationTest | 11 | Cinco criadores/eventos, intensidade, extremos, evolução determinística, água, relógio fracionário e Observer |
| ApplicationTest | 6 | Fluxo completo, isolamento, reabertura, atomicidade após falha, trilha real e assinante com identificação defeituosa |
| SqliteMundoRepositoryTest | 21 | JDBC real, reabertura, histórico/retratos, identidade, injeção, migração, cascata e rollback |
| TransacaoSqliteTest | 1 | Falhas simultâneas de rollback/fechamento não confirmam dados pendentes |
| ProjectStructureTest | 2 | Recursos FXML/CSS presentes |
| DesktopFlowTest | 1 | Integração gráfica com múltiplas verificações do fluxo completo |

O perfil padrão exclui `gui`: **46 testes sem abrir JavaFX**. O perfil `gui-tests` inclui o teste gráfico e exige desktop/toolkit disponível.

`DesktopFlowTest` usa FXML, componentes/diálogos reais, serviço real e banco descartável. Percorre criação, edição inválida/válida, duplicação/renomeação, captura, todos os eventos, tempo, restauração, apresentação, comparação, saída direta da apresentação, salvar/carregar/excluir e reabertura da interface/banco. Ele aciona controles programaticamente; não é uma inspeção humana de todos os pixels.

**Inspeção nativa adicional executada:** JAR empacotado aberto em janela visível; criação pelo diálogo, rotação por arrasto do mouse, zoom pela roda, asteroide com mudança perceptível do globo e dos indicadores, modo Apresentação e avanço manual de etapa. O JAR final foi aberto novamente por `scripts/Run.ps1`, com o banco padrão criado automaticamente. Os logs de inicialização não mostraram exceções; há o aviso JavaFX descrito abaixo.

O script `Build.ps1` foi executado em Windows PowerShell com descoberta das ferramentas locais e concluiu compilação/testes/empacotamento. Sintaxe dos scripts conferida. Download original do JDK foi validado por SHA-256; não foi repetida uma instalação integral dos scripts a partir de `.tools` vazio.

Regressões encontradas/reproduzidas e corrigidas antes da validação final: observador cujo nome lança exceção, limites numéricos de gases/água, relógio fracionário perto do máximo e globo histórico persistindo após troca direta de aba. Os testes correspondentes falharam antes da correção e passaram depois.

Evidência local regenerável (ignorada pelo Git):

- `target/final-verification.log`, `target/surefire-reports/`.
- `target/gui-verification/sandbox.png` e `presentation.png`, inspecionadas visualmente.
- `target/startup.err.log`, `startup.out.log` e logs do lançador.
- JAR produzido: **24.843.899 bytes**; SHA-256 `9eeafb144bab95680d054f6e7b7502508c027c7cc752c404acf3b5cad8c630a7`.

## Limitações e problemas conhecidos

- Modelo global didático, constantes não calibradas e textura cartográfica estilizada; não produz previsões científicas. A invasão alienígena é ficção.
- JAR desta máquina inclui nativos **Windows x64** e precisa de Java 25; não inclui runtime Java. Outras plataformas precisam recompilar e não foram testadas.
- Aceleração 3D depende de GPU/driver. O ambiente testado renderizou o globo; não há fallback 2D para hardware sem suporte a Scene3D, apenas aviso.
- Testado em tela desktop de 1440×960 de conteúdo; não foi realizada uma matriz extensa de DPI/resoluções/acessibilidade nem teste prolongado de carga com milhares de mundos/retratos.
- JavaFX no JAR não modular emite `Unsupported JavaFX configuration: classes were loaded from unnamed module`. A abertura e a renderização passaram; o aviso fica no log, fora da apresentação. Maven/Java 25 também podem emitir aviso da dependência interna Guice/Unsafe. Shade informa sobreposição de manifestos/metadados de JavaFX, sem falha de execução observada.
- Alertas/estatísticas são projeções de sessão. O estado planetário, eventos e retratos são persistentes; estatísticas recomeçam na nova sessão. A sequência de apresentação é a última operação desta sessão, não um replay técnico de histórico antigo.
- Retenção de histórico/retratos é ilimitada nesta versão acadêmica; exclusão de um mundo remove seus próprios registros. Sem sincronização remota/exportador/instalador nativo, que não eram requisitos.

## Classes principais e padrões

Todos os caminhos Java abaixo partem de `src/main/java/com/projekttellus/`.

| Responsabilidade | Localização |
|---|---|
| Entidade e componentes | `domain/Terra`, `EstadoPlanetario`, `Atmosfera`, `Clima`, `Biosfera`, `Hidrosfera`, `Civilizacao` |
| Builder | `domain/TerraBuilder` |
| Motor e indicadores | `simulation/ModeloPlanetario`, `Indicadores` |
| Factory Method | `events/EventoCreator`, cinco `*Creator`, produtos `EventoPlanetario`/`EventoBase` e cinco eventos concretos |
| Observer | `patterns/observer/ObserverBus`, `SimulacaoObserver`, `AlertaObserver`, `EstatisticasObserver`; assinatura da UI em `ui/MainController` |
| Memento | Originador `domain/Terra`, retrato `patterns/memento/TerraMemento`, caretaker `application/SimulacaoService` |
| Casos de uso/trilha real | `application/SimulacaoService`, `ResultadoOperacao`, `Passo` |
| Persistência | `persistence/MundoRepository`, `SqliteMundoRepository`, `SchemaSqlite`, `TransacaoSqlite` |
| Interface | `ui/MainController`, `EnvironmentEditor`, `BuilderDialog`, `PlanetView`, `PresentationPane`, `DetailCell`, `UiDialogs` |
| Recursos | `src/main/resources/fxml/main.fxml`, `styles/app.css`, `db/migrations/V001__mundos.sql` |

## Instruções para o próximo agente

1. Leia `README.md`, `PADROES.md`, `MODELO.md` e `ARQUITETURA.md`. Os documentos `HANDOFF_INICIAL.md`/`BRIEF_SOL.md` são o registro histórico do pedido inicial.
2. Use o checkout atual `Z:/ArqPoo/ProjektTellus`; não é dependente de outro projeto de POO. Ferramentas locais ignoradas em `.tools/` já estão preparadas nesta máquina.
3. Execute `scripts/Build.ps1` para verificação padrão ou `mvn -Pgui-tests clean verify` com JDK25 configurado para incluir o fluxo gráfico. Feche o aplicativo antes de uma limpeza/recompilação; um banco de teste aberto dentro de `target/` impede a limpeza no Windows.
4. Abra `Abrir-ProjektTellus.cmd`. Crie duas Terras, personalize uma, capture um retrato, provoque evento e restaure; carregue a segunda e reabra o aplicativo para demonstrar isolamento e SQLite. Use os quatro atalhos didáticos da apresentação com ações reais.
5. Não duplique o motor para a apresentação, não substitua SQLite por JSON e não remova validações para contornar erros de interface. Mantenha persistência antes da publicação de alterações e testes de falha de transação.
6. JAR, banco, ferramentas e logs não entram no Git. Preserve os mundos reais do usuário ao testar; os testes usam bancos temporários. Regere o JAR quando mudar código, confira a interface e atualize as evidências antes de afirmar compatibilidade adicional.
