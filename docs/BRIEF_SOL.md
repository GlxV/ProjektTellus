# Briefing de execucao — Sol 6.1

Voce recebeu o repositório `GlxV/ProjektTellos` com a **estrutura inicial**, ainda sem simulador implementado. O nome do aplicativo e **ProjektTellus**.

**Leia antes de codificar:** `README.md`, `docs/REQUISITOS.md`, `docs/ARQUITETURA.md` e `docs/HANDOFF_INICIAL.md`.

## Objetivo

Construir o aplicativo desktop Java completo descrito nos requisitos, usando a base Maven/JavaFX ja preparada. Entregar sistemas reais e testaveis, sem criar mocks que finjam implementacao.

## Condicoes obrigatorias

1. Java 25, JavaFX/FXML, Maven e **SQLite JDBC**. POO com boa separacao de camadas.
2. Planeta 2.5D giravel, controles de O2/N2/CO2/He, clima/temperatura, vegetacao, agua/gelo, populacao, indicadores de vida e avancar tempo.
3. Eventos: impacto, supervulcao, guerra nuclear, invasao alienigena ficticia e restauracao, com intensidade configuravel.
4. Varios mundos independentes, salvar/recarregar, snapshots persistentes, restaurar estados.
5. Quatro padroes verdadeiros: **Factory Method, Builder, Observer, Memento**. Testes e diagramas UML correspondentes.
6. Modos **Sandbox** e **Apresentacao** compartilham motor e dados. O modo didatico narra acontecimentos reais em portugues comum por fluxos, sem codigo de metodos/SQL/logs tecnicos.
7. Visual bem resolvido e acessivel; tecnologias reutilizaveis para reduzir boilerplate sem empilhar frameworks a toa.
8. Modelo cientifico simplificado e honesto sobre aproximacoes.

## Estratégia para economizar quota

- Assuma ownership do desenvolvimento. Nao interrompa para solicitar confirmacoes triviais.
- Priorize primeiro compilacao, domínio, persistencia, padroes e testes; depois globo, eventos integrados, UX e apresentacao.
- Execute `mvn test` e `mvn javafx:run` quando houver GUI. Registre o que nao conseguir verificar; nao invente testes.
- Corrija falhas e evite retrabalho estetico antes do MVP estar funcional.
- Reaproveite o esqueleto existente, alterando-o livremente se tecnicamente necessario.
- Registre conclusoes/pendencias no repo, com commits logicos, em vez de longos relatorios na conversa.
- Nao crie trabalho duplicado (dois aplicativos ou dois motores para os modos).

## Criterios de aceite

Cenarios demonstraveis: criar Terra, editar gases e indice de vida, aplicar evento com consequencias, salvar, fechar e reabrir, restaurar snapshot, comparar com Terra intacta; alternar para modo Apresentacao e explicar os quatro padroes por fluxos reais.

## Handoff obrigatorio

Crie `docs/HANDOFF_SOL.md` com: ultimo commit, resumo real do que esta pronto, testes executados e resultados, pendencias e limitações, classe/arquivo de cada padrao, passos de demonstracao e comandos para proximo agente. Evite afirmar que tudo funciona sem validar.

Nao empacote "ProjektTellus.jar" como executavel standalone sem configurar as dependencias e o runtime JavaFX. O repo GitHub se chama `ProjektTellos` por escolha atual do usuario; nao renomeie sem pedido.
