# Capturas reais

A execução canônica está indicada em [latest.json](latest.json): `run-20261115-124710`, 33 PNGs, 249 verificações de harness. Consulte [o roteiro](../DEMO_APRESENTACAO.md) e [o ambiente](../AMBIENTE_APRESENTACAO.md).

`terra-referencia.png`, `demo-restaurado.png` e `planeta-*.png` são cópias byte a byte das imagens canônicas, para uso pelo deck. Os valores completos estão em `deck-visual-values.csv`; cada imagem original tem uma linha em `run-20261115-124710/visual-values.csv`, incluindo o escopo visual. Nenhuma imagem foi gerada por IA ou retocada.

O primeiro ensaio encontrou um atraso de propagação do tamanho de Scene após `stage.setHeight(720)`. O harness passou a aguardar explicitamente 1366×720 antes de verificar. O recibo dessa falha está em `initial-attempt.log`; os rascunhos anteriores foram movidos para `target/capture-drafts`, conforme autorização. Não são a fonte dos slides.

Os globos isolados são snapshots reais de `PlanetView` auxiliar a 768×768, alimentado com o estado confirmado pela UI. O diagrama `12-uml-companheiro-harness.png` também é auxiliar e traz essa identificação na própria imagem. Os quadros `evento-*-meio.png` usam o animador real com relógio controlado em 50%; não são comprovação de FPS ou gravação contínua.
