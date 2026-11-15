"""Verified fallback: a captioned sequence of REAL captures, not a live screen recording."""
from pathlib import Path
import json,subprocess,sys
root=Path(__file__).resolve().parents[1]
capture=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else root/'docs/capturas'/json.loads((root/'docs/capturas/latest.json').read_text(encoding='utf-8-sig'))['directory']
work=root/'target/video-build';work.mkdir(parents=True,exist_ok=True)
out=root/'docs/apresentacao';out.mkdir(parents=True,exist_ok=True)
frames=[('01-builder-dialogo.png','Builder: reunir os componentes e construir uma Terra.'),
        ('02-antes-asteroide.png','Referência: 15 °C, 8 bilhões; humana 100%.'),
        ('03-snapshot-selecionavel.png','Memento: preservar o retrato antes da catástrofe.'),
        ('evento-1-asteroid-meio.png','Factory Method: Asteroide 25%. Quadro intermediário controlado.'),
        ('04-depois-asteroide.png','Resultado confirmado: 8 °C, 6,5 bilhões; humana 64,73%.'),
        ('09-apresentacao-observer.png','Observer: a mudança confirmou a notificação aos assinantes.'),
        ('05-restaurado-ui.png','Memento: recuperar o mesmo estado e a identidade do mundo.'),
        ('08-sqlite-reaberto-ui.png','SQLite: reabrir a biblioteca e recuperar o mundo salvo.')]
concat=[];captions=[]
for i,(name,text) in enumerate(frames):
 p=capture/name
 if not p.is_file():raise FileNotFoundError(p)
 concat += ["file '"+str(p).replace('\\','/')+"'",'duration 3']
 def stamp(sec):return f'00:00:{sec:02d},000'
 captions.append(f'{i+1}\n{stamp(i*3)} --> {stamp((i+1)*3)}\nCAPTURAS REAIS | {text}\n')
concat.append("file '"+str(capture/frames[-1][0]).replace('\\','/')+"'")
(work/'frames.txt').write_text('\n'.join(concat),encoding='utf-8')
(work/'demo.srt').write_text('\n'.join(captions),encoding='utf-8')
video=out/'ProjektTellus-Demo-Capturas.mp4'
cmd=['ffmpeg','-hide_banner','-n','-f','concat','-safe','0','-i',str(work/'frames.txt'),'-vf',
     "scale=1366:688:force_original_aspect_ratio=decrease,pad=1366:768:(ow-iw)/2:0:color=0x0B1523,setsar=1,subtitles=target/video-build/demo.srt:force_style='FontName=Segoe UI,FontSize=18,MarginV=14,Outline=1'",
     '-r','24','-t','24','-c:v','libx264','-preset','medium','-crf','20','-pix_fmt','yuv420p','-movflags','+faststart',str(video)]
subprocess.run(cmd,cwd=root,check=True,stdout=subprocess.DEVNULL)
probe=subprocess.run(['ffprobe','-v','error','-show_format','-show_streams','-of','json',str(video)],check=True,capture_output=True,text=True)
info=json.loads(probe.stdout);v=next(s for s in info['streams'] if s['codec_type']=='video')
assert v['width']==1366 and v['height']==768 and v['r_frame_rate']=='24/1' and v['pix_fmt']=='yuv420p'
assert 23.9<=float(info['format']['duration'])<=24.1
subprocess.run(['ffmpeg','-v','error','-i',str(video),'-f','null','-'],check=True,capture_output=True)
info['evidence_scope']='Sequência legendada de capturas reais. Não é gravação contínua, prova de FPS ou medição da duração das animações. Sem áudio.'
info['capture_source']=str(capture.relative_to(root));info['full_decode']='PASS';info['source_frames']=[x[0] for x in frames]
(root/'docs/evidencias/video-validacao.json').write_text(json.dumps(info,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'video':str(video),'bytes':video.stat().st_size,'decode':'PASS','duration':info['format']['duration']},ensure_ascii=False))
