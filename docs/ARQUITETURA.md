# ProjektTellus — diretrizes arquiteturais propostas

**Este documento descreve a arquitetura desejada, nao o codigo ja implementado.**

## Limites de camadas

- `domain`: Terra, Atmosfera, Clima, Biosfera, Hidrosfera, Civilizacao; regras de validacao e invariantes. **Sem JavaFX, FXML, JDBC nem SQL**.
- `simulation`: evolucao temporal, indicadores e recalculo deterministicos; sem UI.
- `events`: interface de eventos e criadores polimorficos (**Factory Method**). Eventos alteram variaveis reais do modelo; nao apenas a barra de habitabilidade.
- `application`: casos de uso, orquestracao, transacoes e criacao de checkpoints.
- `persistence`: repositorios e implementacao JDBC/SQLite. Migrations ou esquema idempotente, prepared statements, transacoes e integridade referencial.
- `ui`: controladores JavaFX, FXML e globo; view-models quando uteis.
- `patterns`: usar apenas para conceitos transversais que nao tenham pacote de dominio mais apropriado; nao criar duplicatas de classes so para exibicao.

## Fluxo de eventos

1. Usuario escolhe uma acao.
2. Caso de uso opcionalmente cria snapshot antes da mudanca.
3. Criador concreto instancia um EventoPlanetario.
4. Motor aplica os efeitos no dominio e recalcula indicadores.
5. Eventos de dominio sao publicados aos interessados (**Observer**).
6. UI e historico/apresentacao exibem o resultado. Persistencia e feita de forma consistente em SQLite.

A ordem exata devera refletir o fluxo *real* da implementacao; nao desenhar fluxos ficticios.

## Padroes

- **Factory Method:** classe abstrata/interface de criador define metodo de fabrica sobrescrito por criadores concretos para diferentes eventos; nao reduzir a um switch estatico.
- **Builder:** com parametros opcionais e `build()` validado, produzindo estado inicial valido. Evitar obrigar cada cliente a conhecer detalhes de construcao.
- **Observer:** interfaces para publicador/assinantes desacoplados; ao menos dois assinantes com utilidade real. Testar notificacao e remocao de assinatura. Nao confundir exclusivamente com JavaFX property binding.
- **Memento:** estado completo imutavel, restauracao por proprietario/guardiao apropriados, sem expor campos mutaveis; copia profunda se necessario; snapshots de mundos isolados.

## Modo Apresentacao: sem codigo tecnico

Tratar como *projecao didatica* de eventos reais do motor. Associar eventos internos a rotulos em portugues natural:
- `EventoCriado` -> "Evento preparado"
- `SnapshotCriado` -> "Estado anterior preservado"
- `TerraAlterada` -> "Condicoes planetarias alteradas"
- `ObservadoresNotificados` -> "Alteracoes comunicadas"
- `IndicadoresRecalculados` -> "Habitabilidade atualizada"

Esses nomes de eventos sao **exemplos de design**, nao classes ja existentes. Mapeamentos para texto ficam na UI, nunca no dominio.

A demonstracao precisa permitir passo a passo, pausas e repetir exemplos. Os rotulos dos quatro padroes podem aparecer; chamadas Java/SQL, nao.

## Persistencia

SQLite e a fonte persistente dos mundos. Estrutura sugerida, sujeita a evolucao: mundos, estados, gases, eventos e snapshots. Definir chaves estrangeiras, migrations e atomicidade. Banco local deve ficar em `data/` e ser ignorado pelo Git.

Jackson pode auxiliar exportacao/serializacao de payloads, mas nao substituir SQLite. Nao prometer JAR standalone antes de implementar empacotamento proprio.

## Testes essenciais

- Validacao de gases/pressao e indicadores sem valores invalidos.
- Comportamentos dos cinco eventos.
- Factory cria tipos adequados, Builder rejeita mundos invalidos.
- Observer notifica apenas interessados.
- Memento restaura integralmente sem referencia mutavel compartilhada.
- Multiplos saves + reinicio + transacoes SQLite.
- UI: testar manualmente rotacao/zoom e modo didatico, documentar resultados.

## Demo final sugerida

Criar duas Terras -> personalizar uma (Builder) -> salvar ambas (SQLite) -> capturar snapshot -> provocar invasao alienigena (Factory Method) -> observar mudancas e notificacoes (Observer) -> restaurar (Memento) -> recarregar o banco -> explicar UML.
