# Modelo planetário e limites — revisão de 11/11/2026

Modelo **educacional, global, determinístico e ficcional**, sem calibração científica. Não prevê meteorologia regional, química detalhada, espécies, doses de radiação, segurança humana ou produção agrícola real. Os valores abaixo são parâmetros de uma experiência didática.

## Unidades, geografia e compatibilidade

- O₂/N₂/CO₂/He: percentuais [0,100], soma 100, tolerância 1e-6. Outros gases terrestres estão agregados ao N₂. Pressão total [0,20] atm. Pressão parcial = pressão × fração do gás; no vácuo a composição é apenas uma proporção de referência.
- Temperatura [−150,200] °C; aerossóis e radiação são **índices abstratos** [0,100], não concentrações nem doses.
- Água líquida/oceano e gelo são áreas distintas, em % da superfície total, soma ≤100. `L = max(0,100−água−gelo)` é a área continental seca e não glacial.
- Vegetação V [0,100] é a **fração vegetada de L**; área vegetada planetária = L×V/100. Não representa vegetação marinha. Com L=0, V pode permanecer como índice latente de um save, mas sua área e contribuição ao suporte são zero. O índice decai ao evoluir sem continente.
- População inteira [0,100 bilhões]; tempo [0,1 bilhão] anos; avanço [0,01,1000] anos.

A semântica terrestre de V já existia nesta documentação antes da revisão. Nenhum valor persistido é convertido ou truncado. O schema e `PRAGMA user_version=1` são preservados; não há migration porque não há novo campo nem reinterpretação de coluna. Mundos e retratos V001 continuam carregáveis, inclusive oceano=100/V=93. **Os indicadores e a evolução mudam conforme as novas fórmulas**, mas carregar um save não avança tempo nem altera parâmetros. `LegacyPlanetTest` constrói um banco V001 diretamente por SQL, verifica campos antigos, restaura, reabre e compara indicadores/propriedades visuais.

## Funções comuns

`S(x) = u²(3−2u)`, com `u=clamp(x,0,1)`.

`F(x;a,b,c,d) = S((x−a)/(b−a)) × S((d−x)/(d−c))`.

F vale 1 na faixa [b,c] e 0 fora de [a,d], com transições suaves. Todo indicador é `100 × produto das adequações`, limitado a [0,100]. Uma limitação crítica não é escondida por médias com fatores favoráveis.

## Habitabilidade humana H

Adequação do ambiente à sobrevivência sem proteção tecnológica. Não mede área de assentamento nem tamanho populacional.

| Fator | Adequação [0,1] |
|---|---|
| Pressão parcial O₂ | F(pO₂;0,06;0,16;0,30;0,60) |
| Pressão total | F(P;0,15;0,7;1,5;6) |
| CO₂ inspirado | 1 / (1 + [max(0,pCO₂−0,0004)/0,003]²) |
| Temperatura | F(T;−40;5;25;65) |
| Água líquida | S(água/15) |
| Radiação | 1−R/100 |
| Aerossóis | exp(−2A/100) |

Os limites são escolhas educacionais, **não tabelas de tolerância médica**. He e N₂ não recebem penalidade própria: se pO₂, pCO₂ e P forem iguais, H é igual. O₂ insuficiente ou excessivo e CO₂ alto continuam restritivos. Um oceano pode ter H alto, mas suporte convencional nulo.

## Potencial biológico B

Não prevê vida existente ou seu surgimento, nem exige O₂ ou vegetação prévia. Supõe formas hipotéticas associadas à água líquida e energia luminosa. Não inclui vida subterrânea, metanogênica, quimiossintética ou sem água.

| Fator | Adequação [0,1] |
|---|---|
| Temperatura | F(T;−60;0;45;130) |
| Água | S(água/10) |
| Meio atmosférico | S(P/0,15) / (1+max(0,P−5)/10) |
| Radiação | sqrt(1−R/100) |
| Luz disponível | exp(−1,2A/100) |
| Estabilidade | 1 / (1+abs(Talvo−T)/45) |

A estabilidade é a distância ao alvo do próprio motor, disponível a partir de um único estado salvo; não inventa uma história de variação climática que não foi persistida. CO₂ pode sustentar atmosferas não terrestres, mas seu efeito térmico e o desequilíbrio limitam B. Temperaturas extremas e vácuo não recebem pontuações altas.

## Suporte e sustentabilidade da civilização C

`W=S(água/15)`, `Q=sqrt(0,25+0,75V/100)`, `h=H/100`, `b=B/100`, `l=L/100`.

`K = round(clamp(16 bilhões × (l/0,26) × Q × W × h × sqrt(b), 0, 100 bilhões))`.

K é a estimativa de capacidade de suporte em pessoas, **independente da população atual**. O termo base 25% admite produtividade simplificada sem cobertura vegetal; água, continente e sobrevivência continuam necessários. A referência de 16 bilhões é uma escala narrativa do simulador, não estimativa da Terra real.

