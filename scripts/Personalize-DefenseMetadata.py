"""Set supplied academic metadata on the PRIVATE generated candidate, before finalization."""
from pathlib import Path
import zipfile,xml.etree.ElementTree as ET,os
root=Path(__file__).resolve().parents[1]
build=root/os.environ.get('TELLUS_BUILD_DIR','target/editorial-r4-build')
candidate=build/'candidate.pptx';temporary=build/'candidate-metadata.pptx'
dc='http://purl.org/dc/elements/1.1/';cp='http://schemas.openxmlformats.org/package/2006/metadata/core-properties'
ET.register_namespace('dc',dc);ET.register_namespace('cp',cp)
ET.register_namespace('dcterms','http://purl.org/dc/terms/')
ET.register_namespace('dcmitype','http://purl.org/dc/dcmitype/')
ET.register_namespace('xsi','http://www.w3.org/2001/XMLSchema-instance')
with zipfile.ZipFile(candidate) as original,zipfile.ZipFile(temporary,'w',compression=zipfile.ZIP_DEFLATED) as revised:
 for info in original.infolist():
  data=original.read(info.filename)
  if info.filename=='docProps/core.xml':
   xml=ET.fromstring(data)
   values={f'{{{dc}}}title':'ProjektTellus — Simulador Planetário',f'{{{dc}}}creator':'Matheus Luiz Bueno',f'{{{dc}}}subject':'ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS',f'{{{dc}}}description':'Professor: EDER MAGALHAES MACHADO | Turma: 4P | Período: 2º Bimestre de 2026 | Defesa individual de Builder, Factory Method, Observer e Memento',f'{{{cp}}}keywords':'Matheus Luiz Bueno; 4P; EDER MAGALHAES MACHADO; ProjektTellus'}
   for name,value in values.items():
    node=xml.find(name)
    if node is None:node=ET.SubElement(xml,name)
    node.text=value
   data=ET.tostring(xml,encoding='utf-8',xml_declaration=True)
  revised.writestr(info,data)
temporary.replace(candidate)
print('Academic metadata set on private candidate; original r2 untouched.')
