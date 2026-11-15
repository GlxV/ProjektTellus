"""PDF fallback from the actual reviewed slide renders; PPTX remains editable."""
from pathlib import Path
import json,hashlib,sys
local=Path(__file__).resolve().parents[1]/'.tools/audit-python'
if local.exists():sys.path.insert(0,str(local))
from reportlab.pdfgen import canvas
import fitz
root=Path(__file__).resolve().parents[1];out=root/'docs/apresentacao';evidence=root/'docs/evidencias'
pdf=out/'ProjektTellus-Apresentacao.pdf'
c=canvas.Canvas(str(pdf),pagesize=(1024.5,576),pageCompression=1)
c.setTitle('ProjektTellus — Simulador Planetário');c.setAuthor('ProjektTellus — materiais acadêmicos')
for n in range(1,16):
 image=out/f'slide-{n:02}.png'
 if not image.is_file():raise FileNotFoundError(image)
 c.drawImage(str(image),0,0,width=1024.5,height=576);c.showPage()
c.save()
doc=fitz.open(pdf);assert len(doc)==15
render=root/'target/presentation-build/pdf-check';render.mkdir(exist_ok=True)
for i,page in enumerate(doc):
 pix=page.get_pixmap(matrix=fitz.Matrix(4/3,4/3));assert (pix.width,pix.height)==(1366,768)
 pix.save(render/f'slide-{i+1:02}.png')
record={'file':str(pdf.relative_to(root)),'pages':len(doc),'resolution':'1366x768 render per page','format':'Raster slides embedded in PDF; no searchable Java text. Editable source is PPTX.','rendered_all_pages':True,'renderer':'PyMuPDF; bundled Poppler unavailable','sha256':hashlib.sha256(pdf.read_bytes()).hexdigest(),'bytes':pdf.stat().st_size,'reason':'Artifact Tool direct PDF exporter could not resolve #standard-fonts/Helvetica; no bundled dependencies modified.'}
(evidence/'pdf-validacao.json').write_text(json.dumps(record,indent=2),encoding='utf-8')
print(json.dumps(record,ensure_ascii=False))
