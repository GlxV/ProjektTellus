"""Validate the r3 academic revision without executing the planetary simulator."""
from pathlib import Path
import hashlib,json,re,sys,zipfile,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
local=root/'.tools/audit-python'
if local.exists():sys.path.insert(0,str(local))
from PIL import Image,ImageChops,ImageStat
import fitz

out=root/'docs/apresentacao/defesa-r3';build=root/'target/defense-r3-build'
checks=[]
def check(name,condition,details=None):
 checks.append({'check':name,'pass':bool(condition),'details':details})
 if not condition:raise AssertionError(name+': '+str(details))
def readj(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
identity=readj(out/'identificacao-academica.json')
expected={'student':'Matheus Luiz Bueno','discipline':'ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS','professor':'EDER MAGALHAES MACHADO','class':'4P','work':'ProjektTellus — Simulador Planetário','period':'2º Bimestre de 2026'}
check('exact_supplied_identity',identity==expected)
protected=readj(root/'docs/evidencias/defesa-r3-preservacao.json')['protected_before']
for entry in protected:check('preserved_'+entry['file'],digest(root/entry['file'])==entry['sha256'])
check('legacy_artifacts_and_transcription_preserved',len(protected)>=77,len(protected))

def java_compact(text):
 result=[];quote=False;escaped=False
 for c in text:
  if quote:
   if c in '\r\n':raise AssertionError('Java literal contains a presentation newline')
   result.append(c)
   if escaped:escaped=False
   elif c=='\\':escaped=True
   elif c=='"':quote=False
  elif c=='"':quote=True;result.append(c)
  elif not c.isspace():result.append(c)
 if quote:raise AssertionError('Unclosed Java literal')
 return ''.join(result)
for i,fragment in enumerate(readj(out/'trechos-java.json')):
 check('java_source_literal_'+str(i),fragment['raw'].replace('\r\n','\n') in (root/fragment['file']).read_text(encoding='utf-8'),fragment['file'])
for i,block in enumerate(readj(out/'code-displays.json')):
 check('display_changes_only_whitespace_'+str(i),java_compact(block['raw'])==java_compact(block['display']))

pptx=root/'docs/apresentacao/ProjektTellus-Defesa-Individual-r3.pptx'
pdfpath=pptx.with_suffix('.pdf')
ns={'p':'http://schemas.openxmlformats.org/presentationml/2006/main','a':'http://schemas.openxmlformats.org/drawingml/2006/main','dc':'http://purl.org/dc/elements/1.1/'}
def texts(xml):return '\n'.join(''.join(t.text or '' for t in para.findall('.//a:t',ns)) for para in xml.findall('.//a:p',ns))
def normalize(s):return re.sub(r'\s+',' ',s).strip()
with zipfile.ZipFile(pptx) as z:
 slidefiles=[n for n in z.namelist() if re.fullmatch(r'ppt/slides/slide\d+\.xml',n)]
 check('15_slides_retained',len(slidefiles)==15)
 slides={n:ET.fromstring(z.read(f'ppt/slides/slide{n}.xml')) for n in range(1,16)}
 for n,xml in slides.items():
  t=texts(xml)
  check('slide_number_'+str(n),f'{n:02} / 15' in t)
  check('student_footer_'+str(n),'Matheus Luiz Bueno · 4P · ProjektTellus' in t)
  for tr in xml.findall('.//a:rPr',ns):
   font=tr.find('a:latin',ns)
   if font is not None and font.get('typeface')=='Consolas':check('java_font_at_least_17pt_slide_'+str(n),int(tr.get('sz','0'))>=1700,tr.get('sz'))
  notes=ET.fromstring(z.read(f'ppt/notesSlides/notesSlide{n}.xml'))
  nt=texts(notes)
  for value in expected.values():check('notes_identity_'+str(n)+'_'+value,value in nt)
  check('notes_mode_A_and_B_'+str(n),'Modo A' in nt and 'Modo B' in nt)
  check('notes_match_slide_'+str(n),f'Slide {n} ' in nt)
 cover=normalize(texts(slides[1]))
 for key in ['student','discipline','professor','class','period']:check('cover_identity_'+key,expected[key] in cover)
 check('no_invented_institution_or_registration',not any(t in cover.lower() for t in ['matrícula:','universidade:','instituição:']))
 check('slide12_exact_title','Os quatro padrões no fluxo do simulador' in texts(slides[12]))
 check('slide12_separate_operations','Operações independentes' in texts(slides[12]) and 'ordem interna' in texts(slides[12]))
 for term in ['Factory','Memento','Motor','SQLite','Terra','Observer']:check('slide12_internal_stage_'+term,term in texts(slides[12]))
 cb='observer.aoMudar(mudanca);'
 check('observer_callback_one_paragraph',any(''.join(x.text or '' for x in para.findall('.//a:t',ns))==cb for para in slides[9].findall('.//a:p',ns)))
 check('observer_synchronous_and_FX_distinction','Bus síncrono' in texts(slides[9]) and 'Platform.runLater' in texts(slides[9]))
 check('factory_override_product_contract_dispatch',all(x in texts(slides[7]) for x in ['@Override','new Asteroide(intensidade)','EventoPlanetario','evento.aplicar(terra.estado())']))
 check('UML_transformar_is_protected','# transformar(estado, i)' in texts(slides[6]) and 'protected EstadoPlanetario transformar' in (root/'src/main/java/com/projekttellus/events/Asteroide.java').read_text(encoding='utf-8'))
 check('slide14_restoration_example',all(x in texts(slides[14]) for x in ['Asteroide 25%','Memento','64,73%']))
 check('slide14_numeric_limitation_explicit','Limite numérico' in texts(slides[14]) and 'não é uma previsão' in texts(slides[14]))
 note14=texts(ET.fromstring(z.read('ppt/notesSlides/notesSlide14.xml')))
 check('rounding_case_retained_as_limitation','Math.round' in note14 and 'limitação numérica' in note14)
 for n in [4,6,8,10]:check('editable_real_UML_'+str(n),len(slides[n].findall('.//p:cxnSp',ns))>=3)
 for n in [5,7,9,11]:check('editable_Java_'+str(n),len(slides[n].findall('.//a:t',ns))>10)
 core=ET.fromstring(z.read('docProps/core.xml'))
 for key,value in [('creator',expected['student']),('title',expected['work']),('subject',expected['discipline'])]:check('pptx_metadata_'+key,core.find('dc:'+key,ns).text==value)
 check('pptx_metadata_professor',expected['professor'] in core.find('dc:description',ns).text)

pdf=fitz.open(pdfpath);check('PDF_15_pages',len(pdf)==15)
check('PDF_metadata_student',pdf.metadata['author']==expected['student'])
check('PDF_metadata_professor',expected['professor'] in pdf.metadata['subject'])
comparisons=[]
for i,page in enumerate(pdf):
 preview=out/f'slide-{i+1:02}.png'
 with Image.open(preview) as source:
  check('preview_1366x768_'+str(i+1),source.size==(1366,768))
  pix=page.get_pixmap(matrix=fitz.Matrix(4/3,4/3));check('PDF_render_size_'+str(i+1),(pix.width,pix.height)==(1366,768))
  rendered=Image.frombytes('RGB',(pix.width,pix.height),pix.samples)
  diff=ImageStat.Stat(ImageChops.difference(source.convert('RGB'),rendered))
  mean=sum(diff.mean)/3;comparisons.append({'slide':i+1,'mean_channel_error':mean})
  check('PDF_matches_slide_preview_'+str(i+1),mean<.6,mean)
for name in ['Builder','Factory-Method','Observer','Memento']:
 with Image.open(out/('UML-'+name+'.png')) as im:check('UML_2732x1536_'+name,im.size==(2732,1536))

docs=['CONFORMIDADE_TRABALHO','ROTEIRO_APRESENTACAO','GUIA_DEFESA_INDIVIDUAL','DEFESA_CODIGO_JAVA','PERGUNTAS_APRESENTACAO','DEMO_APRESENTACAO','ATALHOS_DEFESA']
for name in docs:
 content=(root/'docs'/f'{name}.md').read_text(encoding='utf-8')
 for key in ['student','discipline','professor','class','period']:check('document_identity_'+name+'_'+key,expected[key] in content)
matrix=(root/'docs/CONFORMIDADE_TRABALHO.md').read_text(encoding='utf-8')
for pattern in ['Builder','Factory Method','Observer','Memento']:check('four_criteria_'+pattern,len(re.findall(r'^\| '+pattern+r' \|',matrix,re.M))==4)
guide=(root/'docs/GUIA_DEFESA_INDIVIDUAL.md').read_text(encoding='utf-8')
check('guide_two_modes_and_interruptions',all(x in guide for x in ['Modo A','Modo B','5–7','10–12','Interrupções']))
deep=(root/'docs/DEFESA_CODIGO_JAVA.md').read_text(encoding='utf-8')
check('four_short_explanations',deep.count('### Em aproximadamente 20 segundos')==4)
check('four_one_minute_explanations',deep.count('### Em aproximadamente um minuto')==4)
check('four_deep_explanations',len(re.findall(r'^### Aprofundada',deep,re.M))==4)
questions=(root/'docs/PERGUNTAS_APRESENTACAO.md').read_text(encoding='utf-8')
check('legacy_62_questions_retained',len(re.findall(r'^#{2,3} \d+\.',questions,re.M))==62)
parts=re.split(r'(?=^### [BID]\d+ —)',questions,flags=re.M)[1:]
check('21_arguition_items',len(parts)==21)
for part in parts:check('arguition_short_and_deep_'+part.splitlines()[0], '**Curta:**' in part and '**Aprofundada:**' in part)
script=(root/'docs/ROTEIRO_APRESENTACAO.md').read_text(encoding='utf-8')
check('15_slide_note_sections',len(re.findall(r'^## Slide \d+ ',script,re.M))==15)
def seconds(v):a,b=v.split(':');return int(a)*60+int(b)
for mode,count,limit in [('A',15,640),('B',12,380)]:
 times=re.findall(r'\*\*Modo '+mode+r':\*\* (\d+:\d+)–(\d+:\d+) \((\d+) s\)',script)
 check('mode_'+mode+'_timed_sections',len(times)==count,len(times))
 prior=0
 for start,end,duration in times:
  check('mode_'+mode+'_time_continuity_'+start,seconds(start)==prior and seconds(end)-seconds(start)==int(duration))
  prior=seconds(end)
 check('mode_'+mode+'_target',prior==limit,prior)
nav=readj(root/'docs/evidencias/defesa-r3-source-navigation.json');check('30_navigation_anchors',len(nav)==30,len(nav))
for entry in nav:check('navigation_path_'+entry['Pattern']+'_'+str(entry['Item']),(root/entry['RelativePath']).is_file() and int(entry['Line'])>0)
for name in docs+['ALTERACOES_SLIDES_DEFESA']:
 p=root/'docs'/f'{name}.md';text=p.read_text(encoding='utf-8')
 for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)',text):
  if target.startswith(('https:','http:','#','mailto:')):continue
  check('local_link_'+name+'_'+target,(p.parent/target.split('#')[0]).resolve().exists())

receipt=root/'docs/evidencias/defesa-r3-validacao.json'
record={'base_commit':'8f7e58d','validation':'academic-material revision only','application_tests_rerun':False,'simulator_experiments_rerun':False,'checked_in_Microsoft_PowerPoint':False,'physical_college_computer_tested':False,'checks':len(checks),'failed':sum(not x['pass'] for x in checks),'pdf_preview_comparisons':comparisons,'protected_legacy_and_production_files':protected,'results':checks,'final_files':{str(pptx.relative_to(root)):digest(pptx),str(pdfpath.relative_to(root)):digest(pdfpath)}}
receipt.write_text(json.dumps(record,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'checks':record['checks'],'failed':record['failed'],'old_files_preserved':len(protected),'PDF_preview_max_mean_error':max(x['mean_channel_error'] for x in comparisons),'application_tests_rerun':False},ensure_ascii=False))
