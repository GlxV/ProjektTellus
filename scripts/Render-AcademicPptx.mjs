// Read-only rendering of the actual exported PPTX with the bundled Artifact Tool.
import fs from 'node:fs/promises';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import {createHash} from 'node:crypto';

const root=path.resolve(import.meta.dirname,'..');
if(!process.env.TELLUS_ARTIFACT_MODULE)throw new Error('Set TELLUS_ARTIFACT_MODULE to the bundled artifact_tool.mjs');
const {PresentationFile,FileBlob}=await import(pathToFileURL(process.env.TELLUS_ARTIFACT_MODULE).href);
const input=path.resolve(root,process.argv[2]||'docs/apresentacao/ProjektTellus-Defesa-Individual-r4.pptx');
const out=path.resolve(root,process.argv[3]||'target/editorial-r4-build/pptx-check');
await fs.mkdir(out,{recursive:true});
const digest=bytes=>createHash('sha256').update(bytes).digest('hex');
const before=digest(await fs.readFile(input));
const p=await PresentationFile.importPptx(await FileBlob.load(input));
if(p.slides.items.length!==15)throw new Error('Expected 15 slides');
for(let i=0;i<p.slides.items.length;i++){
 const png=await p.export({slide:p.slides.items[i],format:'png',scale:1});
 await fs.writeFile(path.join(out,`slide-${String(i+1).padStart(2,'0')}.png`),new Uint8Array(await png.arrayBuffer()));
}
if(digest(await fs.readFile(input))!==before)throw new Error('Source PPTX changed during render');
await fs.writeFile(path.join(out,'source.json'),JSON.stringify({file:input,sha256:before,slides:15,renderer:'Artifact Tool reimport; no native PowerPoint execution'},null,2));
console.log('Rendered all 15 slides from exported PPTX using Artifact Tool; no native PowerPoint execution.');
