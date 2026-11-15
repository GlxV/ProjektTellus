"""Plots derived only from the measured CSV; no simulated/illustrative data."""
import csv, json, sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
local=ROOT/'.tools/audit-python'
if local.exists(): sys.path.insert(0,str(local))
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np

data=(Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'docs/auditoria/experimental-20261115-final').resolve()
rows=list(csv.DictReader((data/'snapshots.csv').open(encoding='utf-8')))
out=ROOT/'docs/graficos';out.mkdir(parents=True,exist_ok=True)
plt.rcParams.update({'font.family':'DejaVu Sans','font.size':12,'axes.facecolor':'#0b1523','figure.facecolor':'#0b1523','text.color':'#ebf4fc','axes.labelcolor':'#ebf4fc','xtick.color':'#abc0d4','ytick.color':'#abc0d4','axes.edgecolor':'#abc0d4','grid.color':'#abc0d4','grid.alpha':.15,'legend.facecolor':'#142236','legend.edgecolor':'#33445a','savefig.facecolor':'#0b1523'})
colors={'H':'#59dfc0','B':'#70beff','C':'#f7c76a'}
labels={'H':'Habitabilidade humana','B':'Potencial biológico','C':'Sustentabilidade'}
manifest=[]
def group(family):return [r for r in rows if r['family']==family]
def val(r,f):return float(r['after_'+f])
def finish(fig,name,description):
 fig.text(.01,.006,'Fonte: snapshots.csv • motor 3e153f0 • escores didáticos, sem calibração científica',color='#abc0d4',fontsize=9)
 fig.tight_layout(rect=(0,.035,1,1));fig.savefig(out/(name+'.png'),dpi=160);fig.savefig(out/(name+'.svg'));plt.close(fig)
 svg=out/(name+'.svg');svg.write_text('\n'.join(line.rstrip() for line in svg.read_text(encoding='utf-8').splitlines())+'\n',encoding='utf-8')
 manifest.append({'file':name+'.png','source':str(data.relative_to(ROOT)/'snapshots.csv'),'description':description})
def lines(ax,rr,x,scale=1):
 rr=sorted(rr,key=lambda r:val(r,x))
 for f in colors:ax.plot([val(r,x)*scale for r in rr],[val(r,f) for r in rr],label=labels[f],color=colors[f],lw=2)
 ax.set_ylim(-2,102);ax.set_ylabel('Indicador (0–100)');ax.grid();ax.legend(fontsize=9)

fig,axes=plt.subplots(1,3,figsize=(16,5))
for ax,fam,x,title in zip(axes,['sweep_o2','sweep_co2','sweep_he_fixed'],['o2','co2','he'],['O₂ substitui N₂; CO₂ = 0,04%','CO₂ substitui N₂; O₂ = 21%','He substitui N₂; O₂ e CO₂ fixos']):
 lines(ax,group(fam)+(group('request_co2')+[r for r in group('scenario') if r['case']=='earth' and float(r['horizon_years'])==0] if fam=='sweep_co2' else []),x);ax.set_xlabel(x.upper()+' (%)');ax.set_title(title,fontsize=12)
 if fam=='sweep_co2':ax.set_xscale('symlog',linthresh=.04)
finish(fig,'01-gases','Varreduras com 1 atm, 15 °C, água 71%, gelo 3%, vegetação 65%, 8 bilhões. Casos binários extremos estão no CSV separado por família.')

fig,axes=plt.subplots(1,2,figsize=(14,5))
lines(axes[0],group('sweep_pressure')+group('request_pressure'),'pressure');axes[0].set_xlabel('Pressão total (atm)');axes[0].set_title('Composição fixa de referência')
lines(axes[1],group('sweep_temperature')+group('request_temperature'),'temperature');axes[1].set_xlabel('Temperatura inicial (°C)');axes[1].set_title('Temperaturas válidas no domínio')
finish(fig,'02-pressao-temperatura','Estados iniciais, sem pressupor estabilidade física de cada combinação.')

fig,axes=plt.subplots(1,2,figsize=(14,5));rr=group('sweep_water')
lines(axes[0],rr,'water');axes[0].set_xlabel('Oceano (%)');axes[0].set_title('Gelo zero; vegetação continental 65%')
rr=sorted(rr,key=lambda r:val(r,'water'));axes[1].plot([val(r,'water') for r in rr],[val(r,'K')/1e9 for r in rr],color=colors['H'],lw=2)
axes[1].set_xlabel('Oceano (%)');axes[1].set_ylabel('Capacidade (bilhões de pessoas)');axes[1].grid();axes[1].set_title('A capacidade exige terra e água')
finish(fig,'03-oceano-suporte','A terra muda ao variar água; esta é uma dependência geométrica intencional, não uma variável independente escondida.')

fig,axes=plt.subplots(1,2,figsize=(14,5))
for name,label,color in [('earth','Referência','#59dfc0'),('population100b','100 bilhões iniciais','#f7c76a'),('anoxic','Sem O₂','#e28ca8'),('cold_min','−150 °C iniciais','#70beff')]:
 rr=sorted([r for r in group('scenario') if r['case']==name],key=lambda r:float(r['horizon_years']))
 axes[0].plot([float(r['horizon_years']) for r in rr],[val(r,'population')/1e9 for r in rr],'o-',label=label,color=color)
 axes[1].plot([float(r['horizon_years']) for r in rr],[val(r,'temperature') for r in rr],'o-',label=label,color=color)
for a in axes:a.set_xscale('symlog',linthresh=1);a.set_xlabel('Anos (escala simétrica log)');a.grid();a.legend(fontsize=9)
axes[0].set_ylabel('População (bilhões)');axes[1].set_ylabel('Temperatura (°C)')
finish(fig,'04-evolucao','Amostras reais em 0, 1, 10, 100 e 1.000 anos. As linhas apenas conectam os pontos medidos.')

