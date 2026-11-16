# Revisão dos 15 slides — defesa individual r3

Aluno: Matheus Luiz Bueno · Turma: 4P · Professor: EDER MAGALHAES MACHADO

Disciplina: ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS · Período: 2º Bimestre de 2026

Esta edição melhora a apresentação anterior, mantendo 15 slides, identidade escura/verde-água/azul e o planeta real. Os arquivos r2, o PDF anterior, o vídeo e a transcrição histórica foram preservados. Nenhum conteúdo dos anexos indisponíveis foi presumido. As referências à Prof. Célia pertencem ao enunciado do projeto conjunto; o professor desta defesa é o informado acima.

| Slide | Revisão concreta | Uso na conversa |
|---|---|---|
| 1 | Identificação completa: aluno, disciplina exata, professor, turma e período. Planeta e composição preservados. | Identificar o trabalho e perguntar qual caminho o professor prefere, sem começar por uma definição longa. |
| 2 | Critérios do enunciado preservados; disciplina/rodapé atualizados e notas adaptadas aos dois modos. | Abertura curta sobre o problema e os quatro critérios de cada padrão. |
| 3 | Arquitetura e dependências revisadas contra o Java; estrutura visual mantida. | Opcional no percurso curto; consultar ao localizar domínio, serviço, eventos, UI e SQLite. |
| 4 | UML e motivação do Builder preservados, com identificação e notas atuais. | Nomear cliente, builder, produto e estado; não inventar Director. |
| 5 | Método fluente repartido em linhas naturais; construção e `return this` mais fáceis de apontar. Tokens do Java preservados. | Mostrar o mesmo builder retornando e o `build()` criando Terra/EstadoPlanetario. |
| 6 | UML real do Factory revisado; `# transformar` indica corretamente protected; herança, realização e criação preservadas. | Apontar Creator, criador concreto, contrato e produto; demais famílias no estudo completo. |
| 7 | `@Override` separado; criação `new Asteroide` visível; retorno `EventoPlanetario` destacado; serviço e chamada polimórfica organizados em outro bloco. | Explicar sobrescrita, produto, inferência de `var` e `aplicar()` sem mudar a implementação para um exemplo fictício. |
| 8 | UML Observer revisado; assinante anônimo de MainController continua distinguido das classes que implementam a interface. | Identificar quem publica, qual contrato é usado e quem recebe. |
| 9 | Quatro blocos: contrato, inscrição, publicação pelo serviço e callback. `observer.aoMudar(mudanca);` em uma única linha destacada. | Distinguir entrega síncrona do bus e atualização futura agendada por `Platform.runLater`. |
| 10 | Papéis Originator/Memento/Caretaker e contrato de armazenamento mantidos; ressalva sobre record público legível preservada. | Responder sobre imutabilidade, encapsulamento e SQLite. |
| 11 | Código de captura/restauração revisado com indentação normalizada, sem alterar tokens ou strings. | Apontar verificação de UUID e reassociação de nome/estado. |
| 12 | Título exato “Os quatro padrões no fluxo do simulador”. Operações independentes separadas do fluxo interno do evento, em seis etapas. | Criar produto → retrato automático → aplicar/calcular → gravar → atualizar Terra → notificar. Captura manual e restauração são operações distintas. |
| 13 | Indicadores e limites científicos mantidos como apoio, com notas que permitem pular o slide na exposição curta. | Só aprofundar se o professor perguntar pelo motor. |
| 14 | Terceiro exemplo substituído por Asteroide 25% e restauração exata medida. Resíduo populacional tratado como limitação numérica nas notas e no rodapé. | Evidenciar teste e recuperação de estado sem transformar a defesa em uma discussão sobre sobrevivência sem O₂. |
| 15 | Demo preservada; notas incluem ciclo completo ou trilha isolada e as contagens anteriores são identificadas como evidência da base. | Demonstrar o padrão solicitado e fechar retomando construção, criação, comunicação e restauração. |

Em todos os slides: rodapé com Matheus Luiz Bueno/4P, notas conversacionais por slide e acesso ao guia de defesa. Fontes de conteúdo não foram reduzidas indiscriminadamente; os slides 5, 7, 9 e 12 foram reorganizados para criar espaço. O arquivo contém os quatro padrões com propósito, motivação, UML e Java real nos dois percursos.

**Modo A:** os 15 slides, aproximadamente 10–12 minutos sem perguntas extensas. **Modo B:** sequência recomendada `1 → 2 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 15`, aproximadamente 6min20s. Slides 3/13/14 são opcionais na abertura curta e continuam disponíveis. Se houver interrupção, abrir a classe, responder e retomar no próximo padrão; não reiniciar a apresentação.
