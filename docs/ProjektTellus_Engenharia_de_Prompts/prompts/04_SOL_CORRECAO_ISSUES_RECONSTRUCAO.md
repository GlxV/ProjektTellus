# Etapa 4 — Sol: correção dos issues #1 e #2

**Status: reconstrução documental.** Os issues foram anotados após observar defeitos reais na interface.

## Contexto

Computador da faculdade: **1366×768, Windows com escala 100%**. Controles inferiores cortados e menu de eventos visualmente sobreposto a slider e botão `Aplicar evento`.

## Prompt reconstruído

> Leia os issues #1 (layout cortado) e #2 (popup de eventos ilegível), `main.fxml`, `app.css`, `MainController`, `PlanetView` e `PresentationPane`.
>
> Corrija a interface em 1366×768 e 100% sem exigir alteração na escala do Windows. A janela deve respeitar área útil; painéis laterais e controles importantes devem permanecer acessíveis por rolagem. Adapte globo e modo Apresentação ao espaço disponível, sem perda visual.
>
> Corrija o ComboBox de eventos: fundo opaco, estados hover/selecionado legíveis, cinco opções acessíveis por mouse/teclado, sem transparência confusa, clipping ou acionamento dos controles atrás do popup.
>
> Preserve eventos animados, Memento, SQLite e quatro padrões. Valide janelas equivalentes a 1366×768/100%, 1920×1080/100% e 1920×1080/125%, com inspeção visual real. Se a escala só puder ser simulada, informe isso. Execute testes, documente evidência, atualize JAR, issue e commit. Feche issues somente após cumprir os critérios.

## Evidência e limite

O commit `9c41cbf` e as issues #1/#2 foram comunicados como resolvidos. A etapa registra **57 testes**. Os testes ocorreram em **janelas equivalentes**, não no computador físico da faculdade.
