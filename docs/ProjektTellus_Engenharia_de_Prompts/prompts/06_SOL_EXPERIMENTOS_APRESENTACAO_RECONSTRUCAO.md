# Etapa 6 — Sol: auditoria experimental e apresentação baseada no PDF

**Status: reconstrução documental do pedido trabalhado na conversa.** O enunciado acadêmico transcrito foi fornecido como fonte primária; uma cópia histórica está em `../fontes/ENUNCIADO_ORIGINAL_TRANSCRITO.md`.

## Prompt reconstruído

> Faça uma auditoria experimental completa do motor atual e depois prepare a apresentação da disciplina de Arquitetura de Softwares e Padrões de Projetos. A apresentação **deve seguir o enunciado original**, e não somente demonstrar as funcionalidades bonitas do aplicativo.
>
> Inspecione regras, fórmulas e domínio. Varra concentrações controladas de O₂, N₂, CO₂ e He, pressões e temperaturas representativas, água, gelo, vegetação, radiação e população. Respeite a soma de gases e os limites do domínio; registre valores inválidos como rejeições. Analise evolução após 1, 10, 100 e 1.000 anos quando permitido. Aplique os cinco eventos em várias intensidades e teste recuperação via Memento.
>
> Para cada caso, registre entrada, saída, três indicadores, capacidade de suporte, mudanças temporais e aparência. Classifique achados em comportamento esperado, simplificação documentada, suspeita de incoerência e bug reproduzido. Gere CSV, relatório, gráficos e capturas sem inventar resultados. Em resposta final, apresente resultados surpreendentes **com números**, e não só a frase “testes passaram”. Preserve saves pessoais usando bases temporárias.
>
> Para a apresentação, respeite o PDF: no mínimo três padrões escolhidos da lista permitida, e para cada padrão **nome/propósito, problema/solução real, UML das classes e Java funcional**. Apresente os quatro padrões usados: Builder, Factory Method, Observer e Memento. Faça slides legíveis em 1366×768, trechos verdadeiros de código, diagramas UML fiéis ao fonte, roteiro de fala, sequência de demo, perguntas prováveis e plano de contingência para Java 25/JavaFX. Inclua matriz de conformidade com o PDF e referências dos arquivos Java.
>
> Execute testes, documente o que de fato foi verificado, faça commit de documentação e artefatos apropriados. A apresentação é acadêmica sobre padrões, não uma palestra de ciência planetária.

## Evidência documental

`docs/VALIDACAO_AUDITORIA_APRESENTACAO.md` registra **2.026 experimentos**, **4.125 estados**, **252.628 checagens** e **137 rejeições esperadas**, com **69 testes JUnit** em Maven. Os dados estão documentados em `docs/auditoria/experimental-20261115-final/` no projeto original. As checagens do harness não equivalem a testes JUnit.
