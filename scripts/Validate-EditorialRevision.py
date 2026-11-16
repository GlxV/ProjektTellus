"""Check the r4 editorial artifacts without running or modifying the simulator."""
from pathlib import Path
import hashlib, json, re, sys, zipfile, xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
local = root / '.tools/audit-python'
if local.exists():
    sys.path.insert(0, str(local))
from PIL import Image, ImageChops, ImageStat
import fitz

out = root / 'docs/apresentacao/editorial-r4'
pptx = root / 'docs/apresentacao/ProjektTellus-Defesa-Individual-r4.pptx'
old = root / 'docs/apresentacao/ProjektTellus-Defesa-Individual-r3.pptx'
checks = []
ns = {'p': 'http://schemas.openxmlformats.org/presentationml/2006/main',
      'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
      'dc': 'http://purl.org/dc/elements/1.1/'}

def check(name, condition, detail=None):
    checks.append({'check': name, 'pass': bool(condition), 'detail': detail})

def readj(p):
    return json.loads(p.read_text(encoding='utf-8-sig'))

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def text(xml):
    return '\n'.join(''.join(t.text or '' for t in p.findall('.//a:t', ns))
                     for p in xml.findall('.//a:p', ns))

def flat(s):
    return re.sub(r'\s+', ' ', s).strip()

def java(s):
    # Preserve every character inside literals while ignoring layout whitespace.
    result, quoted, escaped = [], False, False
    for c in s:
        if quoted:
            assert c not in '\r\n', 'Newline inside a Java string'
            result.append(c)
            if escaped:
                escaped = False
            elif c == '\\':
                escaped = True
            elif c == '"':
                quoted = False
        elif c == '"':
            quoted = True
            result.append(c)
        elif not c.isspace():
            result.append(c)
    assert not quoted
    return ''.join(result)

protected = readj(root / 'docs/evidencias/editorial-r4-preservacao.json')['protected_before']
for entry in protected:
    check('preserved_' + entry['file'], sha(root / entry['file']) == entry['sha256'])
check('118_protected_files', len(protected) == 118, len(protected))
identity = readj(out / 'identificacao-academica.json')
expected = {'student': 'Matheus Luiz Bueno', 'discipline': 'ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS',
            'professor': 'EDER MAGALHAES MACHADO', 'class': '4P',
            'work': 'ProjektTellus — Simulador Planetário', 'period': '2º Bimestre de 2026'}
