# ProjektTellus

**Simulador Planetário Interativo** — aplicação desktop acadêmica de Arquitetura de Software e POO. Crie Terras independentes, altere seu ambiente, provoque eventos e restaure retratos anteriores. Sandbox e Apresentação compartilham o mesmo motor e banco SQLite.

O modelo é **didático e global**, com aproximações explícitas. Os indicadores não representam previsões científicas nem certificação de um ambiente habitável. Invasão alienígena é ficção.

## Executar no Windows x64

Abra **`Abrir-ProjektTellus.cmd`**. Na primeira execução, se necessário, o lançador baixa Java 25 (Eclipse Temurin) e Maven para `.tools/`, verifica os downloads e compila o projeto. Não altera o Java padrão do Windows. Internet é necessária para preparar ferramentas e dependências; depois o JAR e o banco funcionam localmente.

Para compilar e testar:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build.ps1
```

Com **JDK 25** e **Maven 3.9+** já configurados:

```powershell
mvn clean verify
java --enable-native-access=ALL-UNNAMED -jar target/ProjektTellus.jar
# Alternativa de desenvolvimento:
mvn javafx:run
```

Teste gráfico opcional, com um desktop disponível: `mvn -Pgui-tests test`. Ele usa um banco temporário, percorre os controles/diálogos reais e gera capturas em `target/gui-verification/`. Sem esse perfil, a suíte não inicia JavaFX.

`target/ProjektTellus.jar` inclui dependências JavaFX/JDBC; **requer Java 25**, cujo runtime não está embutido. O pacote gerado neste ambiente é Windows x64; em outro sistema, compile novamente para obter os binários nativos correspondentes.

Banco automático: `data/projekttellus.db`, relativo ao diretório de execução. O lançador fixa esse diretório na raiz do projeto. Para outro banco:

```powershell
java --enable-native-access=ALL-UNNAMED -Dprojekttellus.db=C:/Mundos/demo.db -jar target/ProjektTellus.jar
```

## Usar e demonstrar

1. Crie um mundo e personalize condições iniciais (**Builder**).
2. Arraste o globo para girar; use a roda para aproximar/afastar. A aparência acompanha condições ambientais.
3. Ajuste os controles ambientais. Os gases devem somar **100%**; água e gelo juntos podem ocupar até 100% da superfície. Pressão em atm, temperatura em °C, população em pessoas e tempo em anos.
4. Escolha um dos cinco eventos e intensidade (**Factory Method**).
5. Observe os indicadores separados e alertas (**Observer**).
6. Capture um retrato, altere o planeta e restaure pelo histórico (**Memento**).
7. Duplique o mundo, modifique um deles e carregue o outro para mostrar isolamento. Feche e reabra para mostrar SQLite.
8. Ative **Modo apresentação**: a sequência reproduz a última operação concluída, com avanço manual, explicações e comparação antes/depois.

Mudanças confirmadas são salvas automaticamente; há também ação explícita de salvar. Cada alteração ambiental/evento/avanço/restauração preserva um retrato anterior na mesma transação que novo estado e histórico. A exclusão confirmada elimina somente o mundo escolhido, seu histórico e retratos.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Maven do preparador | 3.9.11 |
| JavaFX (FXML e 3D) | 25.0.1 |
| AtlantaFX (Primer Dark) | 2.0.1 |
| SQLite JDBC | 3.50.3.0 |
| JUnit Jupiter | 5.13.4 |

Versões e plugins no `pom.xml`. Records reduzem código repetitivo. Jackson foi dispensado por não haver serialização JSON que justifique outra dependência; SQLite guarda estados em colunas.

## Organização

```text
src/main/java/com/projekttellus/
  domain/                 Terra, componentes e Builder
  simulation/             Indicadores e evolução determinística
  events/                 Eventos e criadores concretos
  patterns/memento/       Retrato imutável
  patterns/observer/      Publicador, alertas e estatísticas
  application/            Casos de uso, caretaker e passos reais
  persistence/            Repository, JDBC, migrations e transações
  ui/                     Controlador, editores, globo e apresentação
src/main/resources/       FXML, CSS e migration SQL
src/test/java/            JUnit de comportamento e integração
docs/                     Modelos, diagramas e transferência
```

- [Arquitetura e UML](docs/ARQUITETURA.md)
- [Os quatro padrões](docs/PADROES.md)
- [Modelo matemático e limites](docs/MODELO.md)
- [Recursos visuais](docs/ASSETS.md)
- [Transferência e validação](docs/HANDOFF_SOL.md)
- [Handoff final da auditoria](docs/HANDOFF_FINAL.md)
- [Requisitos originais](docs/REQUISITOS.md)

Referências técnicas: [compatibilidade JavaFX 25/JDK 25](https://docs.oracle.com/en/java/java-components/javafx/25/javafx-users-guide/supported-jdk-versions.html), [JavaFX com Maven](https://openjfx.io/openjfx-docs/), [API oficial Temurin](https://github.com/adoptium/api.adoptium.net).

O diretório local, aplicativo e remoto configurado se chamam **ProjektTellus**. O endereço remoto existente foi preservado.
