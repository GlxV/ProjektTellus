# Metodologia de prompting no ProjektTellus

## 1. Estrutura utilizada nos pedidos

**Papel e objetivo** → **estado atual do repositório** → **arquivos relevantes** → **comportamento esperado** → **restrições de compatibilidade** → **testes/evidências** → **critérios de aceite** → **entrega/handoff**.

As instruções ficaram mais específicas conforme o projeto cresceu. A primeira rodada descreveu o aplicativo completo; as posteriores definiram interfaces e limites de mudança, como “não tocar no motor de simulação para resolver um defeito de CSS”.

## 2. Evolução dos ciclos

| Ordem | Agente/etapa | Natureza da solicitação | Entrega ou evidência associada |
|---:|---|---|---|
| 1 | Sol — implementação | Construir aplicação Java e quatro padrões | `docs/BRIEF_SOL.md`, `HANDOFF_SOL.md` |
| 2 | Luna — revisão | Auditar padrões, persistência, UML e GUI | `HANDOFF_FINAL.md` — prompt literal ausente |
| 3 | Sol — animações | Cinco efeitos distintos e pulso Memento | `VALIDACAO_ANIMACOES.md` |
| 4 | Sol — issues #1/#2 | Responsividade e dropdown | Issues e commit informado `9c41cbf` |
| 5 | Sol — física didática/óptica | Três indicadores, suporte populacional e halo | `VALIDACAO_REFORMULACAO.md` |
| 6 | Sol — auditoria/defesa | Matriz de experimentos e slides baseados no PDF | `VALIDACAO_AUDITORIA_APRESENTACAO.md` |
| 7 | Sol — defesa individual | Identificação, conversa 1:1, Java e UML | `VALIDACAO_DEFESA_INDIVIDUAL.md` |
| 8 | Sol — r4 editorial | Eliminar checklist, metatextos e falas artificiais | `REVISAO_EDITORIAL_R4.md` |

## 3. Exemplos de especificação melhorada

**Pouco verificável:** “Arruma a interface.”

**Verificável:** “Em Windows 1366×768, escala 100%, permitir rolagem dos controles, manter o popup do ComboBox opaco e testável por mouse e teclado; preservar responsividade e registrar evidência gráfica.”

**Pouco seguro:** “Faz animações melhores.”

**Com limites:** “As animações começam apenas após operação confirmada; não modificam SQLite, não executam evento duas vezes, podem ser puladas, são canceladas ao trocar de mundo e devem respeitar modo reduzido.”

**Pouco transparente:** “O modelo é cientificamente correto.”

**Com premissas explícitas:** “Modelo educacional, não calibrado, com testes extremos, registro de simplificações e análise de incoerências.”

## 4. Validação ≠ instrução

Um prompt não prova que uma tarefa foi concluída. É preciso distinguir:
- requisito *solicitado*;
- implementação *declarada pelo agente*;
- evidência registrada em testes, screenshots e relatórios;
- itens ainda *não verificados* (PowerPoint nativo, computador físico da faculdade, precisão científica).

## 5. Uso acadêmico apropriado

A defesa do trabalho deve centrar-se em **Builder, Factory Method, Observer e Memento**, com propósito, motivação, UML e código Java funcional. Os prompts são documentação complementar do processo de desenvolvimento, não substituem o entendimento individual do código.