check('exact_academic_identity', identity == expected)
with zipfile.ZipFile(pptx) as z, zipfile.ZipFile(old) as prior:
    check('zip_integrity', z.testzip() is None)
    files = [n for n in z.namelist() if re.fullmatch(r'ppt/slides/slide\d+\.xml', n)]
    check('15_slides', len(files) == 15)
    xmls = {n: ET.fromstring(z.read(f'ppt/slides/slide{n}.xml')) for n in range(1, 16)}
    visible = {n: text(x) for n, x in xmls.items()}
    previous = {n: text(ET.fromstring(prior.read(f'ppt/slides/slide{n}.xml'))) for n in range(1, 16)}
    notes = {n: text(ET.fromstring(z.read(f'ppt/notesSlides/notesSlide{n}.xml'))) for n in range(1, 16)}
    forbidden = ['O enunciado pede', 'No mínimo 3 padrões', 'Nome e propósito',
                 'Problema e solução real', 'UML das classes', 'Código funcional em Java',
                 'Entender e defender', 'disponíveis conforme a pergunta',
                 'Quem cria o objeto?', 'Quem conhece o contrato?', 'Quem recebe a mudança?',
                 'Quem pode restaurar o estado?', 'sem Director separado',
                 'não torna o snapshot opaco', 'Limite numérico:',
                 'solução robusta', 'arquitetura escalável', 'experiência inovadora']
    for n, content in visible.items():
        check(f'number_{n}', f'{n:02} / 15' in content)
        check(f'footer_{n}', 'Matheus Luiz Bueno · 4P · ProjektTellus' in content)
        for phrase in forbidden:
            check(f'no_metatext_{n}_{phrase}', phrase.casefold() not in flat(content).casefold())
        check(f'no_presenter_instruction_{n}', not re.search(
            r'\b(defesa|apresentador|checklist|enunciado)\b|conforme a pergunta|professor espera', content, re.I))
        check(f'notes_A_B_{n}', 'Modo A' in notes[n] and 'Modo B' in notes[n])
        check(f'notes_correct_slide_{n}', f'Slide {n} ' in notes[n])
        for value in expected.values():
            check(f'notes_identity_{n}_{value}', value in notes[n])
        for run in xmls[n].findall('.//a:rPr', ns):
            font = run.find('a:latin', ns)
            if font is not None and font.get('typeface') == 'Consolas':
                check(f'code_font_{n}', int(run.get('sz', '0')) >= 1700)
    for key in ['student', 'discipline', 'professor', 'class', 'period']:
        check('cover_' + key, expected[key] in flat(visible[1]))
    check('cover_unchanged', visible[1] == previous[1])
    check('same_dimensions', ET.tostring(ET.fromstring(z.read('ppt/presentation.xml')).find('p:sldSz', ns)) ==
          ET.tostring(ET.fromstring(prior.read('ppt/presentation.xml')).find('p:sldSz', ns)))
    for phrase in ['Construção de mundos', 'Eventos planetários', 'Comunicação de mudanças', 'Recuperação de estados', 'TerraBuilder', 'SQLite']:
        check('slide2_real_project_' + phrase, phrase in visible[2])
    for n, pattern in [(4, 'Builder'), (6, 'Factory Method'), (8, 'Observer'), (10, 'Memento')]:
        check('pattern_purpose_problem_' + pattern, all(t in visible[n] for t in [pattern, 'Propósito', 'Problema real']))
        check('editable_UML_' + pattern, len(xmls[n].findall('.//p:cxnSp', ns)) >= 3)
        before = ET.fromstring(prior.read(f'ppt/slides/slide{n}.xml'))
        def relations(xml):
            # Ignore regenerated OOXML creation UUIDs; compare endpoints and geometry.
            return [(ET.tostring(e.find('p:nvCxnSpPr/p:cNvCxnSpPr', ns)),
                     ET.tostring(e.find('p:spPr', ns)))
                    for e in xml.findall('.//p:cxnSp', ns)]
        check('UML_relations_unchanged_' + pattern, relations(xmls[n]) == relations(before))
    for n in [5, 7, 9, 11]:
        check(f'editable_code_{n}', len(xmls[n].findall('.//a:t', ns)) > 10)
    check('builder_fluent_and_build', all(s in visible[5] for s in ['return this;', 'new Terra', 'new EstadoPlanetario']))
    check('factory_override_and_dispatch', all(s in visible[7] for s in ['@Override', 'new Asteroide(intensidade)', 'EventoPlanetario', 'evento.aplicar(terra.estado())']))
    callback = 'observer.aoMudar(mudanca);'
    check('callback_one_line', callback in visible[9].splitlines())
    check('synchronous_bus_and_FX', all(s in visible[9] for s in ['Bus síncrono', 'Platform.runLater']))
    check('memento_UUID', 'if (!id.equals(memento.mundoId()))' in visible[11] and 'mesmo mundo' in visible[10])
    check('slide12_correct_title', 'Os quatro padrões no fluxo do simulador' in visible[12])
    for term in ['Factory', 'Memento', 'Motor', 'SQLite', 'Terra', 'Observer', 'ordem interna', 'Capturar retrato manual']:
        check('flow_' + term, term in visible[12])
    check('slide14_measured_results', all(s in visible[14] for s in ['20% de He', '85,88%', '98,81%', '64,73%', 'Memento']))
    check('rounding_preserved_in_support', 'arredondamento' in notes[14] and 'duas pessoas' in notes[14])
    check('memento_opacity_in_support', 'público' in notes[10] and 'opacidade' in notes[10])
    check('builder_director_in_support', 'Director' in notes[4])
    check('conclusion_real_roles', all(s in visible[15] for s in ['Constrói a Terra.', 'Cria os eventos.', 'Comunica as mudanças.', 'Recupera estados anteriores.']))
    core = ET.fromstring(z.read('docProps/core.xml'))
    check('metadata_student', core.find('dc:creator', ns).text == expected['student'])
    check('metadata_professor', expected['professor'] in core.find('dc:description', ns).text)

