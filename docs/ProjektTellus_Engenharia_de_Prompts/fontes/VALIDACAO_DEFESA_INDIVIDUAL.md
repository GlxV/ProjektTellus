# Validação da defesa individual — r3

Revisão dos materiais do commit-base `8f7e58d`, concluída em 15/11/2026. O código do simulador não foi alterado.

**Aluno:** Matheus Luiz Bueno · **Turma:** 4P

**Disciplina:** ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS

**Professor:** EDER MAGALHAES MACHADO

**Trabalho:** ProjektTellus — Simulador Planetário · **Período:** 2º Bimestre de 2026

## Artefatos e revisão visual

- [PowerPoint r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pptx): 15 slides editáveis e 15 notas de apresentação, com os modos A e B.
- [PDF r3](apresentacao/ProjektTellus-Defesa-Individual-r3.pdf): 15 páginas, exportadas das mesmas imagens revisadas. O PDF é raster; use o PowerPoint ou os documentos Java para selecionar/pesquisar código.
- Os 15 slides foram renderizados e inspecionados em 1366×768. Os quatro UML também foram exportados em 2732×1536, na pasta [defesa-r3](apresentacao/defesa-r3/).
- Os códigos foram reorganizados sem alterar tokens Java ou literais. O menor tamanho de fonte nos trechos de código é 17 pontos. A chamada `observer.aoMudar(mudanca);` ocupa uma única linha.
- As 15 páginas do PDF foram renderizadas e comparadas aos previews: erro médio máximo por canal **0,0**, na resolução verificada.

O [validador da revisão](../scripts/Validate-DefenseRevision.py) executou **650 verificações, com zero falhas**. A inspeção do pacote, das dimensões e das fontes também não encontrou problemas. Os recibos distinguem esses resultados de execução nativa no Microsoft PowerPoint.

## Conteúdo acadêmico

Builder (4–5), Factory Method (6–7), Observer (8–9) e Memento (10–11) possuem individualmente nome/propósito, problema/solução, UML do projeto e Java real: **16 combinações de padrão e critério**, registradas na [matriz de conformidade](CONFORMIDADE_TRABALHO.md).

O modo A usa os 15 slides em aproximadamente **10min40s**; o modo B usa `1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15`, em aproximadamente **6min20s**. Perguntas extensas e exploração livre não estão incluídas. Esses tempos são sugestões, não limites do enunciado.

O material inclui quatro explicações em três níveis, 62 perguntas anteriores e 21 itens de arguição com respostas curtas e aprofundadas. Os 21 itens retomam alguns temas anteriores; não representam 83 assuntos distintos. Foram validadas **30 âncoras de navegação** para fontes reais, usando `-ListOnly`, sem abrir editor ou aplicativo.

## Revisão técnica independente

Dois apontamentos foram corrigidos antes da exportação final:

- O UML do Factory Method agora identifica `Asteroide.transformar` como protegido (`#`), conforme o Java.
- A resposta sobre falha do SQLite diferencia falha antes do commit, tentativa de rollback e possível exceção ao restaurar autocommit após um commit bem-sucedido. Não promete rollback de uma gravação já confirmada.

O fluxo do slide 12 separa construção, captura manual e restauração. Dentro da aplicação de evento, apresenta criação do produto → captura automática → cálculo → persistência → atualização de Terra → publicação. A publicação do bus é síncrona; o assinante JavaFX agenda a atualização com `Platform.runLater`.

O terceiro experimento do slide 14 foi substituído pela recuperação por Memento após Asteroide 25%, com medidas anteriores reais: habitabilidade **100 → 64,73 → 100**, temperatura **15 → 8 → 15 °C**, população **8 → 6,5 → 8 bilhões**. O resíduo de duas pessoas sem oxigênio continua explicitamente documentado como limitação numérica de arredondamento, sem interpretação científica de sobrevivência.

## Preservação e limites da evidência

Os hashes de **77 arquivos protegidos** permaneceram idênticos: produção e recursos de `src/main`, transcrição histórica, PPTX r2, PDF anterior, vídeo de contingência e banco pessoal. O vídeo preservado é uma sequência de capturas reais de 24 segundos, não uma gravação contínua. A referência histórica à Prof. Célia não foi modificada.

Esta revisão **não repetiu Maven, testes GUI ou experimentos do simulador**, pois alterou materiais e geradores. As 69 aprovações, os 2.026 experimentos e os resultados da demonstração pertencem à entrega anterior, com seus recibos preservados. A revisão não produziu novas medições planetárias.

Não foi realizada abertura nativa no Microsoft PowerPoint nem validação no computador físico/projetor da faculdade. Renderização equivalente em 1366×768 não substitui esse ensaio. Essas são as pendências operacionais; não faltam os quatro critérios textuais exigidos para cada padrão.

## Evidências

- [650 verificações e hashes finais](evidencias/defesa-r3-validacao.json).
- [Integridade e layout do PPTX](evidencias/ProjektTellus-Defesa-Individual-r3.validacao.json).
- [Exportação do PDF](evidencias/ProjektTellus-Defesa-Individual-r3.pdf-validacao.json).
- [Hashes anteriores preservados](evidencias/defesa-r3-preservacao.json).
- [30 âncoras de fontes](evidencias/defesa-r3-source-navigation.json).
- [Mudanças nos 15 slides](ALTERACOES_SLIDES_DEFESA.md).

Para reexecutar a validação da edição entregue: `python scripts/Validate-DefenseRevision.py`. Para gerar outra edição, use novos nomes e pastas conforme o [índice](apresentacao/README.md); o gerador recusa sobrescrever os artefatos existentes.
