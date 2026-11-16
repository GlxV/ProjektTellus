# Revisão editorial da apresentação r4

Base: commit `a06e9b7`, edição r3. A revisão mantém 15 slides, identificação acadêmica de Matheus Luiz Bueno/4P, professor EDER MAGALHAES MACHADO, disciplina ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS e 2º Bimestre de 2026. O tema escuro, verde-água e azul foi preservado.

## Alterações por slide

| Slide | Revisão editorial |
|---|---|
| 1 | Capa mantida, com identificação completa e planeta original. |
| 2 | Retirados “O enunciado pede”, “No mínimo 3 padrões” e os quatro itens de avaliação. O espaço apresenta construção de mundos, eventos planetários, comunicação de mudanças e recuperação de estados. O título passou a “Mundos, eventos e histórico”. |
| 3 | Arquitetura mantida. A fala explica o caminho da ação entre interface, serviço, domínio e repositório. |
| 4 | “Sem Director separado” saiu do slide e permanece no apoio técnico. A legenda agora descreve editor, builder e criação da Terra. Propósito reescrito de forma direta. |
| 5 | Java preservado, com indentação de build() alinhada. A explicação trata do retorno do builder e da criação de uma Terra. |
| 6 | O inventário “Outros pares Creator/produto...” virou uma frase sobre os demais eventos usarem a mesma estrutura de criação. UML preservado. |
| 7 | Código e anotações técnicas mantidos. A fala segue sobrescrita, produto, contrato e aplicação do evento. |
| 8 | A observação extensa sob o UML virou “MainController inscreve um observador para atualizar a interface”. O callback anônimo e o tratamento de exceções continuam no apoio. |
| 9 | Mantidos os quatro blocos, callback em uma linha e distinção entre bus síncrono e agendamento JavaFX. A fala foi simplificada. |
| 10 | A ressalva sobre record público e opacidade saiu do conteúdo visível. A legenda explica preservação de nome/estado e exigência do UUID do mesmo mundo. A limitação continua nas notas e no estudo. |
| 11 | A sequência de instruções para a demonstração foi substituída por uma explicação do efeito da restauração: recuperar nome/estado mantendo a identidade. O Java não mudou. |
| 12 | “Operações independentes, disponíveis conforme a pergunta” virou “Construção, captura e restauração de um mundo”. A sequência interna do serviço continua explícita. |
| 13 | Título alterado para “Indicadores e evolução do planeta”. Mantidos os três indicadores, a pressão parcial e os limites científicos pertinentes ao modelo. |
| 14 | Título “Resultados dos experimentos”. Retirado o rodapé sobre resíduo demográfico, desconectado dos três exemplos. As três comparações reais ganharam espaçamento; a limitação de arredondamento permanece no apoio, sem ser tratada como sobrevivência prevista. |
| 15 | Retirados “Entender e defender” e as quatro perguntas ao apresentador. A conclusão apresenta o mundo restaurado e o papel de cada padrão, junto às funcionalidades e contagens históricas verificadas. |

O [roteiro A/B](ROTEIRO_APRESENTACAO.md) foi revisado nos 15 slides. As falas tratam do programa; os campos técnicos e de demonstração são consultas separadas. A exposição continua planejada para 10min40s no modo A e 6min20s no B, sem perguntas extensas. Esses tempos são sugestões.

## Conteúdo técnico preservado

Os quatro padrões continuam com propósito, problema/solução, UML e Java real nos pares 4–5, 6–7, 8–9 e 10–11. Não há checklist acadêmico no slide 2. A [matriz](CONFORMIDADE_TRABALHO.md) registra os critérios fora da apresentação.

Foram mantidos: construção fluente; criação por subclasses de EventoCreator; contrato EventoPlanetario; entrega síncrona do ObserverBus e Platform.runLater; restauração vinculada a UUID; criação do produto, captura automática, cálculo, persistência, atualização e notificação nessa ordem. A discussão do Memento legível, sem opacidade estrita, continua em [DEFESA_CODIGO_JAVA.md](DEFESA_CODIGO_JAVA.md) e [ESTUDO_PADROES.md](ESTUDO_PADROES.md).

## Arquivos e reprodução

- [PowerPoint r4](apresentacao/ProjektTellus-Defesa-Individual-r4.pptx), com texto, Java, UML e notas editáveis.
- [PDF r4](apresentacao/ProjektTellus-Defesa-Individual-r4.pdf), com as mesmas 15 imagens renderizadas; o PDF é raster e não oferece código pesquisável.
- [Previews e UML r4](apresentacao/editorial-r4/).
- Geradores atualizados em scripts/Build-AcademicDeck.mjs e scripts/Build-AcademicMaterials.ps1. O [índice](apresentacao/README.md) explica a geração em novas pastas, sem sobrescrever edições anteriores.

## Verificação

Os 15 slides foram renderizados e inspecionados individualmente em 1366×768. A inspeção incluiu o texto visível de títulos, blocos, legendas, código e rodapés. Uma revisão independente conferiu texto, UML, trechos e ordem das operações. A indentação final do slide 5 e o espaçamento final do slide 14 foram renderizados novamente e inspecionados.

Os resultados automáticos ficam no [recibo editorial](evidencias/editorial-r4-validacao.json), a integridade do pacote no [recibo do PowerPoint](evidencias/ProjektTellus-Defesa-Individual-r4.validacao.json), e a exportação no [recibo do PDF](evidencias/ProjektTellus-Defesa-Individual-r4.pdf-validacao.json). O [inventário de textos](evidencias/editorial-r4-textos.json) permite comparar os textos visíveis r3/r4 e consultar as notas novas.

Resultado da execução do validador: **916 verificações, zero falhas**. As 15 páginas renderizadas do PDF coincidiram com os previews em 1366×768, com erro médio por canal igual a zero. O PowerPoint final também foi reimportado e seus 15 slides renderizados pelo Artifact Tool: diferença média máxima inferior a 0,0001 por canal, limitada a traços dos diagramas. O [recibo de renderização do PPTX](evidencias/editorial-r4-pptx-render.json) registra as comparações. Os 11 recortes Java continuam presentes e correspondem às fontes; as relações UML mantiveram a geometria e os participantes da r3. Essas verificações complementam a leitura editorial e a inspeção visual, sem medir se a fala foi compreendida pelo estudante.

Os hashes de 118 arquivos protegidos são conferidos pelo validador: artefatos anteriores, produção, transcrição e banco pessoal. R3, r2 e vídeo de contingência permanecem preservados. O [manifesto anterior](evidencias/editorial-r4-preservacao.json) foi capturado antes da revisão desses materiais; o índice atual é um documento editado intencionalmente e não integra a lista de arquivos imutáveis.

Esta revisão não alterou o motor nem repetiu testes Maven, GUI ou experimentos. As contagens de 69 testes e 2.026 experimentos pertencem aos registros anteriores. Não houve abertura no Microsoft PowerPoint nem teste no computador físico da faculdade. Renderização local e verificações de pacote não substituem esse ensaio.

Para repetir as verificações da r4, defina TELLUS_ARTIFACT_MODULE com o caminho do runtime e execute primeiro `scripts/Render-AcademicPptx.mjs` com o Node do runtime, depois `python scripts/Validate-EditorialRevision.py`. O [índice](apresentacao/README.md) descreve as dependências.
