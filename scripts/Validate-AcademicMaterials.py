"""Delivery checks: traceability, requested inputs, outputs and actual execution receipts."""
from pathlib import Path
import csv,json,hashlib,re,zipfile,sys,xml.etree.ElementTree as ET
local=Path(__file__).resolve().parents[1]/'.tools/audit-python'
if local.exists():sys.path.insert(0,str(local))
from PIL import Image
import fitz
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def require(name,condition,details=None):
 checks.append({'check':name,'pass':bool(condition),'details':details})
 if not condition:raise AssertionError(name+': '+str(details))
def j(p):return json.loads((ROOT/p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def csvrows(p):return list(csv.DictReader(p.open(encoding='utf-8-sig',newline='')))
audit=ROOT/'docs/auditoria/experimental-20261115-final'
summary=j(audit/'summary.json');states=csvrows(audit/'snapshots.csv')
require('audit_actual_state_count',len(states)==summary['snapshots'],len(states))
require('audit_unique_experiments',max(int(x['experiment']) for x in states)<=summary['experiments'],summary['experiments'])
aggregates=csvrows(audit/'checks-summary.csv')
require('audit_checks_count',sum(int(x['checks']) for x in aggregates)==summary['checks'],summary['checks'])
require('audit_contracts_zero_failures',all(int(x['failed'])==0 for x in aggregates))
rejects=csvrows(audit/'rejections.csv')
require('rejected_values_count',len(rejects)==summary['rejection_experiments'],len(rejects))
require('rejection_outcomes',all(x['pass']=='true' for x in rejects))
repro=j(audit/'reproducibility.json')
require('independent_process_reproducibility',all(x['identical'] for x in repro['comparisons']))
for x in repro['comparisons']:require('canonical_data_hash_'+x['file'],sha(audit/x['file'])==x['canonical_sha256'])
# Only explicit sweep families count as coverage of requested scalar values.
requests=[('o2',[0,1,5,10,15,21,30,50,70,90,100],['sweep_o2','sweep_o2_binary']),
 ('co2',[0,.04,.1,1,5,10,30,50,80,100],['sweep_co2','sweep_co2_binary','request_co2','scenario']),
 ('he',[0,1,10,20,40,60,80,100],['sweep_he_fixed','sweep_he_binary']),
 ('pressure',[0,.1,.3,.5,1,2,5,10,20],['sweep_pressure','request_pressure']),
 ('temperature',[-150,-100,-50,-20,0,15,30,50,80,120,200],['sweep_temperature','request_temperature']),
 ('water',[0,10,30,50,70,90,100],['sweep_water']),
 ('population',[0,1e6,1e8,1e9,8e9,1e10,1e11],['sweep_population','request_population'])]
for field,requested,families in requests:
 measured=[float(r['after_'+field]) for r in states if r['family'] in families and float(r['horizon_years'])==0]
 missing=[x for x in requested if not any(abs(x-y)<=1e-10 for y in measured)]
 require('all_requested_'+field,not missing,{'missing':missing,'requested':requested})
scenarios={r['case'] for r in states if r['family']=='scenario'}
require('at_least_20_combined_scenarios',len(scenarios)>=20,len(scenarios))
for case in scenarios:
 require('scenario_horizons_'+case,{float(r['horizon_years']) for r in states if r['family']=='scenario' and r['case']==case}=={0,1,10,100,1000})
events=[r for r in states if r['family']=='event']
require('all_five_event_types',len({r['operation'] for r in events})==5)
require('all_six_event_intensities',{float(r['intensity']) for r in events}=={0,.01,.25,.5,.75,1})
require('event_240_trials',len({r['experiment'] for r in events})==240)
# Excerpts have a direct source, including source-file hashes in the study manifest.
for i,snip in enumerate(j('docs/apresentacao/trechos-java.json')):
 source=(ROOT/snip['file']).read_text(encoding='utf-8')
 require('deck_java_literal_'+str(i),snip['raw'] in source,snip['file'])
for snip in j('docs/uml-apresentacao/snippets-deck.json')['snippets']:
 source=ROOT/snip['file'];require('study_source_hash_'+snip['id'],sha(source)==snip['source_sha256'])
 require('study_literal_'+snip['id'],snip['code'] in source.read_text(encoding='utf-8'))
def java_compact(text):
 out=[];quote=False;escaped=False
 for c in text:
  if quote:
   require('java_literal_no_newline',c not in '\r\n')
   out.append(c)
   if escaped:escaped=False
   elif c=='\\':escaped=True
   elif c=='"':quote=False
  elif c=='"':quote=True;out.append(c)
  elif not c.isspace():out.append(c)
 require('java_literal_closed',not quote)
 return ''.join(out)
for i,display in enumerate(j('target/presentation-build/code-displays.json')):
 require('java_display_preserves_tokens_'+str(i),java_compact(display['raw'])==java_compact(display['display']))
for name in ['AUDITORIA_EXPERIMENTAL','CONFORMIDADE_TRABALHO','ROTEIRO_APRESENTACAO','DEMO_APRESENTACAO','PERGUNTAS_APRESENTACAO','ESTUDO_PADROES','CODIGO_JAVA_APRESENTACAO','ENUNCIADO_ORIGINAL_TRANSCRITO']:
 require('required_document_'+name,(ROOT/'docs'/f'{name}.md').is_file())
questions=(ROOT/'docs/PERGUNTAS_APRESENTACAO.md').read_text(encoding='utf-8')
require('62_questions',len(re.findall(r'^#{2,3} \d+\.',questions,re.M))==62)
matrix=(ROOT/'docs/CONFORMIDADE_TRABALHO.md').read_text(encoding='utf-8')
for name in ['Builder','Factory Method','Observer','Memento']:
 require('four_pdf_criteria_'+name,len(re.findall(r'^\| '+name+r' \|',matrix,re.M))==4)
final=ROOT/'docs/apresentacao/ProjektTellus-Apresentacao-r2.pptx'
ns={'a':'http://schemas.openxmlformats.org/drawingml/2006/main','p':'http://schemas.openxmlformats.org/presentationml/2006/main'}
with zipfile.ZipFile(final) as z:
 slides=[x for x in z.namelist() if re.fullmatch(r'ppt/slides/slide\d+\.xml',x)]
 require('pptx_15_slides',len(slides)==15)
 require('pptx_15_speaker_notes',len([x for x in z.namelist() if re.fullmatch(r'ppt/notesSlides/notesSlide\d+\.xml',x)])==15)
 for number in [4,6,8,10]:
  xml=ET.fromstring(z.read(f'ppt/slides/slide{number}.xml'))
  require('editable_UML_slide_'+str(number),len(xml.findall('.//p:cxnSp',ns))>0 and len(xml.findall('.//p:sp',ns))>5)
 for number,token in [(5,'TerraBuilder'),(7,'EventoPlanetario'),(9,'aoMudar'),(11,'TerraMemento')]:
  xml=ET.fromstring(z.read(f'ppt/slides/slide{number}.xml'));texts=' '.join(x.text or '' for x in xml.findall('.//a:t',ns))
  require('editable_java_slide_'+str(number),token in texts)
 for number in range(1,16):
  with Image.open(ROOT/'docs/apresentacao'/f'slide-{number:02}.png') as im:require('preview_1366x768_'+str(number),im.size==(1366,768))
pdf=fitz.open(ROOT/'docs/apresentacao/ProjektTellus-Apresentacao.pdf');require('pdf_15_pages',len(pdf)==15)
for name in ['Builder','Factory-Method','Observer','Memento']:
 with Image.open(ROOT/'docs/uml-apresentacao'/f'{name}.png') as im:require('uml_high_resolution_'+name,im.size==(2732,1536))
latest=j('docs/capturas/latest.json');capture=ROOT/'docs/capturas'/latest['directory']
gui=(capture/'checks.tsv').read_text(encoding='utf-8')
require('gui_249_passes',sum(x.startswith('PASS\t') for x in gui.splitlines())==249)
require('gui_success_receipt',(capture/'SUCCESS.txt').is_file())
for alias in latest['aliases']:require('real_capture_alias_'+alias['file'],sha(ROOT/'docs/capturas'/alias['file'])==alias['sha256'])
video=j('docs/evidencias/video-validacao.json');require('video_full_decode',video['full_decode']=='PASS')
require('video_canonical_capture_source',video['capture_source'].replace('\\','/')=='docs/capturas/'+latest['directory'])
tests={'tests':0,'failures':0,'errors':0,'skipped':0}
for p in (ROOT/'target/surefire-reports').glob('TEST-*.xml'):
 suite=ET.parse(p).getroot()
 for k in tests:tests[k]+=int(suite.attrib[k])
require('final_junit_69_passed',tests=={'tests':69,'failures':0,'errors':0,'skipped':0},tests)
before=j('docs/evidencias/saves-antes.json');before=before if isinstance(before,list) else [before]
for f in before:require('personal_save_unchanged',sha(Path(f['Path'])).upper()==f['Hash'],f['Path'])
record={'checks':len(checks),'failed':sum(not c['pass'] for c in checks),'results':checks,'boundary':'Automated artifact/traceability checks; visual review, scientific validity and physical college computer are separate.'}
(ROOT/'docs/evidencias/materiais-validacao.json').write_text(json.dumps(record,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'checks':len(checks),'failed':record['failed'],'junit':tests,'experiments':summary['experiments']},ensure_ascii=False))
