# Reformulação planetária — 11/11/2026

## Diagnóstico e decisões

Base: `9c41cbf`, checkout limpo. O modelo atual confunde água abundante com suporte populacional, permite crescimento exponencial até um teto técnico e usa vegetação no cálculo do ambiente que determina sua própria recuperação. A textura representa água/gelo qualitativamente e embute poeira/radiação em cores, sem uma atmosfera óptica.

Conservar os records persistidos e a semântica já documentada em MODELO.md: vegetação é a fração vegetada do continente disponível, não da superfície planetária. Área seca = 100 − água − gelo; área vegetada = área seca × vegetação/100. Sem novos campos persistidos, sem conversão de valores, SQLite V001 intacto. Testar bancos V001 construídos independentemente do repository atual, incluindo mundos e retratos oceânicos.

Três resultados derivados do mesmo estado: sobrevivência humana por fatores multiplicativos de pressão parcial/total, temperatura, água, CO₂, radiação e partículas; potencial biológico mais amplo sem exigir O₂ nem vegetação prévia; sustentabilidade por qualidade ambiental, produtividade terrestre e ocupação da capacidade de suporte. Explicações carregam os mesmos fatores usados nos produtos, valores e perdas condicionais em pontos percentuais.

Atmosfera: propriedades puras de espalhamento molecular/partículas, escala vertical e transmissão; visualização estilizada projetada sobre o globo em câmera paralela. Camada radial com iluminação angular, névoa sobre o disco, raio ligado ao zoom/tamanho/deslocamento. Composição uniforme não gira artificialmente com continentes. Interpolar propriedades durante transições, restaurar endpoints exatos. Preservar efeitos 3D e canvas de flashes independentes.

## Execução e verificação

1. [x] Baseline `mvn -B -ntp -Pgui-tests verify`; analisar domínio, eventos, persistência, animações e UI — 57 testes.
2. [x] Testes de regressão primeiro: oceano/capacidade, extremos/pressões parciais, evolução/ecologia, explicabilidade.
3. [x] Motor único, indicadores/fatores, geografia derivada e crescimento limitado; testes direcionados.
4. [x] Propriedades atmosféricas e renderização/interpolação, textura coerente e cache; testes ópticos e GUI.
5. [x] Terceiro indicador, explicações acessíveis e rótulos/unidades nos dois modos; preservar abas e popup.
6. [x] Compatibilidade V001 e Memento; matriz visual de atmosferas reais, cinco eventos máximos, movimento reduzido/desligado, zoom/luz e layout 1366×768/100%.
7. [x] Revisão independente, correções, suíte completa — 69 testes; documentação matemática/arquitetural e comparativos reproduzíveis.
8. [x] JAR atualizado, commit local e relatório de resultados/limites. Push não solicitado. Ver VALIDACAO_REFORMULACAO.md e commit que contém este plano.

Execução direta com checkpoints, conforme a skill executing-plans. Revisão independente ao estabilizar o diff, conforme requesting-code-review.
