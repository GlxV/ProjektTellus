# ProjektTellus — requisitos e aceite

> Contrato de produto acordado antes da implementacao. Nao confundir com funcionalidade pronta.

## Proposito

Simulador educacional e ficcional de cenarios planetarios, desenvolvido em Java com POO forte e quatro padroes de projeto. Tem como objetivo explorar consequencias simplificadas de variacoes ambientais, nao realizar previsoes cientificas reais.

## Obrigatorios

1. Globo terrestre visual e giravel com mouse, zoom e alteracoes visuais associadas ao estado da simulacao. Pode ser esfera JavaFX com textura 2D (experiencia 2.5D).
2. Atmosfera editavel com O2, N2, CO2, He, valores validados e composicao consistente (preferir quantidades/pressao parcial e derivar porcentagens).
3. Temperatura/clima, vegetacao, agua/gelo e populacao modelados de maneira simplificada; avancar tempo; modelos estaveis e previsiveis.
4. Dois indicadores diferentes: habitabilidade humana e potencial de vida biologica; nenhum deve ser apresentado como classificacao cientifica validada.
5. Eventos com intensidade e consequencias no modelo: asteroide, supervulcao, guerra nuclear, invasao alienigena *ficticia* e restauracao ecologica.
6. Mundo independente: criar, nomear, copiar, carregar, salvar, excluir. Estado e historico persistidos em **SQLite obrigatorio**, sem depender de banco remoto.
7. Snapshots/restauracao imutaveis para demonstrar **Memento**; mundo A nunca interfere no mundo B. Diferenciar snapshot em memoria de persistencia no banco.
8. **Factory Method**, **Builder**, **Observer** e **Memento**, implementados corretamente e demonstraveis; ao menos 3 sao requeridos no trabalho, adotamos 4.
9. Dois modos do **mesmo aplicativo**: Sandbox e Apresentacao. Sem motores duplicados.
10. Modo Apresentacao: mostrar fluxos que aconteceram de verdade com frases naturais em portugues, etapas visuais e explicacao curta do padrao. **Nao mostrar codigo, nomes de metodos, SQL ou stack traces na apresentacao principal.**
11. Projeto Maven, testes automatizados, UML dos padroes, README para reproducao, instrucoes Windows e demonstracao repetivel.

## Casos de demonstracao obrigatorios

- **Builder:** criar uma Terra editando condicoes iniciais; explicar montagem/validacao.
- **Factory Method:** escolher um evento e observar sua criacao e aplicacao.
- **Observer:** executar evento ou alterar parametros, mostrando notificacoes dos subsistemas interessados.
- **Memento:** salvar estado, provocar catastrofe e restaurar. Demonstrar que outro mundo continua intacto.
- **SQLite:** fechar/reabrir aplicativo e recuperar ao menos dois mundos, eventos e snapshots.

## Qualidade e seguranca

- Validar entradas e impedir NaN/infinito, quantidades negativas, pressao invalida e saves inconsistentes.
- JDBC com PreparedStatement, fechamento de recursos, transacoes multi-tabela e migrations/inicializacao idempotente.
- Interface nao deve congelar em operacoes demoradas. Sem guardar caminhos/segredos pessoais no repositorio.
- Padrões e POO devem ter responsabilidade real. Preferir composicao em vez de heranca artificial.
- Assets (textura de Terra) precisam de origem/licenca registradas.
- Eventos destrutivos afetam somente a Terra **simulada**, nao arquivos pessoais.
- Nao assumir testes/funcionalidades aprovados sem evidencia.

## Escopo fora da versao academica

Fisica orbital rigorosa, biologia realista, sistemas regionais com milhares de celulas, multiplayer, previsoes ambientais verificadas e exploracao espacial complexa.

## Prazo e entregas academicas

Disciplina: Arquitetura de Software e Padroes de Projetos, bimestre 2/2026, UNIVEL. Apresentacao/entrega entre **16 e 23 de novembro de 2026**; data exata a confirmar com docente. Preparar classe/proposito/motivacao de cada padrao, codigos reais e diagramas UML.
