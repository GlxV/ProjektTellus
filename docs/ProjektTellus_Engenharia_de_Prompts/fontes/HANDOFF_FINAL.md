# ProjektTellus — handoff final

**Data:** 10/11/2026 (Brasília)

**Estado:** implementação existente auditada; compilação, empacotamento e fluxo gráfico verificados neste checkout.

## Funcionalidades prontas

- Aplicação desktop JavaFX com Sandbox e Modo Apresentação, usando o mesmo serviço e motor planetário.
- Criação de mundos personalizados; edição validada de atmosfera, clima, água/gelo, vegetação e população; renomeação, duplicação, carregamento, salvamento e exclusão isolada por mundo.
- Cinco eventos com intensidade, avanço determinístico do tempo e indicadores separados de habitabilidade humana e potencial biológico.
- Persistência local SQLite de mundos, histórico e retratos, com migration versionada, chaves estrangeiras e gravação transacional. Falhas de gravação não publicam uma mudança parcial na Terra ativa.
- Captura e restauração de retratos completos, incluindo nome, componentes e tempo; retratos de outra Terra são rejeitados.
- Globo 3D interativo e trilha de apresentação baseada nos passos de operações reais já confirmadas, com navegação manual, reprodução temporizada e comparação antes/depois.
- Alertas, estatísticas por mundo durante a sessão e atualização da interface por Observer.

## Quatro padrões e classes

| Padrão | Implementação demonstrável |
|---|---|
| Factory Method | `events/EventoCreator`; `AsteroideCreator`, `SupervulcaoCreator`, `GuerraNuclearCreator`, `InvasaoAlienigenaCreator`, `RestauracaoEcologicaCreator`; produtos em `EventoBase` e nas cinco classes de evento. |
| Builder | `domain/TerraBuilder` reúne valores opcionais e constrói uma `Terra` independente com `EstadoPlanetario` validado. |
| Observer | `patterns/observer/ObserverBus`, `SimulacaoObserver`, `AlertaObserver`, `EstatisticasObserver`; `ui/MainController` registra o observador da interface. |
| Memento | `domain/Terra` é o originador; `patterns/memento/TerraMemento` é o retrato imutável; `application/SimulacaoService` coordena captura e restauração; `MundoRepository` persiste o histórico. |

Os diagramas atualizados estão em `docs/ARQUITETURA.md` e `docs/PADROES.md`.

## Testes executados

Ambiente: Temurin JDK **25.0.5+8**, Maven **3.9.11**, Windows x64.

```powershell
mvn -B -ntp -Pgui-tests verify
```

Resultado nesta auditoria: **47 testes, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS**. O comando compilou, empacotou o JAR e executou a suíte. Distribuição por suíte:

| Suíte | Testes | Cobertura principal |
|---|---:|---|
| `DomainTest` | 5 | Builder, invariantes do domínio, indicadores e retratos |
| `SimulationTest` | 11 | Criadores/eventos, intensidade, evolução temporal e Observer |
| `ApplicationTest` | 6 | Fluxo integrado, isolamento, persistência, restauração e falhas transacionais |
| `SqliteMundoRepositoryTest` | 21 | SQLite real, reabertura, migrations, histórico, retratos, cascata e rollback |
| `TransacaoSqliteTest` | 1 | Tratamento de falhas de rollback/fechamento |
| `ProjectStructureTest` | 2 | Presença dos recursos de interface |
| `DesktopFlowTest` | 1 | FXML e controles JavaFX reais com banco temporário |

O fluxo gráfico verificou criação e edição válida/inválida, duplicação e renomeação, captura e restauração, cinco eventos, avanço do tempo, apresentação e comparação visual, salvar/carregar/excluir e reabertura do banco. As capturas geradas em `target/gui-verification/` foram inspecionadas. A aplicação empacotada também já havia sido aberta em uma janela Windows durante a validação anterior registrada em `docs/HANDOFF_SOL.md`.

O log deste ciclo está em `target/audit-verification.log` (ignorado pelo Git); relatórios individuais em `target/surefire-reports/`.

## Limitações conhecidas

- O modelo é didático, global e não calibrado; indicadores e eventos não são previsões científicas. A invasão alienígena é ficção.
- O JAR é produzido para Windows x64 e requer Java 25; o runtime Java não está incluído. Outros sistemas precisam compilar o projeto para sua plataforma.
- A renderização do planeta depende de suporte 3D do driver/GPU. Sem `Scene3D`, a aplicação mostra um aviso; não existe fallback 2D.
- A suíte gráfica automatiza controles e diálogos; não certifica acessibilidade nem uma matriz ampla de DPI/resoluções ou uso prolongado com grandes volumes de histórico.
- Estatísticas e alertas são projeções da sessão; mundos, eventos e retratos são persistentes. O histórico e os retratos não têm política automática de retenção.
- A execução observou o aviso JavaFX sobre classes no módulo não nomeado e avisos do Shade sobre classes/recursos repetidos no JAR. O build e o fluxo gráfico passaram; esses avisos permanecem registrados.

## Como executar

No Windows x64, abra `Abrir-ProjektTellus.cmd` na raiz do projeto. Com JDK 25 e Maven 3.9+ configurados:

```powershell
mvn -Pgui-tests verify
java --enable-native-access=ALL-UNNAMED -jar target/ProjektTellus.jar
```

O banco padrão é `data/projekttellus.db`, relativo à raiz de execução do lançador. Para compilar e executar os testes sem o toolkit gráfico, use `mvn verify`.

## Fluxo recomendado para apresentação

1. Crie uma Terra pelo diálogo do Builder e personalize as condições iniciais; confirme que a soma dos gases é válida.
2. Aplique condições e depois um asteroide ou supervulcão. Mostre a mudança no globo, nos indicadores e no monitor Observer.
3. Capture um retrato, aplique outra catástrofe e restaure o retrato. Em **Apresentação**, avance as etapas reais e compare o globo antes/depois para demonstrar Memento.
4. Demonstre Factory Method aplicando outro tipo de evento e identifique o criador/produto na explicação da trilha. Builder já foi demonstrado na criação.
5. Duplique o mundo, altere apenas um deles, carregue o outro e, por fim, reabra o aplicativo para mostrar o isolamento e a persistência SQLite.

## Commit

Commit final deste handoff: consulte o identificador exato com `git log -1 --format="%H %s"` na raiz do repositório. O histórico é local; não houve push.
