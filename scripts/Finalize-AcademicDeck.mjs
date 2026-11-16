import path from 'node:path';
import fs from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {pathToFileURL} from 'node:url';
const root=path.resolve(import.meta.dirname,'..');
const skill=process.env.TELLUS_PRESENTATIONS_SKILL;
if(!skill)throw new Error('TELLUS_PRESENTATIONS_SKILL is required');
const {finalizePresentation}=await import(pathToFileURL(path.join(skill,'container_tools/artifact_tool_utils.mjs')).href);
const finalPath=path.join(root,'docs/apresentacao',process.env.TELLUS_DECK_NAME||'ProjektTellus-Defesa-Individual-r3.pptx');
const receiptPath=path.join(root,'docs/evidencias',path.parse(finalPath).name+'.validacao.json');
const referencePath=path.join(root,'docs/apresentacao/ProjektTellus-Apresentacao-r2.pptx');
const referenceSha256=createHash('sha256').update(await fs.readFile(referencePath)).digest('hex');
const result=await finalizePresentation({
 workspaceDir:root,
 candidatePath:path.join(root,process.env.TELLUS_BUILD_DIR||'target/defense-r3-build','candidate.pptx'),
 finalPath,
 pythonExecutable:process.env.TELLUS_RUNTIME_PYTHON,
 integrityValidatorPath:path.join(skill,'container_tools/inspect_presentation_package_integrity.py'),
 layoutValidatorPath:path.join(skill,'container_tools/inspect_presentation_layout_geometry.py'),
 explicitTotalSlideCount:15,
 requiredNativeTableOwnerSlides:[],requiredNativeChartOwnerSlides:[],
 fontPolicy:{basis:'reference',families:['Segoe UI','Consolas'],referencePath,referenceSha256},
 layoutArgs:['--expected-slide-size-emu','13011150,7315200','--validate-bullet-geometry','--validate-heading-fit'],
 verifyArtifactToolImport:true,receiptPath,
});
console.log(JSON.stringify(result,null,2));
