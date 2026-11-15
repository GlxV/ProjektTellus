# Plano de auditoria e apresentação — 15/11/2026

Base inicial: checkout limpo, commit `3e153f0`. Fonte acadêmica primária: transcrição integral fornecida pelo estudante, sem acesso aos anexos.

1. Preservar hashes dos saves pessoais; usar somente bancos temporários.
2. Executar auditoria Java reproduzível: varreduras controladas, pelo menos vinte cenários, quatro horizontes, cinco eventos e extremos; registrar CSV, rejeições, invariantes, determinismo e restauração. Responsável: frente experimental.
3. Conferir padrões contra código e produzir matriz de conformidade, estudo, perguntas, roteiro e UML. Responsável: frente acadêmica. Diagramas e trechos devem corresponder ao checkout.
4. Executar fluxo JavaFX com banco temporário, capturar interface/eventos, validar janela equivalente a 1366×768 e registrar limites da evidência. Responsável: frente gráfica.
5. Integrar resultados e criar slides editáveis, PDF e gráficos, priorizando os quatro padrões. Responsável: integração principal.
6. Revisar cobertura, fontes, trechos Java, resultados e legibilidade; executar `mvn -B -ntp -Pgui-tests verify`; conferir hashes dos saves, registrar evidências e fazer commit dos materiais.

As frentes usam arquivos distintos e a integração revisa seus resultados. Mutações do checkout e execuções Maven compartilhadas serão serializadas. Não alterar o motor por limitações científicas; defeitos pequenos exigem reprodução e regressão antes de correção. O computador físico da faculdade permanece não validado; duração de 10–12 minutos é planejamento, não exigência docente.
