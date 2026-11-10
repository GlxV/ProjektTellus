# Modelo planetário e limites

Modelo **didático**, global e determinístico. Não reproduz meteorologia regional, órbitas, química detalhada, ecologia de espécies, doses reais de radiação ou danos reais de armas. Constantes servem à demonstração, sem calibração científica. Invasão alienígena é ficção.

## Unidades

- O₂/N₂/CO₂/He: percentuais [0,100], soma 100 (tolerância 1e-6). Argônio/outros gases terrestres foram incorporados ao N₂.
- Pressão [0,20] atm; pressão parcial = pressão × percentual / 100. No vácuo, a composição representa só uma proporção de referência.
- Temperatura [−150,200] °C. Aerossóis e radiação [0,100]: índices abstratos sem unidade física.
- Vegetação [0,100]: índice de cobertura vegetal potencial das áreas terrestres. Água líquida e gelo: porcentagens da superfície total, soma <=100.
- População inteira [0,100 bilhões]. Tempo [0,1 bilhão] anos; avanço individual [0,01,1.000] anos.

## Indicadores

`conforto(x,ideal,tolerância) = clamp(1 − |x−ideal|/tolerância, 0, 1)`.

Habitabilidade humana = 100 × conforto(pO₂,0,21,0,16) × conforto(pressão,1,1) × conforto(temperatura,15,55) × clamp(água/30,0,1) × fatorCO₂ × (1−radiação/100) × (1−0,6×aerossol/100).

`fatorCO₂ = 1 / [1 + 250 × max(0,pCO₂−0,0004)]`.

Potencial biológico = 100 × conforto(temperatura,20,100) × clamp(água/30,0,1) × clamp(pressão/0,2,0,1) × √(1−radiação/100) × (0,7+0,3×vegetação/100).

O potencial biológico admite organismos sem oxigênio e temperaturas mais amplas. Nenhum indicador prevê o surgimento de vida ou civilização. População não é uma “barra de vida”.

## Tempo

Intervalos são divididos em passos <=1 ano, preservando frações finais. Aerossóis decaem exponencialmente com constante de 8 anos, radiação com 30 anos. Temperatura se aproxima (20 anos) do alvo:

`15 + 5 ln(max(0,00001,pCO₂)/0,0004) − 0,4×aerossol − 0,12×gelo`.

Gelo se aproxima (15 anos) de uma fração dependente da temperatura; água+gelo se conserva. Vegetação se aproxima (40 anos) do potencial biológico. População varia exponencialmente com taxa anual `0,012×H−0,10×(1−H)` (H = habitabilidade/100); zero habitantes não gera população espontânea. Saturações respeitam os limites. Sem aleatoriedade.

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
