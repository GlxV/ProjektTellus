# Handoff inicial — preparar para Sol 6.1

## Realizado nesta etapa

- Repositorio remoto inicializado na `main`.
- `pom.xml` com dependencias definidas e compiler Java 25.
- Entrypoint JavaFX minimo com tela de boas-vindas via FXML e CSS.
- JUnit smoke tests que verificam recursos iniciais.
- Pastas e documentos de requisitos/arquitetura.
- `.gitignore` para bancos locais, binarios, IDE e segredos.

## NAO realizado

- Nao ha motor planetario, Terra, gas, eventos, globo funcional, saves, banco SQLite inicializado, quatro padroes ou Modo Apresentacao.
- Nao ha release nem JAR autossuficiente.
- Nao ha testes de interface graficos nem diagramas UML finais.

## Verificacao

Estrutura e referencias conferidas no GitHub. **Build e GUI locais ainda nao foram executados por este preparador**. O Sol deve comecar com `java -version`, `mvn -version`, `mvn test` e `mvn javafx:run` e resolver qualquer problema.

## Proximos passos do Sol

Ler `docs/BRIEF_SOL.md`, `docs/REQUISITOS.md` e `docs/ARQUITETURA.md`; implementar dominios e SQLite, padroes, eventos, globo, interfaces e testes; gerar `docs/HANDOFF_SOL.md` depois de validar.