fig,axes=plt.subplots(1,2,figsize=(14,5))
for case,label,color in [('anoxic','Sem O₂','#e28ca8'),('ocean','Oceano 100%','#70beff'),('population1','Uma pessoa inicial','#59dfc0')]:
 rr=[r for r in group('scenario') if r['case']==case]
 axes[0].plot([float(r['horizon_years']) for r in rr],[max(1,val(r,'population')) for r in rr],'o-',label=label,color=color)
axes[0].set_yscale('log');axes[0].set_xscale('symlog',linthresh=1);axes[0].set_xlabel('Anos');axes[0].set_ylabel('Pessoas (log)');axes[0].legend();axes[0].grid();axes[0].set_title('Arredondamento cria patamares persistentes')
axes[0].set_xlim(left=0,right=1000)
rr=group('sweep_population')+group('request_population');xx=[val(r,'population') for r in rr]
axes[1].scatter(xx,[val(r,'C') for r in rr],color=colors['C']);axes[1].set_xscale('symlog',linthresh=1e6);axes[1].set_xlabel('População inicial (pessoas)');axes[1].set_ylabel('Sustentabilidade (0–100)');axes[1].grid();axes[1].set_title('Sem população não significa inabitável')
axes[1].set_xlim(left=0);axes[1].set_xticks([0,1e6,1e8,1e9,1e10,1e11],['0','1 mi','100 mi','1 bi','10 bi','100 bi'])
finish(fig,'05-populacao','Patamares medidos: 2 pessoas sem O₂, 4 no oceano e uma pessoa em cenário favorável. Ponto 0 permanece zero; não há colonização espontânea.')

events=['AsteroideCreator','SupervulcaoCreator','GuerraNuclearCreator','InvasaoAlienigenaCreator','RestauracaoEcologicaCreator']
eventlabels=['Asteroide','Supervulcão','Guerra nuclear','Invasão (ficção)','Restauração']
intensities=[0,.01,.25,.5,.75,1]
fig,axes=plt.subplots(1,3,figsize=(17,5))
for ax,f in zip(axes,['H','B','C']):
 matrix=np.array([[val(next(r for r in group('event') if r['case']=='earth/'+e and float(r['intensity'])==i and float(r['horizon_years'])==0),f) for i in intensities] for e in events])
 ax.imshow(matrix,vmin=0,vmax=100,cmap='viridis',aspect='auto');ax.set_xticks(range(6),['0','1','25','50','75','100']);ax.set_xlabel('Intensidade (%)');ax.set_yticks(range(5),eventlabels);ax.set_title(labels[f],fontsize=13)
 for a in range(5):
  for b in range(6):ax.text(b,a,f'{matrix[a,b]:.1f}',ha='center',va='center',fontsize=10,color='black' if matrix[a,b]>65 else 'white')
finish(fig,'06-eventos','Efeitos imediatos na referência. Os índices são não lineares mesmo quando o efeito ambiental é linear na intensidade.')

rr=group('sensitivity');fig,ax=plt.subplots(figsize=(14,6));xx=np.arange(len(rr));width=.25
for i,f in enumerate(['H','B','C']):ax.bar(xx+(i-1)*width,[val(r,f) for r in rr],width,color=colors[f],label=labels[f])
perturbations=['Vegetação 65→66%','População 8→100 bi','Temperatura 15→16 °C','Aerossol 0→1','Radiação 0→1','CO₂ 0,04→1%','O₂ 21→0%','Água 71→72%; gelo 3→2%']
ax.set_xticks(xx,perturbations,rotation=20,ha='right',fontsize=10);ax.set_ylim(0,105);ax.set_ylabel('Indicador após perturbação (0–100)');ax.legend(loc='lower center',bbox_to_anchor=(.5,1),ncol=3,fontsize=11);ax.grid(axis='y');ax.set_title('Comparações controladas com a referência',pad=42)
finish(fig,'07-sensibilidade','Cada rótulo identifica a perturbação no CSV. Não é um ranking universal: magnitude e intervalo de cada variável são diferentes.')

# Summary for the editable deck; select measured rows explicitly.
def scenario(name,year=0):return next(r for r in rows if r['family']=='scenario' and r['case']==name and float(r['horizon_years'])==year)
earth=scenario('earth');ocean=scenario('ocean');anoxic=scenario('anoxic');he=scenario('he20');late=scenario('anoxic',1000)
findings=[
 {'title':'Trocar N₂ por 20% de He preservou os indicadores','text':f'O₂ 21% e CO₂ 0,04%, a 1 atm: humana {val(he,"H"):.0f}%, biológica {val(he,"B"):.0f}% e sustentabilidade {val(he,"C"):.2f}%'.replace('.',',')+'.'},
 {'title':'Um oceano habitável pode ter suporte civilizacional zero','text':f'Oceano 100%: humana {val(ocean,"H"):.0f}%, biológica {val(ocean,"B"):.2f}%; capacidade e sustentabilidade iguais a zero'.replace('.',',')+'.'},
 {'title':'Sem O₂, o arredondamento manteve 2 pessoas','text':f'Sem O₂: humana 0%, capacidade zero; de 8 bilhões para {val(late,"population"):.0f} pessoas em 100 anos, mantendo {val(late,"population"):.0f} após 1.000 anos.'}
]
(ROOT/'docs/auditoria/slide-findings.json').write_text(json.dumps(findings,ensure_ascii=False,indent=2),encoding='utf-8')
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'plots':len(manifest),'rows':len(rows),'output':str(out)},ensure_ascii=False))
