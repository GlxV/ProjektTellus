// Editable slides and UML, authored with the Codex bundled @oai/artifact-tool.
// Run through Build-AcademicMaterials.ps1. All Java excerpts are extracted from source.
import fs from 'node:fs/promises';
import path from 'node:path';
import {pathToFileURL} from 'node:url';

const root=path.resolve(import.meta.dirname,'..');
const runtime=process.env.TELLUS_ARTIFACT_MODULE;
if(!runtime) throw new Error('Set TELLUS_ARTIFACT_MODULE to the bundled artifact_tool.mjs');
const {Presentation,PresentationFile}=await import(pathToFileURL(runtime).href);
const out=path.join(root,'docs/apresentacao');
const build=path.join(root,'target/presentation-build');
await fs.mkdir(out,{recursive:true}); await fs.mkdir(build,{recursive:true});
await fs.access(path.join(root,'docs/CONFORMIDADE_TRABALHO.md'));
const BG='#0B1523', FG='#EBF4FC', MUTED='#ABC0D4', GREEN='#59DFC0', BLUE='#70BEFF';
const p=Presentation.create({slideSize:{width:1366,height:768}});
const sourceRoot='src/main/java/com/projekttellus/';
const snippets=[],codeDisplays=[];
function txt(s,text,x,y,w,h,size=28,color=FG,bold=false,font='Segoe UI') {
  const a=s.shapes.add({geometry:'textbox',name:text.slice(0,55),position:{left:x,top:y,width:w,height:h},fill:'none',line:{fill:'none',width:0}});
  a.text=text; a.text.style={typeface:font,fontSize:size,color,bold,insets:0,autoFit:'none'}; return a;
}
function base(title,kicker='ARQUITETURA DE SOFTWARE E PADRÕES DE PROJETOS') {
 const s=p.slides.add(); s.background.fill=BG;
 txt(s,kicker,56,26,1250,30,18,GREEN,true);
 txt(s,title,56,78,1250,100,44,FG,true);
 txt(s,'ProjektTellus  /  Java 25',56,729,1050,25,17,MUTED);
 txt(s,String(p.slides.items.length).padStart(2,'0')+' / 15',1200,729,110,25,17,MUTED);
 return s;
}
function para(s,title,text,x,y,w=395) {txt(s,title,x,y,w,44,25,GREEN,true);txt(s,text,x,y+50,w,155,28);}
function box(s,title,body,x,y,w=310,h=110) {
 const a=s.shapes.add({geometry:'rect',name:title,position:{left:x,top:y,width:w,height:h},fill:BG,line:{fill:BLUE,width:1.5}});
 txt(s,title,x+12,y+9,w-24,36,23,GREEN,true);
 if(body) txt(s,body,x+12,y+48,w-24,h-49,21,FG);
 return a;
}
function link(s,a,b,kind='uses',from='bottom',to='top') {
 const inherit=kind==='extends'||kind==='implements';
 const c=s.shapes.connect(a,b,{kind:'straight',fromSide:from,toSide:to,line:{style:kind==='implements'||kind==='creates'||kind==='uses'?'dashed':'solid',fill:MUTED,width:1.5},tail:inherit?{type:'none'}:{type:'arrow',width:'sm',length:'sm'}});
 if(inherit){
   const bp=b.position; let cx=bp.left+bp.width/2,cy=bp.top+bp.height;
   if(to==='top')cy=bp.top; if(to==='left'){cx=bp.left;cy=bp.top+bp.height/2;}
   if(to==='right'){cx=bp.left+bp.width;cy=bp.top+bp.height/2;}
   const rot=to==='bottom'?0:to==='left'?90:to==='right'?270:180;
   if(to==='bottom')cy+=8;if(to==='top')cy-=8;if(to==='left')cx-=8;if(to==='right')cx+=8;
   s.shapes.add({geometry:'triangle',position:{left:cx-8,top:cy-8,width:16,height:16,rotation:rot},fill:BG,line:{fill:MUTED,width:1.5}});
 }
 return c;
}
function segment(s,x1,y1,x2,y2){
 return s.shapes.add({geometry:'connector',kind:'straight',position:{left:Math.min(x1,x2),top:Math.min(y1,y2),width:Math.abs(x2-x1),height:Math.abs(y2-y1)},line:{fill:MUTED,width:1.5,style:'dashed'}});
}
async function image(s,file,x,y,w,h) {
 const bytes=await fs.readFile(path.join(root,file));
 s.images.add({blob:bytes.buffer.slice(bytes.byteOffset,bytes.byteOffset+bytes.byteLength),contentType:'image/png',alt:file,fit:'contain',position:{left:x,top:y,width:w,height:h}});
}
async function excerpt(file,start,end) {
 const source=await fs.readFile(path.join(root,sourceRoot+file),'utf8');
 const first=source.indexOf(start); if(first<0)throw new Error('Missing excerpt '+file+' '+start);
 const last=end?source.indexOf(end,first+start.length):source.length;
 if(last<0)throw new Error('Missing end '+file);
 const raw=source.slice(first,last).trimEnd();
 const line=source.slice(0,first).split('\n').length;
 snippets.push({file:sourceRoot+file,line,raw}); return raw;
}
function code(s,text,x,y,w=1200,size=25) {
 const max=Math.floor(w/(size*.60));
 const lines=text.split(/\r?\n/).flatMap(line=>{
   const result=[];let tail=line;
   while(tail.length>max){
    let cut=-1,comma=-1,quoted=false,escaped=false;
    for(let j=0;j<Math.min(max,tail.length);j++){
     const ch=tail[j];
     if(quoted){if(escaped)escaped=false;else if(ch.charCodeAt(0)===92)escaped=true;else if(ch==='"')quoted=false;}
     else if(ch==='"')quoted=true;
     else if(ch===' '||ch==='('||ch===','){cut=j+1;if(ch===',')comma=j+1;}
    }
    if(comma>max/2)cut=comma;
    if(cut<max/3)break;
    result.push(tail.slice(0,cut).trimEnd());tail='        '+tail.slice(cut).trimStart();
   }
   result.push(tail);return result;
 });
 codeDisplays.push({raw:text,display:lines.join('\n')});
 for(let i=0;i<lines.length;i++) txt(s,lines[i],x,y+i*(size*1.35),w,size*1.6,size,/\b(return|public|if|var|new|for|try|catch)\b/.test(lines[i])?GREEN:FG,false,'Consolas');
}
function file(s,name,y=679){txt(s,sourceRoot+name,56,y,1260,42,19,MUTED);}
function notes(s,text){s.speakerNotes.text=text;}
const plan=await fs.readFile(path.join(root,'docs/ROTEIRO_APRESENTACAO.md'),'utf8');
function speech(n){const re=new RegExp('(?:^|\\n)## .*?(?:Slide|slide) '+n+'\\b[\\s\\S]*?(?=\\n## |$)');return plan.match(re)?.[0]??'Roteiro integral: docs/ROTEIRO_APRESENTACAO.md; slide '+n;}

