# Plano de implementação — 10/11/2026

1. Preparar Java 25/Maven e fixar dependências disponíveis e compatíveis.
2. Implementar modelo composto e imutável, Builder, cálculos determinísticos e testes de invariantes.
3. Implementar eventos com criadores concretos, motor, observadores reais, Memento e trilhas das operações reais.
4. Implementar SQLite com transações, mundos, histórico e snapshots, verificando reabertura e isolamento.
5. Integrar interface FXML/CSS: controles ambientais, cinco eventos, biblioteca de mundos, histórico, globo 3D e modo Apresentação.
6. Verificar testes, empacotamento e execução gráfica; corrigir falhas e registrar os limites observados.
7. Atualizar README, UML, explicação dos padrões e HANDOFF_SOL; criar commit local final.

## Decisões

- Estados e componentes são records imutáveis; Terra é o originador e único proprietário do estado atual.
- Gases são frações cuja soma é 100%; pressão é independente e permite calcular pressões parciais.
- Casos de uso persistem o estado, checkpoint e histórico em uma única transação, antes de anunciar uma alteração concluída.
- A apresentação reproduz passos registrados durante as operações reais, com comparações antes/depois.
- O núcleo não importa JavaFX/JDBC; o repositório não depende da interface.
- Tarefas independentes de persistência e interface poderão ser delegadas com contratos definidos. Integração, revisão e validação ficam com o agente principal.

## Verificação

Testes JUnit do domínio, eventos/padrões e SQLite; `mvn clean verify`; execução de `java -jar target/ProjektTellus.jar`; smoke gráfico com JavaFX e inspeção da tela quando possível. Evidência automatizada não substitui inspeção visual/interação humana não executada.
