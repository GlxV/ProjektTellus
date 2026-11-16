"""Reuse measured audit/GUI data for the architecture-focused revised slide 14."""
from pathlib import Path
import csv,json,os
root=Path(__file__).resolve().parents[1]
out=root/os.environ.get('TELLUS_PREVIEW_DIR','docs/apresentacao/editorial-r4');out.mkdir(parents=True,exist_ok=True)
previous=json.loads((root/'docs/auditoria/slide-findings.json').read_text(encoding='utf-8-sig'))
latest=json.loads((root/'docs/capturas/latest.json').read_text(encoding='utf-8-sig'))
source=root/'docs/capturas'/latest['directory']/'measurements.csv'
rows=list(csv.DictReader(source.open(encoding='utf-8-sig',newline='')))
selected={name:next(r for r in rows if r['scenario']==name) for name in ['antes','evento_0','restaurado']}
fields=['o2','n2','co2','he','pressure','temperature','aerosol','radiation','water','ice','vegetation','population','years','human','biological','civil','capacity']
assert all(float(selected['antes'][k])==float(selected['restaurado'][k]) for k in fields)
assert float(selected['evento_0']['temperature'])==8 and float(selected['evento_0']['population'])==6_500_000_000
human=float(selected['evento_0']['human']);assert abs(human-64.72968169602449)<1e-12
findings=previous[:2]+[{'title':'Asteroide 25% e recuperação por Memento','text':f'Humana: 100% → {human:.2f}% → 100%. Temperatura: 15 → 8 → 15 °C; o retrato recuperou o estado completo.'.replace(f'{human:.2f}',f'{human:.2f}'.replace('.',','))}]
record={'revision':out.name,'findings':findings,'sources':['docs/auditoria/experimental-20261115-final/snapshots.csv',source.relative_to(root).as_posix()],'selected_gui_rows':selected,'physical_fields_restored_exactly':fields,'numeric_limitation':'O resíduo de duas pessoas na evolução anóxica é uma limitação numérica de Math.round; não uma previsão realista de sobrevivência. Mantido nas notas e no estudo, substituído como exemplo central do slide.','new_experiments_executed':False}
(out/'experimentos-selecionados.json').write_text(json.dumps(record,ensure_ascii=False,indent=2),encoding='utf-8')
print('Selected measured examples; all physical/indicator fields restored exactly.')
