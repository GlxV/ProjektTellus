// Editable slides and UML, authored with the Codex bundled @oai/artifact-tool.
// Run through Build-AcademicMaterials.ps1. All Java excerpts are extracted from source.
import fs from 'node:fs/promises';
import path from 'node:path';
import {pathToFileURL} from 'node:url';

const root=path.resolve(import.meta.dirname,'..');
const runtime=process.env.TELLUS_ARTIFACT_MODULE;
if(!runtime) throw new Error('Set TELLUS_ARTIFACT_MODULE to the bundled artifact_tool.mjs');
const {Presentation,PresentationFile}=await import(pathToFileURL(runtime).href);
const out=path.join(root,process.env.TELLUS_PREVIEW_DIR||'docs/apresentacao/editorial-r4');
const build=path.join(root,process.env.TELLUS_BUILD_DIR||'target/editorial-r4-build');
const academic={student:'Matheus Luiz Bueno',discipline:'ARQUITETURA DE SOFTWARES E PADRÕES DE PROJETOS',professor:'EDER MAGALHAES MACHADO',class:'4P',work:'ProjektTellus — Simulador Planetário',period:'2º Bimestre de 2026'};
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
function base(title,kicker=academic.discipline) {
 const s=p.slides.add(); s.background.fill=BG;
 txt(s,kicker,56,26,1250,30,18,GREEN,true);
 txt(s,title,56,78,1250,100,44,FG,true);
 txt(s,'Matheus Luiz Bueno · 4P · ProjektTellus',56,729,1050,25,17,MUTED);
 txt(s,String(p.slides.items.length).padStart(2,'0')+' / 15',1200,729,110,25,17,MUTED);
 return s;
}
function para(s,title,text,x,y,w=395) {txt(s,title,x,y,w,44,25,GREEN,true);txt(s,text,x,y+50,w,155,28);}
function box(s,title,body,x,y,w=310,h=110) {
 const a=s.shapes.add({geometry:'rect',name:title,position:{left:x,top:y,width:w,height:h},fill:BG,line:{fill:BLUE,width:1.5}});
 const bodyOffset=title.includes('\n')?78:48;
 txt(s,title,x+12,y+9,w-24,title.includes('\n')?62:36,23,GREEN,true);
 if(body) txt(s,body,x+12,y+bodyOffset,w-24,h-bodyOffset-1,21,FG);
 return a;
}
function link(s,a,b,kind='uses',from='bottom',to='top') {
 const inherit=kind==='extends'||kind==='implements';
 const c=s.shapes.connect(a,b,{kind:'straight',fromSide:from,toSide:to,line:{style:kind==='implements'||kind==='creates'||kind==='uses'?'dashed':'solid',fill:MUTED,width:1.5},head:{type:'none'},tail:inherit?{type:'none'}:{type:'arrow',width:'sm',length:'sm'}});
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
function code(s,text,x,y,w=1200,size=25,formatted=null) {
 const max=Math.floor(w/(size*.60));
 const original=text.split(/\r?\n/);
 const indent=Math.min(...original.filter(line=>line.trim()).map(line=>line.match(/^ */)[0].length));
 const initial=formatted??original.map(line=>line.slice(indent)).join('\n');
 const lines=initial.split(/\r?\n/).flatMap(line=>{
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
 for(let i=0;i<lines.length;i++) {
  const shape=txt(s,lines[i],x,y+i*(size*1.35),w,size*1.6,size,/\b(return|public|if|var|new|for|try|catch)\b/.test(lines[i])?GREEN:FG,false,'Consolas');
  for(const token of ['@Override','EventoPlanetario','observer.aoMudar(mudanca);'])if(lines[i].includes(token)){shape.text.get(token).color=BLUE;shape.text.get(token).bold=true;}
 }
}
function file(s,name,y=679){txt(s,sourceRoot+name,56,y,1260,42,19,MUTED);}
function notes(s,text){s.speakerNotes.text=['Aluno: '+academic.student,'Disciplina: '+academic.discipline,'Professor: '+academic.professor,'Turma: '+academic.class,'Trabalho: '+academic.work,'Período: '+academic.period].join('\n')+'\n\n'+text;}
const plan=await fs.readFile(path.join(root,'docs/ROTEIRO_APRESENTACAO.md'),'utf8');
function speech(n){const re=new RegExp('(?:^|\\n)## .*?(?:Slide|slide) '+n+'\\b[\\s\\S]*?(?=\\n## |$)');return plan.match(re)?.[0]??'Roteiro integral: docs/ROTEIRO_APRESENTACAO.md; slide '+n;}

// 01
let s=base('ProjektTellus','2º Bimestre de 2026 · Trabalho individual');
txt(s,'Simulador Planetário',56,176,690,80,45,FG,true);
txt(s,'Factory Method · Builder\nObserver · Memento',56,282,690,95,30,GREEN);
txt(s,'Aluno',56,395,690,32,20,MUTED);
txt(s,academic.student,56,430,690,50,33,FG,true);
txt(s,'ARQUITETURA DE SOFTWARES\nE PADRÕES DE PROJETOS',56,500,690,76,25,GREEN,true);
txt(s,'Professor: '+academic.professor,56,598,690,40,23,FG);
txt(s,'Turma: 4P · 2º Bimestre de 2026',56,646,690,35,23,MUTED);
await image(s,'docs/capturas/planeta-referencia.png',755,180,540,500);
notes(s,speech(1));
// 02
s=base('Mundos, eventos e histórico');
txt(s,'O ProjektTellus simula mudanças no ambiente e na população de planetas configuráveis.',56,205,565,180,32);
para(s,'Java 25 e JavaFX','Cada mundo tem identidade própria. O SQLite guarda condições, eventos e retratos anteriores.',56,455,565);
for(const [i,title,body] of [
 [0,'Construção de mundos','TerraBuilder reúne as condições iniciais.'],
 [1,'Eventos planetários','Cinco eventos transformam o estado do mundo.'],
 [2,'Comunicação de mudanças','Interface, alertas e estatísticas recebem a alteração.'],
 [3,'Recuperação de estados','Retratos preservam as condições de um mundo.'],
]){
 const y=195+i*125;
 txt(s,title,760,y,550,40,27,GREEN,true);
 txt(s,body,760,y+48,550,65,25);
}
notes(s,speech(2));
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
para(s,'Propósito','Construir uma Terra por etapas, configurando cada componente.',56,195);
para(s,'Problema real','Atmosfera, clima, água, vegetação e população precisam formar um cenário válido.',56,400);
const bd=box(s,'BuilderDialog','coleta valores do editor',515,190,335,100);
const tb=box(s,'TerraBuilder','+ build(): Terra\n+ atmosfera(...): TerraBuilder',515,350,335,130);
const te=box(s,'Terra','id · nome · estado',960,350,335,130);
const es=box(s,'EstadoPlanetario','Atmosfera · Clima · Biosfera\nHidrosfera · Civilizacao',880,560,415,105);
link(s,bd,tb);link(s,tb,te,'creates','right','left');link(s,te,es,'association');
txt(s,'«cria»',857,374,96,35,20,MUTED);
txt(s,'O editor configura o builder; build() cria a Terra.',515,680,780,30,21,MUTED);
notes(s,speech(4)+'\nReferência: https://refactoring.guru/design-patterns/builder');
// 05 Builder Java
s=base('Builder — do editor a uma nova Terra');
code(s,await excerpt('domain/TerraBuilder.java','    public TerraBuilder atmosfera','    public TerraBuilder clima'),56,190,1240,26,'public TerraBuilder atmosfera(Atmosfera valor) {\n    atmosfera = valor;\n    return this;\n}');
code(s,await excerpt('domain/TerraBuilder.java','    public Terra build()','\n}'),56,350,1240,26,'public Terra build() {\n    return new Terra(UUID.randomUUID(), nome,\n            new EstadoPlanetario(atmosfera, clima, biosfera,\n                    hidrosfera, civilizacao, anos));\n}');
file(s,'domain/TerraBuilder.java',550);
txt(s,'1  O método fluente guarda o componente e devolve o próprio builder.',56,610,1240,40,25);
txt(s,'2  build() reúne o estado e cria uma identidade UUID independente.',56,660,1240,40,25);
notes(s,speech(5)+'\nFonte: '+sourceRoot+'domain/TerraBuilder.java.');
// 06 Factory UML
s=base('Factory Method — criação polimórfica','02 / 04  •  PADRÃO DE CRIAÇÃO');
para(s,'Propósito','Deixar cada criador concreto decidir qual evento será instanciado.',56,195);
para(s,'Problema real','O serviço aplica cinco tipos de evento por um contrato comum.',56,400);
const ec=box(s,'«abstract» EventoCreator','+ criar(i): EventoPlanetario',510,190,350,105);
const ep=box(s,'«interface» EventoPlanetario','+ aplicar(estado): EstadoPlanetario',925,190,385,105);
const ac=box(s,'AsteroideCreator','+ criar(i): EventoPlanetario',510,380,350,100);
const eb=box(s,'«abstract» EventoBase','intensidade · aplicar() · transformar()',925,360,385,105);
const ast=box(s,'Asteroide','# transformar(estado, i)',925,535,385,100);
link(s,ac,ec,'extends','top','bottom');link(s,eb,ep,'implements','top','bottom');link(s,ast,eb,'extends','top','bottom');link(s,ac,ast,'creates','right','left');
txt(s,'Guerra nuclear, supervulcão, invasão alienígena e restauração\necológica usam a mesma estrutura de criação.',510,660,800,55,21,MUTED);
notes(s,speech(6)+'\nReferência: https://refactoring.guru/design-patterns/factory-method');
// 07 Factory code
s=base('Factory Method — criar e aplicar um evento');
txt(s,'Criador concreto: AsteroideCreator',56,181,1240,35,24,GREEN,true);
code(s,await excerpt('events/AsteroideCreator.java','    @Override public EventoPlanetario criar','\n}'),56,224,1260,26,'@Override\npublic EventoPlanetario criar(double intensidade) {\n    return new Asteroide(intensidade);\n}');
txt(s,'1  Sobrescrita de criar()\n\n2  new cria o produto',955,225,350,135,24,MUTED);
file(s,'events/AsteroideCreator.java',372);
txt(s,'Cliente: SimulacaoService.aplicar(...)',56,425,1240,35,24,GREEN,true);
code(s,await excerpt('application/SimulacaoService.java','        var evento = Objects.requireNonNull(creator).criar(intensidade);','        passos.add'),56,468,1260,25);
code(s,await excerpt('application/SimulacaoService.java','        var candidato = new Terra(terra.id(), terra.nome(), evento.aplicar(terra.estado()));','        passos.add'),56,516,1260,25,'var candidato = new Terra(terra.id(), terra.nome(),\n        evento.aplicar(terra.estado()));');
file(s,'application/SimulacaoService.java',595);
txt(s,'3  var infere EventoPlanetario, o contrato retornado por criar().',56,645,1240,35,24,BLUE);
txt(s,'4  aplicar() executa o comportamento do produto concreto.',56,685,1240,35,24,GREEN);
notes(s,speech(7)+'\nFontes: '+sourceRoot+'events/AsteroideCreator.java; '+sourceRoot+'application/SimulacaoService.java.');
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
txt(s,'MainController inscreve um observador para atualizar a interface.',520,655,790,55,22,MUTED);
notes(s,speech(8)+'\nReferência: https://refactoring.guru/design-patterns/observer');
// 09 Observer code
s=base('Observer — uma mudança, vários assinantes');
txt(s,'1  Contrato: SimulacaoObserver',56,184,650,40,24,GREEN,true);
const observerContract=await excerpt('patterns/observer/SimulacaoObserver.java','public interface SimulacaoObserver',null);
code(s,observerContract,56,238,665,24);
txt(s,'2  Inscrição: ObserverBus.inscrever',56,420,665,40,24,GREEN,true);
code(s,await excerpt('patterns/observer/ObserverBus.java','        Objects.requireNonNull(observer);','    }'),56,475,665,24);
txt(s,'3  SimulacaoService publica',765,184,540,40,24,GREEN,true);
code(s,await excerpt('application/SimulacaoService.java','            var entrega = observerBus.publicar','\n            ultimoMundo'),765,238,540,23,'var entrega = observerBus.publicar(\n    new MudancaPlanetaria(\n        destino.id(), destino.estado(),\n        indicadores, descricao));');
txt(s,'4  Bus chama cada inscrito',765,420,540,40,24,GREEN,true);
code(s,await excerpt('patterns/observer/ObserverBus.java','                observer.aoMudar(mudanca);','\n                notificados'),765,478,540,26);
txt(s,'Bus síncrono: aguarda o callback.\nUI: Platform.runLater agenda a tela.',765,548,540,80,23,MUTED);
txt(s,'Recebem: alertas, estatísticas e o assinante anônimo da interface.',56,641,1250,38,25,GREEN);
txt(s,sourceRoot+'patterns/observer/SimulacaoObserver.java · ObserverBus.java',56,687,1250,25,17,MUTED);
notes(s,speech(9)+'\nFontes: '+sourceRoot+'patterns/observer/SimulacaoObserver.java; '+sourceRoot+'patterns/observer/ObserverBus.java; '+sourceRoot+'application/SimulacaoService.java; '+sourceRoot+'ui/MainController.java.');
// 10 Memento UML
s=base('Memento — um retrato restaurável','04 / 04  •  PADRÃO COMPORTAMENTAL');
para(s,'Propósito','Preservar um estado anterior e restaurá-lo no mesmo mundo.',56,195);
para(s,'Problema real','Eventos e mudanças ambientais precisam poder ser revertidos.',56,400);
const orig=box(s,'Terra · Originator','+ capturar() · restaurar()',520,190,350,110);
const mem=box(s,'TerraMemento · Memento','mundoId · nome · estado · instante',940,190,370,110);
const care=box(s,'SimulacaoService · Caretaker','coordena captura e recuperação',520,420,350,110);
const repo=box(s,'MundoRepository','«interface»\nsalvar() · snapshots()',940,420,370,110);
link(s,orig,mem,'creates','right','left');link(s,care,orig,'uses','top','bottom');link(s,care,repo,'uses','right','left');link(s,repo,mem,'association','top','bottom');
txt(s,'O retrato preserva nome e estado.\nA restauração exige o UUID do mesmo mundo.',520,615,790,85,24,MUTED);
notes(s,speech(10)+'\nReferência: https://refactoring.guru/design-patterns/memento');
// 11 Memento code
s=base('Memento — captura e restauração segura');
code(s,await excerpt('domain/Terra.java','    public TerraMemento capturar','    public void restaurar'),56,185,1240,25);
code(s,await excerpt('domain/Terra.java','        if (!id.equals(memento.mundoId()))','\n    }'),56,355,1240,25);
file(s,'domain/Terra.java',570);
txt(s,'A Terra recupera o nome e o estado sem trocar sua identidade.',56,635,1250,55,28,GREEN);
notes(s,speech(11)+'\nFonte: '+sourceRoot+'domain/Terra.java.');
// 12 integration
s=base('Os quatro padrões no fluxo do simulador');
txt(s,'Construção, captura e restauração de um mundo',56,181,1250,45,26,BLUE);
box(s,'Builder','Construir mundo',56,240,360,105);
box(s,'Memento','Capturar retrato manual',505,240,360,105);
box(s,'Memento','Restaurar estado anterior',955,240,355,105);
txt(s,'Aplicar evento — ordem interna de SimulacaoService.aplicar',56,380,1250,45,27,GREEN,true);
const labels=[['Factory\nMethod','Criar produto'],['Memento','Capturar retrato\nautomático'],['Motor','Aplicar efeito\nCalcular índices'],['SQLite','Gravar estado,\nhistórico e retrato'],['Terra','Atualizar objeto'],['Observer','Publicar mudança']];
const nodes=labels.map(([a,b],i)=>box(s,a,b,56+i*213,439,195,135));
for(let i=0;i<nodes.length-1;i++)link(s,nodes[i],nodes[i+1],'uses','right','left');
txt(s,'Primeiro confirma no SQLite; depois atualiza a Terra e notifica.\nCapturar manualmente e restaurar são outras operações.',56,634,1250,80,27);
notes(s,speech(12)+'\nFonte: '+sourceRoot+'application/SimulacaoService.java.');
// 13 complementary model
s=base('Indicadores e evolução do planeta');
txt(s,'Humana',56,205,390,60,37,GREEN,true);txt(s,'Respiração e condições\npara humanos sem proteção.',56,290,390,130,30);
txt(s,'Biológica',490,205,390,60,37,BLUE,true);txt(s,'Condições amplas para vida\nassociada à água líquida.',490,290,390,130,30);
txt(s,'Civilização',935,205,390,60,37,GREEN,true);txt(s,'Recursos, área continental\ne ocupação do suporte.',935,290,390,130,30);
txt(s,'Pressão parcial = pressão total × fração do gás',56,475,1250,60,32);
txt(s,'Evolução determinística em passos de até 1 ano.\nModelo global didático, sem calibração científica.',56,580,1250,100,28,MUTED);
notes(s,speech(13)+'\nFonte: simulation/ModeloPlanetario.java e docs/MODELO.md. Os escores são produtos de adequações, não probabilidades. Capacidade usa terra, água, vegetação, humana e raiz do potencial biológico.');
// 14 experiments: literal measurements read from generated selected file.
const findings=JSON.parse(await fs.readFile(path.join(out,'experimentos-selecionados.json'),'utf8')).findings;
s=base('Resultados dos experimentos');
for(let i=0;i<findings.length;i++){
 const y=195+i*160;txt(s,findings[i].title,56,y,1235,48,31,GREEN,true);txt(s,findings[i].text,56,y+55,1240,83,27);
}
notes(s,speech(14)+'\nFontes dos resultados: docs/auditoria/experimental-20261115-final/snapshots.csv e docs/capturas/run-20261115-124710/measurements.csv.');
// 15 conclusion real screenshot
s=base('Construção, eventos, comunicação e recuperação');
txt(s,'Mundos configuráveis com eventos, indicadores e histórico em SQLite.',56,185,1260,70,30,GREEN);
await image(s,'docs/capturas/demo-restaurado.png',56,280,740,417);
for(const [i,name,role] of [
 [0,'Builder','Constrói a Terra.'],
 [1,'Factory Method','Cria os eventos.'],
 [2,'Observer','Comunica as mudanças.'],
 [3,'Memento','Recupera estados anteriores.'],
]){
 txt(s,name,860,280+i*85,440,36,27,GREEN,true);
 txt(s,role,860,318+i*85,440,38,25);
}
txt(s,'69 testes aprovados\n2.026 experimentos registrados',860,645,440,65,22,MUTED);
notes(s,speech(15));

// Export draft and all actual previews. PPTX diagrams and text remain editable.
await fs.writeFile(path.join(out,'trechos-java.json'),JSON.stringify(snippets,null,2));
await fs.writeFile(path.join(out,'identificacao-academica.json'),JSON.stringify(academic,null,2));
await fs.writeFile(path.join(out,'code-displays.json'),JSON.stringify(codeDisplays,null,2));
await fs.writeFile(path.join(build,'code-displays.json'),JSON.stringify(codeDisplays,null,2));
await fs.writeFile(path.join(build,'source.json'),JSON.stringify(p.toProto()));
for(let i=0;i<p.slides.items.length;i++){
 const blob=await p.export({slide:p.slides.items[i],format:'png',scale:1});
 await fs.writeFile(path.join(out,`slide-${String(i+1).padStart(2,'0')}.png`),new Uint8Array(await blob.arrayBuffer()));
}
await (await PresentationFile.exportPptx(p)).save(path.join(build,'candidate.pptx'));
for(const [i,name] of [[3,'Builder'],[5,'Factory-Method'],[7,'Observer'],[9,'Memento']]){
 const high=await p.export({slide:p.slides.items[i],format:'png',scale:2});
 await fs.writeFile(path.join(out,'UML-'+name+'.png'),new Uint8Array(await high.arrayBuffer()));
}
console.log('Exported 15 slides, editable draft and high resolution UML. PDF assembled from reviewed renders. Finalize candidate separately.');