// 01
let s=base('ProjektTellus','TRABALHO INDIVIDUAL  •  BIMESTRE 2 / 2026');
txt(s,'Simulador Planetário',56,187,640,100,48,FG,true);
txt(s,'Factory Method · Builder\nObserver · Memento',56,325,680,130,33,GREEN);
txt(s,'Arquitetura de Software e Padrões de Projetos\nEntrega e apresentação: 16 a 23/11/2026',56,570,690,85,24,MUTED);
await image(s,'docs/capturas/planeta-referencia.png',755,180,540,500);
notes(s,speech(1)+'\nFonte primária: docs/ENUNCIADO_ORIGINAL_TRANSCRITO.md. Identificação nominal não informada; inserir no PowerPoint antes da apresentação.');
// 02
s=base('Um software real para aplicar os padrões');
para(s,'O problema','Reunir condições, executar eventos, comunicar mudanças e recuperar estados.',56,195,565);
para(s,'A proposta','Uma aplicação Java desktop com mundos independentes e histórico persistente.',56,415,565);
txt(s,'O enunciado pede',760,195,530,55,32,GREEN,true);
txt(s,'No mínimo 3 padrões\n\nNome e propósito\nProblema e solução real\nUML das classes\nCódigo funcional em Java',760,270,530,390,29);
notes(s,speech(2)+'\nO enunciado aceita os quatro padrões utilizados. 10–12 minutos é planejamento, não regra da professora. Referências originais: https://refactoring.guru/pt-br/design-patterns/catalog e https://www.devmedia.com.br/guia/padroes-de-projeto-em-java/34456.');
// 03 architecture (editable diagram)
s=base('Arquitetura geral');
const ui=box(s,'ui/','JavaFX · MainController',56,195,550,110);
const app=box(s,'application/','SimulacaoService · casos de uso',56,370,550,110);
const persist=box(s,'persistence/','MundoRepository → SQLite/JDBC',56,555,550,110);
const domain=box(s,'domain/','Terra · EstadoPlanetario · Builder',770,195,540,110);
const engines=box(s,'simulation/ e events/','Cálculo, evolução e produtos de eventos',770,370,540,110);
const patterns=box(s,'patterns/','ObserverBus · TerraMemento',770,555,540,110);
link(s,ui,app);link(s,app,persist);link(s,app,engines,'uses','right','left');link(s,engines,domain,'uses','top','bottom');link(s,app,patterns,'uses','right','left');
notes(s,speech(3)+'\nDiagrama de pacotes simplificado do código. O domínio não depende de JavaFX nem SQLite.');
// 04 Builder UML
s=base('Builder — construção por etapas','01 / 04  •  PADRÃO DE CRIAÇÃO');
para(s,'Propósito','Montar uma Terra com componentes e valores opcionais legíveis.',56,195);
para(s,'Problema real','Atmosfera, clima, água, vegetação e população precisam formar um cenário válido.',56,400);
const bd=box(s,'BuilderDialog','coleta valores do editor',515,190,335,100);
const tb=box(s,'TerraBuilder','+ build(): Terra\n+ atmosfera(...): TerraBuilder',515,350,335,130);
const te=box(s,'Terra','id · nome · estado',960,350,335,130);
const es=box(s,'EstadoPlanetario','Atmosfera · Clima · Biosfera\nHidrosfera · Civilizacao',880,560,415,105);
link(s,bd,tb);link(s,tb,te,'creates','right','left');link(s,te,es,'association');
txt(s,'«cria»',857,374,96,35,20,MUTED);
txt(s,'Builder concreto e fluente; sem Director separado',515,680,780,30,21,MUTED);
notes(s,speech(4)+'\nUML: dependência BuilderDialog→TerraBuilder; build cria Terra e EstadoPlanetario; Terra referencia seu estado. Records imutáveis podem ser compartilhados com segurança. https://refactoring.guru/design-patterns/builder');
// 05 Builder Java
s=base('Builder — do editor a uma nova Terra');
code(s,await excerpt('domain/TerraBuilder.java','    public TerraBuilder atmosfera','    public TerraBuilder clima'),56,190,1240,27);
code(s,await excerpt('domain/TerraBuilder.java','    public Terra build()','\n}'),56,285,1240,27);
file(s,'domain/TerraBuilder.java',470);
txt(s,'1  O método fluente guarda o componente e devolve o próprio builder.',56,545,1240,55,26);
txt(s,'2  build() reúne o estado e cria uma identidade UUID independente.',56,600,1240,55,26);
notes(s,speech(5)+'\nExcertos literais de TerraBuilder.java. A validação de limites e nulos está nos records do domínio, não em uma duplicação no builder. Demonstração: Criar mundo, condições iniciais e Construir e salvar.');
// 06 Factory UML
s=base('Factory Method — criação polimórfica','02 / 04  •  PADRÃO DE CRIAÇÃO');
para(s,'Propósito','Deixar cada criador concreto decidir qual evento será instanciado.',56,195);
para(s,'Problema real','O serviço aplica cinco tipos de evento por um contrato comum.',56,400);
const ec=box(s,'«abstract» EventoCreator','+ criar(i): EventoPlanetario',510,190,350,105);
const ep=box(s,'«interface» EventoPlanetario','+ aplicar(estado): EstadoPlanetario',925,190,385,105);
const ac=box(s,'AsteroideCreator','+ criar(i): EventoPlanetario',510,380,350,100);
const eb=box(s,'«abstract» EventoBase','intensidade · aplicar() · transformar()',925,360,385,105);
const ast=box(s,'Asteroide','+ transformar(estado, i)',925,535,385,100);
link(s,ac,ec,'extends','top','bottom');link(s,eb,ep,'implements','top','bottom');link(s,ast,eb,'extends','top','bottom');link(s,ac,ast,'creates','right','left');
txt(s,'Outros pares Creator/produto: GuerraNuclear · Supervulcao\nInvasaoAlienigena · RestauracaoEcologica',510,660,800,55,21,MUTED);
notes(s,speech(6)+'\nTriângulo vazio + linha contínua = herança; tracejada = realização de interface. AsteroideCreator cria Asteroide; os quatro outros pares seguem a mesma estrutura. https://refactoring.guru/design-patterns/factory-method');
// 07 Factory code
s=base('Factory Method — criar e aplicar um evento');
code(s,await excerpt('events/AsteroideCreator.java','    @Override public EventoPlanetario criar','\n}'),56,190,1260,25);
file(s,'events/AsteroideCreator.java',260);
code(s,await excerpt('application/SimulacaoService.java','        var evento = Objects.requireNonNull(creator).criar(intensidade);','        passos.add'),56,360,1260,25);
code(s,await excerpt('application/SimulacaoService.java','        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));','        passos.add'),56,420,1260,23);
file(s,'application/SimulacaoService.java',505);
txt(s,'O criador decide o produto; o serviço chama aplicar() pelo contrato.',56,590,1250,80,29,GREEN);
notes(s,speech(7)+'\nExcertos reais. A intensidade no domínio é 0..1: 25% na interface equivale a 0.25. O cliente não precisa decidir qual transformar() executar.');
// 08 Observer UML
s=base('Observer — comunicação após a confirmação','03 / 04  •  PADRÃO COMPORTAMENTAL');
para(s,'Propósito','Notificar assinantes por uma interface comum.',56,195);
para(s,'Problema real','Globo, alertas e estatísticas precisam receber a mesma mudança confirmada.',56,400);
const sv=box(s,'SimulacaoService','confirma SQLite e publica',520,190,350,105);
const ob=box(s,'ObserverBus','+ inscrever() · publicar()',520,385,350,105);
const so=box(s,'SimulacaoObserver','«interface»\n+ aoMudar(MudancaPlanetaria)',950,190,360,130);
const al=box(s,'AlertaObserver','alertas por UUID',950,385,360,90);
const st=box(s,'EstatisticasObserver','contagem e extremos por UUID',950,520,360,90);
link(s,sv,ob,'association');link(s,ob,so,'association','right','left');link(s,al,so,'implements','top','bottom');
// Shared realization branch avoids a line through the other concrete observer.
segment(s,950,565,910,565);segment(s,910,565,910,345);segment(s,910,345,1130,345);
txt(s,'MainController também inscreve um observador anônimo para a UI.\nO bus entrega MudancaPlanetaria; uma falha não bloqueia os demais.',520,650,790,65,22,MUTED);
notes(s,speech(8)+'\nBus armazena referências SimulacaoObserver; o serviço cria e inscreve monitor e estatísticas, e MainController inscreve callback da interface com Platform.runLater. https://refactoring.guru/design-patterns/observer');
// 09 Observer code
s=base('Observer — uma mudança, vários assinantes');
code(s,await excerpt('patterns/observer/ObserverBus.java','    public AutoCloseable inscrever','    public void remover'),56,190,1220,25);
code(s,await excerpt('patterns/observer/SimulacaoObserver.java','public interface SimulacaoObserver','\n}'),56,410,1220,25);
txt(s,'}',56,545,1220,40,25,FG,false,'Consolas');
txt(s,'Dentro de publicar():',795,405,500,50,24,MUTED);
code(s,await excerpt('patterns/observer/ObserverBus.java','                observer.aoMudar(mudanca);','\n                notificados'),795,475,510,24);
file(s,'patterns/observer/ObserverBus.java + SimulacaoObserver.java',604);
txt(s,'publicar() percorre os inscritos e chama observer.aoMudar(mudanca).',56,661,1240,52,25,GREEN);
notes(s,speech(9)+'\nFragmento literal interno de publicar(); o bloco completo captura RuntimeException, registra falhas e continua o laço. Estudo completo em ESTUDO_PADROES.md.');
// 10 Memento UML
s=base('Memento — um retrato restaurável','04 / 04  •  PADRÃO COMPORTAMENTAL');
para(s,'Propósito','Preservar um estado anterior e restaurá-lo no mesmo mundo.',56,195);
para(s,'Problema real','Eventos e mudanças ambientais precisam poder ser revertidos.',56,400);
const orig=box(s,'Terra · Originator','+ capturar() · restaurar()',520,190,350,110);
const mem=box(s,'TerraMemento · Memento','mundoId · nome · estado · instante',940,190,370,110);
const care=box(s,'SimulacaoService · Caretaker','coordena captura e recuperação',520,420,350,110);
const repo=box(s,'MundoRepository','«interface»\nsalvar() · snapshots()',940,420,370,110);
link(s,orig,mem,'creates','right','left');link(s,care,orig,'uses','top','bottom');link(s,care,repo,'uses','right','left');link(s,repo,mem,'association','top','bottom');
txt(s,'Estado profundamente imutável; restauração exige o mesmo UUID.\nO record permite leitura: esta variante não torna o snapshot opaco.',520,615,790,85,24,MUTED);
notes(s,speech(10)+'\nO caretaker coordena uso e persistência. Memento não é sinônimo de SQLite: pode existir em memória. https://refactoring.guru/design-patterns/memento');
// 11 Memento code
s=base('Memento — captura e restauração segura');
code(s,await excerpt('domain/Terra.java','    public TerraMemento capturar','    public void restaurar'),56,185,1240,25);
code(s,await excerpt('domain/Terra.java','        if (!id.equals(memento.mundoId()))','\n    }'),56,355,1240,25);
file(s,'domain/Terra.java',570);
txt(s,'Capturar → aplicar Asteroide 25% → selecionar retrato → Restaurar',56,635,1250,55,28,GREEN);
notes(s,speech(11)+'\nO segundo trecho está dentro de restaurar(TerraMemento), após Objects.requireNonNull. O id da Terra é final e não é substituído. O estado imutável anterior pode ser reassociado sem clonagem profunda.');
// 12 integration
s=base('Os quatro padrões na mesma operação');
const labels=[['Builder','Construir a Terra'],['Factory Method','Criar o evento'],['Memento','Preservar estado'],['Observer','Comunicar mudança'],['Memento','Restaurar depois']];
const nodes=labels.map(([a,b],i)=>box(s,a,b,56+i*255,285,235,145));
for(let i=0;i<nodes.length-1;i++)link(s,nodes[i],nodes[i+1],'uses','right','left');
txt(s,'Criação e aplicação pelo serviço; restauração em outra operação.',56,190,1240,70,29,GREEN);
txt(s,'A transação grava estado, histórico e retrato antes da notificação.\nCada operação bem-sucedida retorna passos reais para o modo Apresentação.',56,520,1235,130,30);
notes(s,speech(12)+'\nDentro de SimulacaoService.aplicar, a ordem é criar produto → capturar retrato automático → aplicar e formar candidato → confirmar SQLite → atualizar Terra → publicar. A restauração é outra operação posterior. A demo também realiza uma captura MANUAL anterior ao clique Aplicar evento. Snapshot manual e salvar não emitem notificação de mudança ambiental.');
// 13 complementary model
s=base('Três indicadores, três perguntas');
txt(s,'Humana',56,205,390,60,37,GREEN,true);txt(s,'Respiração e condições\npara humanos sem proteção.',56,290,390,130,30);
txt(s,'Biológica',490,205,390,60,37,BLUE,true);txt(s,'Condições amplas para vida\nassociada à água líquida.',490,290,390,130,30);
txt(s,'Civilização',935,205,390,60,37,GREEN,true);txt(s,'Recursos, área continental\ne ocupação do suporte.',935,290,390,130,30);
txt(s,'Pressão parcial = pressão total × fração do gás',56,475,1250,60,32);
txt(s,'Evolução determinística em passos de até 1 ano.\nModelo global didático, sem calibração científica.',56,580,1250,100,28,MUTED);
notes(s,speech(13)+'\nFonte: simulation/ModeloPlanetario.java e docs/MODELO.md. Os escores são produtos de adequações, não probabilidades. Capacidade usa terra, água, vegetação, humana e raiz do potencial biológico.');
// 14 experiments: literal measurements read from generated selected file.
const findings=JSON.parse(await fs.readFile(path.join(root,'docs/auditoria/slide-findings.json'),'utf8'));
s=base('Experimentos que ajudam a explicar o modelo');
for(let i=0;i<findings.length;i++){
 const y=185+i*164;txt(s,findings[i].title,56,y,1235,55,32,GREEN,true);txt(s,findings[i].text,56,y+60,1240,95,28);
}
notes(s,speech(14)+'\nResultados medidos em docs/auditoria/experimental-20261115-final/snapshots.csv; estado base 3e153f0. '+JSON.stringify(findings));
// 15 conclusion real screenshot
s=base('Demonstração e conclusão');
txt(s,'Construir → capturar → aplicar → observar → restaurar',56,185,1260,70,31,GREEN);
await image(s,'docs/capturas/demo-restaurado.png',56,280,740,417);
txt(s,'Entender e defender',860,285,440,55,31,GREEN,true);
txt(s,'Quem cria o objeto?\nQuem conhece o contrato?\nQuem recebe a mudança?\nQuem pode restaurar o estado?',860,365,440,220,28);
txt(s,'69 testes aprovados · fluxo gráfico real\n2.026 experimentos · modelo didático',860,615,440,80,22,MUTED);
notes(s,speech(15)+'\nContingência: PDF, capturas, UML exportado e Java nos slides. IA auxiliou os materiais desta entrega; a autoria anterior do repositório não foi inferida.');

// Export draft and all actual previews. PPTX diagrams and text remain editable.
await fs.writeFile(path.join(out,'trechos-java.json'),JSON.stringify(snippets,null,2));
await fs.writeFile(path.join(build,'code-displays.json'),JSON.stringify(codeDisplays,null,2));
await fs.writeFile(path.join(build,'source.json'),JSON.stringify(p.toProto()));
for(let i=0;i<p.slides.items.length;i++){
 const blob=await p.export({slide:p.slides.items[i],format:'png',scale:1});
 await fs.writeFile(path.join(out,`slide-${String(i+1).padStart(2,'0')}.png`),new Uint8Array(await blob.arrayBuffer()));
}
await (await PresentationFile.exportPptx(p)).save(path.join(build,'candidate.pptx'));
for(const [i,name] of [[3,'Builder'],[5,'Factory-Method'],[7,'Observer'],[9,'Memento']]){
 const high=await p.export({slide:p.slides.items[i],format:'png',scale:2});
 await fs.writeFile(path.join(root,'docs/uml-apresentacao',name+'.png'),new Uint8Array(await high.arrayBuffer()));
}
console.log('Exported 15 slides, editable draft and high resolution UML. PDF assembled from reviewed renders. Finalize candidate separately.');
