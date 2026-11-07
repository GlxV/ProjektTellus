# ProjektTellus

**Simulador Planetário Interativo** — trabalho acadêmico de Arquitetura de Software e Programação Orientada a Objetos (UNIVEL, 4AN, 2026).

> Status: **estrutura inicial preparada**. O simulador, a física simplificada, os eventos, o banco e os quatro padrões ainda precisam ser implementados. A tela inicial é apenas um ponto de partida, não uma demonstração de funcionalidades prontas.

## Conceito

Sandbox de planetas Terra com globo girável, atmosfera configurável (O₂, N₂, CO₂ e He), indicadores separados de habitabilidade humana e potencial de vida, avanço de tempo e eventos (asteroide, supervulcão, guerra nuclear, invasão alienígena fictícia e restauração ecológica). Vários mundos independentes e snapshots persistidos em **SQLite**.

O mesmo motor será utilizado em **Sandbox** e **Modo Apresentação**, que exibirá fluxos didáticos em português, *não* código Java/SQL.

## Stack inicial

- Java **25** + Maven
- JavaFX **27.0.1** (FXML, controles, esfera 3D nativa)
- AtlantaFX **3.0.0** (tema)
- SQLite JDBC **3.54.1.0** (persistência obrigatória)
- Jackson Databind **2.22.3** (exportação/serialização quando útil)
- JUnit Jupiter **6.1.3** (testes)

As versões estão concentradas no `pom.xml`. O Maven baixa as dependências automaticamente. **JDK 25 ou superior** é necessário.

## Execução da estrutura inicial

```powershell
git clone https://github.com/GlxV/ProjektTellos.git
cd ProjektTellos
mvn test
mvn javafx:run
```

Se não houver `mvn` no Windows, instale/configure Maven e confirme `java -version` e `mvn -version`. A primeira execução exige internet para baixar dependências.

> `mvn package` gera um JAR Maven normal, **não** um executável autossuficiente com JavaFX. O empacotamento distribuível deverá ser configurado durante a implementação; não anunciar `java -jar` como pronto.

## Organização

```text
src/main/java/com/projekttellus/
  Launcher.java           # ponto de entrada Maven/JavaFX
  ProjektTellusApp.java   # Application JavaFX; tela inicial mínima
  domain/                 # entidades e regras POO
  simulation/             # motor/regras de evolução
  events/                 # eventos e Factory Method
  patterns/               # Builder, Observer, Memento (conforme design)
  persistence/            # SQLite / DAOs / repositories
  application/            # casos de uso e serviços
  ui/                     # controladores e visualização
src/main/resources/
  fxml/                   # telas FXML
  styles/                 # CSS
  textures/               # texturas com licenças registradas
  db/                     # scripts/migrations SQL
src/test/java/com/projekttellus/
docs/                      # requisitos, arquitetura e handoff
```

Os diretórios vazios contêm `.gitkeep` e podem ser reorganizados pelo agente se houver justificativa. **Não criar classes vazias ou padrões artificiais só para preencher pastas.**

## Padrões obrigatórios

- **Factory Method:** criadores de eventos planetários;
- **Builder:** construção validada de mundos;
- **Observer:** propagação de mudanças para subsistemas desacoplados;
- **Memento:** capturar/restaurar estado completo e imutável.

Separam-se objetos de domínio, casos de uso, infraestrutura JDBC e interface. Os padrões devem ser observáveis e testáveis no fluxo real, sem acoplamento da simulação ao modo didático.

## Documentos para começar

1. [Requisitos e critérios de aceite](docs/REQUISITOS.md)
2. [Diretrizes de arquitetura](docs/ARQUITETURA.md)
3. [Briefing para Sol 6.1](docs/BRIEF_SOL.md)
4. [Handoff inicial](docs/HANDOFF_INICIAL.md)

**Prazo acadêmico:** apresentações/entregas entre **16 e 23/11/2026** (confirmar a data específica da turma/professora).

**Nomes:** aplicativo/projeto **ProjektTellus**, pacote `com.projekttellus`. O repositório no GitHub foi criado pelo usuário como **ProjektTellos** e não deve ser renomeado sem autorização.