`D=1` se população=0; senão `D=min(1,K/população)`.

`C = 100 × h × S(l/0,10) × W × Q × sqrt(b) × D`.

C distingue área, ecossistema e ocupação da capacidade. Mundos pequenos em terra podem sustentar pequenas populações, mas têm menos diversidade de recursos. Sem terra, K=C=0 mesmo com vegetação V=100; sem habitantes, não há penalidade por superlotação, mas C ainda avalia recursos. A ausência de pressão demográfica não cria população espontaneamente.

## Explicações calculadas

`Indicadores` guarda H, B, C, K e listas imutáveis de `FatorAmbiental`. Cada fator inclui nome, valor, unidade, adequação e explicação da regra real. Favorável: adequação ≥0,8; limitante: ≥0,4; crítico: menor. `IndicatorDetails` usa esses fatores, sem outro algoritmo de pontuação.

A perda condicional de um fator é a diferença entre o produto com esse fator substituído por 1 e a pontuação atual. É expressa em pontos percentuais, não é aditiva e pode ser zero quando outro fator já zera o indicador. A aba mostra também continente, área vegetada, K e população.

## Evolução temporal

Dividir o intervalo em `n=ceil(anos)` passos de `dt=anos/n ≤1`. O relógio é atualizado uma vez no endpoint validado. Mesma entrada produz exatamente a mesma saída; fracionar uma chamada em intervalos diferentes pode produzir pequenas diferenças de integração e arredondamento populacional.

Em cada passo:

1. A decai com `exp(−dt/8)`; R, com `exp(−dt/30)`. São remoção/deposição e recuperação abstratas; não há emissões contínuas.
2. `Talvo=clamp(15+4ln((pCO₂+0,00001)/0,00041)+3ln(max(0,05,P))−0,35A−0,18(gelo−3),−100,120)`. O alvo usa o estado no começo do passo. T aproxima-se dele com constante de 20 anos.
3. Reserva `U=água+gelo`. `geloAlvo=U×S((10−Tnovo)/55)`. Gelo aproxima-se do alvo com constante de 15 anos e água recebe U−gelo. A soma e, portanto, o continente são conservados. Não há evaporação, vapor, relevo ou variação volumétrica: reserva de área é uma abstração, não conservação de massa em kg.
4. Recalcular H/B com o novo clima. `V alvo=B×S(água/20)`; sem continente, alvo=0. V aproxima-se em 40 anos. Ambientes degradados podem recuperar-se gradualmente; ambientes secos, frios ou irradiados perdem cobertura.
5. Recalcular K com essa ecologia. Para população p e capacidade k, `rrec=max(0,(p−k)/(p+k))`; se k=0, rrec=1 para p>0 e 0 para p=0. `taxa=0,012h×max(0,1−p/k)−0,12rrec` quando k>0. Se k=0, crescimento=0 e soma-se mortalidade ambiental `−0,10(1−h)`. A habitabilidade já reduz K e não é novamente penalizada como mortalidade abaixo do suporte. Com ambiente estável, K é também o equilíbrio demográfico. `pnovo=round(p×exp(taxa×dt))`, dentro dos limites; crescimento não ultrapassa K.

A mortalidade por ambiente/recursos é gradual, nunca uma remoção instantânea até K. Não há nascimento a partir de p=0, migração, tecnologia, agricultura regional ou adaptação evolutiva de espécies. K muda com clima e ecologia; superlotação não degrada o terreno diretamente nesta aproximação.

## Eventos

Intensidade i em [0,1]; zero não altera condições ambientais.

| Evento | Efeitos máximos, escalados por i |
|---|---|
| Asteroide | −28 °C; +85 aerossóis; −85% vegetação presente; −75% população; +0,12 ponto de CO₂; 12 pontos de água congelada |
| Supervulcão | −18 °C; +95 aerossóis; −55% vegetação; −30% população; +0,4 ponto de CO₂; 8 pontos congelados |
| Guerra nuclear | −22 °C; +70 aerossóis; +90 radiação; −80% vegetação; −90% população; +0,08 ponto de CO₂; 10 pontos congelados |
| Invasão alienígena | +12 °C; +15 aerossóis; +25 radiação; −60% vegetação; −65% população; converte até 60% do O₂ em He e reduz pressão até 30%; ficção |
| Restauração | Aproxima temperatura de 15 °C em 45%; remove até 70% aerossóis/65% radiação; recupera 45% vegetação faltante; derrete 25% gelo; remove 60% excesso de CO₂ sobre 0,04%; não ressuscita população |

CO₂ é transferido de/para o N₂, conservando frações: é aproximação de composição, sem pretensão de reação química real. Congelamento não excede a água disponível. Eventos respeitam todos os limites de domínio e não avançam o relógio por conta própria.
