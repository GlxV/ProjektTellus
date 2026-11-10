# Animações planetárias — 10/11/2026

1. Preservar esfera, textura procedural, domínio, serviço e transações. Separar a atualização dos indicadores Observer da projeção visual confirmada.
2. Testar primeiro seleção por criador, intensidade zero e limites. Criar plano visual puro, animador com ciclo de vida cancelável e efeitos 3D independentes.
3. Preparar as duas texturas em executor dedicado; interpolar somente pixels e parâmetros visuais. Finalizar sempre no estado exato confirmado, inclusive ao pular ou substituir.
4. Integrar asteroide, vulcão, detonações, frota com extração, restauração e pulso Memento. Manter giro/zoom/luz e adicionar pular/modo reduzido/desativado.
5. Testar ciclo de vida e integração real com SQLite/JavaFX, inclusive falha de persistência, substituição e fechamento. Capturar os cinco eventos em desktop real e inspecionar imagens.
6. Executar `mvn -B -ntp -Pgui-tests verify`, documentar evidências/limites, gerar JAR e criar commit local.

Implementação no checkout limpo atual. Trabalho sequencial: textura, efeitos e integração compartilham contratos estreitos. Nenhuma alteração na persistência ou nos padrões de domínio é necessária.
