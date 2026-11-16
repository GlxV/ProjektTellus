# Validação da auditoria e apresentação — 15/11/2026

Base inspecionada: checkout inicialmente limpo, `3e153f07d8e7c5a6d65975afe5b06984be9f576f`. O commit de entrega acrescenta materiais e harnesses; **nenhum fonte de produção, migration, dependência Maven ou teste preexistente foi alterado**. O enunciado transcrito pelo estudante é a fonte acadêmica primária.

## Evidência executada

| Verificação | Resultado | Recibo |
|---|---|---|
| Maven inicial e final, `mvn -B -ntp -Pgui-tests verify` | 69 testes; 0 falhas, erros ou skips; BUILD SUCCESS nas duas execuções | [Inicial](evidencias/verify-baseline.txt), [final](evidencias/verify-final.txt) |
| Auditoria canônica | 2.026 experimentos, 4.125 estados, 252.628 checagens, 137 rejeições esperadas | [Resumo](auditoria/experimental-20261115-final/summary.json), [CSV](auditoria/experimental-20261115-final/snapshots.csv) |
| Outro processo experimental | Hashes idênticos para estados, fatores, agregados e rejeições; IDs/instantes de Memento não são determinísticos | [Reprodução](auditoria/experimental-20261115-final/reproducibility.json) |
| Fluxo real JavaFX/FXML/controller/SQLite | 249 verificações do harness; 33 PNGs; Builder, 5 eventos, Observer, restauração e reabertura | [Capturas canônicas](capturas/latest.json), [demo](DEMO_APRESENTACAO.md) |
| JAR empacotado real | Janela aberta, esquema V1 e quick_check=ok, banco temporário vazio, WM_CLOSE e saída 0 | [Smoke do pacote](evidencias/package-launch.json) |
| PPTX | 15 slides; Java e UML nativos editáveis; integridade, geometria, fontes e reimportação aprovadas | [Recibo](evidencias/ProjektTellus-Apresentacao-r2.validacao.json) |
| Materiais | Fontes/hashes, valores solicitados, arquivos, contagens, 16 critérios acadêmicos e recibos aprovados | [Validador](evidencias/materiais-validacao.json) |
| PDF | 15 páginas renderizadas em 1366×768, montadas a partir dos slides inspecionados | [Recibo PDF](evidencias/pdf-validacao.json) |
| Vídeo | 24 s, H.264, 1366×768, 24 fps, yuv420p, sem áudio; arquivo existente e decodificação integral sem erro | [Recibo vídeo](evidencias/video-validacao.json) |
| Saves pessoais | SHA256 do banco pessoal igual antes/depois; experimentos e GUI somente em bancos temporários | [Antes](evidencias/saves-antes.json), [depois](evidencias/saves-depois.json) |

As contagens de checagens experimentais, verificações do harness e verificações de arquivos **não são somadas aos 69 testes JUnit**. Rodadas preparatórias e repetição ficam em `target/audit-drafts` e `target/capture-drafts`, ignorados pelo Git, para não misturar resultados canônicos. A primeira falha do harness gráfico foi um atraso na propagação do tamanho da Scene; a espera explícita corrigiu o próprio harness e a falha foi preservada em `capturas/initial-attempt.log`.

## Revisão técnica e visual

Os 15 slides foram inspecionados individualmente em 1366×768; as quatro páginas de UML têm PNGs em 2732×1536. A revisão corrigiu a ordem interna Factory → Memento, a referência do CSV, uma generalização sobre extinção, sobreposição no diagrama Observer e quebras automáticas de Java dentro de literais String. O validador confirma que as quebras finais preservam os tokens e não introduzem newline dentro de strings. Foram inspecionados os sete gráficos e amostras das capturas/eventos; não foi feita inspeção visual dos 4.125 estados.

O PDF é uma contingência raster, sem Java pesquisável. O exportador PDF direto do runtime falhou por `#standard-fonts/Helvetica`; ReportLab/PyMuPDF produziram e renderizaram o arquivo final sem alterar a biblioteca. O vídeo é uma **sequência legendada de capturas reais**, e os quadros intermediários dos eventos usam relógio controlado no animador: nenhuma dessas evidências mede FPS ou duração fluida de animação. O PPTX não foi aberto em Microsoft PowerPoint; houve validação estrutural/reimportação e renderização pela biblioteca.

## Pendências externas e limites

- O computador físico/projetor da faculdade não foi testado. O host local executou JavaFX 3D e escala 100%; janela equivalente não certifica outro equipamento, DPI, driver ou tempo de resposta.
- Nome do estudante e curso/turma não foram fornecidos nesta sessão; a capa mantém projeto e disciplina sem inventar identificação. O vínculo com a disciplina conjunta e autoria individual dependem de declaração verdadeira do estudante.
- O texto integral das duas páginas foi fornecido pelo estudante; o PDF binário e os dois anexos não estavam disponíveis. Não foi inventado conteúdo dos anexos.
- Os quatro padrões atendem os 16 itens de conteúdo do enunciado. A compreensão do aluno, apresentação oral, nota e aceite da docente não são certificáveis pelos arquivos.
- O motor é didático, global e não calibrado cientificamente. O patamar demográfico de 2/4 pessoas é uma suspeita de incoerência numérica reproduzida; foi documentado, sem alterar o motor auditado. Atmosferas hipotéticas com B alto não demonstram vida real.

## O que abrir

[Índice da apresentação](apresentacao/README.md) → PPTX r2 ou PDF → `Apresentar-ProjektTellus.cmd` para banco temporário → [demo](DEMO_APRESENTACAO.md). Para estudar, use [ESTUDO_PADROES](ESTUDO_PADROES.md), [Java completo](CODIGO_JAVA_APRESENTACAO.md), [roteiro](ROTEIRO_APRESENTACAO.md) e [62 perguntas](PERGUNTAS_APRESENTACAO.md). Duração alvo de 10min40s, ajustável: o enunciado não fixa tempo.
