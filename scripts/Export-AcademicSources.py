"""Complete Java sources for studying the slide excerpts, with traceable hashes."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]
prefix=Path('src/main/java/com/projekttellus')
files=['domain/TerraBuilder.java','domain/Terra.java','domain/EstadoPlanetario.java','patterns/memento/TerraMemento.java',
       'events/EventoCreator.java','events/EventoPlanetario.java','events/EventoBase.java','events/AsteroideCreator.java','events/Asteroide.java',
       'patterns/observer/SimulacaoObserver.java','patterns/observer/ObserverBus.java','patterns/observer/MudancaPlanetaria.java',
       'patterns/observer/AlertaObserver.java','patterns/observer/EstatisticasObserver.java','application/SimulacaoService.java',
       'persistence/MundoRepository.java','ui/BuilderDialog.java']
parts=['# Código Java completo para estudo\n\nFontes da implementação do commit base `3e153f0`, exportadas diretamente do checkout. Os blocos são classes completas; dependem dos imports e demais fontes do projeto para compilar. A explicação detalhada está em [ESTUDO_PADROES.md](ESTUDO_PADROES.md).\n\nNão ler tudo durante a apresentação. Comece por TerraBuilder, EventoCreator/AsteroideCreator, ObserverBus/SimulacaoObserver e Terra/TerraMemento. Depois acompanhe a coordenação em SimulacaoService.\n']
manifest=[]
for f in files:
 p=root/prefix/f;raw=p.read_bytes();code=raw.decode('utf-8').replace('\r\n','\n');digest=hashlib.sha256(raw).hexdigest()
 parts.append(f'\n## {p.stem}\n\nCaminho: `{(prefix/f).as_posix()}`. SHA256: `{digest}`.\n\n```java\n{code.rstrip()}\n```\n')
 manifest.append({'file':(prefix/f).as_posix(),'sha256':digest,'lines':len(code.splitlines())})
(root/'docs/CODIGO_JAVA_APRESENTACAO.md').write_text(''.join(parts),encoding='utf-8')
(root/'docs/evidencias/fontes-apresentacao.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print(f'Exported {len(files)} complete source classes/interfaces.')
