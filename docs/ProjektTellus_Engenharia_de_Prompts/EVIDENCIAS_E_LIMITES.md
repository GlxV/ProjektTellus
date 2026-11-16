# Evidências, commits informados e limitações

## Proveniência

As contagens a seguir são **relatos documentados no pacote `docs.zip`**, e não reexecuções feitas para produzir este ZIP de prompts. Algumas referências de commit vieram das conversas e podem ter sido locais, ainda não publicadas na branch principal remota.

| Etapa | Commit informado | Fonte de verificação no projeto | Métrica registrada |
|---|---|---|---|
| Implementação e revisão inicial | `00c0f9bd` (auditoria) | `HANDOFF_SOL.md`, `HANDOFF_FINAL.md` | 47 testes |
| Animações | `2c2b12c` | `VALIDACAO_ANIMACOES.md` | 54 testes |
| Problemas de UI | `9c41cbf` | issues #1/#2 e relato de correção | 57 testes |
| Modelo e óptica | `3e153f0` | `VALIDACAO_REFORMULACAO.md` | 69 testes |
| Experimentos e apresentações | `8f7e58d` | `VALIDACAO_AUDITORIA_APRESENTACAO.md` | 2.026 experimentos; 69 testes |
| Defesa individual r3 | `a06e9b7` | `VALIDACAO_DEFESA_INDIVIDUAL.md` | 650 verificações de artefatos |
| Revisão editorial r4 | `edb66c7` | `REVISAO_EDITORIAL_R4.md` | 916 verificações de artefatos |

## O que significam as métricas

- **69 testes:** testes automatizados Maven/JUnit da versão reformulada, conforme relatórios.
- **2.026 experimentos:** cenários diferentes rodados no harness da auditoria; não são 2.026 testes JUnit.
- **252.628 checagens:** asserções do harness experimental; também não equivalem à suíte JUnit.
- **650/916 verificações:** checks dos materiais da apresentação; não demonstram execução da simulação nem a qualidade da fala do estudante.

## Problemas documentados e resoluções relatadas

- A interface não cabia em resolução 1366×768 e o ComboBox de eventos tinha sobreposição; foram abertas issues separadas e registradas correções.
- O motor atribuía sustentabilidade inadequada a mundo inteiramente oceânico; os indicadores foram separados e o modelo de capacidade de suporte foi reformulado.
- A apresentação r3 mantinha conteúdo metalinguístico/“prompt-like” e a r4 retirou o checklist de requisitos, instruções de bastidores e perguntas ao aluno.

## Limitações que não devem ser escondidas

1. **Modelo educacional**, não calibrado para previsões científicas.
2. **Memento público/legível:** o record da implementação é profundamente imutável pelos componentes usados, mas não oferece opacidade clássica estrita.
3. **Arredondamento demográfico:** auditoria observou resíduos populacionais em alguns cenários inviáveis; isso não constitui previsão real.
4. **Java 25 e suporte JavaFX 3D:** execução no computador físico da faculdade não é certificada pelos testes com janelas equivalentes.
5. **PowerPoint nativo:** validações de PPTX estrutural/renderização por ferramenta não equivalem a abertura no aplicativo Microsoft PowerPoint.
6. **Autoria e entendimento:** uso de IA deve ser declarado de forma correta; esta documentação não prova compreensão individual.
7. **Proveniência dos prompts:** exceto o primeiro briefing preservado, os textos neste ZIP são reconstruções documentais e não logs originais completos.

## Links orientativos

- Repositório: https://github.com/GlxV/ProjektTellus
- Issues: https://github.com/GlxV/ProjektTellus/issues/1 e https://github.com/GlxV/ProjektTellus/issues/2
- Enunciado acadêmico: cópia textual em `fontes/ENUNCIADO_ORIGINAL_TRANSCRITO.md`.
