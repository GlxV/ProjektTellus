"""PDF fallback from the actual reviewed slide renders; PPTX remains editable."""
from pathlib import Path
import json,hashlib,sys,os
local=Path(__file__).resolve().parents[1]/'.tools/audit-python'
if local.exists():sys.path.insert(0,str(local))
from reportlab.pdfgen import canvas
import fitz
root=Path(__file__).resolve().parents[1];out=root/os.environ.get('TELLUS_PREVIEW_DIR','docs/apresentacao/editorial-r4');evidence=root/'docs/evidencias'
pdf=root/'docs/apresentacao'/os.environ.get('TELLUS_PDF_NAME','ProjektTellus-Defesa-Individual-r4.pdf')
c=canvas.Canvas(str(pdf),pagesize=(1024.5,576),pageCompression=1)
c.setTitle('ProjektTellus — Simulador Planetário');c.setAuthor('Matheus Luiz Bueno')
c.setSubject('ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS | EDER MAGALHAES MACHADO | Turma 4P | 2º Bimestre de 2026')
c.setKeywords('ProjektTellus, Matheus Luiz Bueno, EDER MAGALHAES MACHADO, 4P, Builder, Factory Method, Observer, Memento')
for n in range(1,16):
 image=out/f'slide-{n:02}.png'
 if not image.is_file():raise FileNotFoundError(image)
 c.drawImage(str(image),0,0,width=1024.5,height=576);c.showPage()
c.save()
doc=fitz.open(pdf);assert len(doc)==15
render=root/os.environ.get('TELLUS_BUILD_DIR','target/editorial-r4-build')/'pdf-check';render.mkdir(parents=True,exist_ok=True)
for i,page in enumerate(doc):
 pix=page.get_pixmap(matrix=fitz.Matrix(4/3,4/3));assert (pix.width,pix.height)==(1366,768)
 pix.save(render/f'slide-{i+1:02}.png')
record={'file':str(pdf.relative_to(root)),'pages':len(doc),'resolution':'1366x768 render per page','format':'Raster slides embedded in PDF; no searchable Java text. Editable source is PPTX.','rendered_all_pages':True,'renderer':'PyMuPDF; bundled Poppler unavailable','sha256':hashlib.sha256(pdf.read_bytes()).hexdigest(),'bytes':pdf.stat().st_size,'reason':'Reuse the established raster export after #standard-fonts/Helvetica was unavailable in the original delivery. Direct PDF export was not retried in this revision; bundled dependencies remain unchanged.'}
(evidence/(pdf.stem+'.pdf-validacao.json')).write_text(json.dumps(record,indent=2),encoding='utf-8')
print(json.dumps(record,ensure_ascii=False))
