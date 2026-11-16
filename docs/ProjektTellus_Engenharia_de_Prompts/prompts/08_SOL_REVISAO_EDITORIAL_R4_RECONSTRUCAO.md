# Etapa 8 — Sol: revisão editorial r4 para retirar textos artificiais

**Status: reconstrução documental.**

## Problema descoberto

Nos slides havia frases como **“O enunciado pede”**, **“No mínimo 3 padrões”**, **“Disponíveis conforme a pergunta”** e **“Entender e defender”**. Isso tornava o material uma checklist de preparação, não uma apresentação natural do software.

## Prompt reconstruído

> Revise os quinze slides, PDF, PowerPoint, notas e scripts da r3. Elimine do conteúdo **visível** checklists do enunciado, instruções ao apresentador, comentários de bastidor, perguntas de estudo, justificativas defensivas e linguagem genérica que pareça texto de prompt. Não retire informação técnica necessária nem altere o significado do Java ou UML.
>
> No slide 2, substitua inteiramente o checklist por funcionalidades reais: construir mundos, executar eventos, comunicar mudanças e recuperar estados. Nos slides de padrões, mantenha propósito, problema real, estrutura UML e código funcional, mas mova as ressalvas avançadas (Director, opacidade do Memento, callbacks, arredondamento) para notas ou materiais de estudo quando não forem necessárias na tela.
>
> No slide 12, retire “disponíveis conforme a pergunta” e descreva as operações reais do serviço. No slide 15, retire “Entender e defender” e as perguntas para o aluno; escreva uma conclusão sobre o que Builder, Factory Method, Observer e Memento realmente fazem no projeto. Revise as 30 falas A/B para um registro natural, sem sacrificar precisão.
>
> Preserve os quinze slides, identidade visual e r3. Atualize fontes de geração, gere PPTX/PDF r4, reimporte e renderize para inspeção em 1366×768, compare Java com os arquivos verdadeiros e registre cada alteração sem alegar testes não feitos. Salve relatório editorial e commit.

## Evidência

`docs/REVISAO_EDITORIAL_R4.md` registra as substituições, validação dos quinze slides e **916 verificações dos materiais**, sem falhas. O documento explicita que **não houve nova rodada Maven nem teste no computador físico da faculdade**.
