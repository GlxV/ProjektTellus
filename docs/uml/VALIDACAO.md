# Conferência da UML oficial

Edição conferida em **16/11/2026**, sobre as classes Java do checkout com base em `08280e7`. A documentação é uma seleção de visões; não é um inventário de cada membro de todas as classes auxiliares.

## Ajustes em relação aos materiais anteriores

| Ponto conferido | Representação oficial |
|---|---|
| Arquitetura era um fluxograma | Diagrama de classes com associações, dependências, herança e realização diferenciadas. `SqliteMundoRepository` realiza `MundoRepository`. |
| Builder mostrava somente parte dos parâmetros | Sete operações fluentes, `build()`, campos correspondentes e os seis accessors dos componentes de `EstadoPlanetario`, além das duas funções derivadas. |
| Records apareciam com componentes como atributos públicos | Accessors públicos com `()`. Os campos gerados são privados e finais; construtores e métodos gerados foram omitidos conforme a legenda. |
| Factory Method não explicitava todas as operações do contrato | `nome`, `intensidade`, `aplicar`, construtor protegido da base e `transformar` protegido/abstrato; cinco pares reais preservados. Retorno de `criar` é a interface. Overrides repetidos são descritos no README. |
| Relação com os assinantes aparecia como agregação | Associação `0..*`, sustentada pela lista do bus, sem posse exclusiva. Inscrição removível e callback anônimo da interface descritos. |
| Serviço aparecia associado a `Terra` sem distinguir armazenamento de uso | Dependência: `Terra` é parâmetro/local do serviço. A associação armazenada à Terra selecionada pertence a `MainController.current`. |
| Diagramas textuais antigos usavam composição com os estados | Associações para referências imutáveis compartilháveis. A mesma instância de estado pode ser usada por Terra, duplicação e memento. Os materiais antigos foram preservados. |
| Motor aparecia ligado ao serviço sem distinguir uso estático | Dependência rotulada como chamadas estáticas, sem supor um campo/instância de `ModeloPlanetario`. |
| Consulta de retratos tinha retorno genérico incompleto | `snapshots(UUID id)` retorna `List<TerraMemento>`. A interface do repositório depende dos retratos, sem representá-la como dona de uma coleção em memória. |
| Arquivo de integração da apresentação continha uma sequência | Visão estrutural dos quatro padrões e sequência de eventos agora são dois diagramas distintos. |
| Detalhes de confirmação eram condensados em notas | Sequência explicita produto, estado anterior, Terra candidata, persistência, atualização do destino e bus, incluindo falha de persistência e execução diferida da interface. |

Não foram tratados como erros os recortes já declarados nos materiais da apresentação. A implementação anônima de Observer e a falta de opacidade estrita do Memento já estavam corretamente registradas; essas ressalvas foram mantidas.

## Verificações

- Leitura dos contratos e implementações de domínio, eventos, aplicação, observadores e persistência, além dos pontos de criação, inscrição e execução no controlador JavaFX.
- Conferência automatizada das **79 declarações de membros exibidas** com `javap -p`, após compilar os **42 arquivos Java atuais** dos pacotes centrais com JDK 25 em diretório temporário de ferramentas. A comparação inclui nomes, parâmetros, retornos, campos e visibilidades; operações abstratas foram também confrontadas com as declarações Java. Relacionamentos e ordem de execução foram conferidos por leitura do código.
- Renderização das sete fontes com Mermaid CLI **11.12.0**, Mermaid **11.17.2** e Puppeteer **23.11.1**, nos dois formatos, com escala **3×** nos PNGs. Nenhum export depende de serviço remoto.
- SVGs abertos no Chromium em viewport **1366×768**: conferência visual e de limites do texto. Notas da sequência foram reposicionadas para não ultrapassar o `viewBox`. Os diagramas altos usam rolagem vertical.
- PNGs abertos e decodificados, com conferência de dimensões, integridade e conteúdo visual. SVGs conferidos como XML e renderizados, sem scripts, imagens externas ou `foreignObject`.
- Links relativos e referências de imagem do README principal e desta documentação conferidos contra os arquivos existentes. As sete imagens são Markdown comum com caminhos relativos à pasta, compatível com a exibição no GitHub.
- Escopo do diff conferido: somente README principal e `docs/uml/`. Fontes Java, domínio, persistência, testes, slides e arquivos de `docs/uml-apresentacao/` preservados. A suíte do simulador não foi executada, pois não houve mudança no aplicativo.

O teste local de renderização valida os arquivos e caminhos usados pelo README. A edição ainda não foi publicada no remoto; não representa inspeção de uma página já publicada no GitHub.

## Manutenção

Execute [Export-Uml.ps1](Export-Uml.ps1) após editar qualquer `.mmd`. Revise PNG **e** SVG, sobretudo textos longos e multiplicidades. Registre mudanças de contrato no [README](README.md) e confira se as relações ainda correspondem a campos, parâmetros, heranças ou implementações existentes no Java.