fragments = readj(out / 'trechos-java.json')
check('same_real_Java_excerpts_as_r3', fragments == readj(root / 'docs/apresentacao/defesa-r3/trechos-java.json'))
for i, fragment in enumerate(fragments):
    check(f'Java_literal_source_{i}', fragment['raw'].replace('\r\n', '\n') in (root / fragment['file']).read_text(encoding='utf-8'))
all_visible = '\n'.join(visible.values())
for i, block in enumerate(readj(out / 'code-displays.json')):
    check(f'Java_whitespace_only_{i}', java(block['raw']) == java(block['display']))
    check(f'Java_actually_visible_{i}', all(line.strip() in all_visible for line in block['display'].splitlines() if line.strip()))

service = (root / 'src/main/java/com/projekttellus/application/SimulacaoService.java').read_text(encoding='utf-8')
apply = service.split('public ResultadoOperacao aplicar(')[1].split('public ResultadoOperacao avancar(')[0]
confirm = service.split('private ResultadoOperacao confirmar(')[1]
check('source_apply_order', apply.index('creator).criar') < apply.index('terra.capturar(') < apply.index('evento.aplicar(') < apply.index('return confirmar('))
check('source_confirm_order', confirm.index('ModeloPlanetario.calcular') < confirm.index('repository.salvar') < confirm.index('destino.atualizar') < confirm.index('observerBus.publicar'))

comparison = []
with fitz.open(pptx.with_suffix('.pdf')) as pdf:
    check('PDF_15_pages', len(pdf) == 15)
    check('PDF_student', pdf.metadata['author'] == expected['student'])
    for i, page in enumerate(pdf, 1):
        with Image.open(out / f'slide-{i:02}.png') as source:
            check(f'preview_resolution_{i}', source.size == (1366, 768))
            pix = page.get_pixmap(matrix=fitz.Matrix(4/3, 4/3))
            rendered = Image.frombytes('RGB', (pix.width, pix.height), pix.samples)
            mean = sum(ImageStat.Stat(ImageChops.difference(source.convert('RGB'), rendered)).mean) / 3
            comparison.append({'slide': i, 'mean_channel_error': mean})
            check(f'PDF_matches_preview_{i}', mean == 0.0, mean)
imported = root / 'target/editorial-r4-build/pptx-check'
import_receipt = readj(imported / 'source.json')
check('reimport_render_current_PPTX', import_receipt['sha256'] == sha(pptx))
pptx_comparison = []
for i in range(1, 16):
    with Image.open(out / f'slide-{i:02}.png') as a, Image.open(imported / f'slide-{i:02}.png') as b:
        check(f'PPTX_reimport_resolution_{i}', b.size == (1366, 768))
        mean = sum(ImageStat.Stat(ImageChops.difference(a.convert('RGB'), b.convert('RGB'))).mean) / 3
        pptx_comparison.append({'slide': i, 'mean_channel_error': mean, 'size': b.size})
        check(f'PPTX_reimport_matches_preview_{i}', mean < .001, mean)
(root / 'docs/evidencias/editorial-r4-pptx-render.json').write_text(json.dumps(
    {'source_sha256': sha(pptx), 'renderer': import_receipt['renderer'], 'comparisons': pptx_comparison}, indent=2), encoding='utf-8')
for name in ['Builder', 'Factory-Method', 'Observer', 'Memento']:
    with Image.open(out / ('UML-' + name + '.png')) as im:
        check('UML_high_resolution_' + name, im.size == (2732, 1536))

script = (root / 'docs/ROTEIRO_APRESENTACAO.md').read_text(encoding='utf-8')
check('15_script_sections', len(re.findall(r'^## Slide \d+ ', script, re.M)) == 15)
for mode, total, count in [('A', 640, 15), ('B', 380, 12)]:
    timing = re.findall(r'\*\*Modo ' + mode + r':\*\* (\d+):(\d+)–(\d+):(\d+) \((\d+) s\)', script)
    check('timing_count_' + mode, len(timing) == count)
    previous_end = 0
    for a, b, c, d, seconds in timing:
        start, end = int(a)*60+int(b), int(c)*60+int(d)
        check(f'timing_{mode}_{start}', start == previous_end and end-start == int(seconds))
        previous_end = end
    check('duration_' + mode, previous_end == total)
for n, section in enumerate(re.split(r'^## Slide \d+ .*$', script, flags=re.M)[1:], 1):
    for mode in ['A', 'B']:
        speech = re.search(r'\*\*Fala ' + mode + r':\*\* ([\s\S]*?)(?=\n\n|$)', section)
        check(f'speech_present_{mode}_{n}', speech is not None)
        if speech:
            check(f'no_production_commentary_{mode}_{n}', not re.search(r'tokens|whitespace|display final|reorganiza|enunciado pede|entender e defender', speech[1], re.I))
matrix = (root / 'docs/CONFORMIDADE_TRABALHO.md').read_text(encoding='utf-8')
for pattern in ['Builder', 'Factory Method', 'Observer', 'Memento']:
    check('four_criteria_' + pattern, len(re.findall(r'^\| ' + pattern + r' \|', matrix, re.M)) == 4)
record = {'base_commit': 'a06e9b7', 'checks': len(checks), 'failed': sum(not c['pass'] for c in checks),
          'application_tests_rerun': False, 'simulator_experiments_rerun': False,
          'checked_in_Microsoft_PowerPoint': False, 'physical_college_computer_tested': False,
          'protected_files': len(protected), 'pdf_preview_comparisons': comparison,
          'pptx_reimport_preview_comparisons': pptx_comparison,
          'final_files': {p.relative_to(root).as_posix(): sha(p) for p in [pptx, pptx.with_suffix('.pdf')]},
          'results': checks}
(root / 'docs/evidencias/editorial-r4-validacao.json').write_text(json.dumps(record, ensure_ascii=False, indent=2), encoding='utf-8')
(root / 'docs/evidencias/editorial-r4-textos.json').write_text(json.dumps(
    {'r3': previous, 'r4': visible, 'notes_r4': notes}, ensure_ascii=False, indent=2), encoding='utf-8')
# Check links only after producing this run's linked receipt and text inventory.
for relative in ['docs/ROTEIRO_APRESENTACAO.md', 'docs/CONFORMIDADE_TRABALHO.md', 'docs/apresentacao/README.md', 'docs/REVISAO_EDITORIAL_R4.md']:
    p = root / relative
    for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)', p.read_text(encoding='utf-8')):
        if not target.startswith(('http:', 'https:', '#')):
            check('link_' + relative + '_' + target, (p.parent / target.split('#')[0]).exists())
record['checks'] = len(checks)
record['failed'] = sum(not c['pass'] for c in checks)
(root / 'docs/evidencias/editorial-r4-validacao.json').write_text(json.dumps(record, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({k: record[k] for k in ['checks', 'failed', 'protected_files', 'application_tests_rerun']}, ensure_ascii=False))
for c in checks:
    if not c['pass']:
        print(json.dumps(c, ensure_ascii=False))
sys.exit(bool(record['failed']))
